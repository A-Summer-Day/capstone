package ca.mohawk.le.mytime;


import android.os.Bundle;

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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewHealthTestFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private View view;
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    private String name, frequency, unit;
    private EditText getName, getFrequency;
    private Spinner spinner;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users");
    private String currentUserId;
    private FirebaseUser currentUser;

    public NewHealthTestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_new_health_test, container, false);
        Button add_button = view.findViewById(R.id.add_new_button);
        Button cancel_button = view.findViewById(R.id.cancel_button);
        add_button.setOnClickListener(this);
        cancel_button.setOnClickListener(this);

        spinner = view.findViewById(R.id.frequency_spinner);

        /*
        *  ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.frequency_array, android.R.layout.simple_spinner_item);
        * */
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
            case R.id.add_new_button:
                name = getName.getText().toString();
                frequency = String.valueOf(getFrequency.getText().toString());

                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(frequency)){

                    myref.child("healthtests").child(name).child("frequency").setValue(frequency);
                    myref.child("healthtests").child(name).child("unit").setValue(unit);

                    getName.setText("");
                    getFrequency.setText("");
                    healthTestFragment = new HealthTestFragment();
                    fragmentTransaction.replace(R.id.generalLayout, healthTestFragment);
                    fragmentTransaction.commit();

                }else{
                    Toast.makeText(getActivity(), "Name and frequency cannot be blank.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
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
}
