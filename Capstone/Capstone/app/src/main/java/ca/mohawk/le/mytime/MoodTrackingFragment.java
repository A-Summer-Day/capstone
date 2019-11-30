package ca.mohawk.le.mytime;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.widget.Spinner;


/**
 * A simple {@link Fragment} subclass.
 */
public class MoodTrackingFragment extends Fragment implements AdapterView.OnItemSelectedListener,
        View.OnClickListener, CalendarView.OnDateChangeListener {
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users"); // Database reference
    private View view;
    private CalendarView calendar;
    private CheckBox logMood;
    private AppCompatImageButton editMoodDetailButton;
    private String whatMood; // current mood
    private String currentUserId;
    private FirebaseUser currentUser;
    private String selectedYear, selectedMonth, selectedDayOfMonth, selectedDate;
    private SimpleDateFormat sdf;
    private boolean updating; // flag to keep track of whether user is allowed to edit or not
    private boolean checked;
    private ArrayAdapter adapter;
    private View editInfo;
    Spinner spinner;
    public MoodTrackingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_mood_tracking, container, false);
        spinner = view.findViewById(R.id.moods);

        adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.moods_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // set up mood spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(0,false);
        spinner.setOnItemSelectedListener(this);
        spinner.setEnabled(false);

        calendar = view.findViewById(R.id.calendar);
        logMood = view.findViewById(R.id.logMood);

        calendar.setOnDateChangeListener(this);
        editMoodDetailButton = view.findViewById(R.id.editMoodDetailsButton);
        logMood.setOnClickListener(this);
        editMoodDetailButton.setOnClickListener(this);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();
        myref = myref.child(currentUserId); // set database reference path to current user id

        calendar.setMaxDate(new Date().getTime()); // disable future dates on calendar

        Long date = calendar.getDate();

        SimpleDateFormat df = new SimpleDateFormat("dd");
        SimpleDateFormat mf = new SimpleDateFormat("MM");
        SimpleDateFormat yf = new SimpleDateFormat("yyyy");
        sdf = new SimpleDateFormat("dd/MM/yyyy");
        selectedDate = sdf.format(date); // get current date
        selectedDayOfMonth = df.format(date); // get current day of month
        selectedMonth = mf.format(date); // get current month
        selectedYear = yf.format(date); // get current year

        editInfo = view.findViewById(R.id.addInfo);
        editInfo.setVisibility(View.GONE); // hide edit view

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){

                }else{ // if data exists
                    logMood.setChecked(true); // check the box
                    editInfo.setVisibility(View.VISIBLE); // show data details

                    String mood = dataSnapshot.child("mood").getValue().toString();
                    spinner.setSelection(adapter.getPosition(mood)); // set spinner to mood retrieved from data

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // Check if there is any mood data for this user on current date
        myref.child("mood-tracking").child(selectedYear).child(selectedMonth).
                child(selectedDayOfMonth).addListenerForSingleValueEvent(valueEventListener);

        updating = true;
        return view;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.moods) {

            whatMood = parent.getItemAtPosition(position).toString(); // update mood

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onClick(View v) {
        checked = logMood.isChecked();
        switch (v.getId()){
            case R.id.editMoodDetailsButton:
                updating = updateInfo(updating); //update info
                break;
            case R.id.logMood:
                if(checked){
                    // if user checks a date, add that date data to cloud
                    myref.child("mood-tracking/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth + "/mood").setValue("Neutral");
                    editInfo.setVisibility(View.VISIBLE); // show the details
                }else{
                    // if user un-checks a date, remove that date data from cloud
                    myref.child("mood-tracking/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth).removeValue();
                    editInfo.setVisibility(View.GONE); // hide the details
                }
                break;

        }
    }

    private boolean updateInfo(boolean updating) {
        if(updating){ // editing
            spinner.setEnabled(true); // allow user to choose from spinner
            editMoodDetailButton.setImageResource(android.R.drawable.ic_menu_save); // update the image button
            return false;
        }else { // done editing
            spinner.setEnabled(false); // disable spinner
            // update database
            myref.child("mood-tracking").child(selectedYear).child(selectedMonth).
                    child(selectedDayOfMonth).child("mood").setValue(whatMood);

            editMoodDetailButton.setImageResource(android.R.drawable.ic_menu_edit); // update the image button
            return true;
        }
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
        selectedYear = Integer.toString(year); // update selected year
        selectedMonth = Integer.toString(month + 1); // update selected month
        selectedDayOfMonth = Integer.toString(dayOfMonth); // update selected day of month

        logMood.setChecked(false); // uncheck the log box
        editInfo.setVisibility(View.GONE); // hide details

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){ // if there is no data
                    spinner.setSelection(0,false);
                }else{ // if data exists
                    logMood.setChecked(true); // re-check the log box
                    editInfo.setVisibility(View.VISIBLE); // show details

                    String mood = dataSnapshot.child("mood").getValue().toString();
                    spinner.setSelection(adapter.getPosition(mood)); // set spinner value to mood retrieved
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // Check if there is any mood data for this user at selected year and selected month
        myref.child("mood-tracking").child(selectedYear).child(selectedMonth).
                child(selectedDayOfMonth).addListenerForSingleValueEvent(valueEventListener);

    }
}
