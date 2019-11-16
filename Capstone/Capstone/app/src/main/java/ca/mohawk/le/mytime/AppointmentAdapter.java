package ca.mohawk.le.mytime;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class AppointmentAdapter extends ArrayAdapter<Appointment> {

    public AppointmentAdapter(Context context, ArrayList<Appointment> appointments) {
        super(context, 0, appointments);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Appointment appointment = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.upcoming_appointment, parent, false);
        }

        TextView name = convertView.findViewById(R.id.test_name);
        TextView address = convertView.findViewById(R.id.test_address);
        TextView doctor = convertView.findViewById(R.id.test_doctor);
        TextView date = convertView.findViewById(R.id.test_date);


        name.setText(appointment.name);
        address.setText(appointment.address);
        doctor.setText(appointment.doctor);
        date.setText(appointment.date);

        return convertView;
    }


}
