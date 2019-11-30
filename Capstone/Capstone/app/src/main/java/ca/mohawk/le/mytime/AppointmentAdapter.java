package ca.mohawk.le.mytime;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Custom adapter to store and display appointments
 */
public class AppointmentAdapter extends ArrayAdapter<Appointment> {

    public AppointmentAdapter(Context context, ArrayList<Appointment> appointments) {
        super(context, 0, appointments);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Appointment appointment = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.upcoming_appointment, parent, false);
        }

        // Lookup view for data population
        TextView name = convertView.findViewById(R.id.test_name);
        TextView address = convertView.findViewById(R.id.test_address);
        TextView doctor = convertView.findViewById(R.id.test_doctor);
        TextView date = convertView.findViewById(R.id.test_date);

        // Populate the data into the template view using the data object
        name.setText(appointment.name);
        address.setText(appointment.address);
        doctor.setText(appointment.doctor);
        date.setText(appointment.date + " " + appointment.time);

        // Return the completed view to render on screen
        return convertView;
    }


}
