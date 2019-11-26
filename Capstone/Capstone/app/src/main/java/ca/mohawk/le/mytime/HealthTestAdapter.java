package ca.mohawk.le.mytime;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class HealthTestAdapter extends ArrayAdapter<HealthTest> {

    public HealthTestAdapter(Context context, ArrayList<HealthTest> healthtests) {
        super(context, 0, healthtests);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HealthTest healthTest = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.health_test_detail, parent, false);
        }

        TextView name = convertView.findViewById(R.id.healthtest_name);
        TextView frequency = convertView.findViewById(R.id.healthtest_frequency);
        TextView lastTestDate = convertView.findViewById(R.id.healthtest_last_testdate);


        name.setText(healthTest.name);
        frequency.setText("Every " + healthTest.frequency + " " + healthTest.unit);
        lastTestDate.setText("Last test date: " + healthTest.lastTestDate);
        return convertView;
    }
}
