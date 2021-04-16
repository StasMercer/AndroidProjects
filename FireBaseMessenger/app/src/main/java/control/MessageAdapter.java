package control;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.example.android.firebasemessenger.R;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import model.Message;


/**
 * Created by Mercer on 03.03.2018.
 */

public class MessageAdapter extends ArrayAdapter {
    private Context context;
    private String sideState;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private SharedPreferences preferences;

    public MessageAdapter(@NonNull Context context, ArrayList<Message> messages) {
        super(context, 0, messages);
        this.context = context;
        preferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);


    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //присваюємо наш елемент списку тому вью яке може ресайклитись
        View listItemView = convertView;
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        //создаємо вью із нашого леяута, і піхаємо його в свободну ячейку списка
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.adapter_message_item, parent, false);
        }
        //вибираємо то повідомлення яке закріплено за нащим вью
        final Message currentMessage = (Message) getItem(position);

        if (currentMessage.getSender().equals(firebaseUser.getDisplayName())) {
            sideState = context.getString(R.string.right);
        } else sideState = "left";


        if (sideState == "left") {


            LinearLayout linearLayout = (LinearLayout) listItemView.findViewById(R.id.mainMessageLinear);
            linearLayout.setGravity(Gravity.LEFT);

            //дальше ми встановлюємо текст і картинки у вже визначеного вью
            TextView senderTv = (TextView) listItemView.findViewById(R.id.senderTv);
            senderTv.setText(currentMessage.getSender());
            ImageView imgValue = (ImageView) listItemView.findViewById(R.id.image_value);
            TextView msgValue = (TextView) listItemView.findViewById(R.id.messageValue);

            if (currentMessage.getType().equals("image")) {
                //ImageView imgValue = (ImageView)listItemView.findViewById(R.id.image_value);
                imgValue.setVisibility(View.VISIBLE);
                msgValue.setVisibility(View.GONE);

                Glide
                        .with(imgValue.getContext())
                        .load(currentMessage.getValue())
                        .fitCenter()
                        .placeholder(R.drawable.ic_search_black_24dp)
                        .into(imgValue);
                listItemView.setTag(R.string.messageImage, "image");
                listItemView.setTag(R.string.image, currentMessage.getValue());

            } else {
                Glide.clear(imgValue);//
                imgValue.setVisibility(View.GONE);//
                //дальше ми встановлюємо текст і картинки у вже визначеного вью
                msgValue.setVisibility(View.VISIBLE);
                msgValue.setText(currentMessage.getValue());
                listItemView.setTag(R.string.messageImage, "text");
            }


            //дальше ми встановлюємо текст і картинки у вже визначеного вью
            TextView timestampTv = (TextView) listItemView.findViewById(R.id.timeValue);
            timestampTv.setText(currentMessage.getTimestamp().substring(5));

            ImageView imgViewRight = (ImageView) listItemView.findViewById(R.id.RightImageView);
            imgViewRight.setVisibility(View.INVISIBLE);

            //дальше ми встановлюємо текст і картинки у вже визначеного вью
            ImageView imgViewLeft = (ImageView) listItemView.findViewById(R.id.LeftImageView);
            imgViewLeft.setVisibility(View.VISIBLE);

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(currentMessage.getProfilePhoto());

            Glide.clear(imgViewLeft);
            Glide.with(imgViewLeft.getContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.ic_search_black_24dp)
                    .into(imgViewLeft);


        } else {


            LinearLayout linearLayout = (LinearLayout) listItemView.findViewById(R.id.mainMessageLinear);
            linearLayout.setGravity(Gravity.RIGHT);

            //дальше ми встановлюємо текст і картинки у вже визначеного вью
            TextView senderTv = (TextView) listItemView.findViewById(R.id.senderTv);
            senderTv.setText(R.string.Me);

            ImageView imgValue = (ImageView) listItemView.findViewById(R.id.image_value);
            TextView msgValue = (TextView) listItemView.findViewById(R.id.messageValue);
            if (currentMessage.getType().equals("image")) {

                msgValue.setVisibility(View.GONE);
                imgValue.setVisibility(View.VISIBLE);
                Glide
                        .with(imgValue.getContext())
                        .load(currentMessage.getValue())
                        .fitCenter()
                        .placeholder(R.drawable.ic_search_black_24dp)
                        .into(imgValue);
                listItemView.setTag(R.string.messageImage, "image");
                listItemView.setTag(R.string.image, currentMessage.getValue());

            } else {
                Glide.clear(imgValue);
                imgValue.setVisibility(View.GONE);
                //дальше ми встановлюємо текст і картинки у вже визначеного вью
                msgValue.setVisibility(View.VISIBLE);
                msgValue.setText(currentMessage.getValue());
                listItemView.setTag(R.string.messageImage, "text");
            }

            //дальше ми встановлюємо текст і картинки у вже визначеного вью
            TextView timestampTv = (TextView) listItemView.findViewById(R.id.timeValue);
            timestampTv.setText(currentMessage.getTimestamp().substring(5));

            ImageView imgViewLeft = (ImageView) listItemView.findViewById(R.id.LeftImageView);
            imgViewLeft.setVisibility(View.GONE);

            //дальше ми встановлюємо текст і картинки у вже визначеного вью
            final ImageView imgViewRight = (ImageView) listItemView.findViewById(R.id.RightImageView);
            imgViewRight.setVisibility(View.VISIBLE);


            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(currentMessage.getProfilePhoto());


            Glide.with(imgViewRight.getContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .signature(new StringSignature(preferences.getString("photoTime", "1")))
                    .into(imgViewRight);


        }


        return listItemView;
    }
}
