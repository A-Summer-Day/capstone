package ca.mohawk.le.mytime;


import android.app.Activity;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;


/**
 * A simple {@link Fragment} subclass.
 */
public class PeriodStatisticsFragment extends Fragment implements
        NumberPicker.OnValueChangeListener, View.OnClickListener {
    private View view;
    private TextView cycleLength, periodLength;
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users"); // Database reference
    private String currentUserId, first, last;
    private FirebaseUser currentUser;
    private NumberPicker monthPicker, yearPicker;
    private int selectedYear, selectedMonth;
    private TextView typicalCycleLength, typicalPeriodLength, monthlyPeriodLength, monthlyPeriodRange;
    private Button viewStats;
    static final int MAX_YEAR = 2099; // max year for calendar
    static final int MIN_YEAR = 1900; // min year for calendar
    private int totalDays, totalCycles, totalMonthlyDays;
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
        myref = myref.child(currentUserId); // set database reference path to current user id

        monthPicker = view.findViewById(R.id.month_picker);
        yearPicker = view.findViewById(R.id.year_picker);

        yearPicker.setMinValue(MIN_YEAR); // set min year
        yearPicker.setMaxValue(MAX_YEAR); // set max year
        yearPicker.setOnValueChangedListener(this);
        selectedYear = Integer.parseInt(yearFormat.format(date)); // get current year
        yearPicker.setValue(selectedYear);  // set current year as selected year

        monthPicker.setMaxValue(12); // set min month
        monthPicker.setMinValue(1); // set max month
        monthPicker.setOnValueChangedListener(this);
        selectedMonth = Integer.parseInt(monthFormat.format(date)); // get current month
        monthPicker.setValue(selectedMonth); // set current month as selected month


        totalDays = 0; // todal days that are period dates
        totalCycles = 0; // total cycles
        totalMonthlyDays = 0; // total days that are period dates within a specific month

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){ // if there is no data
                    typicalCycleLength.setText("N/A");
                    typicalPeriodLength.setText("N/A");
                }else { // if data exists

                    for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                        if(ds.getChildrenCount() > 0){

                            for (final DataSnapshot ds1 : ds.getChildren()){
                                List<Date> temp = new ArrayList<>(); // temp list to store all logged dates of a month
                                int count = (int) ds1.getChildrenCount();
                                for(DataSnapshot ds2: ds1.getChildren()){
                                    DateFormat fullFormat = new SimpleDateFormat("MM/dd/yyyy");
                                    String dateString = ds1.getKey() + "/" + ds2.getKey() + "/" + ds.getKey();
                                    try{
                                        Date d = fullFormat.parse(dateString);
                                        //dates.add(d);
                                        temp.add(d);
                                    }catch(ParseException e){
                                        e.printStackTrace();
                                    }
                                }

                                // add just the first and last dates from that specific month
                                dates.add(temp.get(0));
                                dates.add(temp.get(temp.size() - 1));

                                totalDays += count;
                                totalCycles += 1;
                            }
                        }
                    }

                    // calculate and set typical period length
                    int pl = Math.round(totalDays/totalCycles);
                    typicalPeriodLength.setText(Integer.toString(pl));
                    Collections.sort(dates);
                    int days = getLength(dates);
                    // calculate and set typical cycle length
                    double cycleLength =  (double)days/(totalCycles-1);
                    typicalCycleLength.setText(Integer.toString((int) Math.round(cycleLength)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


        // Check if there is any period data for this user
        myref.child("period-tracking").addListenerForSingleValueEvent(valueEventListener);

        return view;
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        switch(picker.getId()){
            case R.id.month_picker:
                selectedMonth = newVal; // update selected month
                // reset top half fields
                monthlyPeriodRange.setText("");
                monthlyPeriodLength.setText("");
                break;
            case R.id.year_picker:
                selectedYear = newVal; // update selected year
                // reset top half fields
                monthlyPeriodRange.setText("");
                monthlyPeriodLength.setText("");
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
                        if(!dataSnapshot.exists()){ // if there is no data
                            monthlyPeriodRange.setText("N/A");
                            monthlyPeriodLength.setText("N/A");
                        }else { // if data exists
                            if(dataSnapshot.getChildrenCount() > 0){
                                last = "";
                                first = "";
                                // find the first and last period date of that month
                                myref.child("period-tracking").child(Integer.toString(selectedYear))
                                        .child(Integer.toString(selectedMonth)).orderByKey().limitToFirst(1)
                                        .addChildEventListener(new ChildEventListener() {
                                            @Override
                                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                first = selectedMonth + "/" + dataSnapshot.getKey() + "/" + selectedYear;
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

                                // set fields accordingly
                                totalMonthlyDays += dataSnapshot.getChildrenCount();
                                monthlyPeriodLength.setText(Integer.toString((int)totalMonthlyDays));
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };

                // Check if there is any period data for this user at selected year and selected month
                myref.child("period-tracking").child(Integer.toString(selectedYear)).child(Integer.
                        toString(selectedMonth)).addListenerForSingleValueEvent(vel);

                break;

        }
    }

    // return the total day differences between pairs of dates
    public int getLength(List<Date> dates){
        int length = 0;
        // remove first and last dates because there is no previous date for first date
        // and there is no following date for last date
        dates.remove(0);
        dates.remove(dates.size() - 1);
        for(int i = 0; i< dates.size(); i += 2){
            dates.get(i);
            dates.get(i+1);
            // calculate the difference between each date pair
            int difference = (int)(dates.get(i).getTime() - dates.get(i+1).getTime());
            int days = (int)(difference / (1000 * 60 * 60 * 24));
            length += days;
        }
        Log.d("REF CHECK", Integer.toString(length));
        return length;
    }


}
