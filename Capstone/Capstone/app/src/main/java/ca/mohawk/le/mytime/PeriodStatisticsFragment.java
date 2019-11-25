package ca.mohawk.le.mytime;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class PeriodStatisticsFragment extends Fragment implements NumberPicker.OnValueChangeListener, View.OnClickListener {
    private View view;
    private TextView cycleLength, periodLength;
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users");
    private String currentUserId, first, last;
    private FirebaseUser currentUser;
    private NumberPicker monthPicker, yearPicker;
    private int selectedYear, selectedMonth;
    private TextView typicalCycleLength, typicalPeriodLength, monthlyPeriodLength, monthlyPeriodRange;
    private Button viewStats;
    static final int MAX_YEAR = 2099;
    static final int MIN_YEAR = 1900;
    private float totalDays, totalCycles, totalMonthlyDays;
    private List<Date> dates;

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


        viewStats = view.findViewById(R.id.viewStatsButton);
        viewStats.setOnClickListener(this);

        dates = new ArrayList<>();

        typicalCycleLength = view.findViewById(R.id.cycleLength);
        typicalPeriodLength = view.findViewById(R.id.periodLength);
        monthlyPeriodLength = view.findViewById(R.id.monthly_period);
        monthlyPeriodRange = view.findViewById(R.id.period_range);

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
        selectedYear = Integer.parseInt(yearFormat.format(date));
        yearPicker.setValue(selectedYear);

        monthPicker.setMaxValue(12);
        monthPicker.setMinValue(1);
        monthPicker.setOnValueChangedListener(this);
        selectedMonth = Integer.parseInt(monthFormat.format(date));
        monthPicker.setValue(selectedMonth);


        totalDays = 0;
        totalCycles = 0;
        totalMonthlyDays = 0;

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    typicalCycleLength.setText("N/A");
                    typicalPeriodLength.setText("N/A");
                }else {

                    for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                        if(ds.getChildrenCount() > 0){
                            for (final DataSnapshot ds1 : ds.getChildren()){
                                int count = (int) ds1.getChildrenCount();
                                ds1.getRef().orderByKey().limitToFirst(1)
                                        .addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                        Log.d("REF FIRST CHILD", dataSnapshot.getKey());
                                        DateFormat fullFormat = new SimpleDateFormat("MM/dd/yyyy");
                                        String dateString = ds1.getKey() + "/" + dataSnapshot.getKey() + "/" + ds.getKey();
                                        try{
                                            Date d = fullFormat.parse(dateString);
                                            dates.add(d);
                                        }catch(ParseException e){
                                            e.printStackTrace();
                                        }

                                        Log.d("REF FULL FIRST", dateString);
                                    }

                                    @Override
                                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                    }

                                    @Override
                                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                ds1.getRef().orderByKey().limitToLast(1)
                                        .addChildEventListener(new ChildEventListener() {
                                            @Override
                                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                Log.d("REF LAST CHILD", dataSnapshot.getKey());
                                                DateFormat fullFormat = new SimpleDateFormat("MM/dd/yyyy");
                                                String dateString = ds1.getKey() + "/" + dataSnapshot.getKey() + "/" + ds.getKey();
                                                try{
                                                    Date d = fullFormat.parse(dateString);
                                                    dates.add(d);
                                                }catch(ParseException e){
                                                    e.printStackTrace();
                                                }

                                                Log.d("REF FULL LAST", dateString);
                                            }

                                            @Override
                                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                            }

                                            @Override
                                            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                            }

                                            @Override
                                            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                totalDays += count;
                                totalCycles += 1;
                            }
                        }

                    }

                    int pl = Math.round(totalDays/totalCycles);
                    typicalPeriodLength.setText(Integer.toString(pl));
                    if(dates.size() > 0){
                        dates.remove( dates.size() - 1 );
                        dates.remove(0);
                    }

                    Log.d("REF ARRAY", Integer.toString(dates.size()));
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.viewStatsButton:
                totalMonthlyDays = 0;
                ValueEventListener vel = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()){
                            monthlyPeriodRange.setText("N/A");
                            monthlyPeriodLength.setText("N/A");
                        }else {
                            if(dataSnapshot.getChildrenCount() > 0){
                                last = "";
                                first = "";
                                myref.child("period-tracking").child(Integer.toString(selectedYear))
                                        .child(Integer.toString(selectedMonth)).orderByKey().limitToFirst(1)
                                        .addChildEventListener(new ChildEventListener() {
                                            @Override
                                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                first = selectedMonth + "/" + dataSnapshot.getKey() + "/" + selectedYear;
                                                Log.d("REF FIRST", first);
                                            }

                                            @Override
                                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                            }

                                            @Override
                                            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                            }

                                            @Override
                                            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });


                                myref.child("period-tracking").child(Integer.toString(selectedYear))
                                        .child(Integer.toString(selectedMonth)).limitToLast(1)
                                        .addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                        last = selectedMonth + "/" + dataSnapshot.getKey() + "/" + selectedYear;
                                        Log.d("REF LAST", last);
                                        monthlyPeriodRange.setText(first + " - " + last);
                                    }

                                    @Override
                                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                    }

                                    @Override
                                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                totalMonthlyDays += dataSnapshot.getChildrenCount();
                                monthlyPeriodLength.setText(Integer.toString((int)totalMonthlyDays));
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
                myref.child("period-tracking").child(Integer.toString(selectedYear)).child(Integer.
                        toString(selectedMonth)).addListenerForSingleValueEvent(vel);

                break;

        }
    }
}
