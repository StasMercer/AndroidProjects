package control;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.example.android.firebasemessenger.R;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import model.ChatInUser;
import model.UserBlockContent;

/**
 * Created by Mercer on 27.02.2018.
 * кастомний адаптер для блока в якому буде показано
 * фотографію користувача,
 * його імя,
 * телефон або останне повідомлення в залежності від creatingState
 */

public class UserBlockAdapter extends ArrayAdapter {
    private String creatingState;
    private Context mContext;

    public UserBlockAdapter(@NonNull Context context, ArrayList<?> userBlockContents, String creatingState) {
        super(context, 0, userBlockContents);
        this.creatingState = creatingState;
        mContext = context;
    }


    /**
     * шоб поняти як работає цей метод треба розбиратися із адаптером,
     * я цього робити не буду тому напишу що приблизно він робить
     *
     * @param position    каждий елемент вью має свою позицію в списку, це вона і є
     * @param convertView отвічає за то шоб іспользувати ті ячейки ліст вью, які юзер пролистав
     * @param parent
     * @return повертає вьюгруп яке буде елементом списку
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        //присваюємо наш елемент списку тому вью яке може ресайклитись
        View listItemView = convertView;

        //создаємо вью із нашого леяута, і піхаємо його в свободну ячейку списка
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.adapter_userblock_item, parent, false);
        }

        if (creatingState == "createDialogs") {


            //вибираємо то повідомлення яке закріплено за нащим вью
            UserBlockContent currentMessage = (UserBlockContent) getItem(position);


            //дальше ми встановлюємо текст і картинки у вже визначеного вью
            TextView userTv = (TextView) listItemView.findViewById(R.id.top_tv);
            userTv.setText(currentMessage.getUsername());

            //дальше ми встановлюємо текст і картинки у вже визначеного вью
            TextView phoneNumber = (TextView) listItemView.findViewById(R.id.bottom_tv);
            phoneNumber.setText(currentMessage.getPhoneNumber());

            //дальше ми встановлюємо текст і картинки у вже визначеного вью
            ImageView imgView = (ImageView) listItemView.findViewById(R.id.imgIcon);


            //Glide.clear(imgView);


            StorageReference downloadRef = FirebaseStorage
                    .getInstance()
                    .getReference()
                    .child(currentMessage.getProfilePhoto());

            Glide.with(imgView.getContext())
                    .using(new FirebaseImageLoader())
                    .load(downloadRef)
                    .centerCrop()
                    .placeholder(R.drawable.ic_account_box_18pt_2x)
                    .into(imgView);

            listItemView.setTag(R.string.token, currentMessage.getToken());
            listItemView.setTag(R.string.profile_photo, currentMessage.getProfilePhoto());
            listItemView.setTag(R.string.username, currentMessage.getUsername());
            listItemView.setTag(R.string.phoneNumber, currentMessage.getPhoneNumber());

        } else if (creatingState == "displayChats") {
            //вибираємо то повідомлення яке закріплено за нащим вью

            ChatInUser currentMessage = (ChatInUser) getItem(position);


            //дальше ми встановлюємо текст і картинки у вже визначеного вью
            TextView userTv = (TextView) listItemView.findViewById(R.id.top_tv);
            userTv.setText(currentMessage.getUsername());

            //дальше ми встановлюємо текст і картинки у вже визначеного вью
            TextView lastMessage = (TextView) listItemView.findViewById(R.id.bottom_tv);
            lastMessage.setText(currentMessage.getLastMessage());

            //дальше ми встановлюємо текст і картинки у вже визначеного вью
            final ImageView imgView = (ImageView) listItemView.findViewById(R.id.imgIcon);


            StorageReference downloadRef = FirebaseStorage
                    .getInstance()
                    .getReference()
                    .child(currentMessage.getProfilePhoto());

            Glide.with(imgView.getContext())
                    .using(new FirebaseImageLoader())
                    .load(downloadRef)
                    .placeholder(R.drawable.ic_account_box_18pt_2x)
                    .centerCrop()
                    .into(imgView);

            Log.d("tag", "display chats ");
            listItemView.setTag(R.string.token, currentMessage.getToken());
            listItemView.setTag(R.string.chat_id,currentMessage.getChatId());
            listItemView.setTag(R.string.profile_photo, currentMessage.getProfilePhoto());
            listItemView.setTag(R.string.username, currentMessage.getUsername());
            listItemView.setTag(R.string.phoneNumber, currentMessage.getPhoneNumber());
        }


        return listItemView;
    }


}
