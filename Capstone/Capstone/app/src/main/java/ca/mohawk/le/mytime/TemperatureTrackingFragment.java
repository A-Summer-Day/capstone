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
public class TemperatureTrackingFragment extends Fragment implements CalendarView.OnDateChangeListener,
        View.OnClickListener{
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users");
    private View view;
    private CalendarView calendar;
    private CheckBox logTemperature;
    private AppCompatImageButton editTemperatureDetailsButton;
    private EditText getTemperature;
    private String temperature;
    private String currentUserId;
    private FirebaseUser currentUser;
    private String selectedYear, selectedMonth, selectedDayOfMonth, selectedDate;
    private SimpleDateFormat sdf;
    private boolean updating;
    private boolean checked;
    private View editInfo;

    public TemperatureTrackingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =inflater.inflate(R.layout.fragment_temperature_tracking, container, false);
        calendar = view.findViewById(R.id.calendar);
        logTemperature = view.findViewById(R.id.logTemperature);

        calendar.setOnDateChangeListener(this);
        editTemperatureDetailsButton = view.findViewById(R.id.editTemperatureDetailsButton);
        logTemperature.setOnClickListener(this);
        editTemperatureDetailsButton.setOnClickListener(this);

        getTemperature = view.findViewById(R.id.temperature);
        temperature = Integer.toString(37);
        getTemperature.setText(temperature);
        getTemperature.setEnabled(false);
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
                    logTemperature.setChecked(true);
                    editInfo.setVisibility(View.VISIBLE);

                    temperature = dataSnapshot.child("temperature").getValue().toString();
                    getTemperature.setText(temperature);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        myref.child("temperature-tracking").child(selectedYear).child(selectedMonth).
                child(selectedDayOfMonth).addListenerForSingleValueEvent(valueEventListener);

        updating = true;
        return view;
    }

    @Override
    public void onClick(View v) {
        checked = logTemperature.isChecked();
        switch (v.getId()){
            case R.id.editTemperatureDetailsButton:
                updating = updateInfo(updating);
                break;
            case R.id.logTemperature:
                if(checked){
                    myref.child("temperature-tracking/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth + "/temperature").setValue(temperature);
                    editInfo.setVisibility(View.VISIBLE);
                }else{
                    myref.child("temperature-tracking/" + selectedYear + "/" + selectedMonth + "/" +
                            selectedDayOfMonth).removeValue();
                    editInfo.setVisibility(View.GONE);
                }
                break;

        }
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
        selectedYear = Integer.toString(year);
        selectedMonth = Integer.toString(month + 1);
        selectedDayOfMonth = Integer.toString(dayOfMonth);

        logTemperature.setChecked(false);
        editInfo.setVisibility(View.GONE);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){

                }else{
                    logTemperature.setChecked(true);
                    editInfo.setVisibility(View.VISIBLE);

                    temperature = dataSnapshot.child("temperature").getValue().toString();

                    getTemperature.setText(temperature);

                    Log.d("SNAPSHOT",dataSnapshot.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        myref.child("temperature-tracking").child(selectedYear).child(selectedMonth).
                child(selectedDayOfMonth).addListenerForSingleValueEvent(valueEventListener);
    }

    private boolean updateInfo(boolean updating){
        if(updating){
            getTemperature.setEnabled(true);
            editTemperatureDetailsButton.setImageResource(android.R.drawable.ic_menu_save);
            return false;
        }else{
            getTemperature.setEnabled(false);

            temperature = getTemperature.getText().toString();

            myref.child("temperature-tracking").child(selectedYear).child(selectedMonth).
                    child(selectedDayOfMonth).child("temperature").setValue(temperature);

            editTemperatureDetailsButton.setImageResource(android.R.drawable.ic_menu_edit);
            return true;
        }
    }
}
