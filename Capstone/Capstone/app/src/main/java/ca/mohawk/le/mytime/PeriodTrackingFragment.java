package ca.mohawk.le.mytime;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class PeriodTrackingFragment extends Fragment implements CalendarView.OnDateChangeListener,
        View.OnClickListener{

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users");
    private View view;
    private CalendarView calendar;
    private CheckBox logPeriod;
    private AppCompatImageButton editPeriodDetailsButton;
    private EditText getSymptoms, getMoods, getWeight;
    private String symptoms, moods, weight;
    private String currentUserId;
    private FirebaseUser currentUser;
    private String selectedYear, selectedMonth, selectedDayOfMonth, selectedDate;
    private SimpleDateFormat sdf;
    private boolean updating;
    private boolean checked;
    private View editInfo;

    public PeriodTrackingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_period_tracking, container, false);
        calendar = view.findViewById(R.id.calendar);
        logPeriod = view.findViewById(R.id.logPeriod);

        calendar.setOnDateChangeListener(this);
        editPeriodDetailsButton = view.findViewById(R.id.editPeriodDetailsButton);
        logPeriod.setOnClickListener(this);
        editPeriodDetailsButton.setOnClickListener(this);

        getSymptoms = view.findViewById(R.id.symptoms);
        getMoods = view.findViewById(R.id.moods);
        getWeight = view.findViewById(R.id.weight);

        getSymptoms.setEnabled(false);
        getMoods.setEnabled(false);
        getWeight.setEnabled(false);

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
                    logPeriod.setChecked(true);
                    editInfo.setVisibility(View.VISIBLE);

                    String symptoms = dataSnapshot.child("symptoms").getValue().toString();
                    String moods = dataSnapshot.child("moods").getValue().toString();
                    String weight = dataSnapshot.child("weight").getValue().toString();

                    getSymptoms.setText(symptoms);
                    getMoods.setText(moods);
                    getWeight.setText(weight);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        myref.child("period-tracking").child(selectedYear).child(selectedMonth).
                child(selectedDayOfMonth).addListenerForSingleValueEvent(valueEventListener);

        updating = true;
        return view;
    }


    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {

        selectedYear = Integer.toString(year);
        selectedMonth = Integer.toString(month + 1);
        selectedDayOfMonth = Integer.toString(dayOfMonth);

        logPeriod.setChecked(false);
        editInfo.setVisibility(View.GONE);
        Long date = calendar.getDate();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){

                }else{
                    logPeriod.setChecked(true);
                    editInfo.setVisibility(View.VISIBLE);

                    String symptoms = dataSnapshot.child("symptoms").getValue().toString();
                    String moods = dataSnapshot.child("moods").getValue().toString();
                    String weight = dataSnapshot.child("weight").getValue().toString();

                    getSymptoms.setText(symptoms);
                    getMoods.setText(moods);
                    getWeight.setText(weight);

                    Log.d("SNAPSHOT",dataSnapshot.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        myref.child("period-tracking").child(selectedYear).child(selectedMonth).
                child(selectedDayOfMonth).addListenerForSingleValueEvent(valueEventListener);

    }

    @Override
    public void onClick(View v) {
        checked = logPeriod.isChecked();
        switch (v.getId()){
            case R.id.editPeriodDetailsButton:
                updating = updateInfo(updating);
                break;
            case R.id.logPeriod:
                if(checked){
                    myref.child("period-tracking/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth + "/logged").setValue("True");
                    myref.child("period-tracking/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth + "/symptoms").setValue("");
                    myref.child("period-tracking/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth + "/moods").setValue("");
                    myref.child("period-tracking/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth + "/weight").setValue("");

                    editInfo.setVisibility(View.VISIBLE);
                }else{
                    myref.child("period-tracking/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth).removeValue();
                    editInfo.setVisibility(View.GONE);
                }
                break;

        }
    }

    private boolean updateInfo(boolean updating){
        if(updating){
            getSymptoms.setEnabled(true);
            getMoods.setEnabled(true);
            getWeight.setEnabled(true);
            editPeriodDetailsButton.setImageResource(android.R.drawable.ic_menu_save);
            return false;
        }else{
            getSymptoms.setEnabled(false);
            getMoods.setEnabled(false);
            getWeight.setEnabled(false);

            symptoms = getSymptoms.getText().toString();
            moods = getMoods.getText().toString();
            weight = getWeight.getText().toString();

            myref.child("period-tracking").child(selectedYear).child(selectedMonth).
                    child(selectedDayOfMonth).child("symptoms").setValue(symptoms);
            myref.child("period-tracking").child(selectedYear).child(selectedMonth).
                    child(selectedDayOfMonth).child("moods").setValue(moods);
            myref.child("period-tracking").child(selectedYear).child(selectedMonth).
                    child(selectedDayOfMonth).child("weight").setValue(weight);

            editPeriodDetailsButton.setImageResource(android.R.drawable.ic_menu_edit);
            return true;
        }
    }




}
