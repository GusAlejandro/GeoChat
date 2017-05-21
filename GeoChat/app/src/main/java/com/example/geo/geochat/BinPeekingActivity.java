package com.example.geo.geochat;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;

public class BinPeekingActivity extends AppCompatActivity {

    private PostAdapter mPostAdapter;
    private ImageView exploreBin;
    private ListView mfeedListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bin_peeking);
        exploreBin = (ImageView) findViewById(R.id.locationImage);
        Bundle testM = getIntent().getExtras();
        final String value = testM.getString("currentLocation");
        if(testM != null){
            if(value.equals("UCSB")){
                exploreBin.setImageResource(R.drawable.islavista);
            }else if(value.equals("IV")){
                exploreBin.setImageResource(R.drawable.ucsb);
            }
        }
        exploreBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BinPeekingActivity.this,ExploreFeed.class);
                intent.putExtra("theBin",value);
                startActivity(intent);
            }
        });
    }


}
