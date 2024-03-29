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
public class SexualActivityTrackingFragment extends Fragment implements View.OnClickListener,
        CalendarView.OnDateChangeListener {
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users"); // Database reference
    private View view;
    private CalendarView calendar;
    private CheckBox logActivity, onProtection, onPeriod;
    private AppCompatImageButton editSexualActivityDetailsButton;
    private EditText getPartner;
    private String partner, period, protection;
    private String currentUserId;
    private FirebaseUser currentUser;
    private String selectedYear, selectedMonth, selectedDayOfMonth, selectedDate;
    private SimpleDateFormat sdf;
    private boolean updating;  // flag to keep track of whether user is allowed to edit or not
    private boolean checked;
    private View editInfo;

    public SexualActivityTrackingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sexual_activity_tracking, container, false);
        editSexualActivityDetailsButton = view.findViewById(R.id.editSexualActivityDetailsButton);
        calendar = view.findViewById(R.id.calendar);
        logActivity = view.findViewById(R.id.logSexualActivity);

        calendar.setOnDateChangeListener(this);
        logActivity.setOnClickListener(this);
        editSexualActivityDetailsButton.setOnClickListener(this);

        getPartner = view.findViewById(R.id.with_whom);
        onPeriod = view.findViewById(R.id.logOnPeriod);
        onPeriod.setOnClickListener(this);
        onProtection = view.findViewById(R.id.logProtection);
        onProtection.setOnClickListener(this);

        getPartner.setEnabled(false);
        onPeriod.setEnabled(false);
        onProtection.setEnabled(false);

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
                    logActivity.setChecked(true); // check the box
                    editInfo.setVisibility(View.VISIBLE); // show data details

                    // set the fields accordingly
                    String partner = dataSnapshot.child("partner").getValue().toString();
                    getPartner.setText(partner);
                    protection = dataSnapshot.child("on-protection").getValue().toString();
                    if(protection == "Yes"){
                        onProtection.setChecked(true);
                    }
                    period = dataSnapshot.child("on-period").getValue().toString();
                    if(period == "Yes"){
                        onPeriod.setChecked(true);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // Check if there is any sexual activity data for this user on current date
        myref.child("sexual-activity-tracking").child(selectedYear).child(selectedMonth).
                child(selectedDayOfMonth).addListenerForSingleValueEvent(valueEventListener);

        updating = true;
        return view;
    }

    @Override
    public void onClick(View v) {
        checked = logActivity.isChecked();
        switch (v.getId()){
            case R.id.editSexualActivityDetailsButton:
                updating = updateInfo(updating); //update info
                break;
            case R.id.logPeriod:
                // change the on-period value accordingly
                period = (onPeriod.isChecked()) ? "Yes" : "No";
                break;
            case R.id.logProtection:
                // change the on-protection value accordingly
                protection = (onProtection.isChecked()) ? "Yes" : "No";
                break;
            case R.id.logSexualActivity:
                if(checked){
                    // if user checks a date, add that date data to cloud
                    myref.child("sexual-activity-tracking/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth + "/logged").setValue("True");
                    myref.child("sexual-activity-tracking/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth + "/partner").setValue("");
                    myref.child("sexual-activity-tracking/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth + "/on-protection").setValue("No");
                    myref.child("sexual-activity-tracking/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth + "/on-period").setValue("No");

                    editInfo.setVisibility(View.VISIBLE);
                }else{
                    // if user un-checks a date, remove that date data from cloud
                    myref.child("sexual-activity-tracking/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth).removeValue();
                    editInfo.setVisibility(View.GONE);
                }
                break;

        }
    }

    private boolean updateInfo(boolean updating) {

        if(updating){ // editing
            // enable all fields
            getPartner.setEnabled(true);
            onPeriod.setEnabled(true);
            onProtection.setEnabled(true);
            editSexualActivityDetailsButton.setImageResource(android.R.drawable.ic_menu_save); // update the image button
            return false;
        }else{ // done editing
            // disable all fields
            getPartner.setEnabled(false);
            onPeriod.setEnabled(false);
            onProtection.setEnabled(false);

            partner = getPartner.getText().toString();
            period = (onPeriod.isChecked()) ? "Yes" : "No";
            protection = (onProtection.isChecked()) ? "Yes" : "No";

            // update database
            myref.child("sexual-activity-tracking").child(selectedYear).child(selectedMonth).
                    child(selectedDayOfMonth).child("partner").setValue(partner);
            myref.child("sexual-activity-tracking").child(selectedYear).child(selectedMonth).
                    child(selectedDayOfMonth).child("on-protection").setValue(protection);
            myref.child("sexual-activity-tracking").child(selectedYear).child(selectedMonth).
                    child(selectedDayOfMonth).child("on-period").setValue(period);

            editSexualActivityDetailsButton.setImageResource(android.R.drawable.ic_menu_edit); // update the image button
            return true;
        }
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
        selectedYear = Integer.toString(year);  // update selected year
        selectedMonth = Integer.toString(month + 1); // update selected month
        selectedDayOfMonth = Integer.toString(dayOfMonth); // update selected day of month

        logActivity.setChecked(false); // uncheck the log box
        // reset all fields
        getPartner.setText("");
        onProtection.setChecked(false);
        onPeriod.setChecked(false);
        editInfo.setVisibility(View.GONE); // hide details
        Long date = calendar.getDate();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){

                }else{ // if data exists

                    logActivity.setChecked(true); // re-check the log box
                    editInfo.setVisibility(View.VISIBLE); // show details

                    // set fields accordingly
                    partner = dataSnapshot.child("partner").getValue().toString();
                    getPartner.setText(partner);
                    protection = dataSnapshot.child("on-protection").getValue().toString();

                    if(protection.equals("Yes")){
                        onProtection.setChecked(true);
                    }
                    period = dataSnapshot.child("on-period").getValue().toString();
                    if(period.equals("Yes")){
                        onPeriod.setChecked(true);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // Check if there is any period data for this user at selected year, selected month, and selected day
        myref.child("sexual-activity-tracking").child(selectedYear).child(selectedMonth).
                child(selectedDayOfMonth).addListenerForSingleValueEvent(valueEventListener);
    }
}
