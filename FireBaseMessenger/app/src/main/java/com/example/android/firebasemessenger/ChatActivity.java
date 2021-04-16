package com.example.android.firebasemessenger;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import control.FirebaseWorker;
import control.MessageAdapter;
import control.QueryMaker;
import model.Message;
import model.ResponseFromServer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Mercer on 03.03.2018.
 */

public class ChatActivity extends BaseActivity {
    private final int IMAGE_PICK_OK = 2;
    private Message message = new Message();
    private EditText messageValue;
    private ImageButton sendMessage, sendImage;
    private ListView listView;
    private String senderPhone, sender, value, timestamp, profilePhoto, type, token, imageUrl;
    private int senderOnlineState = 0;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = mAuth.getCurrentUser();
    private String chatId;
    private ArrayList<Message> messageArrayList = new ArrayList<>();
    private DatabaseReference myRef = FirebaseDatabase
            .getInstance()
            .getReference("users")
            .child(mAuth.getCurrentUser().getPhoneNumber())
            .child("isOnline");

    @Override
    public void onStop() {
        super.onStop();
        myRef.setValue(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        myRef.setValue(1);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);


        messageValue = (EditText) findViewById(R.id.messageValueEt);
        sendMessage = (ImageButton) findViewById(R.id.sendMessageBtn);
        sendImage = (ImageButton) findViewById(R.id.sendImageBtn);
        final FirebaseUser user = mAuth.getCurrentUser();

        listView = (ListView) findViewById(R.id.chatMainLv);
        chatId = getIntent().getStringExtra("chatId");
        token = getIntent().getStringExtra("token");
        senderPhone = getIntent().getStringExtra("phoneNumber");

//        показуємо повідомлення з бази даних
        displayMessages();
        Calendar c = Calendar.getInstance();

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(senderPhone).child("isOnline");
        Log.d(TAG, "SenderPhone  ="+senderPhone);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                senderOnlineState = Integer.parseInt(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        SimpleDateFormat df = new SimpleDateFormat("MM.dd HH:mm");

        final String formattedDate = df.format(c.getTime());

        // обробка на натискання кнопки надіслати
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // беремо текст з едіта
                String value = messageValue.getText().toString();
                // создаємо екземпляр повідомлення яке буде додане в базу даних

                message.setValue(value);
                message.setType("text");
                message.setSender(firebaseUser.getDisplayName());
                message.setProfilePhoto("images/" + user.getPhoneNumber());
                message.setTimestamp(formattedDate);

                // заповнили обєкт і створили екземпляр для роботи з базою даних
                FirebaseWorker firebaseWorker = new FirebaseWorker();

                // перевірка едіта на пустоту і заповнення бази даних
                if (!value.equals("")) {
                    firebaseWorker.createMessage(message, chatId);

                    firebaseWorker.updateLastMessages(senderPhone, chatId, value);
                    Log.d(TAG, "senderOnlineState: "+senderOnlineState+user.getPhoneNumber());
                    if (senderOnlineState == 0) {
                        //создаємо обєкт який реалізує інтерфейс запросів, вибрали шо будемо робити логін
                        QueryMaker queryMaker = new QueryMaker();

                        Call<ResponseFromServer> pushNotificationsProcess = queryMaker
                                .getApiInterface()
                                .pushNotifications(token, user.getDisplayName(), "images/" + user.getPhoneNumber(), value);
                        Log.d(TAG, "notification add: "+token+"\n"
                                + user.getDisplayName()+"\n"+
                                "photo = images/"+user.getPhoneNumber()+"\n"
                                +value
                        );
                        //ассинхронний запрос на notifications
                        pushNotificationsProcess.enqueue(new Callback<ResponseFromServer>() {
                            @Override
                            public void onResponse(Call<ResponseFromServer> call, Response<ResponseFromServer> response) {

                                Log.d(TAG, "Notify" + token + " "+response.body().getResponse().toString());
                            }

                            //Код єслі неправильний ответ сервера, або вообще не пройшов запрос
                            @Override
                            public void onFailure(Call<ResponseFromServer> call, Throwable t) {
                                Log.d(TAG, "onFailure: pushNotifications");
                            }
                        });

                    }

                    messageValue.setText("");
                }


            }
        });


        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, IMAGE_PICK_OK);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IMAGE_PICK_OK: {
                    showProgressDialog();
                    String key = FirebaseDatabase.getInstance().getReference().push().getKey();
                    StorageReference uploadRef = FirebaseStorage.getInstance().getReference().child(chatId + "/" + key);
                    Uri file = data.getData();

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
                            Calendar c = Calendar.getInstance();
                            SimpleDateFormat df = new SimpleDateFormat("MM.dd HH:mm");

                            final String formattedDate = df.format(c.getTime());

                            message.setValue(taskSnapshot.getDownloadUrl().toString());
                            message.setType("image");
                            message.setSender(firebaseUser.getDisplayName());
                            message.setProfilePhoto("images/" + firebaseUser.getPhoneNumber());
                            message.setTimestamp(formattedDate);

                            // заповнили обєкт і створили екземпляр для роботи з базою даних
                            FirebaseWorker firebaseWorker = new FirebaseWorker();
                            firebaseWorker.createMessage(message, chatId);
                            hideProgressDialog();

                            if (senderOnlineState == 0) {

                                QueryMaker queryMaker = new QueryMaker();

                                Call<ResponseFromServer> pushNotificationsProcess = queryMaker
                                        .getApiInterface()
                                        .pushNotifications(token,
                                                mAuth.getCurrentUser().getDisplayName(),
                                                "images/" + mAuth.getCurrentUser().getPhoneNumber(),
                                                "image");

                                //ассинхронний запрос на notifications
                                pushNotificationsProcess.enqueue(new Callback<ResponseFromServer>() {
                                    @Override
                                    public void onResponse(Call<ResponseFromServer> call, Response<ResponseFromServer> response) {
                                        Log.d(TAG, "Notify" + token + " ");
                                    }

                                    //Код єслі неправильний ответ сервера, або вообще не пройшов запрос
                                    @Override
                                    public void onFailure(Call<ResponseFromServer> call, Throwable t) {
                                        Log.d(TAG, "onFailure: pushNotifications");
                                    }
                                });

                            }

                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            taskSnapshot.getTotalByteCount();

                            Log.d(TAG, "onProgress: " + String.valueOf(taskSnapshot.getBytesTransferred()));
                        }
                    });
                }
            }
        }
    }

    /**
     * метод відображає повідомлення з бази даних
     */
    private void displayMessages() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                messageArrayList = new ArrayList<>();

                for (DataSnapshot locSnapshot : dataSnapshot.getChildren()) {


                    sender = locSnapshot.getValue(Message.class).getSender();
                    value = locSnapshot.getValue(Message.class).getValue();
                    profilePhoto = locSnapshot.getValue(Message.class).getProfilePhoto();
                    type = locSnapshot.getValue(Message.class).getType();
                    timestamp = locSnapshot.getValue(Message.class).getTimestamp();
                    messageArrayList.add(new Message(value, type, timestamp, profilePhoto, sender, chatId));
                }

                /**
                 * обновляємо інтерфейс з отриманими з бази повідомленнями
                 */
                updateUi(messageArrayList);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * обновляємо інтерфейс
     *
     * @param messagesList отримуєм в якості повідомлень для відображення
     */
    private void updateUi(ArrayList<Message> messagesList) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        FirebaseCrash.log("updated UI");
        /**
         * створюємо екземпляр власного адаптера
         */
        final MessageAdapter messageAdapter = new MessageAdapter(ChatActivity.this, messagesList);

        /**
         * присвоюємо нашому ліст вью адаптер для повідомлень
         */
        listView.setAdapter(messageAdapter);



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if (view.getTag(R.string.messageImage).equals("image")){
                    Intent intent = new Intent(ChatActivity.this,ImageActivity.class);
                    intent.putExtra("photoLink", (String) view.getTag(R.string.image));
                    startActivity(intent);
                }


            }
        });



    }


}


