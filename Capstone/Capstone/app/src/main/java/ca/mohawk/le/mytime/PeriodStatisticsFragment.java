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
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class PeriodStatisticsFragment extends Fragment implements NumberPicker.OnValueChangeListener {
    private View view;
    private TextView cycleLength, periodLength;
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users");
    private String currentUserId;
    private FirebaseUser currentUser;
    private NumberPicker monthPicker, yearPicker;
    private int selectedYear, selectedMonth;
    private TextView typicalCycleLength, typicalPeriodLength;
    static final int MAX_YEAR = 2099;
    static final int MIN_YEAR = 1900;
    private float totalDays, totalCycles;
    public PeriodStatisticsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_period_statistics, container, false);

        Date date = new Date();
        DateFormat monthFormat = new SimpleDateFormat("MM");
        DateFormat yearFormat = new SimpleDateFormat("yyyy");

        typicalCycleLength = view.findViewById(R.id.cycleLength);
        typicalPeriodLength = view.findViewById(R.id.periodLength);

        fm = getFragmentManager();
        fragmentTransaction = fm.beginTransaction();

        cycleLength = view.findViewById(R.id.cycleLength);
        periodLength = view.findViewById(R.id.periodLength);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();
        myref = myref.child(currentUserId);

        monthPicker = view.findViewById(R.id.month_picker);
        yearPicker = view.findViewById(R.id.year_picker);

        yearPicker.setMinValue(MIN_YEAR);
        yearPicker.setMaxValue(MAX_YEAR);
        yearPicker.setOnValueChangedListener(this);
        yearPicker.setValue(Integer.parseInt(yearFormat.format(date)));

        monthPicker.setMaxValue(12);
        monthPicker.setMinValue(1);
        monthPicker.setOnValueChangedListener(this);
        monthPicker.setValue(Integer.parseInt(monthFormat.format(date)));

        totalDays = 0;
        totalCycles = 0;

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){

                }else {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if(ds.getChildrenCount() > 0){
                            for (DataSnapshot ds1 : ds.getChildren()){
                                int count = (int) ds1.getChildrenCount();
                                totalDays += count;
                                totalCycles += 1;
                                Log.d("REF", ds1.getKey());
                            }
                        }
                        //DatabaseReference childref = myref.child(ds.getKey());


                    }
                    Log.d("REF", Float.toString(totalCycles));
                    Log.d("REF", Float.toString(totalDays));
                    int pl = Math.round(totalDays/totalCycles);
                    //int pl = int (totalDays/totalCycles + 0.5);
                    typicalPeriodLength.setText(Integer.toString(pl));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };



        myref.child("period-tracking").addListenerForSingleValueEvent(valueEventListener);



        return view;
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        switch(picker.getId()){
            case R.id.month_picker:
                selectedMonth = newVal;
                break;
            case R.id.year_picker:
                selectedYear = newVal;
                break;
        }

    }
}
