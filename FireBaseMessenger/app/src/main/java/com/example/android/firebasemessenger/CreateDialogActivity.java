package com.example.android.firebasemessenger;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import control.FirebaseWorker;
import control.UserBlockAdapter;
import model.UserBlockContent;


/**
 * в цьому актівіті виконується пошук користувачів,
 * їхне відображення на екрані та створення діалогу з ними
 */
public class CreateDialogActivity extends AppCompatActivity implements MenuItem.OnActionExpandListener {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference myRef;
    private ArrayList<UserBlockContent> users;
    private ListView usersLv;
    private Toolbar mToolbar;
    private SearchView searchView;
    private FirebaseWorker firebaseWorker;
    private String profilePhoto;
    private String username;
    private String phoneNumber;
    private String token;

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_dialog_layout);

        usersLv = (ListView) findViewById(R.id.usersLv);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_dialog);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        firebaseWorker = new FirebaseWorker(this);

        mToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));


        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        usersLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FirebaseUser user = mAuth.getCurrentUser();
                profilePhoto = (String) view.getTag(R.string.profile_photo);
                username = (String) view.getTag(R.string.username);
                phoneNumber = (String) view.getTag(R.string.phoneNumber);
                token = (String) view.getTag(R.string.token);
                myRef = FirebaseDatabase.getInstance()
                        .getReference("friends")
                        .child(user.getPhoneNumber())
                        .child(phoneNumber).child("phoneNumber");


                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("tag", dataSnapshot.getValue() + " value");

                        if (dataSnapshot.getValue() == null) {
                            //створюємо чат по кліку на елемент списка, тобто обраного користувача

                            final String chatId = firebaseWorker.createChat(phoneNumber, username, profilePhoto, token);





                            Toast.makeText(CreateDialogActivity.this.getBaseContext(), "chat created", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CreateDialogActivity.this.getBaseContext(), MessengerActivity.class);
                            startActivity(intent);
                            finish();

                        } else {


                            Toast.makeText(CreateDialogActivity.this.getBaseContext(), "You alredy have chat", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CreateDialogActivity.this.getBaseContext(), MessengerActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("tag", databaseError.getMessage());
                    }
                });


            }
        });
    }

    /**
     * метод відображає текстове поле пошуку в actionBar
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);


        final MenuItem myActionMenuItem = menu.findItem(R.id.app_bar_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
       searchView.setOnSearchClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               findViewById(R.id.text_info_search_hint).setVisibility(View.INVISIBLE);
           }
       });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                return false;
            }

            /**
             * тут дивимося на введений текст в полі та виконуємо пошук
             *
             * @param s буковка на яку змінився текст
             * @return
             */
            @Override
            public boolean onQueryTextChange(String s) {
                String query = "";
                query += s;
                if (query.length() > 3) {
                    searchOnQuery(query);
                }

                return false;
            }
        });

        return true;
    }

    /**
     * @param query строка з текстом для пошуку
     */
    public void searchOnQuery(final String query) {


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("tag", "start search  for" + query);

                        //отримуємо список користувачів типу User( package model)
                        users = collectPhoneNumbers((HashMap<String, HashMap<String, String>>) dataSnapshot.getValue(), query);

                        // оновлюємо інтерфейс користувача з отриманим списком
                        updateUi(users);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                        Log.d("tag", databaseError.getMessage());
                    }
                });
    }

    /**
     * @param usersList
     */
    public void updateUi(ArrayList<UserBlockContent> usersList) {

        UserBlockAdapter adapter = new UserBlockAdapter(this, usersList, "createDialogs");

        usersLv.setAdapter(adapter);


    }

    /**
     * метод отримує дані користувача по запиту
     *
     * @param users користувач
     * @param query запит
     * @return
     */
    private ArrayList<UserBlockContent> collectPhoneNumbers(HashMap<String, HashMap<String, String>> users, String query) {


        ArrayList<UserBlockContent> names = new ArrayList<>();

        //iterate through each user, ignoring their UID
        for (Map.Entry entry : users.entrySet()) {

            Map<String, String> singleUser = (Map) entry.getValue();
            String neededPhoneNumber = singleUser.get("phoneNumber");
            if (neededPhoneNumber.contains(query)) {

                names.add(new UserBlockContent(
                        singleUser.get("profilePhoto"),
                        singleUser.get("phoneNumber"),
                        singleUser.get("username"),
                        singleUser.get("messageToken")));
            }

        }

        return names;


    }


    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return false;
    }
}
