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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
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
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MoodStatisticsFragment extends Fragment implements View.OnClickListener, NumberPicker.OnValueChangeListener {
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
    DatabaseReference myref = database.getReference().child("users");
    private String currentUserId;
    private FirebaseUser currentUser;
    private NumberPicker monthPicker, yearPicker;
    private int selectedYear, selectedMonth;
    private Button viewStats;
    static final int MAX_YEAR = 2099;
    static final int MIN_YEAR = 1900;
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

        pieChart = view.findViewById(R.id.chart);
        getEntries();
        pieDataSet = new PieDataSet(pieEntries, "");
        pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        pieDataSet.setColors(Color.RED, Color.GREEN, Color.BLUE, Color.GRAY);


        pieDataSet.setSliceSpace(2f);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(10f);
        pieDataSet.setSliceSpace(5f);
        List<LegendEntry> legends = new ArrayList<>();
        LegendEntry red = new LegendEntry();
        red.label = "Missed";
        red.formColor = Color.RED;
        legends.add(red);
        LegendEntry blue = new LegendEntry();
        blue.label = "Completed";
        blue.formColor = Color.BLUE;
        legends.add(blue);
        pieChart.getLegend().setCustom(legends);

        return view;
    }

    private void getEntries() {
        pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(2f, 0));
        pieEntries.add(new PieEntry(4f, 1));
        pieEntries.add(new PieEntry(6f, 2));
        pieEntries.add(new PieEntry(8f, 3));
        pieEntries.add(new PieEntry(7f, 4));
        pieEntries.add(new PieEntry(3f, 5));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.viewStatsButton:
                break;
        }
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
