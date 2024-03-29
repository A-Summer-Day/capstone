package ca.mohawk.le.mytime;

// Pie chart source:
// https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/main/java/com/xxmassdeveloper/mpchartexample/PieChartActivity.java

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MoodStatisticsFragment extends Fragment implements View.OnClickListener,
        NumberPicker.OnValueChangeListener {
    private View view;
    PieChart pieChart;
    PieData pieData;
    PieDataSet pieDataSet;
    ArrayList pieEntries;
    ArrayList PieEntryLabels;
    Legend legend;
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users"); // Database reference
    private String currentUserId;
    private FirebaseUser currentUser;
    private NumberPicker monthPicker, yearPicker;
    private int selectedYear, selectedMonth;
    private Button viewStats;
    private int happyCount, sadCount, neutralCount, daysInMonth;
    static final int MAX_YEAR = 2099; // max year for calendar
    static final int MIN_YEAR = 1900; // min year for calendar
    private float totalDays, totalCycles, totalMonthlyDays;
    public MoodStatisticsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_mood_statistics, container, false);

        Date date = new Date();
        DateFormat monthFormat = new SimpleDateFormat("MM");
        DateFormat yearFormat = new SimpleDateFormat("yyyy");


        viewStats = view.findViewById(R.id.viewStatsButton);
        viewStats.setOnClickListener(this);

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

        pieChart = view.findViewById(R.id.chart);
        pieChart.setVisibility(View.GONE); // hide pie chart on page load

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.viewStatsButton:
                // calculate total days in a specific month
                daysInMonth = selectedMonth == 2 ?
                        28 + (selectedYear % 4 == 0 ? 1:0) - (selectedYear % 100 == 0 ?
                                (selectedYear % 400 == 0 ? 0 : 1) : 0) :
                        31 - (selectedMonth-1) % 7 % 2;

                happyCount = 0; // variable to keep track of happy days
                sadCount = 0; // variable to keep track of sad days
                neutralCount = 0; // variable to keep track of neutral days

                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()){ // if there is no data
                            Toast.makeText(getActivity(), "No data to display.", Toast.LENGTH_SHORT).show();
                        }else{ // if data exists
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                if(dataSnapshot.getChildrenCount()>0){ // increment counts accordingly
                                    if(ds.child("mood").getValue().equals("Sad")){
                                        sadCount++;
                                    }else if(ds.child("mood").getValue().equals("Happy")){
                                        happyCount++;
                                    }else{
                                        neutralCount++;
                                    }
                                }


                                pieEntries = new ArrayList<>();
                                pieEntries.add(new PieEntry((float)happyCount/daysInMonth, "Happy"));
                                pieEntries.add(new PieEntry((float)sadCount/daysInMonth, "Sad"));
                                pieEntries.add(new PieEntry((float)neutralCount/daysInMonth, "Neutral"));

                                // calculate days with no data
                                int remainingCount = daysInMonth - (sadCount+happyCount+neutralCount);
                                pieEntries.add(new PieEntry((float)remainingCount/daysInMonth, "N/A"));


                                pieDataSet = new PieDataSet(pieEntries, "");
                                pieDataSet.setValueFormatter(new MyValueFormatter());

                                pieData = new PieData(pieDataSet);
                                pieChart.setData(pieData); // set data for pie chart
                                pieChart.setUsePercentValues(true); // show %
                                pieData.setValueTextSize(10f);
                                pieDataSet.setColors(Color.parseColor("#F08080"),Color.parseColor("#87CEFA"),
                                        Color.parseColor("#C0C0C0"), Color.parseColor("#F5F5F5"));
                                pieChart.setEntryLabelTextSize(10f);
                                pieChart.setEntryLabelColor(Color.BLACK);
                                Legend legend = pieChart.getLegend();
                                legend.setTextSize(15f);

                                pieChart.setDrawHoleEnabled(false);
                                pieChart.getDescription().setEnabled(false);
                                pieChart.invalidate();
                                pieChart.setVisibility(View.VISIBLE); // show pie chart
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };

                // Check if there is any mood data for this user at selected year and selected month
                myref.child("mood-tracking").child(Integer.toString(selectedYear))
                        .child(Integer.toString(selectedMonth))
                        .addListenerForSingleValueEvent(valueEventListener);

                break;
        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        pieChart.setVisibility(View.GONE); // hide pie chart when month changes
        switch(picker.getId()){
            case R.id.month_picker:
                selectedMonth = newVal; // update selected year
                break;
            case R.id.year_picker:
                selectedYear = newVal; // update selected month
                break;
        }
    }


}
