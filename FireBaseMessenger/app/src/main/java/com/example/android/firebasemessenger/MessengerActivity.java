package com.example.android.firebasemessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;
import com.mindorks.placeholderview.PlaceHolderView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import control.UserBlockAdapter;
import model.ChatInUser;


/**
 * Created by Mercer on 18.02.2018.
 */

public class MessengerActivity extends BaseActivity implements View.OnClickListener, DrawerMenuItem.DrawerCallBack {

    final String TAG = "tag";
    private ActionBarDrawerToggle  drawerToggle;
    int drawerOpenState = 0;
    private ArrayList<ChatInUser> chatInUsers;
    private PlaceHolderView mDrawerView;
    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    private ListView chatsLv;
    private FloatingActionButton addFriendsBtn;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messenger_layout);

        chatsLv = (ListView) findViewById(R.id.chatsLv);

        addFriendsBtn = (FloatingActionButton) findViewById(R.id.addFriendsBtn);
        addFriendsBtn.setOnClickListener(this);

        mDrawer = (DrawerLayout)findViewById(R.id.drawerLayout);
        mDrawerView = (PlaceHolderView)findViewById(R.id.drawerView);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);


        Log.d(TAG, "onCreateMessengerActivity: messnger" + user.toString());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(user.getPhoneNumber());

        // Write a message to the database
        myRef.child("phoneNumber").setValue(user.getPhoneNumber());
        Log.d(TAG, "onCreateMessengerActivity: " + user.getPhoneNumber());
        myRef.child("profilePhoto").setValue("images/" + user.getPhoneNumber());
        Log.d(TAG, "onCreateMessengerActivity: " + user.getPhotoUrl());

        myRef.child("username").setValue(user.getDisplayName());
        Log.d(TAG, "onCreateMessengerActivity: " + user.getDisplayName());

        MyFirebaseInstanceIdService idService = new MyFirebaseInstanceIdService();
        idService.onTokenRefresh();
        Log.d(TAG, "onCreateMessengerActivity: tokenMsg" + FirebaseInstanceId.getInstance().getToken());

        FirebaseMessaging.getInstance().subscribeToTopic("test");
        displayChats();

        mDrawer.setStatusBarBackground(R.color.colorPrimary);
        setupDrawer();

    }

    /**
     * метод встановлює бокове меню в мессенджер
     */
    private void setupDrawer(){



        mDrawerView
                .addView(new DrawerHeader(this, user.getDisplayName(), user.getPhoneNumber(), user.getPhotoUrl().toString()))
                .addView(new DrawerMenuItem(this.getApplicationContext(), DrawerMenuItem.DRAWER_MENU_ITEM_PROFILE,this ))

                //might be add in future
                /*.addView(new DrawerMenuItem(this.getApplicationContext(), DrawerMenuItem.DRAWER_MENU_ITEM_MESSAGE,this))
                .addView(new DrawerMenuItem(this.getApplicationContext(), DrawerMenuItem.DRAWER_MENU_ITEM_NOTIFICATIONS,this))

                .addView(new DrawerMenuItem(this.getApplicationContext(), DrawerMenuItem.DRAWER_MENU_ITEM_SETTINGS,this))*/
                .addView(new DrawerMenuItem(this.getApplicationContext(), DrawerMenuItem.DRAWER_MENU_ITEM_LOGOUT,this));

        drawerToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.open_drawer, R.string.close_drawer){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerOpenState = 1;

            }


            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                drawerOpenState = 0;
            }
        };


        drawerToggle.syncState();

    }

    /**
     * показуємо чати в користувача
     */
    private void displayChats() {
        FirebaseUser user = mAuth.getCurrentUser();
        try {
            DatabaseReference myRef = FirebaseDatabase
                    .getInstance()
                    .getReference("users")
                    .child(user.getPhoneNumber())
                    .child("chats");

            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    chatInUsers = collectChats((HashMap<String, HashMap<String, String>>) dataSnapshot.getValue());
                    updateUi(chatInUsers);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } catch (NullPointerException e) {

        }

    }

    /**
     * збираємо чати і піхаємо значення в список
     *
     * @param chatMap номер телефону, ( імя, фотка, останне повідомлення)
     * @return список чатів
     */
    private ArrayList<ChatInUser> collectChats(HashMap<String, HashMap<String, String>> chatMap) {
        ArrayList<ChatInUser> chats = null;
        try {


            chats = new ArrayList<>();

            //iterate through each user, ignoring their UID
            for (Map.Entry entry : chatMap.entrySet()) {

                Map<String, String> singleChat = (Map) entry.getValue();

                chats.add(new ChatInUser(
                        singleChat.get("phoneNumber"),
                        entry.getKey().toString(),
                        singleChat.get("username"),
                        singleChat.get("profilePhoto"),
                        singleChat.get("lastMessage"),
                        singleChat.get("token")));

            }

            return chats;

        } catch (NullPointerException e) {

        }
        return chats;
    }

    public void updateUi(ArrayList<ChatInUser> chatInUsersList) {


        UserBlockAdapter adapter = new UserBlockAdapter(this, chatInUsersList, "displayChats");

        chatsLv.setAdapter(adapter);

        chatsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {


                Intent intent = new Intent(MessengerActivity.this,ChatActivity.class);
                intent.putExtra("chatId",view.getTag(R.string.chat_id).toString());
                intent.putExtra("token", view.getTag(R.string.token).toString());
                intent.putExtra("phoneNumber", view.getTag(R.string.phoneNumber).toString());
                intent.putExtra("profilePhoto", view.getTag(R.string.profile_photo).toString());
                startActivity(intent);
                finish();

            }
        });
    }

    /**
     * згортаємо бокове меню або сам месенджер
     */
    @Override
    public void onBackPressed() {
        if(drawerOpenState == 1){
            mDrawer.closeDrawers();
        }else{
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }


    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case (R.id.addFriendsBtn):{
                Intent intent = new Intent(this, CreateDialogActivity.class);
                startActivity(intent);
            }
        }
    }


    @Override
    public void onProfileMenuSelected() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    //might add in future
    /*@Override
    public void onMessagesMenuSelected() {

    }

    @Override
    public void onNotificationsMenuSelected() {

    }

    @Override
    public void onSettingsMenuSelected() {

    }
*/
    @Override
    public void onLogoutMenuSelected() {

        mAuth.signOut();
        if (mAuth.getCurrentUser() != null) {
            Log.d("blet","suk");
        }else{

            Intent intent = new Intent (this,AuthActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onStart() {


        super.onStart();
    }
}
