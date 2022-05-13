package com.v_chek_host.vcheckhostsdk.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;


import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.FocusingProcessor;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.v_chek_host.vcheckhostsdk.R;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

public class ScanVinNumberActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int requestPermissionID = 101;
    ImageButton btnClose;
    //    Button btnOk;
    String data;
    SurfaceView mCameraView;
    TextView mTextView;
    //    RadioGroup rdGroup;
    CameraSource mCameraSource;
    Pattern regex = Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]");
    String radioButtonSelected = "barcode";
    boolean isInitiaited = false;
    boolean isScanned = false;
    private String searchedVinNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_vin_number);
        Bundle bundle = getIntent().getExtras();
       /* if (bundle != null) {
            searchedVinNumber = bundle.getString(StrConstants.SEARCH_VIN_NUMBER);
        }*/
        mCameraView = findViewById(R.id.surfaceView);
        mTextView = findViewById(R.id.text_view);
//        rdGroup = view.findViewById(R.id.rdGroup);

      //  BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
      //  navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        barcodeDection();

        ImageView ivClose = findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVinNumber("");
            }
        });
    }

    /*private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_bar_code:
                    mTextView.setText("");
                    if (mCameraSource != null) {
                        barcodeDection();
                    }
                    return true;
                case R.id.navigation_qr:
                    mTextView.setText("");
                    if (mCameraSource != null) {
                        qrCodeDetection();
                    }
                    return true;
                case R.id.navigation_ocr:
                    mTextView.setText("");
                    if (mCameraSource != null) {
                        textDetection();
                    }
                    return true;
            }
            return false;
        }
    };*/

    @Override
    public void onResume() {
        super.onResume();
        //((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onStop() {
        super.onStop();
        // ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != requestPermissionID) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(ScanVinNumberActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mCameraSource.start(mCameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    void updateTextinView(String text) {
        text = text.replace(" ", "");
        //if (text.length() == 17) {
        if (!isScanned) {
            isScanned = true;
            mTextView.setText(text);
            sendVinNumber(mTextView.getText().toString());
        }
        // }
    }


    void sendVinNumber(String vin) {
        VinNumber vinNumber = new VinNumber(vin);
        Log.v("helllo", vin);
        Intent intent = new Intent();
        intent.putExtra("scan_no", vin);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void textDetection() {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(ScanVinNumberActivity.this).build();
        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies not loaded yet");
         //   Toast.makeText(this, R.string.error_scan_will_not_work_try_again, Toast.LENGTH_SHORT).show();
        }
        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {
            }

            /**
             * Detect all the text from camera using TextBlock and the values into a stringBuilder
             * which will then be set to the textView.
             * */
            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                final SparseArray<TextBlock> items = detections.getDetectedItems();
                if (items.size() != 0) {
                    mTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i = 0; i < items.size(); i++) {
                                TextBlock item = items.valueAt(i);
                                stringBuilder.append(item.getValue());
//                                    stringBuilder.append("");
                                System.out.println("Scan text OCR:  " + stringBuilder.toString());
                            }
                            String s = "";
//                            String s = "OCR ";
                            s = s + stringBuilder.toString();
                            updateTextinView(s);
//                            }
                        }
                    });
                }
            }
        });
        initialiseCameraSource(textRecognizer);
        /**
         * Add call back to SurfaceView and check if camera permission is granted.
         * If permission is granted we can start our cameraSource and pass it to surfaceView
         */
        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScanVinNumberActivity.this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ScanVinNumberActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                requestPermissionID);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.stop();
            }
        });
        startCameraView();
    }

    void initialiseCameraSource(Detector detector) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        mCameraSource = new CameraSource.Builder(ScanVinNumberActivity.this, detector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(screenHeight, screenWidth)
//                .setRequestedPreviewSize(1280, 1024)
                .setAutoFocusEnabled(true)
                .setRequestedFps(2.0f)
                .build();
    }

    void startCameraView() {
        try {
            if (ActivityCompat.checkSelfPermission(ScanVinNumberActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mCameraSource.start(mCameraView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void barcodeDection() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(ScanVinNumberActivity.this)
                .setBarcodeFormats(Barcode.EAN_13 | Barcode.EAN_8 | Barcode.UPC_A | Barcode.UPC_E
                        | Barcode.CODE_39 | Barcode.CODE_93
                        | Barcode.CODE_128 | Barcode.ITF | Barcode.CODABAR | Barcode.QR_CODE)
                .build();
        if (!barcodeDetector.isOperational()) {
          ///  Toast.makeText(ScanVinNumberActivity.this, R.string.error_scan_will_not_work_try_again, Toast.LENGTH_SHORT).show();
            return;
        }
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> items = detections.getDetectedItems();
                if (items.size() != 0) {
                    mTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder stringBuilder = new StringBuilder();
                            String s = "";
//                            String s = "BAR ";
                            s = s + (items.valueAt(0).displayValue);
                            System.out.println("Scan text BAR code:  " + s);
                            updateTextinView(s);
                        }
                    });
                }

            }
        });
        initialiseCameraSource(barcodeDetector);
        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScanVinNumberActivity.this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ScanVinNumberActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                requestPermissionID);
                        return;
                    }
                    startCameraView();
                    isInitiaited = true;
//                    mCameraSource.start(mCameraView.getHolder());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.stop();
            }
        });
        if (isInitiaited) {
            startCameraView();
        }
    }

    private void qrCodeDetection() {
        BarcodeDetector qrcodeDetector = new BarcodeDetector.Builder(ScanVinNumberActivity.this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        if (!qrcodeDetector.isOperational()) {
         ///   Toast.makeText(ScanVinNumberActivity.this, R.string.error_scan_will_not_work_try_again, Toast.LENGTH_SHORT).show();
            return;
        }
        qrcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> items = detections.getDetectedItems();
                if (items.size() != 0) {
                    mTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder stringBuilder = new StringBuilder();
                            String s = "";
//                             s = "QR ";
                            s = s + (items.valueAt(0).displayValue);
                            System.out.println("Scan text QR:  " + s);
                            updateTextinView(s);
                        }
                    });
                }

            }
        });
        initialiseCameraSource(qrcodeDetector);
        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScanVinNumberActivity.this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ScanVinNumberActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                requestPermissionID);
                        return;
                    }
//                    mCameraSource.start(mCameraView.getHolder());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.stop();
            }
        });
        startCameraView();
    }

    public class VinNumber {
        public String vinNo;

        public VinNumber(String vinNo) {
            this.vinNo = vinNo;
        }

        public String getVinNo() {
            return vinNo;
        }

        public void setVinNo(String vinNo) {
            this.vinNo = vinNo;
        }
    }

    public class CentralBarcodeFocusingProcessor extends FocusingProcessor<Barcode> {

        public CentralBarcodeFocusingProcessor(Detector<Barcode> detector, Tracker<Barcode> tracker) {
            super(detector, tracker);
        }

        @Override
        public int selectFocus(Detector.Detections<Barcode> detections) {

            SparseArray<Barcode> barcodes = detections.getDetectedItems();
            Frame.Metadata meta = detections.getFrameMetadata();
            double nearestDistance = Double.MAX_VALUE;
            int id = -1;

            for (int i = 0; i < barcodes.size(); ++i) {
                int tempId = barcodes.keyAt(i);
                Barcode barcode = barcodes.get(tempId);
                float dx = Math.abs((meta.getWidth() / 2) - barcode.getBoundingBox().centerX());
                float dy = Math.abs((meta.getHeight() / 2) - barcode.getBoundingBox().centerY());

                double distanceFromCenter =  Math.sqrt((dx * dx) + (dy * dy));

                if (distanceFromCenter < nearestDistance) {
                    id = tempId;
                    nearestDistance = distanceFromCenter;
                }
            }
            return id;
        }
    }
}