package control;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import model.ChatInUser;
import model.Message;
import model.User;

/**
 * Created by Mercer on 28.02.2018.
 * сложна штука (нет) цей клас нужен для внесення в базу значень переданих в параметри методів
 */

public class FirebaseWorker {
    private final String TAG = "tag";
    private String messageKey;
    private Map<String, Object> map;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference myRef;
    private Context context;
    public FirebaseWorker(){

    }
    public FirebaseWorker(Context context) {
        this.context = context;
    }


    /**
     * створюємо чат
     *
     * @param subjectPhone    телефон користувача з яким потрібно створити чат
     * @param subjectUsername імя користувача з яким потрібно створити чат
     * @param subjectPhoto    силка на фото для скачування користувача з яким потрібно створити чат
     */
    public String createChat(String subjectPhone, String subjectUsername, String subjectPhoto, String token) {
        Calendar c = Calendar.getInstance();

        String messageKey = FirebaseDatabase.getInstance().getReference("users").push().getKey();

        SimpleDateFormat df = new SimpleDateFormat("MM.dd HH:mm");

        String formattedDate = df.format(c.getTime());
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        /**
         * добавляємо в друзі вибраного із списку користувача
         */
        myRef = FirebaseDatabase
                .getInstance()
                .getReference("friends").child(firebaseUser.getPhoneNumber());
        /**
         * екземпляр який ми будемо передавати на сервер
         * тут імя, телефон, силка на фотку
         */
        User user = new User();

        user.setUsername(subjectUsername);
        user.setProfilePhoto(subjectPhoto);
        user.setPhoneNumber(subjectPhone);
        map = new HashMap<>();

        map.put(subjectPhone, user);
        /**
         * обновили дані
         */
        myRef.updateChildren(map);



        /**
         * створюємо чат у користувача із вибраним користувачем
         */
        myRef = FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(firebaseUser.getPhoneNumber())
                .child("chats");

        ChatInUser chat = new ChatInUser();
        chat.setPhoneNumber(subjectPhone);
        chat.setToken(token);
        chat.setProfilePhoto(subjectPhoto);
        chat.setUsername(subjectUsername);
        chat.setLastMessage("chat created");
        chat.setTimestamp(formattedDate);

        map.clear();

        map.put(messageKey, chat);

        myRef.updateChildren(map);

        // Створюємо чат у вибраного користувача з нашим користувачем
        myRef = FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(subjectPhone)
                .child("chats");

        chat.setPhoneNumber(user.getPhoneNumber());
        chat.setUsername(firebaseUser.getDisplayName());
        chat.setProfilePhoto("images/"+firebaseUser.getPhoneNumber());
        chat.setLastMessage("chat created");
        chat.setTimestamp(formattedDate);
        chat.setToken(FirebaseInstanceId.getInstance().getToken());
        map.clear();
        map.put(messageKey, chat);

        myRef.updateChildren(map);

        //тут создали повідомлення про то що чат створений
        myRef = FirebaseDatabase
                .getInstance()
                .getReference("messages")
                .child(firebaseUser.getPhoneNumber());


        Message message = new Message();


        Log.d("tag", "profile photo in chats = " + firebaseUser.getPhotoUrl().toString());
        message.setProfilePhoto(firebaseUser.getPhotoUrl().toString());
        message.setSender(firebaseUser.getDisplayName());
        message.setTimestamp(formattedDate);
        message.setType("text");
        message.setValue("chat have been created");

        map.clear();
        map.put(messageKey, message);

        myRef.updateChildren(map);

        return messageKey;
    }

    /**
     * створюємо повідомлення в базу даних
     * @param message екземпляр типу повідомлення
     * @param chatId ід чату
     */
    public void createMessage(Message message, String chatId) {
        // String timestamp = String.valueOf( new Date().getTime());
        messageKey = FirebaseDatabase.getInstance().getReference("messages").child(chatId).push().getKey();

        myRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId);

        map = new HashMap<>();
        map.put(messageKey,message);

        myRef.updateChildren(map);
    }

    public void updateLastMessages(String subjectPhone, String chatId, String value) {
        myRef = FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(mAuth.getCurrentUser().getPhoneNumber())
                .child("chats")
                .child(chatId)
                .child("lastMessage");

        myRef.setValue(value);


        myRef = FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(subjectPhone)
                .child("chats")
                .child(chatId)
                .child("lastMessage");

        myRef.setValue(value);
    }


}
