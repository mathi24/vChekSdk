/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.v_chek_host.vcheckhostsdk.ml;

import static com.v_chek_host.vcheckhostsdk.BuildConfig.SDK_VERSION_CODE;
import static com.v_chek_host.vcheckhostsdk.ui.ClassifierActivity.convertBitmapToByte;
import static com.v_chek_host.vcheckhostsdk.ui.ClassifierActivity.encryptImage;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.v_chek_host.vcheckhostsdk.Details;
import com.v_chek_host.vcheckhostsdk.R;
import com.v_chek_host.vcheckhostsdk.VinNumberActivity;
import com.v_chek_host.vcheckhostsdk.ml.adapter.OcrListAdapter;
import com.v_chek_host.vcheckhostsdk.model.entity.Imageitem;
import com.v_chek_host.vcheckhostsdk.model.entity.ParentMetaData;
import com.v_chek_host.vcheckhostsdk.model.entity.ParentResponseData;
import com.v_chek_host.vcheckhostsdk.model.entity.Result;
import com.v_chek_host.vcheckhostsdk.presenter.contract.IModelResultPresenter;
import com.v_chek_host.vcheckhostsdk.presenter.implement.ModelResultPresenter;
import com.v_chek_host.vcheckhostsdk.ui.VinCompareActivity;
import com.v_chek_host.vcheckhostsdk.utils.GPSTracker;
import com.v_chek_host.vcheckhostsdk.utils.PreferenceStorage;
import com.v_chek_host.vcheckhostsdk.utils.Utility;
import com.v_chek_host.vcheckhostsdk.view.IModelResultView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class VinCompareResultActivity extends VinCompareTrainCameraActivity implements OnImageAvailableListener, IModelResultView {

    private static final int RESULT_CROP = 200;
    private static Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    // private static Size DESIRED_PREVIEW_SIZE = new Size(1360, 960);
    //private static Size DESIRED_PREVIEW_SIZE = new Size(960, 1360);
    ;
    private static final float TEXT_SIZE_DIP = 10;
    private Bitmap rgbFrameBitmap = null;
    private long lastProcessingTimeMs;
    private Integer sensorOrientation;
    /*private Classifier classifier;*/
    private String modelOverlayUrl;
    String root = Environment.getExternalStorageDirectory().toString();
    String timeStamp;
    File file;
    /**
     * Input image size of the model along x axis.
     */
    private int imageSizeX;
    /**
     * Input image size of the model along y axis.
     */
    private int imageSizeY;
    public static boolean isVideoRecordStarted = false;

    ImageView startVideo;
    ImageView ivClose;
    ImageView ivCapturePrevieImageView;
    ImageView ivCapturePrevieImageView2;
    TextView tvSingleAndMulti;
    boolean isSingleAndMulti = false;
    // public List<MultipleImageUploadModel> multipleImageUploadModelList = V3PassAndFailActivity.multipleImageUploadModelList;
    public boolean isUploading = false;
    Handler mHandler;
    Runnable runnable;
    boolean isCallingAPI;
    String imagePosition = "";
    String imagePath = "";
    // Call<MultipleImageModelResponce> call;
    LinearLayout rotationView;
    int imageUrlPosition = 1;
    private RectangleOverLayBARCodeView rectangleOverLayBARCodeView;
    private RectangleOverLayQRCodeView rectangleOverLayQRCodeView;
    private int selectedItem = 1;
    public AlertDialog alertDialog;
    public String vinNumber = "";
    MediaPlayer ring;
    IModelResultPresenter modelResultPresenter;
    private String activityModelString;
    Gson gson;
    private List<Result.ModelData> modelData;
    public String modelId;
    public String modelName;
    public String modelVersion;
    public String AOI;
    private String seqNo;
    private String failMsg;
    private String failMsgColor;
    private String passMsg;
    private String passMsgColor;
    private String reTakeMsg;
    private boolean retakeConfirm;
    boolean isBeepSound = false;
    protected TextView resultTextView, retakeTextView;
    protected CardView retakeCard;
    protected RelativeLayout inspktLyt;
    protected ImageView okImage, cancelImage, resultImageView;
    boolean isScanned = false;
    private List<ParentResponseData.ModelResult> modelResultData = new ArrayList<>();
    private static List<ParentResponseData.ModelResult> modelResultDataApi = new ArrayList<>();
    public byte[] mondelImage = null;
    private ParentMetaData parentData;
    String modelResultString;
    private ParentResponseData parentResponseData;
    private ParentResponseData parentResponseDataApi;
    private GPSTracker tracker;
    private Location currentLocation;
    String scannedResult = "";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            vinNumber = bundle.getString(getString(R.string.key_vin_no));
        }
        ring = MediaPlayer.create(VinCompareResultActivity.this, R.raw.long_beep);
        modelResultPresenter = new ModelResultPresenter(this, this);
        activityModelString = PreferenceStorage.getInstance(VinCompareResultActivity.this).getActivityModel();
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
        tracker = new GPSTracker(VinCompareResultActivity.this);
        //fetchImagesDataOnLocal();
        mHandler = new Handler();
        /*runnable = new Runnable() {
            @Override
            public void run() {
                boolean apiCall = false;
                for (int i = 0; i < multipleImageUploadModelList.size(); i++
                ) {
                    if (!apiCall) {
                        if (!multipleImageUploadModelList.get(i).isUploaded()) {
                            apiCall = true;
                            int finalI = i;
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    //TODO your background code
                                    callMultipleImageUploadAPI(finalI, multipleImageUploadModelList.get(finalI).getLocalImagePath());

                                }
                            });
                        }
                    }
                }

            }
        };*/

    }

    public static int getScreenHeight(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static int getScreenWidth(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vin_compare_camera_connection_fragment;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        DESIRED_PREVIEW_SIZE = new Size(getScreenHeight(this),
                getScreenWidth(this));
        System.out.println("DESIRED_PREVIEW_SIZE " + DESIRED_PREVIEW_SIZE);
        ;
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    protected String getOverlayUrl() {
        return modelOverlayUrl;
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {


        /*recreateClassifier(getModel(), getDevice(), getNumThreads());
        if (classifier == null) {
            LOGGER.e("No classifier on preview!");
            return;
        }*/
        previewWidth = size.getWidth();
        previewHeight = size.getHeight();
        sensorOrientation = rotation - getScreenOrientation();
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    }

    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();

            m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(
                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                throw ex;
            }
        }
        return b;
    }

    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int size, boolean recycle) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == size && h == size) return bitmap;
        // scale the image so that the shorter side equals to the target;
        // the longer side will be center-cropped.
        float scale = (float) size / Math.min(w, h);
        Bitmap target = Bitmap.createBitmap(size, size, getConfig(bitmap));
        int width = Math.round(scale * bitmap.getWidth());
        int height = Math.round(scale * bitmap.getHeight());
        Canvas canvas = new Canvas(target);
        canvas.translate((size - width) / 2f, (size - height) / 2f);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }

    private static Config getConfig(Bitmap bitmap) {
        Config config = bitmap.getConfig();
        if (config == null) {
            config = Config.ARGB_8888;
        }
        return config;
    }

    @Override
    protected void processImage() {
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        if (isVideoRecordStarted) {
                            isVideoRecordStarted = false;
                            if (rgbFrameBitmap != null) {
                                /*rgbFrameBitmap = rotate(rgbFrameBitmap, 90);*/
                                if (isSingleAndMulti) {

                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Bitmap dstBmp;
                                            Bitmap reSizedBitmap = rgbFrameBitmap.copy(Bitmap.Config.ARGB_8888, true);
                                            reSizedBitmap = rotate(reSizedBitmap, 90);
                                            if (selectedItem == 1) {
                                                //barcode
                                                float x1 = reSizedBitmap.getWidth() / 20;

                                                float x2 = reSizedBitmap.getWidth() - x1;

                                                float heightDiff = x2 - x1;

                                                float y1 = (reSizedBitmap.getHeight() / 2) - (heightDiff / 5);

                                                float y2 = (reSizedBitmap.getHeight() / 2) + (heightDiff / 5);

                                                dstBmp = Bitmap.createBitmap(reSizedBitmap, Math.round(x1), Math.round(y1), Math.round(x2 - x1)
                                                        , Math.round(y2 - y1));
                                            } else if (selectedItem == 2) {
                                                //qrcode
                                                float x1 = reSizedBitmap.getWidth() / 5;

                                                float x2 = reSizedBitmap.getWidth() - x1;

                                                float heightDiff = x2 - x1;

                                                float y1 = (reSizedBitmap.getHeight() / 2) - (heightDiff / 2);

                                                float y2 = (reSizedBitmap.getHeight() / 2) + (heightDiff / 2);

                                                dstBmp = Bitmap.createBitmap(reSizedBitmap, Math.round(x1), Math.round(y1), Math.round(x2 - x1)
                                                        , Math.round(y2 - y1));
                                            } else {
                                                // ocr
                                                float x1 = reSizedBitmap.getWidth() / 20;

                                                float x2 = reSizedBitmap.getWidth() - x1;

                                                float heightDiff = x2 - x1;

                                                float y1 = (reSizedBitmap.getHeight() / 2) - (heightDiff / 5);

                                                float y2 = (reSizedBitmap.getHeight() / 2) + (heightDiff / 5);

                                                dstBmp = Bitmap.createBitmap(reSizedBitmap, Math.round(x1), Math.round(y1), Math.round(x2 - x1)
                                                        , Math.round(y2 - y1));

                                            }

                                           /* if (rgbFrameBitmap.getWidth() >= rgbFrameBitmap.getHeight()) {

                                                dstBmp = Bitmap.createBitmap(
                                                        rgbFrameBitmap,
                                                        rgbFrameBitmap.getWidth() / 2 - rgbFrameBitmap.getHeight() / 2,
                                                        0,
                                                        rgbFrameBitmap.getHeight(),
                                                        rgbFrameBitmap.getHeight()
                                                );

                                            } else {

                                                dstBmp = Bitmap.createBitmap(
                                                        rgbFrameBitmap,
                                                        0,
                                                        rgbFrameBitmap.getHeight() / 2 - rgbFrameBitmap.getWidth() / 2,
                                                        rgbFrameBitmap.getWidth(),
                                                        rgbFrameBitmap.getWidth()
                                                );
                                            }*/
                                            //  ivCapturePrevieImageView.setVisibility(View.VISIBLE);
                                            // ivCapturePrevieImageView.setImageBitmap(dstBmp);
                                            //  ivCapturePrevieImageView2.setImageBitmap(rgbFrameBitmap);
                                            // isVideoRecordStarted = false;
                                            // startVideo.setVisibility(View.VISIBLE);
                                            if (selectedItem == 1) {
                                                scanBarcode(dstBmp);
                                            } else if (selectedItem == 2) {
                                                scanBarcode(dstBmp);
                                            } else {
                                                scanOCRCode(dstBmp);
                                            }
                                            // readyForNextImage();
                                        }
                                    });
                                }
                            }
                            /*OutputStream os = null;
                            Bitmap previewBitmap = cropImage(rgbFrameBitmap, previewWidth,
                                    previewHeight, 0, 0, previewWidth,
                                    (previewHeight - PreferenceStorage.getModelBottomCropHeight(CaptureActivity.this)));
                            try {
                                imageUrlPosition = imageUrlPosition + 1;
                                timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                file = new File(saveImageInLocalStorage() + "/train" + imageUrlPosition + ".png");
                                os = new BufferedOutputStream(new FileOutputStream(file));
                                rgbFrameBitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
                                os.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (file.exists()) {

                                //saveFileLocal();
                                Bundle bundle = new Bundle();
                                bundle.putString("file", file + "");
                                if (isSingleAndMulti) {
                                    // MultipleImageUploadModel multipleImageUploadModel = new MultipleImageUploadModel(file.getAbsolutePath(), "", false, "");
                                    //multipleImageUploadModelList.add(multipleImageUploadModel);
                                    Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ivCapturePrevieImageView.setVisibility(View.VISIBLE);
                                            ivCapturePrevieImageView.setImageBitmap(myBitmap);
                                            isVideoRecordStarted = false;
                                            startVideo.setVisibility(View.VISIBLE);
                                            readyForNextImage();
                                            if (!isUploading) {
                                                isUploading = true;
                                                // mHandler.postDelayed(runnable, 1000);
                                            }
                                            //callMultipleImageUploadAPI();
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                            Bitmap dstBmp;
                                            if (rgbFrameBitmap.getWidth() >= rgbFrameBitmap.getHeight()) {

                                                dstBmp = Bitmap.createBitmap(
                                                        rgbFrameBitmap,
                                                        rgbFrameBitmap.getWidth() / 2 - rgbFrameBitmap.getHeight() / 2,
                                                        0,
                                                        rgbFrameBitmap.getHeight(),
                                                        rgbFrameBitmap.getHeight()
                                                );

                                            } else {

                                                dstBmp = Bitmap.createBitmap(
                                                        rgbFrameBitmap,
                                                        0,
                                                        rgbFrameBitmap.getHeight() / 2 - rgbFrameBitmap.getWidth() / 2,
                                                        rgbFrameBitmap.getWidth(),
                                                        rgbFrameBitmap.getWidth()
                                                );
                                            }
                                            ivCapturePrevieImageView.setVisibility(View.VISIBLE);
                                            ivCapturePrevieImageView.setImageBitmap(dstBmp);
                                           // ivCapturePrevieImageView.setVisibility(View.INVISIBLE);
                                           // Intent intent = new Intent(CaptureActivity.this, PreviewImageActivity.class);
                                           // intent.putExtras(bundle);
                                            //startActivity(intent);
                                           // finish();
                                            // performCrop(file.getAbsolutePath());
                                        }
                                    });

                                }

                            } else {
                                System.out.println("File is not exist");
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        // Stuff that updates the UI
                                        isVideoRecordStarted = false;
                                        startVideo.setVisibility(View.VISIBLE);
                                        readyForNextImage();
                                    }
                                });

                            }*/
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    isVideoRecordStarted = false;
                                    startVideo.setVisibility(View.VISIBLE);
                                    readyForNextImage();
                                }
                            });
                        }
                    }
                });
    }

    public void scanBarcode(Bitmap bitmap) {
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_QR_CODE,
                                Barcode.FORMAT_AZTEC)
                        .build();
        BarcodeScanner scanner = BarcodeScanning.getClient();
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        Task<List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        // Task completed successfully
                        // ...
                        if (barcodes != null && barcodes.size() > 0) {
                           /* Toast.makeText(VinCompareResultActivity.this, barcodes.get(0).getDisplayValue(), Toast.LENGTH_SHORT).show();
                            Intent data = new Intent();
                            // data.putExtra(BarcodeObject, best);
                            data.putExtra("scan_no", barcodes.get(0).getDisplayValue());
                            //setResult(CommonStatusCodes.SUCCESS, data);
                            setResult(RESULT_OK, data);
                            finish();*/
                            scannedResult = barcodes.get(0).getDisplayValue();
                            byte[] byteImage = convertBitmapToByte(rgbFrameBitmap);
                            Imageitem imageitem = new Imageitem();
                            if (!isScanned) {
                                isScanned = true;
                                if (vinNumber.equalsIgnoreCase(barcodes.get(0).getDisplayValue())) {
                                    imageitem.setStatus("PASS");
                                    imageitem.setResultMsg(passMsg);
                                    imageitem.setByteImage(byteImage);
                                    resultTextView.setTextColor(Color.parseColor(passMsgColor));
                                    resultTextView.setText(passMsg);
                                } else {
                                    imageitem.setStatus("FAIL");
                                    imageitem.setResultMsg(failMsg);
                                    imageitem.setByteImage(byteImage);
                                    resultTextView.setTextColor(Color.parseColor(failMsgColor));
                                    resultTextView.setText(failMsg);
                                }
                                if (!isBeepSound) {
                                    ring.start();
                                    isBeepSound = true;
                                }
                                if (retakeConfirm) {
                                    showRetake(imageitem);
                                } else {
                                    processResponse(imageitem);
                                }
                            }
                        } else {
                            isVideoRecordStarted = false;
                            startVideo.setVisibility(View.VISIBLE);
                            Toast.makeText(VinCompareResultActivity.this, "Not found", Toast.LENGTH_SHORT).show();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    readyForNextImage();
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                        isVideoRecordStarted = false;
                        startVideo.setVisibility(View.VISIBLE);
                        Toast.makeText(VinCompareResultActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                readyForNextImage();
                            }
                        });
                    }
                });
    }

    public void scanOCRCode(Bitmap bitmap) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                // ...
                                /*Toast.makeText(getApplicationContext(), visionText.getText(), Toast.LENGTH_SHORT).show();
                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    String blockText = block.getText();
                                    System.out.println("blockText " + blockText);
                                }*/
                                //
                                if (visionText != null && visionText.getTextBlocks().size() > 0) {

                                    showListOfOcrText(visionText);
                                } else {
                                    isVideoRecordStarted = false;
                                    startVideo.setVisibility(View.VISIBLE);
                                    Toast.makeText(VinCompareResultActivity.this, "Not found", Toast.LENGTH_SHORT).show();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            readyForNextImage();
                                        }
                                    });
                                }

                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        isVideoRecordStarted = false;
                                        startVideo.setVisibility(View.VISIBLE);
                                        Toast.makeText(VinCompareResultActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                readyForNextImage();
                                            }
                                        });
                                    }
                                });
    }

    public static Bitmap cropImage(Bitmap picture, int width, int height, int imageCropX, int imageCropY, int imageCropWidth, int imageCropHeight) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(picture, width, height, true);
        Bitmap cropedBitmap = Bitmap.createBitmap(scaledBitmap, imageCropX, imageCropY, imageCropWidth, imageCropHeight);
        return cropedBitmap;
    }

    public void showListOfOcrText(Text visionText) {

        List<OcrListAdapter.OcrList> ocrListList = new ArrayList<>();
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.ocr_list_view, viewGroup, false);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit);
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            OcrListAdapter.OcrList ocrList = new OcrListAdapter.OcrList();
            String input=block.getText();
            input = input.replace(" ", "");
            input = input.replaceAll("[$&+,:;=\\\\?@#|/'<>\".^*()%!-]", "");
            if(input.length()>VinNumberActivity.vinMaxLength)
                input = input.substring(0, VinNumberActivity.vinMaxLength);
            ocrList.setOcrText(input);
            ocrListList.add(ocrList);
        }
        if (ocrListList.size() > 0) {
            ocrListList.get(0).setSelected(true);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        OcrListAdapter ocrListAdapter = new OcrListAdapter(ocrListList, VinCompareResultActivity.this,1);
        recyclerView.setAdapter(ocrListAdapter);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                isVideoRecordStarted = false;
                startVideo.setVisibility(View.VISIBLE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        readyForNextImage();
                    }
                });
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (OcrListAdapter.OcrList ocrList : ocrListList) {
                    if (ocrList.isSelected() && !TextUtils.isEmpty(ocrList.getOcrText())) {
                        alertDialog.dismiss();
                       /* System.out.println("SelectedItem " + ocrList.getOcrText());
                        Intent data = new Intent();
                        // data.putExtra(BarcodeObject, best);
                        data.putExtra("scan_no", ocrList.getOcrText());
                        //setResult(CommonStatusCodes.SUCCESS, data);
                        setResult(RESULT_OK, data);
                        finish();
                        break;*/
                        byte[] byteImage = convertBitmapToByte(rgbFrameBitmap);
                        Imageitem imageitem = new Imageitem();
                        scannedResult = ocrList.getOcrText();
                        if (!isScanned) {
                            isScanned = true;
                            if (vinNumber.equalsIgnoreCase(ocrList.getOcrText())) {
                                imageitem.setStatus("PASS");
                                imageitem.setResultMsg(passMsg);
                                imageitem.setByteImage(byteImage);
                                resultTextView.setTextColor(Color.parseColor(passMsgColor));
                                resultTextView.setText(passMsg);
                            } else {
                                imageitem.setStatus("FAIL");
                                imageitem.setResultMsg(failMsg);
                                imageitem.setByteImage(byteImage);
                                resultTextView.setTextColor(Color.parseColor(failMsgColor));
                                resultTextView.setText(failMsg);
                            }
                            if (!isBeepSound) {
                                ring.start();
                                isBeepSound = true;
                            }
                            if (retakeConfirm) {
                                showRetake(imageitem);
                            } else {
                                processResponse(imageitem);
                            }
                        }
                    }
                }
            }
        });
        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onInferenceConfigurationChanged() {
        if (rgbFrameBitmap == null) {
            // Defer creation until we're getting camera frames.
            return;
        }
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
                // isSent = false;
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

    public void processResponse(Imageitem imageItem) {
        if (imageItem != null/* && !isDetect*/) {
            // try {
            if (modelResultData == null)
                modelResultData = new ArrayList<>();
            mondelImage = imageItem.getByteImage();
            modelResultData.add(new ParentResponseData.ModelResult(modelId, modelName, seqNo, modelVersion, AOI, imageItem.getStatus(),
                    imageItem.getResultMsg(), encryptImage(mondelImage), scannedResult));

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
            String lat = "";
            String lng = "";
            currentLocation = tracker.getLocation();
            if (currentLocation != null) {
                lat = String.valueOf(currentLocation.getLatitude());
                lng = String.valueOf(currentLocation.getLongitude());
            }
            parentResponseDataApi = new ParentResponseData(parentData.getMetaData()
                    , new ParentResponseData.ActivityResult(activityResult, activityResultMsg,
                    modelResultDataApi, lat, lng,
                    Utility.deviceDetail, String.valueOf(SDK_VERSION_CODE), Utility.getDeviceUDID(this)), parentData.getPrimaryData());
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
                Utility.exceptionLog(VinCompareResultActivity.this,
                        Utility.getLog("Parent application call back is null"),
                        PreferenceStorage.getInstance(this).getUserId(),
                        PreferenceStorage.getInstance(this).getSiteId(),
                        PreferenceStorage.getInstance(this).getActivityId(),
                        PreferenceStorage.getInstance(this).getModelId(),"2");
            }
            modelResultPresenter.modelResultAsyncTask(parentResponseDataApi);
            finish();
        }
    }

    void initViewAction() {
        startVideo = findViewById(R.id.startCapture);
        ivClose = findViewById(R.id.image_close);
        tvSingleAndMulti = findViewById(R.id.tvSingleAndMulti);
        ivCapturePrevieImageView = findViewById(R.id.ivCapturePreview);
        ivCapturePrevieImageView2 = findViewById(R.id.ivCapturePreview2);
        SwitchMaterial switchVideo = findViewById(R.id.video_switch);
        ImageView ivVideo = findViewById(R.id.ivVideo);
        resultTextView = findViewById(R.id.result);
        retakeCard = findViewById(R.id.card_view_retake);
        resultImageView = findViewById(R.id.ins_img);
        inspktLyt = findViewById(R.id.inspkt_lyt);
        retakeTextView = findViewById(R.id.txt_retake);
        rectangleOverLayBARCodeView = (RectangleOverLayBARCodeView) findViewById(R.id.brCodeOverlay);
        rectangleOverLayQRCodeView = (RectangleOverLayQRCodeView) findViewById(R.id.qrCodeOverLay);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        isVideoRecordStarted = false;
        startVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVideoRecordStarted = true;
                startVideo.setVisibility(View.GONE);
            }
        });

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startVideo.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                closeActivity();
            }
        });
        tvSingleAndMulti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSingleAndMulti) {
                    isSingleAndMulti = true;
                    tvSingleAndMulti.setText("Multi");
                } else {
                    isSingleAndMulti = false;
                    tvSingleAndMulti.setText("Single");
                }
            }
        });
        ivCapturePrevieImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSingleAndMulti) {
                    // Intent intent = new Intent(getApplicationContext(), V3FetchImagesListActivity.class);
                    //startActivity(intent);
                    // finish();
                }
            }
        });
       /* switchVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent intent = new Intent(CaptureActivity.this, CustomRecordActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });*/
       /* ivVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CaptureActivity.this, CustomRecordActivity.class);
                startActivity(intent);
                finish();
            }
        });*/

    }

    private void closeActivity() {
       /* if(PreferenceStorage.getInstance(getApplicationContext()).getVinNeed()==1) {
            Intent intent = new Intent(CaptureActivity.this, VinNumberActivity.class);
           // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }*/
        finish();

    }

    public static byte[] convertBitmapToByte(Bitmap image) {
        Bitmap bmp = image;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public static String encryptImage(byte[] imageArray) {
        String s = Base64.encodeToString(imageArray, Base64.NO_WRAP).trim();
        return s;
    }

    /*public void processResponse(Imageitem imageItem) {

    }*/

    @Override
    public synchronized void onResume() {
        super.onResume();
        // EventBus.getDefault().register(this);
        initViewAction();
        // multipleImageUploadModelList.clear();
    }

    @Override
    public void onBackPressed() {
        closeActivity();
    }

   /* public void saveFileLocal() {
        class SaveImage extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                TrainModel trainModel = new TrainModel();
                trainModel.setImagePATH(file.getAbsolutePath());
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .dao()
                        .insert(trainModel);
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Bundle bundle = new Bundle();
                bundle.putString("file", file + "");
                if (isSingleAndMulti) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ivCapturePrevieImageView.setVisibility(View.VISIBLE);
                            ivCapturePrevieImageView.setImageBitmap(myBitmap);
                            isVideoRecordStarted = false;
                            startVideo.setVisibility(View.VISIBLE);
                            readyForNextImage();
                            //callMultipleImageUploadAPI();
                            callingAPI();
                        }
                    });
                } else {
                    ivCapturePrevieImageView.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(CaptureActivity.this, PreviewImageActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }
            }
        }
        SaveImage saveImage = new SaveImage();
        saveImage.execute();
    }

    public void fetchImagesDataOnLocal() {
        class FetchImages extends AsyncTask<Void, Void, List<TrainModel>> {

            @Override
            protected List<TrainModel> doInBackground(Void... voids) {
                List<TrainModel> trainModelList = DatabaseClient.getInstance(getApplicationContext())
                        .getAppDatabase().dao().getAllCourses();
                return trainModelList;
            }

            @Override
            protected void onPostExecute(List<TrainModel> trainModels) {
                super.onPostExecute(trainModels);
                for (TrainModel trainModel : trainModels) {
                    File file = new File(trainModel.getImagePATH());
                    if (file.exists()) {
                        file.delete();
                    }
                    deleteImagesFromLocal(trainModel);
                }
            }
        }
        FetchImages fetchImages = new FetchImages();
        fetchImages.execute();
    }

    public void deleteImagesFromLocal(TrainModel trainModel) {
        class DeleteImages extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .dao()
                        .delete(trainModel);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
            }
        }
        DeleteImages deleteImages = new DeleteImages();
        deleteImages.execute();
    }*/

   /* public void callMultipleImageUploadAPI(int position, String imagePath) {
        String base64Image = base64Image(imagePath);
        call = ApiClient.getInstance().getApi().getMultipleImageUpload(
                StrConstants.API_VALUE, String.valueOf(V3PassAndFailActivity.modelID), base64Image, SharedPreferenceManager.getUserid(getApplicationContext()));
        call.enqueue(new Callback<MultipleImageModelResponce>() {
            @Override
            public void onResponse(Call<MultipleImageModelResponce> call, Response<MultipleImageModelResponce> response) {
                int statusCode = response.code();
                if (statusCode == 200) {
                    if (Integer.parseInt(response.body().getResponseCode()) == 200) {
                        if (response.body().getMessage().getStatusCode() == 200) {
                            multipleImageUploadModelList.get(position).setUploaded(true);
                            multipleImageUploadModelList.get(position).setMlModelImageID(response.body().getMessage().getGetMlModelImageId() + "");
                            File file = new File(imagePath);
                            if (file.exists()) {
                                file.delete();
                            }
                            isUploading = false;
                            isCallingAPI = false;
                            //mHandler.postDelayed(runnable, 1000);
                            callingAPI();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<MultipleImageModelResponce> call, Throwable t) {
                if (!call.isCanceled()) {
                    isUploading = false;
                    isCallingAPI = false;
                    callingAPI();
                }
                //mHandler.postDelayed(runnable, 1000);
            }
        });
    }*/

    @Override
    public synchronized void onDestroy() {
        mHandler.removeCallbacks(runnable);
        tracker.stopUsingGPS();
        super.onDestroy();
      /*  if (call != null) {
            call.cancel();
        }*/
    }

    public String base64Image(String filePath) {
        Bitmap bm = BitmapFactory.decodeFile(filePath);
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, bOut);
        String base64Image = Base64.encodeToString(bOut.toByteArray(), Base64.DEFAULT);
        return base64Image;
    }

   /* public void callingAPI() {
        if (!isCallingAPI) {
            for (int i = 0; i < multipleImageUploadModelList.size(); i++) {
                if (!multipleImageUploadModelList.get(i).isUploaded()) {
                    isCallingAPI = true;
                    imagePath = multipleImageUploadModelList.get(i).getLocalImagePath();
                    imagePosition = i + "";
                    break;
                }
            }
            if (isCallingAPI) {
                //btnAdd.setEnabled(false);
                // btnAdd.setBackground();
                //btnAdd.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.activity_title_color));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        File file = new File(imagePath);
                        if (file.exists()) {
                            callMultipleImageUploadAPI(Integer.parseInt(imagePosition), imagePath);
                        }
                    }
                });
            }
        }
    }*/

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(12);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public String saveImageInLocalStorage() {
        String filePath = null;
        File externalFilesDir = getExternalFilesDir(null);
        String dir = null;
        if (null != externalFilesDir) {
            dir = externalFilesDir.getAbsolutePath();
        }
        String packageName = getPackageName();
        if (!TextUtils.isEmpty(dir)) {
            if (!dir.endsWith(File.separator)) {
                filePath = dir + File.separator;
            } else {
                filePath = dir + packageName + File.separator;
            }
        }
        return filePath;

    }

    private void performCrop(String picUri) {
        try {
            //Start Crop Activity

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            File f = new File(picUri);
            Uri contentUri = Uri.fromFile(f);

            cropIntent.setDataAndType(contentUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 280);
            cropIntent.putExtra("outputY", 280);

            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, RESULT_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CROP && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap selectedBitmap = extras.getParcelable("data");
            Intent intent = new Intent(VinCompareResultActivity.this, PreviewImageActivity.class);
            intent.putExtra("cropimage", selectedBitmap);
            startActivity(intent);
            finish();
        }
    }

    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_bar_code2) {// mTextView.setText("");
                /*if (mCameraSource != null) {
                    isBarcodeScanner = false;
                    mGraphicOverlay = (GraphicOverlay<BarcodeGraphic>) findViewById(R.id.graphicOverlay);
                    mPreview.stop();
                    mCameraSource.stop();
                    mCameraSource = null;
                    mPreview = null;
                    mPreview = (CameraSourcePreview) findViewById(R.id.preview);
                    createCameraSource(autoFocus, useFlash);
                    startCameraSource();
                }*/
                rectangleOverLayQRCodeView.setVisibility(View.GONE);
                rectangleOverLayBARCodeView.setVisibility(View.VISIBLE);
                selectedItem = 1;
                return true;
                /*case R.id.navigation_qr:
                   // mTextView.setText("");
                    if (mCameraSource != null) {
                        qrCodeDetection();
                    }
                    return true;*/
            } else if (itemId == R.id.navigation_ocr2) {// mTextView.setText("");
             /*   if (mCameraSource != null) {
                    isBarcodeScanner = true;
                    mGraphicOverlayOCR = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);
                    mPreview.stop();
                    mCameraSource.stop();
                    mCameraSource = null;
                    mPreview = null;
                    mPreview = (CameraSourcePreview) findViewById(R.id.preview);
                    ocrCameraSource(autoFocus, useFlash);
                    startCameraSource();
                }*/
                rectangleOverLayQRCodeView.setVisibility(View.GONE);
                rectangleOverLayBARCodeView.setVisibility(View.VISIBLE);
                selectedItem = 3;
                return true;
            } else if (itemId == R.id.navigation_qr2) {
                rectangleOverLayBARCodeView.setVisibility(View.GONE);
                rectangleOverLayQRCodeView.setVisibility(View.VISIBLE);
                selectedItem = 2;
                return true;
            }
            return false;
        }
    };

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
    public synchronized void onPause() {
        super.onPause();
        finish();
    }
}
