package ca.mohawk.le.mytime;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Custom adapter to store and display healthtests
 */
public class HealthTestAdapter extends ArrayAdapter<HealthTest> {

    public HealthTestAdapter(Context context, ArrayList<HealthTest> healthtests) {
        super(context, 0, healthtests);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        HealthTest healthTest = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.health_test_detail, parent, false);
        }

        // Lookup view for data population
        TextView name = convertView.findViewById(R.id.healthtest_name);
        TextView frequency = convertView.findViewById(R.id.healthtest_frequency);
        TextView lastTestDate = convertView.findViewById(R.id.healthtest_last_testdate);

        // Populate the data into the template view using the data object
        name.setText(healthTest.name);
        frequency.setText("Every " + healthTest.frequency + " " + healthTest.unit);
        lastTestDate.setText("Last test date: " + healthTest.lastTestDate);

        // Return the completed view to render on screen
        return convertView;
    }
}
