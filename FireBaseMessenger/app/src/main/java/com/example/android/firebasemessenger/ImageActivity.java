package com.example.android.firebasemessenger;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageActivity extends AppCompatActivity {
    String photoUrl;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_layout);

        photoUrl = getIntent().getStringExtra("photoLink");
        imageView = (ImageView) findViewById(R.id.img);
        Glide
                .with(imageView.getContext())
                .load(photoUrl)
                .into(imageView);

    }

}
