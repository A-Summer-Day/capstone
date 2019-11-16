package ca.mohawk.le.mytime;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class MoodTrackingFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private View view;
    private CalendarView calendarView;
    Spinner spinner;
    public MoodTrackingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_mood_tracking, container, false);
        spinner = view.findViewById(R.id.moods_spinner);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.moods_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setSelection(0,false);
        spinner.setOnItemSelectedListener(this);
        return view;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.moods_spinner) {
            Log.d("LOG", "Item " + position + " was selected");
            String what = parent.getItemAtPosition(position).toString();

            Log.d("LOG", "Choice is " + what);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onClick(View v) {

    }
}
