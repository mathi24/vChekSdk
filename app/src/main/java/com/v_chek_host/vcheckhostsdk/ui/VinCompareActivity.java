package com.v_chek_host.vcheckhostsdk.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.v_chek_host.vcheckhostsdk.Details;
import com.v_chek_host.vcheckhostsdk.R;
import com.v_chek_host.vcheckhostsdk.barcodescanner.BarcodeCaptureActivity;
import com.v_chek_host.vcheckhostsdk.barcodescanner.BarcodeGraphic;
import com.v_chek_host.vcheckhostsdk.barcodescanner.BarcodeTrackerFactory;
import com.v_chek_host.vcheckhostsdk.barcodescanner.camera.BarcodeGraphicTracker;
import com.v_chek_host.vcheckhostsdk.barcodescanner.camera.CameraSourcePreview;
import com.v_chek_host.vcheckhostsdk.barcodescanner.camera.GraphicOverlay;
import com.v_chek_host.vcheckhostsdk.model.entity.Imageitem;
import com.v_chek_host.vcheckhostsdk.model.entity.ParentMetaData;
import com.v_chek_host.vcheckhostsdk.model.entity.ParentResponseData;
import com.v_chek_host.vcheckhostsdk.model.entity.Result;
import com.v_chek_host.vcheckhostsdk.ocrscanner.OcrDetectorProcessor;
import com.v_chek_host.vcheckhostsdk.ocrscanner.OcrGraphic;
import com.v_chek_host.vcheckhostsdk.presenter.contract.IModelResultPresenter;
import com.v_chek_host.vcheckhostsdk.presenter.implement.ModelResultPresenter;
import com.v_chek_host.vcheckhostsdk.utils.GPSTracker;
import com.v_chek_host.vcheckhostsdk.utils.PreferenceStorage;
import com.v_chek_host.vcheckhostsdk.utils.Utility;
import com.v_chek_host.vcheckhostsdk.view.IModelResultView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import retrofit2.Call;

import static com.v_chek_host.vcheckhostsdk.BuildConfig.SDK_VERSION_CODE;
import static com.v_chek_host.vcheckhostsdk.ui.ClassifierActivity.convertBitmapToByte;
import static com.v_chek_host.vcheckhostsdk.ui.ClassifierActivity.encryptImage;

public class VinCompareActivity extends AppCompatActivity implements IModelResultView, BarcodeGraphicTracker.BarcodeUpdateListener {
    private static final String TAG = "MainActivity";
    private static final int requestPermissionID = 101;
    ImageButton btnClose;
    //    Button btnOk;
    String data;
    //SurfaceView mCameraView;
    TextView mTextView;
    // CameraSource mCameraSource;
    boolean isInitiaited = false;
    boolean isScanned = false;
    private String searchedVinNumber = "";
    private List<ParentResponseData.ModelResult> modelResultData = new ArrayList<>();
    private static List<ParentResponseData.ModelResult> modelResultDataApi = new ArrayList<>();
    private ParentResponseData parentResponseData;
    private ParentResponseData parentResponseDataApi;
    private List<Result.ModelData> modelData;
    private ParentMetaData parentData;
    String modelResultString;
    IModelResultPresenter modelResultPresenter;
    Gson gson;
    public String vinNumber = "";
    public String modelId;
    public String modelName;
    public String modelVersion;
    public String AOI;
    private String seqNo;
    protected RelativeLayout inspktLyt;
    protected ImageView okImage, cancelImage, resultImageView;
    protected CardView retakeCard;
    protected TextView resultTextView, retakeTextView;
    private String failMsg;
    private String failMsgColor;
    private String passMsg;
    private String passMsgColor;
    private String reTakeMsg;
    private String activityModelString;
    private boolean retakeConfirm;
    MediaPlayer ring;
    boolean isBeepSound = false;
    boolean isSent = false;
    public byte[] mondelImage = null;
    private GPSTracker tracker;
    private Location currentLocation;
    // private static final String TAG = "Barcode-reader";

    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String BarcodeObject = "Barcode";

    private com.v_chek_host.vcheckhostsdk.barcodescanner.camera.CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;
    private GraphicOverlay<OcrGraphic> mGraphicOverlayOCR;

    // helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    public boolean isBarcodeScanner = false;
    boolean autoFocus;
    boolean useFlash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_vin_number);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            vinNumber = bundle.getString(getString(R.string.key_vin_no));
        }
        ring = MediaPlayer.create(VinCompareActivity.this, R.raw.long_beep);
        // mCameraView = findViewById(R.id.surfaceView);
        mTextView = findViewById(R.id.text_view);
        okImage = findViewById(R.id.retake_img);
        cancelImage = findViewById(R.id.cnt_img);
        retakeCard = findViewById(R.id.card_view_retake);
        resultImageView = findViewById(R.id.ins_img);
        inspktLyt = findViewById(R.id.inspkt_lyt);
        resultTextView = findViewById(R.id.result);
        retakeTextView = findViewById(R.id.txt_retake);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation_view);
        navigation.setVisibility(View.VISIBLE);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay<BarcodeGraphic>) findViewById(R.id.graphicOverlay);
        mGraphicOverlayOCR = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);
        modelResultPresenter = new ModelResultPresenter(this, this);
        activityModelString = PreferenceStorage.getInstance(VinCompareActivity.this).getActivityModel();
        gson = new Gson();
        Type modelType = new TypeToken<ArrayList<Result.ModelData>>() {
        }.getType();
        modelData = gson.fromJson(activityModelString, modelType);
        for (int i = 0; i < modelData.size(); i++) {
            modelId = modelData.get(i).getModelId();
            modelName = modelData.get(i).getModelName();
            modelVersion = modelData.get(i).getModelVersion();
            AOI = modelData.get(i).getModelMark();
            passMsg = modelData.get(i).getPassMsg();
            passMsgColor = modelData.get(i).getPassMsgColor();
            failMsg = modelData.get(i).getFailMsg();
            failMsgColor = modelData.get(i).getFailMsgColor();
            retakeConfirm = (modelData.get(i).getRetake() > 0) ? true : false;
            reTakeMsg = modelData.get(i).getReTakeMsg();
            seqNo = modelData.get(i).getSeqNo();
        }
        // read parameters from the intent used to launch the activity.
        autoFocus = getIntent().getBooleanExtra(AutoFocus, true);
        useFlash = getIntent().getBooleanExtra(UseFlash, false);
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            if (!isBarcodeScanner) {
                createCameraSource(autoFocus, useFlash);
            } else {
                ocrCameraSource(autoFocus, useFlash);
            }
        } else {
            requestCameraPermission();
        }

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

       /* Snackbar.make(mGraphicOverlay, "Tap to capture. Pinch/Stretch to zoom",
                Snackbar.LENGTH_LONG)
                .show();*/
        // barcodeDection();
        ImageView ivClose = findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tracker = new GPSTracker(VinCompareActivity.this);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_bar_code) {// mTextView.setText("");
                if (mCameraSource != null) {
                    isBarcodeScanner = false;
                    mGraphicOverlay = (GraphicOverlay<BarcodeGraphic>) findViewById(R.id.graphicOverlay);
                    mPreview.stop();
                    mCameraSource.stop();
                    mCameraSource = null;
                    mPreview = null;
                    mPreview = (CameraSourcePreview) findViewById(R.id.preview);
                    createCameraSource(autoFocus, useFlash);
                    startCameraSource();
                }
                return true;
                /*case R.id.navigation_qr:
                   // mTextView.setText("");
                    if (mCameraSource != null) {
                        qrCodeDetection();
                    }
                    return true;*/
            } else if (itemId == R.id.navigation_ocr) {// mTextView.setText("");
                if (mCameraSource != null) {
                    isBarcodeScanner = true;
                    mGraphicOverlayOCR = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);
                    mPreview.stop();
                    mCameraSource.stop();
                    mCameraSource = null;
                    mPreview = null;
                    mPreview = (CameraSourcePreview) findViewById(R.id.preview);
                    ocrCameraSource(autoFocus, useFlash);
                    startCameraSource();
                }
                return true;
            }
            return false;
        }
    };


    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
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

        findViewById(R.id.topLayout).setOnClickListener(listener);
        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     * <p>
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = this;

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay, this);
        barcodeDetector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        com.v_chek_host.vcheckhostsdk.barcodescanner.camera.CameraSource.Builder builder = new com.v_chek_host.vcheckhostsdk.barcodescanner.camera.CameraSource.Builder(this, barcodeDetector)
                .setFacing(com.v_chek_host.vcheckhostsdk.barcodescanner.camera.CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f);

        // make sure that auto focus is an available option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setFocusMode(
                    autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);
        }

        mCameraSource = builder
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .build();
    }


    @Override
    public void onResume() {
        super.onResume();
        //((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    @Override
    protected void onDestroy() {
        tracker.stopUsingGPS();
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }


    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                this);
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                if (!isBarcodeScanner) {
                    mPreview.start(mCameraSource, mGraphicOverlay);
                } else {
                    mPreview.start(mCameraSource, mGraphicOverlayOCR);
                }
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /**
     * onTap returns the tapped barcode result to the calling Activity.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the activity is ending.
     */
    private boolean onTap(float rawX, float rawY) {
        // Find tap point in preview frame coordinates.
        int[] location = new int[2];
        mGraphicOverlay.getLocationOnScreen(location);
        float x = (rawX - location[0]) / mGraphicOverlay.getWidthScaleFactor();
        float y = (rawY - location[1]) / mGraphicOverlay.getHeightScaleFactor();

        // Find the barcode whose center is closest to the tapped point.
        Barcode best = null;
        float bestDistance = Float.MAX_VALUE;
        for (BarcodeGraphic graphic : mGraphicOverlay.getGraphics()) {
            Barcode barcode = graphic.getBarcode();
            if (barcode.getBoundingBox().contains((int) x, (int) y)) {
                // Exact hit, no need to keep looking.
                best = barcode;
                break;
            }
            float dx = x - barcode.getBoundingBox().centerX();
            float dy = y - barcode.getBoundingBox().centerY();
            float distance = (dx * dx) + (dy * dy);  // actually squared distance
            if (distance < bestDistance) {
                best = barcode;
                bestDistance = distance;
            }
        }

        if (best != null) {
          /*  Intent data = new Intent();
            // data.putExtra(BarcodeObject, best);
            data.putExtra("scan_no", best);
            //setResult(CommonStatusCodes.SUCCESS, data);
            setResult(RESULT_OK, data);
            finish();*/
            takeImage(best.displayValue);
            return true;
        }
        return false;
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!isBarcodeScanner) {
                return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
            } else {
                return onTapOCR(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
            }
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }

    @SuppressLint("InlinedApi")
    private void ocrCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = this;

        // A text recognizer is created to find text.  An associated processor instance
        // is set to receive the text recognition results and display graphics for each text block
        // on screen.
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlayOCR));

        if (!textRecognizer.isOperational()) {
            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.
        mCameraSource =
                new com.v_chek_host.vcheckhostsdk.barcodescanner.camera.CameraSource.Builder(this, textRecognizer)
                        .setFacing(com.v_chek_host.vcheckhostsdk.barcodescanner.camera.CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(2.0f)
                        .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                        .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                        .build();
    }

    private boolean onTapOCR(float rawX, float rawY) {
        OcrGraphic graphic = mGraphicOverlayOCR.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        if (graphic != null) {
            text = graphic.getTextBlock();
            if (text != null && text.getValue() != null) {
              /*  Intent data = new Intent();
                //data.putExtra(TextBlockObject, text.getValue());
                data.putExtra("scan_no", text.getValue());
                //setResult(CommonStatusCodes.SUCCESS, data);
                setResult(RESULT_OK, data);
                finish();*/
                takeImage(text.getValue());
            } else {
                Log.d(TAG, "text data is null");
            }
        } else {
            Log.d(TAG, "no text detected");
        }
        return text != null;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, true);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            if (!isBarcodeScanner) {
                createCameraSource(autoFocus, useFlash);
            } else {
                ocrCameraSource(autoFocus, useFlash);
            }
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    void updateTextinView(String text) {
        text = text.replace(" ", "");
        //if (text.length() == 17) {
        if (!isScanned) {
            isScanned = true;
            mTextView.setText(text);
            sendVinNumber(mTextView.getText().toString());
            Imageitem imageitem = new Imageitem();
            if (vinNumber.equals(text)) {
                imageitem.setStatus("PASS");
                imageitem.setResultMsg(passMsg);
                showRetake(imageitem);
            } else {
                imageitem.setStatus("FAIL");
                imageitem.setResultMsg(failMsg);
                showRetake(imageitem);
            }
        }
        // }
    }


    void showRetake(Imageitem result) {
        Imageitem imageitem = result;
        runOnUiThread(new Runnable() {
            public void run() {
                retakeCard.setVisibility(View.VISIBLE);
                if (imageitem.getStatus().equalsIgnoreCase("Fail")) {
                    inspktLyt.setBackgroundColor(Color.parseColor("#FF5722"));
                    resultImageView.setBackgroundResource(R.drawable.ic_fail_white_24dp);
                    retakeTextView.setText(failMsg + "" + reTakeMsg);
                } else if (imageitem.getStatus().equalsIgnoreCase("Pass")) {
                    inspktLyt.setBackgroundColor(Color.parseColor("#8BC34A"));
                    resultImageView.setBackgroundResource(R.drawable.ic_pass_white_24dp);
                    retakeTextView.setText(passMsg + "" + reTakeMsg);
                }
            }
        });
        okImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retakeCard.setVisibility(View.GONE);
                processResponse(imageitem);
            }
        });
        cancelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retakeCard.setVisibility(View.GONE);
                isBeepSound = false;
                isSent = false;
                runOnUiThread(new Runnable() {
                    public void run() {
                        resultTextView.setText("");
                        isScanned = false;
                        isBeepSound = false;
                        //  startVideo.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    void sendVinNumber(String vin) {
       /* Log.v("helllo", vin);
        Intent intent = new Intent();
        intent.putExtra("scan_no", vin);
        setResult(RESULT_OK, intent);
        finish();*/
    }


    private void takeImage(final String text) {
        try {
            mCameraSource.takePicture(null, new com.v_chek_host.vcheckhostsdk.barcodescanner.camera.CameraSource.PictureCallback() {

                private File imageFile;

                @Override
                public void onPictureTaken(byte[] bytes) {
                    try {
                        Bitmap loadedImage = null;
                        loadedImage = BitmapFactory.decodeByteArray(bytes, 0,
                                bytes.length);
                        byte[] byteImage = convertBitmapToByte(loadedImage);
                        if (!isScanned) {
                            isScanned = true;
                          /*  mTextView.setText(text);
                            sendVinNumber(mTextView.getText().toString());*/
                            Imageitem imageitem = new Imageitem();
                            if (vinNumber.equals(text)) {
                                imageitem.setStatus("PASS");
                                imageitem.setResultMsg(passMsg);
                                imageitem.setByteImage(byteImage);
                                resultTextView.setTextColor(Color.parseColor(passMsgColor));
                                resultTextView.setText(passMsg);
                                // showRetake(imageitem);
                            } else {
                                imageitem.setStatus("FAIL");
                                imageitem.setResultMsg(failMsg);
                                imageitem.setByteImage(byteImage);
                                resultTextView.setTextColor(Color.parseColor(failMsgColor));
                                resultTextView.setText(failMsg);
                                //  showRetake(imageitem);
                            }
                            //  processResponse(imageitem);
                            if (!isBeepSound) {
                                ring.start();
                                isBeepSound = true;
                            }
                            if (retakeConfirm)
                                showRetake(imageitem);
                            else
                                processResponse(imageitem);


                        }
                       /* // convert byte array into bitmap
                        Bitmap loadedImage = null;
                        Bitmap rotatedBitmap = null;
                        loadedImage = BitmapFactory.decodeByteArray(bytes, 0,
                                bytes.length);
                        // rotate Image
                        Matrix rotateMatrix = new Matrix();
                        rotateMatrix.postRotate(*/
                        /*rotation*//*0);
                        rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0,
                                loadedImage.getWidth(), loadedImage.getHeight(),
                                rotateMatrix, false);
                        String state = Environment.getExternalStorageState();
                        File folder = null;
                        if (state.contains(Environment.MEDIA_MOUNTED)) {
                            folder = new File(Environment
                                    .getExternalStorageDirectory() + "/vTally");
                        } else {
                            folder = new File(Environment
                                    .getExternalStorageDirectory() + "/vTally");
                        }
                        boolean success = true;
                        if (!folder.exists()) {
                            success = folder.mkdirs();
                        }
                        if (success) {
                            java.util.Date date = new java.util.Date();
                            imageFile = new File(folder.getAbsolutePath()
                                    + File.separator
                                    + "Image.jpg");
                            imageFile.createNewFile();
                        } else {
                            Toast.makeText(getBaseContext(), "Image Not saved",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                        // save image into gallery
                        rotatedBitmap = resize(rotatedBitmap, 800, 600);
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                        FileOutputStream fout = new FileOutputStream(imageFile);
                        fout.write(ostream.toByteArray());
                        fout.close();
                        Bitmap outPutBitmap = null;
                        if (success) {
                            outPutBitmap = BitmapFactory.decodeFile(folder.getAbsolutePath()
                                    + File.separator + "Image.jpg");
                        }
                        Bitmap finalOutPutBitmap = outPutBitmap;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                             //   uploadPresenter.uploadAsyncTask(intentData, encodeTobase64(finalOutPutBitmap), masterIdString);
                            }
                        }).start();*/

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception ex) {
            //  txTextoCapturado.setText("Error al capturar fotografia!");
        }

    }

    private Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;
            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    public void processResponse(Imageitem imageItem) {
        if (imageItem != null/* && !isDetect*/) {
            // try {
            if (modelResultData == null)
                modelResultData = new ArrayList<>();
            mondelImage = imageItem.getByteImage();
            modelResultData.add(new ParentResponseData.ModelResult(modelId, modelName, seqNo, modelVersion, AOI, imageItem.getStatus(),
                    imageItem.getResultMsg(), encryptImage(mondelImage),"helllo"));

            gson = new Gson();
            modelResultString = gson.toJson(modelResultData);
            PreferenceStorage.getInstance(this).setModelResult(modelResultString);


            // isDetect = true;


            String ParentMetaDataStr = PreferenceStorage.getInstance(this).getActivity();
            gson = new Gson();
            Type metaType = new TypeToken<ParentMetaData>() {
            }.getType();
            parentData = gson.fromJson(ParentMetaDataStr, metaType);
            if (PreferenceStorage.getInstance(this).getVinNeed() == 1)
                parentData.getMetaData().setObjectId(vinNumber);
            String activityResult = "PASS";
            String activityResultMsg = PreferenceStorage.getInstance(this).
                    getActivityPassMsg();
            gson = new Gson();
            Type apiType = new TypeToken<ArrayList<ParentResponseData.ModelResult>>() {
            }.getType();
            modelResultDataApi = gson.fromJson(modelResultString, apiType);
            for (int i = 0; i < modelResultData.size(); i++) {
                if (modelResultData.get(i).inspktResult.equals("FAIL")) {
                    activityResult = "FAIL";
                    activityResultMsg = PreferenceStorage.getInstance(this).
                            getActivityFailMsg();
                }
                modelResultData.get(i).inspktResultImage = null;
                modelResultData.get(i).AoiImage = null;
                modelResultData.get(i).resultConfid = null;
            }

            parentResponseData = new ParentResponseData(parentData.getMetaData()
                    , new ParentResponseData.ActivityResult(activityResult, activityResultMsg,
                    modelResultData), parentData.getPrimaryData());
            String lat="";
            String lng="";
            currentLocation = tracker.getLocation();
            if(currentLocation!=null){
                lat= String.valueOf(currentLocation.getLatitude());
                lng= String.valueOf(currentLocation.getLongitude());
            }
            parentResponseDataApi = new ParentResponseData(parentData.getMetaData()
                    , new ParentResponseData.ActivityResult(activityResult, activityResultMsg,
                    modelResultDataApi,lat,lng,
                    Utility.deviceDetail,String.valueOf( SDK_VERSION_CODE),Utility.getDeviceUDID(this)), parentData.getPrimaryData());
            gson = new Gson();
            String activityApiString = gson.toJson(parentResponseDataApi);
            Log.v("Activity Api Result", activityApiString);
            gson = new Gson();
            String activityResultString = gson.toJson(parentResponseData);
            //  System.out.println("Activity Result" + activityResultString);
            Details.result = activityResultString;
            if (Details.getInstance().getCallback() != null) {
                Details.getInstance().getCallback().onSuccess(activityResultString);
            } else {
                Utility.exceptionLog(VinCompareActivity.this,
                        Utility.getLog("Parent application call back is null"), PreferenceStorage.getInstance(this).getUserId()
                        ,PreferenceStorage.getInstance(this).getSiteId(),
                        PreferenceStorage.getInstance(this).getActivityId(),
                        PreferenceStorage.getInstance(this).getModelId(),"2");
            }
            modelResultPresenter.modelResultAsyncTask(parentResponseDataApi);
            finish();


        }
    }

    @Override
    public void onModelResultError(@NonNull String message) {

    }

    @Override
    public void onModelResultOk(@NonNull Result.BaseResponse baseResponse) {
        if (baseResponse.getmResponseCode().equals("200")) {
            System.out.println("Api Req" + baseResponse.getmMessage());
        }
    }

    @Override
    public void onModelResultStart(@NonNull Call<Result.BaseResponse> call) {

    }

    @Override
    public void onModelResultFinish() {

    }

    @Override
    public void onBarcodeDetected(Barcode barcode) {

    }

}