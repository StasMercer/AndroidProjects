package com.example.android.firebasemessenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

/**
 * Created by Mercer on 23.02.2018.
 * клас бокового меню
 */

@NonReusable
@Layout(R.layout.drawer_header)
public class DrawerHeader {
    SharedPreferences sharedPreferences;
    private Uri photoUri;
    private String mUsername;
    private String mPhoneNumber;
    private String mProfilePhoto;
    private Context context;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @View(R.id.profileImageView)
    private ImageView profileImage;

    @View(R.id.nameTxt)
    private TextView nameTxt;

    @View(R.id.emailTxt)
    private TextView phoneTxt;

    public DrawerHeader(Context context, String username, String phoneNumber, String profilePhoto) {
        this.context = context;
        mPhoneNumber = phoneNumber;
        mUsername = username;
        mProfilePhoto = profilePhoto;

        sharedPreferences = context.getSharedPreferences("prefs",Context.MODE_PRIVATE);
    }

    @Resolve
    private void onResolved() {


        nameTxt.setText(mAuth.getCurrentUser().getDisplayName());
        phoneTxt.setText(mAuth.getCurrentUser().getPhoneNumber());

        Log.d("tag","resolved "+mUsername+" "+mPhoneNumber);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + mAuth.getCurrentUser().getPhoneNumber());

        Glide
                .with(context)
                .using(new FirebaseImageLoader())
                .load(storageReference)
                .signature(new StringSignature(sharedPreferences.getString("photoTime", "1")))
                .into(profileImage);

    }
}