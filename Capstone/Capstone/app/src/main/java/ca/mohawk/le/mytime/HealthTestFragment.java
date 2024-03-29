package ca.mohawk.le.mytime;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HealthTestFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    FirebaseDatabase database = FirebaseDatabase.getInstance(); // Database instance
    DatabaseReference myref = database.getReference().child("users"); // Database reference
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    private String currentUserId;
    private FirebaseUser currentUser;
    private SimpleDateFormat sdf;
    private View view;
    private ArrayList<HealthTest> healthTests; // list of health tests

    public HealthTestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_health_test, container, false);

        fm = getFragmentManager();
        fragmentTransaction = fm.beginTransaction();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();

        myref = myref.child(currentUserId);  // set database reference path to current user id
        healthTests = new ArrayList<>(); // initialize an array of health tests

        // Get the custom healthtest adapter
        final HealthTestAdapter adapter = new HealthTestAdapter(getActivity().getApplicationContext(),healthTests);
        final ListView listView = view.findViewById(R.id.healthtest_list);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){

                }else{ // if data exists
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        String name = ds.getKey();
                        String frequency = ds.child("frequency").getValue().toString();
                        String unit = ds.child("unit").getValue().toString();
                        String lastTestDate = ds.child("last-testdate").getValue().toString();
                        HealthTest healthTest = new HealthTest(name,frequency,unit,lastTestDate);
                        healthTests.add(healthTest);// add to the healthtests array
                    }
                    listView.setAdapter(adapter); // set listview adapter to display the healthtests
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // Check if there is any healthtest data for this user
        myref.child("healthtests").addListenerForSingleValueEvent(valueEventListener);

        ImageButton addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(this);

        listView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_button:
                // take user to the add new healthtest screen
                NewHealthTestFragment newHealthTestFragment = new NewHealthTestFragment();
                fragmentTransaction.replace(R.id.generalLayout, newHealthTestFragment);
                fragmentTransaction.commit();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // if a user click on any item in the healthtest list, take the user to the healthtest screen
        HealthTest healthTest = (HealthTest) parent.getAdapter().getItem(position); // get item clicked

        Bundle bundle = new Bundle();
        bundle.putString("test-name", healthTest.name);
        bundle.putString("test-frequency", healthTest.frequency);
        bundle.putString("test-unit", healthTest.unit);
        bundle.putString("test-last-testdate", healthTest.lastTestDate);

        NewHealthTestFragment newHealthTestFragment = new NewHealthTestFragment();
        newHealthTestFragment.setArguments(bundle); // send data along with the fragment
        fragmentTransaction.replace(R.id.generalLayout, newHealthTestFragment);
        fragmentTransaction.commit();
    }
}
