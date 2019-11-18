package ca.mohawk.le.mytime;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

import androidx.annotation.NonNull;

public class Appointment {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference();
    String name;
    String doctor;
    String address;
    String date;
    String time;

    public Appointment(String name, String doctor, String address, String date, String time) {
        this.name = name;
        this.doctor = doctor;
        this.address = address;
        this.date = date;
        this.time = time;
    }

}
