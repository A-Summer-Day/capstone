package ca.mohawk.le.mytime;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChangePasswordFragment extends Fragment implements View.OnClickListener {
    private View view;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference().child("users"); // Database reference
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    private String currentUserId;
    private FirebaseUser currentUser;
    private EditText getOldPassword, getNewPassword;
    private String oldPassword, newPassword;
    private Button changeButton, cancelButton;
    public ChangePasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_change_password, container, false);

        fm = getFragmentManager();
        fragmentTransaction = fm.beginTransaction();

        // Get the current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();

        getOldPassword = view.findViewById(R.id.oldPassword);
        getNewPassword = view.findViewById(R.id.newPassword);

        changeButton = view.findViewById(R.id.changeButton);
        cancelButton =  view.findViewById(R.id.cancelButton);

        changeButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.changeButton: // if user decides to change password

                oldPassword = getOldPassword.getText().toString(); // get old password
                newPassword= getNewPassword.getText().toString(); // get new password

                // Make sure both password fields are not blank
                if(!TextUtils.isEmpty(oldPassword) && !TextUtils.isEmpty(newPassword)){
                    // Check if old password is correct
                    AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);
                    currentUser.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    if(task.isSuccessful()){ // old password is correct, change it
                                        currentUser.updatePassword(newPassword);
                                        ProfileFragment profileFragment = new ProfileFragment();
                                        fragmentTransaction.replace(R.id.generalLayout, profileFragment);
                                        fragmentTransaction.commit();
                                        Toast.makeText(getActivity(), "Password reset!", Toast.LENGTH_LONG).show();
                                    }else{
                                        // old password is incorrect, let the user know
                                        Toast.makeText(getActivity(), "Old password is incorrect!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else{
                    Toast.makeText(getActivity(), "Both passwords cannot be blank!", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.cancelButton: // If user cancels, take user back to the profile page
                ProfileFragment profileFragment = new ProfileFragment();
                fragmentTransaction.replace(R.id.generalLayout, profileFragment);
                fragmentTransaction.commit();
                break;
        }
    }


}
