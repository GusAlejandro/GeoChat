package com.example.geo.geochat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ExploreFeed extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_feed);
        Bundle theBin = getIntent().getExtras();
        final String value = theBin.getString("theBin");

    }
}
