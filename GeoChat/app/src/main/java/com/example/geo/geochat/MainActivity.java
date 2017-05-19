package com.example.geo.geochat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.content.SharedPreferences;
import com.example.geo.geochat.Point;
import com.example.geo.geochat.BinManager;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

//    TODO: fix the navigation labels
    private TextView mTextMessage;
    private TextView locationData;
    private ListView mfeedListView;
    private PostAdapter mPostAdapter;
    private LocationManager mlocationManager;
    private LocationListener mlocationListener;
    private static String TAG = "LOG_CAT";
    static final String SAVED_LOCATION = "MyLocation";
    private int taco = 0;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRefIV;
    DatabaseReference myRefUCSB = database.getReference().child("ucsb");
    private ChildEventListener mChildEventListener;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_cam:
                    mTextMessage.setText(R.string.title_camera);
                    return true;
                case R.id.navigation_album:
                    mTextMessage.setText(R.string.title_album);
                    return true;
                case R.id.navigation_uploads:
                    mTextMessage.setText(R.string.title_uploads);
                    return true;
            }
            return false;
        }

    };

    public String getBin(Location location){
        // Takes in location object and returns appropriate string that represents the bin it belongs to
        Point currentSpot = new Point(location.getLatitude(),location.getLongitude());
        // check if UCSB
        if (BinManager.isInBin(BinManager.mUCSB,currentSpot)){
            return "UCSB";
        }else if (BinManager.isInBin(BinManager.mIV,currentSpot)){
            return "IV";
        }else{
            return "nope";
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        myRefIV = database.getReference().child("iv");
        mlocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mTextMessage = (TextView) findViewById(R.id.message);
        locationData = (TextView) findViewById(R.id.location);
        mfeedListView = (ListView) findViewById(R.id.photoListView);
        List<PhotoPost> feedPosts = new ArrayList<>();
        mPostAdapter = new PostAdapter(this, R.layout.feed_post, feedPosts);
        mfeedListView.setAdapter(mPostAdapter);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_album);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        final SharedPreferences myPreferences = getSharedPreferences(SAVED_LOCATION, MODE_PRIVATE);
        final String saved_location = myPreferences.getString("savedLocation","NULL");
        mChildEventListener = new ChildEventListener() {
            //TODO: At the moment we get it from oldest to newest, coudl add date field to order by date newwest to oldest
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG,"child was added");
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
        if (!saved_location.equals("NULL")){
            locationData.setText(saved_location);
            // reopening the app, location is stored, but may be wrong, either way create listener
            if(saved_location.equals("UCSB")){
                myRefUCSB.addChildEventListener(mChildEventListener);
                taco = 1;
            }else if(saved_location.equals("IV")){
                Log.i(TAG,"saved location has added a listner");
                myRefIV.addChildEventListener(mChildEventListener);
                taco = 1;
            }
        }
        mlocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(getBin(location).equals("UCSB")){
                    if(locationData.getText() != "UCSB"){
                        if(locationData.getText() == "NO LOCATION FOUND"){
                            //now ucsb, used to be no location found
                            myRefUCSB.addChildEventListener(mChildEventListener);
                        }else if(locationData.getText() == "IV"){
                            //used to be IV, now ucsb
                            myRefIV.removeEventListener(mChildEventListener);
                            myRefUCSB.addChildEventListener(mChildEventListener);
                        }else{
                            // used to be empty, now UCSB
                            if(taco == 0){
                                myRefUCSB.addChildEventListener(mChildEventListener);
                            }
                        }
                    }
                    locationData.setText("UCSB");
                }else if(getBin(location).equals("IV")){
                    if(locationData.getText() != "IV"){
                        if(locationData.getText() == "NO LOCATION FOUND"){
                            // now ucsb, used to be no location found
                            Log.i(TAG,"addeddd HEREEE");
                            myRefIV.addChildEventListener(mChildEventListener);
                        }else if(locationData.getText() == "UCSB"){
                            //used to be ucsb, now IV
                            myRefUCSB.removeEventListener(mChildEventListener);
                            Log.i(TAG,"addeddd OVAAAHHHHHEREEE");
                            myRefIV.addChildEventListener(mChildEventListener);
                        }else{
                            //used to be empty, now IV
                            Log.i(TAG,"addeddd ANOTHAAA OVAAAHHHHHEREEE");
                            if(taco ==0){
                                myRefIV.addChildEventListener(mChildEventListener);
                            }

                        }
                    }
                    locationData.setText("IV");
                }else{
                    // check if it was IV or UCSB before, then remove their listener
                    if(locationData.getText() == "UCSB"){
                        myRefUCSB.removeEventListener(mChildEventListener);
                    }else if(locationData.getText() == "IV"){
                        myRefIV.removeEventListener(mChildEventListener);
                    }
                    locationData.setText("NO LOCATION FOUND");
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.i(TAG,"goo");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
        try {
            mlocationManager.requestLocationUpdates("network",5000,0,mlocationListener);
            Log.i(TAG,"location has been requested");
        }catch (SecurityException e){
            Log.i(TAG,"exception caught");
        }
        configureButton();


//        mChildEventListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Log.i(TAG,"child was added");
//                PhotoPost photoPost =  dataSnapshot.getValue(PhotoPost.class);
//                mPostAdapter.add(photoPost);
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
        //myRefIV.addChildEventListener(mChildEventListener);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode){
            case 10:
                configureButton();
                break;
            default:
                break;
        }
    }

    void configureButton(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET},10);
            }
            return;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        final SharedPreferences myPreferences = getSharedPreferences(SAVED_LOCATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = myPreferences.edit();
        editor.putString("savedLocation",locationData.getText().toString());
        editor.apply();
//        if(locationData.getText() == "UCSB"){
//            myRefUCSB.removeEventListener(mChildEventListener);
//        }else if(locationData.getText() == "IV"){
//            myRefIV.removeEventListener(mChildEventListener);
//        }
    }

}

