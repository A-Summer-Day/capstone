package ca.mohawk.le.mytime;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;

import androidx.annotation.NonNull;

public class FirebaseIDService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseIDService";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        //String token = FirebaseInstanceId.getInstance().getToken();
        sendRegistrationToServer(s);

    }

    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
    }
}
