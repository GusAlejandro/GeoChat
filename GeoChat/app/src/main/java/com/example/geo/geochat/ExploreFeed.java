package com.example.geo.geochat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ExploreFeed extends AppCompatActivity {

    private ListView mfeedListView;
    private PostAdapter mPostAdapter;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myIV = database.getReference().child("IV");
    DatabaseReference myUCSB = database.getReference().child("UCSB");
    private ChildEventListener mChildEventListener;
    private static String TAG = "LOG_CAT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_feed);
        mfeedListView = (ListView) findViewById(R.id.feedListView);
        List<PhotoPost> feedPosts = new ArrayList<>();
        mPostAdapter = new PostAdapter(this, R.layout.feed_post, feedPosts);
        mfeedListView.setAdapter(mPostAdapter);
        Bundle theBin = getIntent().getExtras();
        final String value = theBin.getString("theBin");


        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                PhotoPost photoPost =  dataSnapshot.getValue(PhotoPost.class);
                mPostAdapter.add(photoPost);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        if(value.equals("IV")){
            myUCSB.addChildEventListener(mChildEventListener);
        }else if(value.equals("UCSB")){
            myIV.addChildEventListener(mChildEventListener);

        }

    }
}
