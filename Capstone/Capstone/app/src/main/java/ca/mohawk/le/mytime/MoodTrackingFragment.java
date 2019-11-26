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
    DatabaseReference myref = database.getReference().child("users");
    private View view;
    private CalendarView calendar;
    private CheckBox logMood;
    private AppCompatImageButton editMoodDetailButton;
    private String whatMood;
    private String currentUserId;
    private FirebaseUser currentUser;
    private String selectedYear, selectedMonth, selectedDayOfMonth, selectedDate;
    private SimpleDateFormat sdf;
    private boolean updating;
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
        myref = myref.child(currentUserId);
        calendar.setMaxDate(new Date().getTime());

        Long date = calendar.getDate();

        SimpleDateFormat df = new SimpleDateFormat("dd");
        SimpleDateFormat mf = new SimpleDateFormat("MM");
        SimpleDateFormat yf = new SimpleDateFormat("yyyy");
        sdf = new SimpleDateFormat("dd/MM/yyyy");
        selectedDate = sdf.format(date);
        selectedDayOfMonth = df.format(date);
        selectedMonth = mf.format(date);
        selectedYear = yf.format(date);
        editInfo = view.findViewById(R.id.addInfo);
        editInfo.setVisibility(View.GONE);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){

                }else{
                    logMood.setChecked(true);
                    editInfo.setVisibility(View.VISIBLE);

                    String mood = dataSnapshot.child("mood").getValue().toString();
                    spinner.setSelection(adapter.getPosition(mood));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        myref.child("mood-tracking").child(selectedYear).child(selectedMonth).
                child(selectedDayOfMonth).addListenerForSingleValueEvent(valueEventListener);

        updating = true;
        return view;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.moods) {

            whatMood = parent.getItemAtPosition(position).toString();

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
                updating = updateInfo(updating);
                break;
            case R.id.logMood:
                if(checked){
                    myref.child("mood/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth + "/mood").setValue("Neutral");
                    editInfo.setVisibility(View.VISIBLE);
                }else{
                    myref.child("mood/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth).removeValue();
                    editInfo.setVisibility(View.GONE);
                }
                break;

        }
    }

    private boolean updateInfo(boolean updating) {
        if(updating){
            spinner.setEnabled(true);
            editMoodDetailButton.setImageResource(android.R.drawable.ic_menu_save);
            return false;
        }else {
            spinner.setEnabled(false);
            myref.child("mood-tracking").child(selectedYear).child(selectedMonth).
                    child(selectedDayOfMonth).child("mood").setValue(whatMood);

            editMoodDetailButton.setImageResource(android.R.drawable.ic_menu_edit);
            return true;
        }
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
        selectedYear = Integer.toString(year);
        selectedMonth = Integer.toString(month + 1);
        selectedDayOfMonth = Integer.toString(dayOfMonth);

        logMood.setChecked(false);
        editInfo.setVisibility(View.GONE);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    spinner.setSelection(adapter.getPosition(0));
                }else{
                    logMood.setChecked(true);
                    editInfo.setVisibility(View.VISIBLE);

                    String mood = dataSnapshot.child("mood").getValue().toString();
                    spinner.setSelection(adapter.getPosition(mood));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        myref.child("mood-tracking").child(selectedYear).child(selectedMonth).
                child(selectedDayOfMonth).addListenerForSingleValueEvent(valueEventListener);

    }
}
