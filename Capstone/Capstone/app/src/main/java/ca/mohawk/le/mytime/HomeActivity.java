package ca.mohawk.le.mytime;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout myDrawer;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String currentUserId;
    private String token;
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref;  // Database reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        myref  = database.getReference().child("users"); // set database reference path to "users"

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(
                new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w("GET TOKEN", "getInstanceId failed", task.getException());
                    return;
                }

                // Get new Instance ID token
                token = task.getResult().getToken();

                // Get the current user and update token on FireBase database
                currentUser = FirebaseAuth.getInstance().getCurrentUser();
                currentUserId = currentUser.getUid();
                myref.child(currentUserId).child("token").setValue(token);

                // For debug purpose
                Log.d("GET TOKEN", token);

            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();

        mAuth = FirebaseAuth.getInstance();

        myDrawer = findViewById(R.id.drawer_layout);
        NavigationView myNavView = findViewById(R.id.nav_view);
        myNavView.setNavigationItemSelectedListener(this);

        ActionBar myActionBar = getSupportActionBar();
        myActionBar.setDisplayHomeAsUpEnabled(true);

        ActionBarDrawerToggle myactionbartiggle = new ActionBarDrawerToggle(
                this, myDrawer, (R.string.open), (R.string.close));
        myDrawer.addDrawerListener(myactionbartiggle);
        myactionbartiggle.syncState();

        fm = getSupportFragmentManager();
        fragmentTransaction = fm.beginTransaction();
        WelcomeFragment welcomePage = new WelcomeFragment();

        fragmentTransaction.replace(R.id.generalLayout, welcomePage);
        fragmentTransaction.commit();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        fm = getSupportFragmentManager();
        fragmentTransaction = fm.beginTransaction();

        menuItem.setChecked(true);

        // Close the Drawer
        myDrawer.closeDrawers();

        switch (menuItem.getItemId()) {
            case R.id.nav_profile:
                // Take user to Profile Page
                ProfileFragment profilePage = new ProfileFragment();
                fragmentTransaction.replace(R.id.generalLayout, profilePage);
                fragmentTransaction.commit();
                break;
            case R.id.nav_home:
                // Take user to Welcome Page
                WelcomeFragment welcomePage = new WelcomeFragment();
                fragmentTransaction.replace(R.id.generalLayout, welcomePage);
                fragmentTransaction.commit();
                break;
            case R.id.nav_period_tracking:
                // Take user to Period Tracking Page
                PeriodTrackingFragment periodTrackingPage = new PeriodTrackingFragment();
                fragmentTransaction.replace(R.id.generalLayout, periodTrackingPage);
                fragmentTransaction.commit();
                break;
            case R.id.nav_sexual_activity_tracking:
                // Take user to Sexual Activity Tracking Page
                SexualActivityTrackingFragment sexualActivityTrackingPage = new SexualActivityTrackingFragment();
                fragmentTransaction.replace(R.id.generalLayout, sexualActivityTrackingPage);
                fragmentTransaction.commit();
                break;
            case R.id.nav_health_tests:
                // Take user to Health Tests Page
                HealthTestFragment healthTestPage = new HealthTestFragment();
                fragmentTransaction.replace(R.id.generalLayout, healthTestPage);
                fragmentTransaction.commit();
                break;
            case R.id.nav_mood_tracking:
                // Take user to Mood Tracking Page
                MoodTrackingFragment moodTrackingPage = new MoodTrackingFragment();
                fragmentTransaction.replace(R.id.generalLayout, moodTrackingPage);
                fragmentTransaction.commit();
                break;
            case R.id.nav_temperature_tracking:
                // Take user to Temperature Tracking Page
                TemperatureTrackingFragment temperatureTrackingPage = new TemperatureTrackingFragment();
                fragmentTransaction.replace(R.id.generalLayout, temperatureTrackingPage);
                fragmentTransaction.commit();
                break;
            case R.id.nav_appointments:
                // Take user to Appointment Page
                AppointmentFragment appointmentPage = new AppointmentFragment();
                fragmentTransaction.replace(R.id.generalLayout, appointmentPage);
                fragmentTransaction.commit();
                break;
            case R.id.nav_graphs_and_reports:
                // Take user to Statistics Page
                ReportsFragment reportsFragment = new ReportsFragment();
                fragmentTransaction.replace(R.id.generalLayout, reportsFragment);
                fragmentTransaction.commit();
                break;
            case R.id.nav_logout:
                // Log user out
                mAuth.signOut();
                updateUI(null);

                /**AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // ...
                            }
                        }); */
                break;

        }

        return false;
    }

    private void updateUI(FirebaseUser currentUser) {
        // if signed-out, take user back to the Login/Register page
        if(currentUser == null){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Find out the current state of the drawer (open or closed)
        boolean isOpen = myDrawer.isDrawerOpen(GravityCompat.START);

        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                // Home button - open or close the drawer
                if (isOpen == true) {
                    myDrawer.closeDrawer(GravityCompat.START);
                } else {
                    myDrawer.openDrawer(GravityCompat.START);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
