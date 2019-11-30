package ca.mohawk.le.mytime;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewAppointmentFragment extends Fragment implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private View view;
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    private String name, doctor, address, selectedDay, selectedMonth, selectedYear,selectedHour, selectedMinute;
    private  EditText getName, getAddress, getDoctor, getDate, getTime;
    private int year,month,day,hour,minute;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users"); // Database reference
    private String currentUserId;
    private FirebaseUser currentUser;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    final Calendar c = Calendar.getInstance();

    public NewAppointmentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_new_appointment, container, false);

        Button add_button = view.findViewById(R.id.add_new_button);
        Button cancel_button = view.findViewById(R.id.cancel_button);
        add_button.setOnClickListener(this);
        cancel_button.setOnClickListener(this);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();
        myref = myref.child(currentUserId); // set database reference path to current user id

        getName = view.findViewById(R.id.test_name);
        getAddress = view.findViewById(R.id.test_address);
        getDoctor = view.findViewById(R.id.test_doctor);
        getDate = view.findViewById(R.id.test_date);
        getTime = view.findViewById(R.id.test_time);
        
        getDate.setOnClickListener(this);
        getTime.setOnClickListener(this);
        
        fm = getFragmentManager();
        fragmentTransaction = fm.beginTransaction();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cancel_button:
                // Discard and take user back to the appointment page
                AppointmentFragment appointmentFragment = new AppointmentFragment();
                fragmentTransaction.replace(R.id.generalLayout, appointmentFragment);
                fragmentTransaction.commit();
                break;
            case R.id.add_new_button:
                name = getName.getText().toString();
                doctor = getDoctor.getText().toString();
                address = getAddress.getText().toString();

                // make sure name, date and time of appointment are not blank
                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(selectedYear) &&
                        !TextUtils.isEmpty(selectedMonth) && !TextUtils.isEmpty(selectedDay)
                        && !TextUtils.isEmpty(selectedHour) && !TextUtils.isEmpty(selectedMinute)){

                    String date = selectedMonth + "-" + selectedDay + "-" + selectedYear;
                    String time = selectedHour + ":" + selectedMinute;

                    // write to database
                    myref.child("appointments").child(date).child("name").setValue(name);
                    myref.child("appointments").child(date).child("address").setValue(address);
                    myref.child("appointments").child(date).child("doctor").setValue(doctor);
                    myref.child("appointments").child(date).child("time").setValue(time);

                    // reset fields
                    getName.setText("");
                    getAddress.setText("");
                    getDoctor.setText("");
                    getDate.setText("");
                    getTime.setText("");
                    datePickerDialog.updateDate(year,month,day);
                    timePickerDialog.updateTime(hour,minute);

                    // take user back to appointment page
                    appointmentFragment = new AppointmentFragment();
                    fragmentTransaction.replace(R.id.generalLayout, appointmentFragment);
                    fragmentTransaction.commit();

                }else{
                    Toast.makeText(getActivity(), "Appointment name and date time cannot be blank.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.test_date:
                // open the date picker
                openDatePickerDialog(v);
                break;
            case R.id.test_time:
                // open the time picker
                openTimePickerDialog(v);
                break;
        }
    }

    private void openTimePickerDialog(View v) {
        hour = c.get(Calendar.HOUR);
        minute = c.get(Calendar.MINUTE);
        timePickerDialog = new TimePickerDialog(getActivity(),this,hour,minute,true); // TimePickerDialog instance
        // if there is selected time, show it next time time picker is opened
        if(!TextUtils.isEmpty(selectedHour) && !TextUtils.isEmpty(selectedMinute)) {
            timePickerDialog.updateTime(Integer.parseInt(selectedHour),
                    Integer.parseInt(selectedMinute));
        }
        timePickerDialog.show(); // show time picker
    }

    private void openDatePickerDialog(View v) {
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(getActivity(),this,year,month,day); // DatePickerDialog instance
        // if there is selected date, show it next time date picker is opened
        if(!TextUtils.isEmpty(selectedYear) && !TextUtils.isEmpty(selectedMonth) && !TextUtils.isEmpty(selectedDay)) {
            datePickerDialog.updateDate(Integer.parseInt(selectedYear),
                    Integer.parseInt(selectedMonth) - 1,Integer.parseInt(selectedDay));

        }
        datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis()); // disable past dates
        datePickerDialog.show(); // show date picker
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        selectedDay = String.valueOf(dayOfMonth); // update selected day of month
        selectedMonth = String.valueOf(month + 1); // update selected month
        selectedYear = String.valueOf(year); // update selected year
        getDate.setText(selectedMonth + "/" + selectedDay + "/" + selectedYear); // display the date
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        selectedHour = String.format("%02d", hourOfDay); // update selected hour
        selectedMinute = String.format("%02d", minute); // update selected minute
        getTime.setText(selectedHour + ":" + selectedMinute); // display the time
    }
}
