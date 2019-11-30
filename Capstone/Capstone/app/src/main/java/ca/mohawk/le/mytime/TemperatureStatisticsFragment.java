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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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
public class TemperatureStatisticsFragment extends Fragment implements View.OnClickListener,
        NumberPicker.OnValueChangeListener {
    private View view;
    LineChart lineChart;
    LineData lineData;
    LineDataSet lineDataSet;
    ArrayList <Entry> lineEntries;
    Legend legend;
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users");  // Database reference
    private String currentUserId;
    private FirebaseUser currentUser;
    private NumberPicker monthPicker, yearPicker;
    private int selectedYear, selectedMonth, daysInMonth;
    private Button viewStats;
    static final int MAX_YEAR = 2099; // max year for calendar
    static final int MIN_YEAR = 1900; // min year for calendar

    public TemperatureStatisticsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_temperature_statistics, container, false);

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
        monthPicker.setValue(selectedMonth);  // set current month as selected month

        lineChart = view.findViewById(R.id.chart);
        lineChart.setVisibility(View.GONE); // hide line chart on page load

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

                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()){ // if there is no data
                            Toast.makeText(getActivity(), "No data to display.", Toast.LENGTH_SHORT).show();
                        }else{ // if data exists
                            // initialize the data with default value 37
                            lineEntries = new ArrayList<>(daysInMonth);
                            for(int i = 0; i < daysInMonth; i++){
                                lineEntries.add(new Entry(i+1,37));
                            }

                            // if there is data for any date, change it
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                if(dataSnapshot.getChildrenCount()>0){
                                    float temp = Float.parseFloat(ds.child("temperature").getValue().toString());
                                    int m = Integer.parseInt(ds.getKey());
                                    lineEntries.set(m-1, new Entry(m,temp));
                                }
                                lineDataSet = new LineDataSet(lineEntries, "Temperature");
                                lineDataSet.setColors(Color.parseColor("#F08080"));
                                lineDataSet.setValueTextColor(Color.BLACK);
                                // Controlling X axis
                                XAxis xAxis = lineChart.getXAxis();
                                // Set the xAxis position to bottom. Default is top
                                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                YAxis yAxisRight = lineChart.getAxisRight();
                                yAxisRight.setEnabled(false);
                                YAxis yAxisLeft = lineChart.getAxisLeft();
                                yAxisLeft.setGranularity(1f);
                                Legend legend = lineChart.getLegend();
                                legend.setTextSize(15f);
                                legend.setTextColor(Color.parseColor("#F08080"));
                                lineData = new LineData(lineDataSet);
                                lineChart.setData(lineData); // set data for line chart
                                lineDataSet.setDrawValues(false);
                                lineDataSet.setDrawCircles(false);
                                lineChart.getDescription().setEnabled(false);
                                lineChart.invalidate();
                                lineChart.setVisibility(View.VISIBLE); // show line chart
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };

                // Check if there is any temperature data for this user at selected year and selected month
                myref.child("temperature-tracking").child(Integer.toString(selectedYear))
                        .child(Integer.toString(selectedMonth))
                        .addListenerForSingleValueEvent(valueEventListener);

                break;
        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        lineChart.setVisibility(View.GONE); // hide line chart when month changes
        switch(picker.getId()){
            case R.id.month_picker:
                selectedMonth = newVal; // update selected month
                break;
            case R.id.year_picker:
                selectedYear = newVal; // update selected year
                break;
        }
    }
}
