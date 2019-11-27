package ca.mohawk.le.mytime;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewHealthTestFragment extends Fragment implements View.OnClickListener,
        AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener {
    private View view;
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    private String name, frequency, unit, lastTestdate, selectedDay, selectedMonth, selectedYear;
    private EditText getName, getFrequency, getLastTestDate;
    private int year,month,day;
    private Spinner spinner;
    private Button add_button, cancel_button, delete_button;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users");
    private String currentUserId;
    private FirebaseUser currentUser;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    final Calendar c = Calendar.getInstance();

    public NewHealthTestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_new_health_test, container, false);

        add_button = view.findViewById(R.id.add_new_button);
        cancel_button = view.findViewById(R.id.cancel_button);
        delete_button = view.findViewById(R.id.delete_button);
        add_button.setOnClickListener(this);
        cancel_button.setOnClickListener(this);
        delete_button.setOnClickListener(this);

        spinner = view.findViewById(R.id.frequency_spinner);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.frequency_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setSelection(0,false);
        spinner.setOnItemSelectedListener(this);
        unit = "days";

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();
        myref = myref.child(currentUserId);

        getName = view.findViewById(R.id.test_name);
        getFrequency = view.findViewById(R.id.test_frequency);
        getLastTestDate = view.findViewById(R.id.test_last_testdate);
        getLastTestDate.setOnClickListener(this);
        Bundle bundle = getArguments();
        if(bundle != null && bundle.containsKey("test-name")){
            getName.setText(bundle.getString("test-name"));
            getFrequency.setText(bundle.getString("test-frequency"));
            getLastTestDate.setText(bundle.getString("test-last-testdate").replace("-", "/"));
            getName.setEnabled(false);
            spinner.setSelection(adapter.getPosition(bundle.getString("test-unit")));
            add_button.setText("Save");
        }

        fm = getFragmentManager();
        fragmentTransaction = fm.beginTransaction();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cancel_button:
                HealthTestFragment healthTestFragment = new HealthTestFragment();
                fragmentTransaction.replace(R.id.generalLayout, healthTestFragment);
                fragmentTransaction.commit();
                break;
            case R.id.delete_button:
                String testToDelete = getName.getText().toString();
                myref.child("healthtests").child(testToDelete).removeValue();
                healthTestFragment = new HealthTestFragment();
                fragmentTransaction.replace(R.id.generalLayout, healthTestFragment);
                fragmentTransaction.commit();
                break;
            case R.id.add_new_button:
                name = getName.getText().toString();
                frequency = String.valueOf(getFrequency.getText().toString());

                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(frequency)){


                    if(TextUtils.isEmpty(selectedDay)
                            || TextUtils.isEmpty(selectedMonth) || TextUtils.isEmpty(selectedYear)){
                        myref.child("healthtests").child(name).child("last-testdate").setValue("N/A");
                    }else{
                        String date = selectedMonth + "-" + selectedDay + "-" + selectedYear;
                        myref.child("healthtests").child(name).child("last-testdate").setValue(date);
                        datePickerDialog.updateDate(year,month,day);
                        getLastTestDate.setText("");
                    }

                    myref.child("healthtests").child(name).child("frequency").setValue(frequency);
                    myref.child("healthtests").child(name).child("unit").setValue(unit);

                    getName.setText("");
                    getFrequency.setText("");
                    getLastTestDate.setText("");
                    spinner.setSelection(0);
                    healthTestFragment = new HealthTestFragment();
                    fragmentTransaction.replace(R.id.generalLayout, healthTestFragment);
                    fragmentTransaction.commit();

                }else{
                    Toast.makeText(getActivity(), "Name and frequency cannot be blank.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.test_last_testdate:
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
                    Integer.parseInt(selectedMonth) - 1,Integer.parseInt(selectedDay));

        }
        //datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
        datePickerDialog.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.frequency_spinner) {
            unit = parent.getItemAtPosition(position).toString();

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        selectedDay = String.valueOf(dayOfMonth);
        selectedMonth = String.valueOf(month + 1);
        selectedYear = String.valueOf(year);
        getLastTestDate.setText(selectedMonth + "/" + selectedDay + "/" + selectedYear);
    }
}
