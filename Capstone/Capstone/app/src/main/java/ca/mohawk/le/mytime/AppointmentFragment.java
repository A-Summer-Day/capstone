package ca.mohawk.le.mytime;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
public class AppointmentFragment extends Fragment implements View.OnClickListener {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users"); // Database reference
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    private String currentUserId,token;
    private FirebaseUser currentUser;
    private SimpleDateFormat sdf;
    private View view;
    private ArrayList<Appointment> appointments; // list of appointments

    public AppointmentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_appointment, container, false);

        fm = getFragmentManager();
        fragmentTransaction = fm.beginTransaction();

        // Get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();
        myref = myref.child(currentUserId); // set database reference path to current user id
        appointments = new ArrayList<>(); // initialize an array of appointments

        // Get the custom appointment adapter
        final AppointmentAdapter adapter = new AppointmentAdapter(getActivity().getApplicationContext(),appointments);

        final ListView listView = view.findViewById(R.id.appointment_list);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){

                }else{ // if data exists
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        String date = ds.getKey().replace("-", "/");
                        String time = ds.child("time").getValue().toString();
                        try{
                            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                            String datetime = date + " " + time;
                            Date dateFormatted = sdf.parse(datetime);
                            Log.d("BEFORE OR AFTER", (new Date()).toString());

                            // if any appointment in the database is before current datetime, remove it
                            if(new Date().after(dateFormatted)){
                                ds.getRef().removeValue();
                            }else{ // else, add to the appointments array
                                String name = ds.child("name").getValue().toString();
                                String address = ds.child("address").getValue().toString();
                                String doctor = ds.child("doctor").getValue().toString();

                                Appointment appointment = new Appointment(name,doctor,address,date,time);
                                appointments.add(appointment);
                            }
                        }catch(ParseException e){

                        }
                    }
                    listView.setAdapter(adapter); // set listview adapter to display the appointments
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // Check if there is any appointment data for this user
        myref.child("appointments").addListenerForSingleValueEvent(valueEventListener);

        ImageButton addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_button: // take user to the add new appointment screen
                NewAppointmentFragment newAppointmentFragment = new NewAppointmentFragment();
                fragmentTransaction.replace(R.id.generalLayout, newAppointmentFragment);
                fragmentTransaction.commit();
                break;
        }
    }
}
