package com.example.android.firebasemessenger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

import model.Constants;

public class ProfileActivity extends AppCompatActivity {

    private final int PICK_IMAGE = 2;
    private Uri downloadUrl;
    private SharedPreferences sharedPreferences;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private Button changePhotoBtn;
    private TextView nameTv, lastNameTV;
    private ImageView profileImageView;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        int check = 0;
        String username = mUser.getDisplayName();
        String[] userNameParams = new String[2];


        setContentView(R.layout.profile_layout);

        changePhotoBtn = (Button) findViewById(R.id.change_profile_photo_btn);

        nameTv = (TextView) findViewById(R.id.profile_name_tv);
        nameTv.setText(username);

        profileImageView = (ImageView) findViewById(R.id.profile_image_view);

        StorageReference downloadRef = FirebaseStorage.getInstance().getReference().child("images/" + mUser.getPhoneNumber());

        Log.d(Constants.TAG, "download ref: " + downloadRef.getName() + " " + downloadRef.getPath());


        Glide
                .with(ProfileActivity.this)
                .using(new FirebaseImageLoader())
                .load(downloadRef)
                .signature(new StringSignature(sharedPreferences.getString("photoTime", "1")))
                .into(profileImageView);

        changePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 2);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE: {
                    Uri file = data.getData();
                    final Calendar calendar = Calendar.getInstance();

                    final Editor editor = sharedPreferences.edit();
                    StorageReference uploadRef = storageRef.child("images/" + mUser.getPhoneNumber());
                    UploadTask uploadTask = uploadRef.putFile(file);


// Register observers to listen for when the download is done or if it fails
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            downloadUrl = taskSnapshot.getDownloadUrl();
                            editor.putString("photoTime", String.valueOf(calendar.getTimeInMillis()));
                            editor.commit();

                            Glide.clear(profileImageView);
                            Glide
                                    .with(getApplicationContext())
                                    .load(downloadUrl)
                                    .centerCrop()
                                    .signature(new StringSignature(downloadUrl.toString()))
                                    .into(profileImageView);

                        }
                    });


                }
            }
        }
    }
}
