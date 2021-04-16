package com.example.android.firebasemessenger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.concurrent.TimeUnit;


/**
 * Created by Mercer on 18.02.2018.
 */

public class AuthActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "PhoneAuthActivity";
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    // тут у нас будуть положення аутентифікації через телефон
    private static final int STATE_INITIALIZED = 1; // всьо підготувалось
    private static final int STATE_CODE_SENT = 2; // отправили код верифікації
    private static final int STATE_VERIFY_FAILED = 3; // верифікація прощла хреново
    private static final int STATE_VERIFY_SUCCESS = 4; // верифікація успішна
    private static final int STATE_SIGNIN_FAILED = 5; // вхід в аккаунт не пройшов
    private static final int STATE_SIGNIN_SUCCESS = 6; // вхід в аккаунт успішний
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Button confirmBtn, verifyBtn, resendBtn;
    private EditText mPhoneNumberField, mVerificationField;
    private TextView mInfoTv;
    private SharedPreferences sharedPref;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_layout);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        sharedPref = getSharedPreferences("prefs", MODE_PRIVATE);

        mPhoneNumberField = (EditText) findViewById(R.id.phoneNumber_et);

        mVerificationField = (EditText) findViewById(R.id.verifyNumber_et);

        mInfoTv = (TextView) findViewById(R.id.greeting);


        confirmBtn = (Button) findViewById(R.id.confirm_btn);
        confirmBtn.setOnClickListener(this);

        verifyBtn = (Button) findViewById(R.id.verify_btn);
        verifyBtn.setOnClickListener(this);

        resendBtn = (Button) findViewById(R.id.resend_btn);
        resendBtn.setOnClickListener(this);
        resendBtn.setEnabled(false);
        // омагад це щось тіпа створення екземпляра з перевизначенням його методів(хотя тут може бути інтерфейс)
        //Не суть, це обратний отзив от їхньго сервака тут є парочка методів які визиваються в різних випадках
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // Цей зворотний виклик буде викликаний у двох ситуаціях:
            // 1 - Миттєва перевірка У деяких випадках номер телефону може бути моментально
            // підтверджено без необхідності надсилати або вводити код підтвердження.
            // 2 - Автозавантаження. На деяких пристроях сервіси Google Play можуть працювати автоматично
            // виявляє вхідну перевірку SMS і виконує перевірку без дії користувача.
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                hideProgressDialog();
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential);
                Log.d("UI", "verify succes");
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential);
            }

            // Цей зворотний виклик викликається невірним запитом для підтвердження,
            // Наприклад, якщо формат номера телефону недійсний.
            @Override
            public void onVerificationFailed(FirebaseException e) {

                Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                hideProgressDialog();
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    mPhoneNumberField.setError("Invalid phone number.");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Toast.makeText(AuthActivity.this, "Too many query", Toast.LENGTH_LONG).show();
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                updateUI(STATE_VERIFY_FAILED);
                Log.d("UI", "vrify failed");
                // [END_EXCLUDE]
            }


            // Код підтвердження SMS був відправлений на вказаний номер телефону
            // тепер потрібно попросити користувача ввести код, а потім побудувати обліковий запис
            // поєднання коду з ідентифікатором підтвердження.
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                resendBtn.setEnabled(true);
                updateUI(STATE_CODE_SENT);
                Log.d("UI", "code sent");

            }
        };


    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Перевіряємо чи юзер уже входив у систему, єслі да то не треба буде копошитись із смс
        FirebaseUser user = mAuth.getCurrentUser();
        updateUI(user);
        // [START_EXCLUDE]
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(mPhoneNumberField.getText().toString());
        }
        // [END_EXCLUDE]
    }
    // [END on_start_check_user]

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    /**
     * верифікація номеру
     *
     * @param phoneNumber висилає код смс по даному номеру
     */
    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
        showProgressDialog();
    }

    /**
     * получили код і мутим вход
     *
     * @param verificationId
     * @param code
     */
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    /**
     * метод сложний, він виконує таск для ношої глобальної змінної, ставить на нього
     * слушатель, який реагірує на облікових даних
     *
     * @param credential облікові дані
     */
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // [START_EXCLUDE]
                            updateUI(STATE_SIGNIN_SUCCESS, user);
                            Log.d("UI", "signin succes");
                            // [END_EXCLUDE]
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                mVerificationField.setError("Invalid code.");
                                // [END_EXCLUDE]
                            }
                            // [START_EXCLUDE silent]
                            // Update UI
                            updateUI(STATE_SIGNIN_FAILED);
                            Log.d("UI", "signin failed");
                            // [END_EXCLUDE]
                        }
                    }
                });
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    // [END sign_in_with_phone]

    /**
     * тут дуже тонко реалізована перегрузка даже самоперегрузка, від кількості вхідних даних
     * ми вирішуєм шо будемо робити далі
     *
     * @param uiState
     */
    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    /**
     * тут чекаємо чи юзер вже ввійшов у систему, єслі да то потім піде інтент
     * на головне актівіті мессенджера, інакще запускаємо все спочатку
     *
     * @param user нащ користувач
     */
    private void updateUI(FirebaseUser user) {

        if (user == null) {
            updateUI(STATE_INITIALIZED);
            Log.d("UI", "state init");
        } else {
            Intent i = new Intent(AuthActivity.this, MessengerActivity.class);
            startActivity(i);
            Log.d("UI", "signin succes");
        }


    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    /**
     * тіки при комбінації всіх 3 параметрів ми починаємо їхню перевірку
     *
     * @param uiState положення верифікації
     * @param user    поточний користувач
     * @param cred    облікові дані
     */
    private void updateUI(int uiState, final FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:
                // Initialized state, show only the phone number field and start button
                enableViews(confirmBtn, mPhoneNumberField);
                disableViews(verifyBtn, mVerificationField);
                break;
            case STATE_CODE_SENT:
                // Code sent state, show the verification field, the
                enableViews(verifyBtn, mVerificationField);
                disableViews(confirmBtn, mPhoneNumberField);
                mInfoTv.setText(R.string.status_code_sent);
                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options
                enableViews(confirmBtn, mPhoneNumberField);

                mInfoTv.setText(R.string.status_verification_failed);
                break;
            case STATE_VERIFY_SUCCESS:
                // Verification has succeeded, proceed to firebase sign in
                disableViews(confirmBtn, verifyBtn, mPhoneNumberField,
                        mVerificationField);
                mInfoTv.setText(R.string.status_verification_succeeded);


                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.getSmsCode() != null) {
                        mVerificationField.setText(cred.getSmsCode());
                    } else {
                        mVerificationField.setText(R.string.instant_validation);
                    }
                }


                break;
            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check
                mInfoTv.setText(R.string.status_sign_in_failed);
                break;
            case STATE_SIGNIN_SUCCESS:

                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse("images/+380982233199"))
                        .build();

                firebaseUser.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User profile updated. on AuthActivity");

                                }
                            }
                        });

                if (firebaseUser.getDisplayName() == null) {
                    Intent i = new Intent(AuthActivity.this, NameActivity.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(AuthActivity.this, MessengerActivity.class);
                    startActivity(i);
                }


                // Np-op, handled by sign-in check

                break;
        }

    }

    /**
     * чисто перевірка на пустоту
     *
     * @return тру єслі поле номеру не пусте
     */
    private boolean validatePhoneNumber() {
        String phoneNumber = mPhoneNumberField.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberField.setError("Invalid phone number.");
            return false;
        }

        return true;
    }


    /**
     * включає в'ювси
     *
     * @param views які нада включити
     */
    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    /**
     * виключає в'ювси
     *
     * @param views які нада виключити
     */
    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }

    /**
     * обичний онклік лістнер
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_btn:
                if (validatePhoneNumber()) {

                    startPhoneNumberVerification(mPhoneNumberField.getText().toString());

                }


                break;
            case R.id.verify_btn:
                String code = mVerificationField.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    mVerificationField.setError("Cannot be empty.");
                    return;
                }

                verifyPhoneNumberWithCode(mVerificationId, code);
                break;

            case R.id.resend_btn:
                resendVerificationCode(mPhoneNumberField.getText().toString(), mResendToken);
                break;
        }
    }


}
