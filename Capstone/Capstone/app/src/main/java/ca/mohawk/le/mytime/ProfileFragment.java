package ca.mohawk.le.mytime;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.database.ValueEventListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    //StorageReference imageRef = storageRef.child("profile.jpg");
    StorageReference imageRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference();
    private FragmentManager fm;
    private FragmentTransaction fragmentTransaction;
    private EditText name, dob, email, phone;
    private Button changePasswordButton;
    private AppCompatImageButton editButton;
    private URI filePath;
    private View view;
    private static int GET_FROM_GALLERY = 1;
    private ImageView profilePicture;
    private FirebaseUser currentUser;
    private String currentUserId;
    private boolean updating;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_profile, container, false);
        name = view.findViewById(R.id.userName);
        dob = view.findViewById(R.id.dateOfBirth);
        email = view.findViewById(R.id.emailAddress);
        phone = view.findViewById(R.id.phoneNumber);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(this);
        fm = getFragmentManager();
        fragmentTransaction = fm.beginTransaction();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();

        if(currentUser != null){
            email.setText(currentUser.getEmail());
            imageRef = storageRef.child("profile_" + currentUserId);

        }

        name.setEnabled(false);
        dob.setEnabled(false);
        email.setEnabled(false);
        phone.setEnabled(false);
        updating = true;
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){

                }else{

                    String getName = dataSnapshot.child("name").getValue().toString();
                    String getPhone = dataSnapshot.child("phone").getValue().toString();
                    String getDob = dataSnapshot.child("dob").getValue().toString();

                    name.setText(getName);
                    phone.setText(getPhone);
                    dob.setText(getDob);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        myref.child("users").child(currentUserId).child("personal-info").
                addListenerForSingleValueEvent(valueEventListener);
        profilePicture = view.findViewById(R.id.profilePicture);
        editButton = view.findViewById(R.id.editInfoButton);
        //Glide.with(this).load("https://boygeniusreport.files.wordpress.com/2016/11/puppy-dog.jpg").into(profilePicture);
        profilePicture.setOnClickListener(this);
        editButton.setOnClickListener(this);
        imageRef.getMetadata().addOnSuccessListener(
                new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(getContext()).load(uri).centerCrop().into(profilePicture);
                            }
                        });

                        //Toast.makeText(getActivity(), "Profile picture existed!", Toast.LENGTH_SHORT).show();
                    }
                }
                //
        ).addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {

               }
           }
        );



        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.profilePicture:
                chooseImage();
                break;
            case R.id.editInfoButton:
                updating = updateInfo(updating);
                break;
            case R.id.changePasswordButton:
                ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
                fragmentTransaction.replace(R.id.generalLayout, changePasswordFragment);
                fragmentTransaction.commit();
                break;
        }
    }

    private void chooseImage() {
        startActivityForResult(new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK){

            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try{
                bitmap = MediaStore.Images.Media.getBitmap(
                        getActivity().getContentResolver(), selectedImage);
                profilePicture.setImageBitmap(bitmap);
                profilePicture.getLayoutParams().height = 300;
                profilePicture.getLayoutParams().width = 300;
                profilePicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
                profilePicture.requestLayout();
                uploadImage();
            }catch(IOException e){
                Log.e("ERROR", "Error getting image!");
            }
        }
    }

    private void uploadImage() {
        ImageView profilePicture = view.findViewById(R.id.profilePicture);
        profilePicture.setDrawingCacheEnabled(true);
        profilePicture.buildDrawingCache();

        Bitmap bitmap = ((BitmapDrawable)profilePicture.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Upload Failed!", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //taskSnapshot.getMetadata();
                Toast.makeText(getActivity(), "Upload Succeeded!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean updateInfo(boolean updating){
        name = view.findViewById(R.id.userName);
        dob = view.findViewById(R.id.dateOfBirth);
        email = view.findViewById(R.id.emailAddress);
        phone = view.findViewById(R.id.phoneNumber);
        editButton = view.findViewById(R.id.editInfoButton);

        if(updating){
            name.setEnabled(true);
            dob.setEnabled(true);
            email.setEnabled(true);
            phone.setEnabled(true);
            editButton.setImageResource(android.R.drawable.ic_menu_save);
            return false;
        }else{
            final String newName = name.getText().toString();
            String newDOB = dob.getText().toString();
            final String newEmail = email.getText().toString();
            String newPhone = phone.getText().toString();

            currentUser.updateEmail(newEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                email.setText(newEmail);
                                Log.d("UPDATE", "Email updated.");
                            }
                        }
                    });

            myref.child("users").child(currentUserId).child("personal-info").child("phone").setValue(newPhone);
            myref.child("users").child(currentUserId).child("personal-info").child("name").setValue(newName);
            myref.child("users").child(currentUserId).child("personal-info").child("dob").setValue(newDOB);

            name.setEnabled(false);
            dob.setEnabled(false);
            email.setEnabled(false);
            phone.setEnabled(false);
            editButton.setImageResource(android.R.drawable.ic_menu_edit);
            return true;
        }
    }
}
