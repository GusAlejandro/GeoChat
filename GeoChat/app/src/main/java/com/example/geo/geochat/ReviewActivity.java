package com.example.geo.geochat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ReviewActivity extends AppCompatActivity {

    private ImageView mImg;
    private Bitmap mBitmap;
    private Button mPostButton;
    private Button mRetakeButton;
    private String mLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_review);

        mImg = (ImageView) findViewById(R.id.capturedImg);

        Bundle values = getIntent().getExtras();
        mLocation = values.getString("location");

        byte[] bmpByteArray = values.getByteArray("image");
        mBitmap = BitmapFactory.decodeByteArray(bmpByteArray,0,bmpByteArray.length);

        mImg.setImageBitmap(mBitmap);

        // set the retake button to close action
        mRetakeButton = (Button) findViewById(R.id.retakeBtn);
        mRetakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // find out and write out the Firebase write button
        mPostButton = (Button) findViewById(R.id.postBtn);
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 // set up firebase below
            }
        });
    }

}
