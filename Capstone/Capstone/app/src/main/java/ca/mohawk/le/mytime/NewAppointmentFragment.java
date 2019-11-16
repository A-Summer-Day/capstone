package ca.mohawk.le.mytime;


import android.app.DatePickerDialog;
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
import android.widget.Toast;

import java.util.Calendar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewAppointmentFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private View view;
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    private String name, doctor, address, selectedDay, selectedMonth, selectedYear;
    private  EditText getName, getAddress, getDoctor, getDate;
    private int year,month,day;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users");
    private String currentUserId;
    private FirebaseUser currentUser;
    private DatePickerDialog datePickerDialog;
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
        myref = myref.child(currentUserId);

        getName = view.findViewById(R.id.test_name);
        getAddress = view.findViewById(R.id.test_address);
        getDoctor = view.findViewById(R.id.test_doctor);
        getDate = view.findViewById(R.id.test_date);

        getDate.setOnClickListener(this);

        fm = getFragmentManager();
        fragmentTransaction = fm.beginTransaction();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cancel_button:
                AppointmentFragment appointmentFragment = new AppointmentFragment();
                fragmentTransaction.replace(R.id.generalLayout, appointmentFragment);
                fragmentTransaction.commit();
                break;
            case R.id.add_new_button:
                name = getName.getText().toString();
                doctor = getDoctor.getText().toString();
                address = getAddress.getText().toString();

                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(selectedYear) &&
                        !TextUtils.isEmpty(selectedMonth) && !TextUtils.isEmpty(selectedDay)){
                    String date = month + "-" + day + "-" + year;
                    myref.child("appointments").child(date).child("name").setValue(name);
                    myref.child("appointments").child(date).child("address").setValue(address);
                    myref.child("appointments").child(date).child("doctor").setValue(doctor);

                    getName.setText("");
                    getAddress.setText("");
                    getDoctor.setText("");
                    getDate.setText("");
                    datePickerDialog.updateDate(year,month,day);
                    appointmentFragment = new AppointmentFragment();
                    fragmentTransaction.replace(R.id.generalLayout, appointmentFragment);
                    fragmentTransaction.commit();

                }else{
                    Toast.makeText(getActivity(), "Appointment name and date cannot be blank.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.test_date:
                openDatePickerDialog(v);
                break;
        }
    }

    private void openDatePickerDialog(View v) {
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(getActivity(),this,year,month,day);
        if(!TextUtils.isEmpty(selectedYear) && !TextUtils.isEmpty(selectedMonth) && !TextUtils.isEmpty(selectedDay)) {
            datePickerDialog.updateDate(Integer.parseInt(selectedYear),
                    Integer.parseInt(selectedMonth),Integer.parseInt(selectedDay));

        }
        datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        selectedDay = String.valueOf(dayOfMonth);
        selectedMonth = String.valueOf(month);
        selectedYear = String.valueOf(year);
        getDate.setText(selectedMonth + "/" + selectedDay + "/" + selectedYear);
    }
}
