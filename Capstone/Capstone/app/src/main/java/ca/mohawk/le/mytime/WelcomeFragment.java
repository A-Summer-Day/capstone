package ca.mohawk.le.mytime;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeFragment extends Fragment implements View.OnClickListener {
    private View view;
    private static final String TAG = "MainActivity";
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    private Button trackPeriodButton, trackMoodsButton, trackTemperatureButton, trackSexualActivitiesButton, profileButton;
    public WelcomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_welcome, container, false);
        //FirebaseDatabase.getInstance().getReference().keepSynced(true);

        fm = getFragmentManager();
        fragmentTransaction = fm.beginTransaction();
        trackMoodsButton = view.findViewById(R.id.trackMoodsButton);
        trackPeriodButton = view.findViewById(R.id.trackPeriodButton);
        trackSexualActivitiesButton = view.findViewById(R.id.trackSexualActivitiesButton);
        trackTemperatureButton = view.findViewById(R.id.trackTemperatureButton);
        profileButton = view.findViewById(R.id.profileButton);

        trackMoodsButton.setOnClickListener(this);
        trackPeriodButton.setOnClickListener(this);
        trackSexualActivitiesButton.setOnClickListener(this);
        trackTemperatureButton.setOnClickListener(this);
        profileButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trackPeriodButton:
                PeriodTrackingFragment periodTrackingFragment = new PeriodTrackingFragment();
                fragmentTransaction.replace(R.id.generalLayout, periodTrackingFragment);
                fragmentTransaction.commit();
                break;
            case R.id.trackSexualActivitiesButton:
                SexualActivityTrackingFragment sexualActivityTrackingFragment = new SexualActivityTrackingFragment();
                fragmentTransaction.replace(R.id.generalLayout, sexualActivityTrackingFragment);
                fragmentTransaction.commit();
                break;
            case R.id.trackMoodsButton:
                MoodTrackingFragment moodTrackingFragment = new MoodTrackingFragment();
                fragmentTransaction.replace(R.id.generalLayout, moodTrackingFragment);
                fragmentTransaction.commit();
                break;
            case R.id.trackTemperatureButton:
                TemperatureTrackingFragment temperatureTrackingFragment = new TemperatureTrackingFragment();
                fragmentTransaction.replace(R.id.generalLayout, temperatureTrackingFragment);
                fragmentTransaction.commit();
                break;
            case R.id.profileButton:
                ProfileFragment profileFragment = new ProfileFragment();
                fragmentTransaction.replace(R.id.generalLayout, profileFragment);
                fragmentTransaction.commit();
                break;
        }
    }
}
