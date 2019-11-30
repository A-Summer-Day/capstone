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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class SexualActivityStatisticsFragment extends Fragment implements
        NumberPicker.OnValueChangeListener, View.OnClickListener {
    private View view;
    private TextView cycleLength, periodLength;
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users"); // Database reference
    private String currentUserId;
    private FirebaseUser currentUser;
    private NumberPicker monthPicker, yearPicker;
    private int selectedYear, selectedMonth;
    private TextView yearlyPartnerCount, yearlyEncounterCount, monthlyPartnerCount, monthlyEncounterCount;
    private Button viewStats;
    static final int MAX_YEAR = 2099; // max year for calendar
    static final int MIN_YEAR = 1900; // min year for calendar
    private int totalPartners, totalEncounters, totalMonthlyEnCounters, totalMonthlyPartners;
    private Map<String,String> listOfPartners;

    public SexualActivityStatisticsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sexual_activity_statistics, container, false);

        Date date = new Date();
        DateFormat monthFormat = new SimpleDateFormat("MM");
        DateFormat yearFormat = new SimpleDateFormat("yyyy");


        viewStats = view.findViewById(R.id.viewStatsButton);
        viewStats.setOnClickListener(this);

        listOfPartners = new TreeMap<>(String.CASE_INSENSITIVE_ORDER); // list of partners, case-insensitive

        yearlyPartnerCount = view.findViewById(R.id.totalPartners);
        yearlyEncounterCount = view.findViewById(R.id.totalSexualEncounters);
        monthlyPartnerCount = view.findViewById(R.id.monthly_partners);
        monthlyEncounterCount = view.findViewById(R.id.monthly_encounters);

        fm = getFragmentManager();
        fragmentTransaction = fm.beginTransaction();


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();
        myref = myref.child(currentUserId); // set database reference path to current user id

        monthPicker = view.findViewById(R.id.month_picker);
        yearPicker = view.findViewById(R.id.year_picker);

        yearPicker.setMinValue(MIN_YEAR); // set min year
        yearPicker.setMaxValue(MAX_YEAR); // set max year
        yearPicker.setOnValueChangedListener(this);
        selectedYear = Integer.parseInt(yearFormat.format(date)); // get current year
        yearPicker.setValue(selectedYear); // set current year as selected year

        monthPicker.setMaxValue(12); // set min month
        monthPicker.setMinValue(1); // set max month
        monthPicker.setOnValueChangedListener(this);
        selectedMonth = Integer.parseInt(monthFormat.format(date)); // get current month
        monthPicker.setValue(selectedMonth); // set current month as selected month

        totalEncounters = 0; // total yearly encounters
        totalPartners = 0; // total yearly partners
        totalMonthlyEnCounters = 0; // total monthly encounters
        totalMonthlyPartners = 0; // total monthly partners

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){ // if there is no data
                    yearlyEncounterCount.setText("N/A");
                    yearlyPartnerCount.setText("N/A");
                }else { // if data exists
                    for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                        if(ds.getChildrenCount() > 0){
                            for (final DataSnapshot ds1 : ds.getChildren()){
                                String p = ds1.child("partner").getValue().toString();
                                // if partner is not in list, add partner to list
                                // case-insensitive
                                // for example, "peter" or "Peter" are both the same
                                if(!p.equals("") && !listOfPartners.containsKey(p)){
                                    listOfPartners.put(p,p);
                                    totalPartners += 1; // increment the count accordingly
                                }

                                totalEncounters += 1;
                            }
                        }
                    }

                    // set yearly partners and encounters
                    yearlyPartnerCount.setText(Integer.toString(totalPartners));
                    yearlyEncounterCount.setText(Integer.toString(totalEncounters));

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // Check if there is any sexual activity data for this user
        myref.child("sexual-activity-tracking").child(Integer.toString(selectedYear))
                .addListenerForSingleValueEvent(valueEventListener);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.viewStatsButton:
                totalMonthlyEnCounters = 0;
                totalMonthlyPartners = 0;
                myref.child("sexual-activity-tracking").child(Integer.toString(selectedYear))
                        .child(Integer.toString(selectedMonth)).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.exists()) {
                                    monthlyPartnerCount.setText("N/A");
                                    monthlyEncounterCount.setText("N/A");
                                }else{
                                    listOfPartners = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                                    for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                                        if(dataSnapshot.getChildrenCount() > 0){
                                            totalMonthlyEnCounters = (int) dataSnapshot.getChildrenCount();
                                            String p = ds.child("partner").getValue().toString();
                                            // if partner is not in list, add partner to list
                                            // case-insensitive
                                            // for example, "peter" or "Peter" are both the same
                                            if(!p.equals("") && !listOfPartners.containsKey(p)){
                                                listOfPartners.put(p,p);
                                                totalMonthlyPartners += 1; // increment the count accordingly
                                            }
                                        }
                                    }

                                    // set monthly partners and encounters
                                    monthlyPartnerCount.setText(Integer.toString(totalMonthlyPartners));
                                    monthlyEncounterCount.setText(Integer.toString(totalMonthlyEnCounters));

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        }
                );
                break;
        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        switch(picker.getId()){
            case R.id.month_picker:
                selectedMonth = newVal; // update selected month
                // reset top half fields
                monthlyPartnerCount.setText("");
                monthlyEncounterCount.setText("");
                break;
            case R.id.year_picker:
                selectedYear = newVal; // update selected year
                // reset top half fields
                monthlyPartnerCount.setText("");
                monthlyEncounterCount.setText("");
                totalEncounters = 0; // reset yearly encounters
                totalPartners = 0; // reset yearly partners
                // initialize new list of yearly partners, case-insensitive
                listOfPartners = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()){ // if there is no data
                            yearlyEncounterCount.setText("N/A");
                            yearlyPartnerCount.setText("N/A");
                        }else { // if data exists
                            for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                                if(ds.getChildrenCount() > 0){
                                    for (final DataSnapshot ds1 : ds.getChildren()){
                                        // if partner is not in list, add partner to list
                                        // case-insensitive
                                        // for example, "peter" or "Peter" are both the same
                                        String p = ds1.child("partner").getValue().toString();
                                        if(!p.equals("") && !listOfPartners.containsKey(p)){
                                            listOfPartners.put(p,p);
                                            totalPartners += 1; // increment the count accordingly
                                        }

                                        totalEncounters += 1;
                                    }
                                }
                            }

                            // set yearly partners and encounters
                            yearlyPartnerCount.setText(Integer.toString(totalPartners));
                            yearlyEncounterCount.setText(Integer.toString(totalEncounters));

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };

                // Check if there is any sexual activity data for this user at selected year and selected month
                myref.child("sexual-activity-tracking").child(Integer.toString(selectedYear))
                        .addListenerForSingleValueEvent(valueEventListener);
                break;
        }
    }
}
