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
import android.widget.TextView;
import android.content.SharedPreferences;
import com.example.geo.geochat.Point;
import com.example.geo.geochat.BinManager;

public class MainActivity extends AppCompatActivity {

//    TODO: fix the navigation labels
    private TextView mTextMessage;
    private TextView locationData;
    private LocationManager mlocationManager;
    private LocationListener mlocationListener;
    private static String TAG = "LOG_CAT";
    static final String SAVED_LOCATION = "MyLocation";


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
        mlocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mTextMessage = (TextView) findViewById(R.id.message);
        locationData = (TextView) findViewById(R.id.location);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_album);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        final SharedPreferences myPreferences = getSharedPreferences(SAVED_LOCATION, MODE_PRIVATE);
        String saved_location = myPreferences.getString("savedLocation","NULL");
        if (!saved_location.equals("NULL")){
            locationData.setText(saved_location);
        }
        mlocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(getBin(location).equals("UCSB")){
                    locationData.setText("UCSB");
                }else if(getBin(location).equals("IV")){
                    locationData.setText("IV");
                }else{
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
    }

}

