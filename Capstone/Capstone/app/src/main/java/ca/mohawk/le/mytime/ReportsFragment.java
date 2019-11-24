package ca.mohawk.le.mytime;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReportsFragment extends Fragment implements View.OnClickListener {
    private  View view;
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    Button moodStatistics, temperatureStatistics, periodStatistics, sexualActivityStatistics;
    public ReportsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_reports, container, false);

        fm = getFragmentManager();
        fragmentTransaction = fm.beginTransaction();

        moodStatistics = view.findViewById(R.id.moodsStatistics);
        sexualActivityStatistics = view.findViewById(R.id.sexualActivityStatistics);
        temperatureStatistics = view.findViewById(R.id.temperatureStatistics);
        periodStatistics = view.findViewById(R.id.periodStatistics);

        moodStatistics.setOnClickListener(this);
        sexualActivityStatistics.setOnClickListener(this);
        temperatureStatistics.setOnClickListener(this);
        moodStatistics.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.periodStatistics:
                break;
            case R.id.sexualActivityStatistics:
                break;
            case R.id.moodsStatistics:
                break;
            case R.id.temperatureStatistics:
                break;
        }
    }
}
