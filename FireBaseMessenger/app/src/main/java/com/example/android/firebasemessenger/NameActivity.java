package com.example.android.firebasemessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * дане актівіті відображає поля для вводу імені користувача
 */
public class NameActivity extends AppCompatActivity {
    private static final String TAG = "log";
    private EditText nameEt, lastNameEt;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.name_layout);

        nameEt = (EditText) findViewById(R.id.name_et);
        lastNameEt = (EditText) findViewById(R.id.last_name_et);

        nextBtn = (Button) findViewById(R.id.next_btn);
        /**
         *  створюємо імя користувача якшо його нема  та записуємо його,
         *  інакше переходимо на інше актіві
         */
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser.getDisplayName() == null && validateName()) {

                    updateUI(firebaseUser);

                } else {
                    Intent i = new Intent(NameActivity.this, MessengerActivity.class);
                    startActivity(i);
                    Log.d(TAG, "onClick: privet");
                }


            }
        });
    }

    /**
     * @param user наш користувач для якого буде встановлення імені
     */
    private void updateUI(FirebaseUser user) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nameEt.getText().toString() + " " + lastNameEt.getText().toString())
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated. on NameActivity");

                            Intent i = new Intent(NameActivity.this, MessengerActivity.class);
                            startActivity(i);
                            finish();
                        } else
                            Toast.makeText(NameActivity.this, "error updating user", Toast.LENGTH_SHORT);
                    }
                });
    }

    /**
     *
     * @return 1 єслі поле для імені не пусте, 0 єслі пусте
     */
    private boolean validateName() {
        String name = nameEt.getText().toString();
        if (TextUtils.isEmpty(name)) {
            nameEt.setError("Invalid name.");
            return false;
        }

        return true;
    }
}
