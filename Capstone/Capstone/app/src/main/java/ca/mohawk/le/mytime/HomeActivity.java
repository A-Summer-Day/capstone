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
    DatabaseReference myref = database.getReference().child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w("GET TOKEN", "getInstanceId failed", task.getException());
                    return;
                }

                // Get new Instance ID token
                token = task.getResult().getToken();

                // Log and toast
                //String msg = getString(R.string.msg_token_fmt, token);
                Log.d("GET TOKEN", token);
                //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();

            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    myref.child(currentUserId).child("personal-info").child("token").setValue(token);
                    Log.d("GET TOKEN", "TOKEN SET");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        myref.child(currentUserId).child("personal-info").child("token").
                addListenerForSingleValueEvent(valueEventListener);

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
                ProfileFragment profilePage = new ProfileFragment();
                fragmentTransaction.replace(R.id.generalLayout, profilePage);
                fragmentTransaction.commit();
                break;
            case R.id.nav_settings:
                SettingsFragment settingsPage = new SettingsFragment();
                fragmentTransaction.replace(R.id.generalLayout, settingsPage);
                fragmentTransaction.commit();
                break;
            case R.id.nav_period_tracking:
                PeriodTrackingFragment periodTrackingPage = new PeriodTrackingFragment();
                fragmentTransaction.replace(R.id.generalLayout, periodTrackingPage);
                fragmentTransaction.commit();
                break;
            case R.id.nav_sexual_activity_tracking:
                SexualActivityTrackingFragment sexualActivityTrackingPage = new SexualActivityTrackingFragment();
                fragmentTransaction.replace(R.id.generalLayout, sexualActivityTrackingPage);
                fragmentTransaction.commit();
                break;
            case R.id.nav_test_tracking:
                //SexualActivityTrackingFragment sexualActivityTrackingPage = new SexualActivityTrackingFragment();
                //fragmentTransaction.replace(R.id.generalLayout, sexualActivityTrackingPage);
                //fragmentTransaction.commit();
                break;
            case R.id.nav_mood_tracking:
                MoodTrackingFragment moodTrackingPage = new MoodTrackingFragment();
                fragmentTransaction.replace(R.id.generalLayout, moodTrackingPage);
                fragmentTransaction.commit();
                break;
            case R.id.nav_temperature_tracking:
                TemperatureTrackingFragment temperatureTrackingPage = new TemperatureTrackingFragment();
                fragmentTransaction.replace(R.id.generalLayout, temperatureTrackingPage);
                fragmentTransaction.commit();
                break;
            case R.id.nav_appointments:
                AppointmentFragment appointmentFragment = new AppointmentFragment();
                fragmentTransaction.replace(R.id.generalLayout, appointmentFragment);
                fragmentTransaction.commit();
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                updateUI(null);
                //Toast.makeText(this, "CAMERA!", Toast.LENGTH_SHORT).show();
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
