package com.example.geo.geochat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.geo.geochat.camera.CameraSourcePreview;
import com.example.geo.geochat.camera.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CameraCapture extends AppCompatActivity {

    private static final String TAG = "FaceTracker";
    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private Bitmap mCapturedImage;
    private Button mReviewButton;
    private ImageView mImageView;
    private LinearLayout mLinearLayout;
    private Button mPostButton;
    private Button mRetakeButton;
    private String mLocation;
    private Button mCameraSwapButton;

    private DatabaseReference mPhotosReference;

    private FirebaseStorage mPhotoStorage;
    private StorageReference mStorageReference;
    private Uri mDownloadUrl;


    public static Bitmap rotate(Bitmap source){
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String strManufacturer = android.os.Build.MANUFACTURER;
        setContentView(R.layout.activity_camera_capture);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        mReviewButton = (Button) findViewById(R.id.captureButton);
        mPostButton = (Button) findViewById(R.id.postBtn);
        mRetakeButton = (Button) findViewById(R.id.retakeBtn);
        mLinearLayout = (LinearLayout) findViewById(R.id.postRetakeLayout);
        mLinearLayout.setVisibility(View.INVISIBLE);
        mCameraSwapButton = (Button) findViewById(R.id.swapButton);

        if(haveFrontCamera()){
            mCameraSwapButton.setVisibility(View.VISIBLE);
            mCameraSwapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int facing = mCameraSource.getCameraFacing();
                    switch(facing){
                        case CameraSource.CAMERA_FACING_BACK:
                            mCameraSource.stop();
                            mCameraSource.release();
                            createCameraSource(CameraSource.CAMERA_FACING_FRONT);
                            try{
                                Log.i("LOG_CAT","source start");
                                mCameraSource.start();
                                Log.i("LOG_CAT","source end");
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            break;
                        case CameraSource.CAMERA_FACING_FRONT:
                            mCameraSource.stop();
                            mCameraSource.release();
                            createCameraSource(CameraSource.CAMERA_FACING_BACK);
                            try{
                                Log.i("LOG_CAT","source start");
                                mCameraSource.start();
                                Log.i("LOG_CAT","source end");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            break;
                    }
                }
            });
        } else {
            mCameraSwapButton.setVisibility(View.GONE);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //grab currentLocation to ship to the next upload activity
        Bundle testM = getIntent().getExtras();
        mLocation = testM.getString("currentLocation");
        mPhotosReference = FirebaseDatabase.getInstance().getReference().child(mLocation);

        mPhotoStorage = FirebaseStorage.getInstance();
        mStorageReference = mPhotoStorage.getReference();

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int permissionGranted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        if (permissionGranted == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(CameraSource.CAMERA_FACING_BACK);
        } else {
            requestCameraPermission();
        }

        //set on click listener after you have a camera up for capture
        mReviewButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view){
                if(mCameraSource != null){
                    mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data) {
                            //capture image doesnt' work below, just take the data and start somethign, because we dont' need it otherwise
                            mCapturedImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                            if(strManufacturer.equals("samsung")){
                                mCapturedImage = rotate(mCapturedImage);
                            }
                            mImageView.setImageBitmap(mCapturedImage);
                            mPreview.addView(mImageView);
                            mPreview.bringChildToFront(mImageView);
                            mReviewButton.setVisibility(View.GONE);
                            mCameraSwapButton.setVisibility(View.GONE);
                            mLinearLayout.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        mPostButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add firebase stuff
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mCapturedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                String uniqueID = UUID.randomUUID().toString();
                StorageReference childRef = mStorageReference.child(uniqueID+".jpg");
                UploadTask uploadTask = childRef.putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        mDownloadUrl = taskSnapshot.getDownloadUrl();
                        Log.i("LOG_CAT","before image into database");
                        Long tsLong = System.currentTimeMillis()/1000;
                        //tsLong = tsLong * -1;
                        PhotoPost thePost = new PhotoPost(mDownloadUrl.toString(),tsLong,"lol");
                        mPhotosReference.push().setValue(thePost);
                        //mPhotosReference.child("photoURL").setValue(mDownloadUrl.toString());
                        //mPhotosReference.child("time").setValue(tsLong);


                        Log.i("LOG_CAT","uploaded image into database");
                    }
                });

                finish();
            }
        });

        mRetakeButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPreview.removeView(mImageView);
                mLinearLayout.setVisibility(View.INVISIBLE);
                mReviewButton.setVisibility(View.VISIBLE);
                mCameraSwapButton.setVisibility(View.VISIBLE);
            }
        });
        mImageView = new ImageView(getApplicationContext());
        mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{android.Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    private boolean haveFrontCamera() {
        try {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == CameraMetadata.LENS_FACING_FRONT) {
                    return true;
                }
            }
            return false;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource(int facing) {

        // TODO: Create a face detector for real time face detection
        // 1. Get the application's context
        Context context = getApplicationContext();

        // 2. Create a FaceDetector object for real time detection
        //    Ref: https://developers.google.com/vision/android/face-tracker-tutorial
        FaceDetector detector = new FaceDetector.Builder(context)
                .setMode(FaceDetector.FAST_MODE)
                .build();

        // 4. Create a GraphicFaceTrackerFactory
        GraphicFaceTrackerFactory graphicFaceTrackerFactory = new GraphicFaceTrackerFactory();

        // 5. Pass the GraphicFaceTrackerFactory to
        //    a MultiProcessor.Builder to create a MultiProcessor
        MultiProcessor multiProcessor = new MultiProcessor.Builder<>(graphicFaceTrackerFactory).build();

        // 6. Associate the MultiProcessor with the real time detector
        detector.setProcessor(multiProcessor);

        // 7. Check if the real time detector is operational
        if(!detector.isOperational()){
            Log.w(TAG, "Face detector not operational");
        }

        // 8. Create a camera source to capture video images from the camera,
        //    and continuously stream those images into the detector and
        //    its associated MultiProcessor
        mCameraSource = new CameraSource.Builder(context,detector)
                .setRequestedPreviewSize(800,600)
                .setFacing(facing)
                .setRequestedFps(30.0f)
                .build();

    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource(CameraSource.CAMERA_FACING_BACK);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        finish();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, null);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {

        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {

        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {

        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {

        }
    }
}
