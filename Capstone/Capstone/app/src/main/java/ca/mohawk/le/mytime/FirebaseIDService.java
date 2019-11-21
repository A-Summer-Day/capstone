package ca.mohawk.le.mytime;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;

import androidx.annotation.NonNull;

public class FirebaseIDService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseIDService";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users");
    private String currentUserId;
    private FirebaseUser currentUser;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d(TAG, "Refreshed token: " + s);
        sendRegistrationToServer(s);

    }

    private void sendRegistrationToServer(String token) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();
        myref.child(currentUserId).child("personal-info").child("token").setValue(token);
        // Add custom implementation, as needed.
    }
}
