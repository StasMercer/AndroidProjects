package com.example.android.firebasemessenger;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    private static final String TAG = "tag";

    @Override
    public void onTokenRefresh() {
        try {
            // Get updated InstanceID token.
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();

            String userPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(userPhone).child("messageToken");
            myRef.setValue(refreshedToken);
            // If you want to send messages to this application instance or
            // manage this apps subscriptions on the server side, send the
            // Instance ID token to your app server.
            sendRegistrationToServer(refreshedToken);
        }catch (Exception e){

        }
    }

    private void sendRegistrationToServer(String refreshedToken) {

    }

}
