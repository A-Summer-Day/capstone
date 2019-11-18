package ca.mohawk.le.mytime;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    DatabaseReference myref = database.getReference().child("users");
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    private String currentUserId;
    private FirebaseUser currentUser;
    private SimpleDateFormat sdf;
    private View view;
    private ArrayList<Appointment> appointments;

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
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();
        myref = myref.child(currentUserId);
        appointments = new ArrayList<>();
        final AppointmentAdapter adapter = new AppointmentAdapter(getActivity().getApplicationContext(),appointments);

        final ListView listView = view.findViewById(R.id.appointment_list);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){

                }else{
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        //String year = ds.getKey();
                        //String month = ds.child(year).getKey();
                        //String day = ds.child(year).child(month).getKey();
                        String date = ds.getKey().replace("-", "/");
                        try{
                            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                            Date dateFormatted = sdf.parse(date);
                            Log.d("BEFORE OR AFTER", (new Date()).toString());
                            if(new Date().after(dateFormatted)){
                                Log.d("BEFORE OR AFTER", ds.getRef().toString());
                                ds.getRef().removeValue();
                                //myref.child(ds.getKey()).removeValue();
                            }else{
                                Log.d("BEFORE OR AFTER", "BEFORE");
                                String name = ds.child("name").getValue().toString();
                                String address = ds.child("address").getValue().toString();
                                String doctor = ds.child("doctor").getValue().toString();
                                String time = ds.child("time").getValue().toString();
                                Appointment appointment = new Appointment(name,doctor,address,date,time);
                                appointments.add(appointment);
                            }
                        }catch(ParseException e){

                        }
                    }
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        myref.child("appointments").addListenerForSingleValueEvent(valueEventListener);
        ImageButton addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_button:
                NewAppointmentFragment newAppointmentFragment = new NewAppointmentFragment();
                fragmentTransaction.replace(R.id.generalLayout, newAppointmentFragment);
                fragmentTransaction.commit();
                break;
        }
    }
}
