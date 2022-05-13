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

package com.v_chek_host.vcheckhostsdk.ui;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

import static com.v_chek_host.vcheckhostsdk.BuildConfig.SDK_VERSION_CODE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.v_chek_host.vcheckhostsdk.Details;
import com.v_chek_host.vcheckhostsdk.ModelCheckActivity;
import com.v_chek_host.vcheckhostsdk.VinNumberActivity;
import com.v_chek_host.vcheckhostsdk.appinterface.Detector;
import com.v_chek_host.vcheckhostsdk.R;
import com.v_chek_host.vcheckhostsdk.customview.Classifier;
import com.v_chek_host.vcheckhostsdk.model.api.ApiService;
import com.v_chek_host.vcheckhostsdk.model.entity.ApiInspktResult;
import com.v_chek_host.vcheckhostsdk.model.entity.Imageitem;
import com.v_chek_host.vcheckhostsdk.model.entity.InspktResult;
import com.v_chek_host.vcheckhostsdk.model.entity.ParentMetaData;
import com.v_chek_host.vcheckhostsdk.model.entity.ParentResponseData;
import com.v_chek_host.vcheckhostsdk.model.entity.Result;
import com.v_chek_host.vcheckhostsdk.model.entity.SendImage;
import com.v_chek_host.vcheckhostsdk.presenter.contract.IModelResultPresenter;
import com.v_chek_host.vcheckhostsdk.presenter.implement.ModelResultPresenter;
import com.v_chek_host.vcheckhostsdk.utils.PreferenceStorage;
import com.v_chek_host.vcheckhostsdk.utils.TFLiteObjectDetectionAPIModel;
import com.v_chek_host.vcheckhostsdk.customview.OverlayView;
import com.v_chek_host.vcheckhostsdk.env.BorderedText;
import com.v_chek_host.vcheckhostsdk.env.ImageUtils;
import com.v_chek_host.vcheckhostsdk.env.Logger;
import com.v_chek_host.vcheckhostsdk.tracking.MultiBoxTracker;
import com.v_chek_host.vcheckhostsdk.utils.Utility;
import com.v_chek_host.vcheckhostsdk.utils.bean.StepBean;
import com.v_chek_host.vcheckhostsdk.view.IModelResultView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends ODCameraActivity implements OnImageAvailableListener, IModelResultView {
    private static final Logger LOGGER = new Logger();

    // Configuration values for the prepackaged SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 320;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "model.tflite";
    private static final String TF_OD_API_LABELS_FILE = "labels.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final boolean MAINTAIN_ASPECT = false;
    //private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    //private static  Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    //private static  Size DESIRED_PREVIEW_SIZE = new Size(1024, 768);
    //private static  Size DESIRED_PREVIEW_SIZE = new Size(1600, 1200);
    private static Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    //private static  Size DESIRED_PREVIEW_SIZE = new Size(2340, 1080);
    // private static  Size DESIRED_PREVIEW_SIZE = new Size(960, 540);
    //private static  Size DESIRED_PREVIEW_SIZE = new Size(1024, 576);
    //private static  Size DESIRED_PREVIEW_SIZE = new Size(2560, 1920);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;

    private Detector detector;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private BorderedText borderedText;

    protected static ImageView startCapture;
    protected static ImageView ivClose;
    public static boolean isCaptureStarted = false;
    private Boolean pauseCall = false;
    TextView focusAlert;

    private static String activityModelString;
    private Gson gson;
    private static List<Result.ModelData> modelData;
    private static List<ParentResponseData.ModelResult> modelResultData = new ArrayList<>();
    private boolean dataBool;
    private boolean seqBool;

    public String modelId;
    public String modelName;
    public String modelVersion;
    public String AOI;
    private int width;
    private int height;
    private int bottomCropheight;
    private int cropX;
    private int cropY;
    private int cropWidth;
    private int cropHeight;
    private int pcropX;
    private int pcropY;
    private int pcropWidth;
    private int pcropHeight;
    private boolean previewBool;
    private boolean previewScan;
    private boolean retakeConfirm;
    private boolean retakePassConfirm;
    private boolean retakeFailConfirm;
    private String previewLable;
    private String reTakeMsg;

    private String passMsg;
    private String passMsgColor;
    private String failMsg;
    private String failMsgColor;
    private String negativeLable;
    private String negativeMsg;
    private String negativeMsgColor;
    private int previewConfidence;
    private int passConfidence;
    private int apiPassConfidence;
    private int failConfidence;
    private int apiFailConfidence;
    private int negativeConfidence;
    private int apiNegativeConfidence;
    private int seqCount;
    private String seqNo;
    private static String modelFileName;
    private static String modelOverlayUrl;
    private static String serverModelUrl;
    private static String apiPredictionKey;
    private static List<String> modelLableName;
    private static List<StepBean> stepsBeanList = null;
    private String stepString;
    private static List<ParentResponseData.ModelResult> modelResultDataApi = new ArrayList<>();
    private static ParentResponseData parentResponseData;
    private static ParentResponseData parentResponseDataApi;
    private static ParentMetaData parentData;
    private List<StepBean> stepData;
    private MediaPlayer ring;
    private List<String> passLable = new ArrayList<>();
    private List<String> failLable = new ArrayList<>();
    private int waitTime = 0;
    private boolean cropBool;
    private boolean previewCropBool;
    private boolean serverModelCheck;
    private boolean serverSecondaryCheck;
    private String apiDisplayMsg;
    private List<Result.InRules> lstInRules = null;
    private List<Result.OutRules> lstOutRules = null;
    private Result.ValidationRules validationRules;
    private boolean isfailHandlerCalled;

    private static Imageitem imageitem;
    boolean isSent = false;
    boolean isBeepSound = false;
    private Boolean reTakeBool = false;
    private boolean passCallBool;
    public boolean isDetect = false;
    public byte[] mondelImage = null;
    private String modelResultString = null;
    private String modelString = null;
    public String vinNumber = "";
    private String ParentMetaDataStr = null;
    private String activityResultString = null;
    private IModelResultPresenter modelResultPresenter;
    int screenWidth = 0;
    int screenHeight = 0;

    int inputSize = 0;

    Bundle bundle;
    boolean inFlag = false;
    private static Bitmap previewCropBitmap = null;
    private static Bitmap cropBitmap = null;
    String strFocusAlert = "";
    private Handler failHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
        }

        bundle = getIntent().getExtras();
        ring = MediaPlayer.create(this, R.raw.camera_shutter);
        if (bundle != null) {
            vinNumber = bundle.getString(getString(R.string.key_vin_no));
        }
        activityModelString = PreferenceStorage.getInstance(this).getActivityModel();
        modelResultPresenter = new ModelResultPresenter(this, this);
        stepString = PreferenceStorage.getInstance(this).getStep();
        gson = new Gson();
        Type modelType = new TypeToken<ArrayList<Result.ModelData>>() {
        }.getType();
        modelData = gson.fromJson(activityModelString, modelType);
        gson = new Gson();
        Type stepType = new TypeToken<ArrayList<StepBean>>() {
        }.getType();
        stepData = gson.fromJson(stepString, stepType);
        gson = new Gson();
        Type modelResultType = new TypeToken<ArrayList<ParentResponseData.ModelResult>>() {
        }.getType();
        modelResultData = gson.fromJson(PreferenceStorage.getInstance(this).getModelResult(), modelResultType);

        for (int i = 0; i < modelData.size(); i++) {
            if (!modelData.get(i).isCheckStatus() && !dataBool) {
                modelId = modelData.get(i).getModelId();
                modelName = modelData.get(i).getModelName();
                modelVersion = modelData.get(i).getModelVersion();
                AOI = modelData.get(i).getModelMark();
                width = Integer.parseInt(modelData.get(i).getPreviewWidth());
                PreferenceStorage.getInstance(this).setPreviewWidth(width);
                height = Integer.parseInt(modelData.get(i).getPreviewHeight());
                PreferenceStorage.getInstance(this).setPreviewHeight(height);
                bottomCropheight = modelData.get(i).getBottomCropHeight();
                cropX = Integer.parseInt(modelData.get(i).getCropXAxis());
                cropY = Integer.parseInt(modelData.get(i).getCropYAxis());
                cropWidth = Integer.parseInt(modelData.get(i).getCropWidth());
                cropHeight = Integer.parseInt(modelData.get(i).getCropHeight());
                waitTime = modelData.get(i).getWaitTime();
              /*  if(i==0) {
                    cropX = 320;
                    cropY = 0;
                    cropWidth = 280;
                    cropHeight = 200;
                    cropX = 320;
                    cropY = 50;
                    cropWidth = 280;
                    cropHeight = 200;
                }else {
                    cropX =280 ;
                    cropY = 50;
                    cropWidth = 100;
                    cropHeight = 50;
                }*/
                pcropX = modelData.get(i).getpCropX();
                pcropY = modelData.get(i).getpCropY();
                pcropWidth = modelData.get(i).getpCropWidth();
                pcropHeight = modelData.get(i).getpCropHeight();
                previewLable = modelData.get(i).getPreviewLable();
                previewConfidence = Integer.parseInt(modelData.get(i).getPreviewConfidence());
                passLable = modelData.get(i).getPassLabel();
                passMsg = modelData.get(i).getPassMsg();
                passMsgColor = modelData.get(i).getPassMsgColor();
                passConfidence = Integer.parseInt(modelData.get(i).getPassConfidence());
                //passConfidence = 30;
                failLable = modelData.get(i).getFailLable();
                failMsg = modelData.get(i).getFailMsg();
                failMsgColor = modelData.get(i).getFailMsgColor();
                failConfidence = Integer.parseInt(modelData.get(i).getFailConficdence());
                apiPassConfidence = modelData.get(i).getApiPassConfidence();
                apiFailConfidence = modelData.get(i).getApiFailConfidence();
                apiNegativeConfidence = modelData.get(i).getApiNegativeConfidence();
                // colorLable = modelData.get(i).getColorName();
                // failConfidence = 30;
                negativeLable = modelData.get(i).getNegativeLabel();
                negativeMsgColor = modelData.get(i).getNegativeMsgColor();
                negativeMsg = modelData.get(i).getNegativeMsg();
                negativeConfidence = Integer.parseInt(modelData.get(i).getNegativeConfidence());
                previewBool = (Integer.parseInt(modelData.get(i).getModelPreview()) > 0) ? true : false;
                cropBool = (Integer.parseInt(modelData.get(i).getCropBool()) > 0) ? true : false;
                previewCropBool = (modelData.get(i).getPreviewCropBool() > 0) ? true : false;
                serverModelCheck = (modelData.get(i).getModelScope() > 0) ? true : false;
                serverSecondaryCheck = (modelData.get(i).getModelSecondaryChek() > 0) ? true : false;
                retakeConfirm = (modelData.get(i).getRetake() > 0) ? true : false;
                retakePassConfirm = (modelData.get(i).getRetakePass() > 0) ? true : false;
                retakeFailConfirm = (modelData.get(i).getRetakeFail() > 0) ? true : false;
                reTakeMsg = modelData.get(i).getReTakeMsg();
                serverModelUrl = modelData.get(i).getServerModelUrl();
                apiPredictionKey = modelData.get(i).getApiPredictionKey();
                seqNo = modelData.get(i).getSeqNo();
                modelFileName = modelData.get(i).getModelFileName();
                modelLableName = modelData.get(i).getModelLable();
                modelOverlayUrl = modelData.get(i).getModelOverlayUrl();
                apiDisplayMsg = modelData.get(i).getDisplayMessage();
                validationRules = modelData.get(i).getValidationRules();
                lstInRules = validationRules.getInRulesDdata();
                lstOutRules = validationRules.getOutRulesDdata();
                strFocusAlert = modelData.get(i).getValidationRulesMessage();
                seqCount = i;
                dataBool = true;
            } else if (!modelData.get(i).isCheckStatus()) {
                seqBool = true;
            }
        }
        failHandler = new Handler(getMainLooper());
        try {
            CaocConfig.Builder.create()
                    .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                    .enabled(true) //default: true
                    //.showErrorDetails(false) //default: true
                    .showRestartButton(false) //default: true
                    //.trackActivities(false) //default: false
                    .minTimeBetweenCrashesMs(1) //default: 3000
                    .errorActivity(VchekErrorActivity.class)
                    //.restartActivity(VinNumberActivity.class)
                    .showErrorDetails(false)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {

        startCapture = findViewById(R.id.startCapture);
        ivClose = findViewById(R.id.image_close);
        focusAlert = findViewById(R.id.focus_alert);
        isCaptureStarted = false;
        startCapture.setVisibility(View.GONE);
        startCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCaptureStarted = true;
                startCapture.setVisibility(View.GONE);
            }
        });

        focusAlert.setVisibility(View.GONE);
        //focusAlert.setText(strFocusAlert);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pauseCall = true;
                //closeActivity();
            }
        });

        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(this);

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            this,
                            modelFileName,
                            modelLableName,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing Detector!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Detector could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);


        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

        //for rotation
   /* Matrix matrix = new Matrix();
    matrix.postRotate(30);
    Bitmap rotated = Bitmap.createBitmap(rgbFrameBitmap, 0, 0, rgbFrameBitmap.getWidth(), rgbFrameBitmap.getHeight(),
            matrix, true);*/

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                        if (isDebug()) {
                            tracker.drawDebug(canvas);
                        }
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }

    @Override
    protected void processImage() {
    /*if (!isSent){
      resultTextViewOD.setText("");*/
      /*final Paint paint = new Paint();
      paint.setColor(Color.RED);*/
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        LOGGER.i("Running detection on image " + currTimestamp);
                        //RND
                        Display display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        float density = getResources().getDisplayMetrics().density;


                        if (getResources().getInteger(R.integer.size) == 5) {

                            if (size.y == 720) {
                                if (size.x == 1344/* || size.x == 1184*/) {
                                    inputSize = 360;
                                } else {
                                    inputSize = 400;
                                }
                            } else if (size.y == 480) {
                                System.out.println("480");
                            } else if (size.y == 1080) {
                                inputSize = 320;
                            } else {
                                inputSize = 320;
                            }

                        } else if (getResources().getInteger(R.integer.size) == 7) {
                            screenWidth = (size.x - ((int) (density * 100)));
                            screenHeight = (size.x - ((int) (density * 100)));
                        } else if (getResources().getInteger(R.integer.size) == 10) {
                            screenWidth = (size.x - ((int) (density * 100)));
                            screenHeight = (size.x - ((int) (density * 100)));
                        }
                        //end

                        final long startTime = SystemClock.uptimeMillis();
                        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                        final List<Detector.Recognition> results = detector.recognizeImage(croppedBitmap, inputSize);
                           /* if (cropBool) {
                                cropBitmap = Classifier.cropedImage(rgbFrameBitmap, width, height, cropX, cropY, cropWidth, cropHeight);
                            } else {
                                cropBitmap = rgbFrameBitmap;
                            }*/
                        LOGGER.v("Detect: %s", results);

                        cropCopyBitmap = Bitmap.createBitmap(rgbFrameBitmap);
                        final Canvas canvas = new Canvas(cropCopyBitmap);
                        //   canvas.drawColor(Color.GREEN);
                        final Paint paint = new Paint();
                        //paint.setColor(Color.GREEN);
                        paint.setStyle(Style.STROKE);
                        paint.setStrokeWidth(2.0f);

                        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                        switch (MODE) {
                            case TF_OD_API:
                                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                                break;
                        }

                        final List<Detector.Recognition> mappedRecognitions =
                                new ArrayList<Detector.Recognition>();

                        for (final Detector.Recognition result : results) {
                            final RectF location = result.getLocation();
                            if (location != null && result.getConfidence() >= minimumConfidence) {
                                if (modelName.equalsIgnoreCase("Parking Gear Check")) {
                                    if (result.getTitle().equalsIgnoreCase("GearNamesBoard") ||
                                            result.getTitle().equalsIgnoreCase("GearShifter"))
                                    /*if (result.getTitle().equalsIgnoreCase("GearSet"))*/{
                                        canvas.drawRect(location, paint);
                                        cropToFrameTransform.mapRect(location);
                                        result.setLocation(location);
                                        mappedRecognitions.add(result);
                                    }
                                }else {
                                    canvas.drawRect(location, paint);
                                    cropToFrameTransform.mapRect(location);
                                    result.setLocation(location);
                                    mappedRecognitions.add(result);
                                }
                              }
                            }

                        //my Changes
                        String objectName = "";

                        for (int x = 0; x < lstInRules.size(); x++) {
                            if (lstInRules.get(x).getCrop().equals("1")) {
                                objectName = lstInRules.get(x).getObjectName();
                                break;
                            }
                        }

                       /* if (lstInRules.size()==0 && lstOutRules.size()==0){
                           if (objectName.isEmpty()){
                               if (results.size()==0){

                               }
                           }
                        }*/
                        int left = 0, top = 0, right = 0, bottom = 0;
                        int xOffset=0, yOffset=0, boxWidth=0, boxHight=0;

                        if (!objectName.equals("")) {
                            if (results.size() > 0) {
                                boolean flagObjectWithCropExist = false;
                                for (int y = 0; y < results.size(); y++) {
                                    if (results.get(y).getTitle().equals(objectName)) {
                                        flagObjectWithCropExist = true;
                                        RectF loc = results.get(y).getLocation();
                                        left = (int) (loc.left);
                                        top = (int) loc.top;
                                        right = (int) loc.right;
                                        bottom = (int) loc.bottom;

                                        double leftVal = loc.left/4.25;
                                        double leftVal1 = leftVal/inputSize;
                                        double leftVal2 = leftVal1*320;
                                        double leftVal3 = leftVal2*4.25;
                                         left = (int)leftVal3;

                                        double rightVal = loc.right/4.25;
                                        double rightVal1 = rightVal/inputSize;
                                        double rightVal2 = rightVal1*320;
                                        double rightVal3 = rightVal2*4.25;
                                        right = (int)rightVal3;

                                        double topVal = loc.top/3;
                                        double topVal1 = topVal/inputSize;
                                        double topVal2 = topVal1*320;
                                        double topVal3 = topVal2*3;
                                        top = (int)topVal3;

                                        double bottomVal = loc.bottom/3;
                                        double bottomVal1 = bottomVal/inputSize;
                                        double bottomVal2 = bottomVal1*320;
                                        double bottomVal3 = bottomVal2*3;
                                        bottom = (int)bottomVal3;

                                        break;
                                    }
                                }
                                //newCrop
                                if (flagObjectWithCropExist) {
                                    xOffset = left;
                                    yOffset = top;
                                    boxWidth = right - left;
                                    boxHight = bottom - top;
                                    //cropBitmap = Classifier.cropedImage(rgbFrameBitmap, width, height, cropX, cropY, cropWidth, cropHeight);
                                    if(xOffset >= 0 && yOffset >=0) {
                                        if((xOffset+boxWidth<rgbFrameBitmap.getWidth()) && yOffset+boxHight<rgbFrameBitmap.getHeight()) {
                                            cropBitmap = Classifier.cropedImage(rgbFrameBitmap, rgbFrameBitmap.getWidth(),
                                                    rgbFrameBitmap.getHeight(), xOffset, yOffset, boxWidth, boxHight);
                                        }
                                    }
                                }else{
                                    cropBitmap = Classifier.cropedImage(rgbFrameBitmap, 640, 480, 0, 0, 640, 480);

                                    //cropBitmap = rgbFrameBitmap;
                                }

                            }
                        } else {
                            //cropBitmap = rgbFrameBitmap;
                            cropBitmap = Classifier.cropedImage(rgbFrameBitmap, 640, 480, 0, 0, 640, 480);
                        }


                        //check outRules
                        boolean outFlag = false;
                        for (int a = 0; a < lstOutRules.size(); a++) {
                            for (int i = 0; i < results.size(); i++) {
                                if (lstOutRules.get(a).getObjectName().equals(results.get(i).getTitle())) {
                                    outFlag = true;
                                    startCapture.setVisibility(View.GONE);
                                    break;
                                }
                            }
                            if (outFlag) {
                                break;
                            }
                        }
                        //checkInRules
                        inFlag = false;
                        if (!outFlag) {
                            //green color canvas
                            tracker.trackResults(mappedRecognitions, currTimestamp, "green");
                            trackingOverlay.postInvalidate();
                            computingDetection = false;
                            //

                            for (int a = 0; a < lstInRules.size(); a++) {
                                inFlag = false;
                                for (int i = 0; i < results.size(); i++) {
                                    if (lstInRules.get(a).getObjectName().equals(results.get(i).getTitle())) {
                                        inFlag = true;
                                    }
                                }
                                if (!inFlag) {
                                    break;
                                }
                            }
                        } else {//Show error Message
                            // focusAlert.setVisibility(View.VISIBLE);
                            //red color canvas
                            tracker.trackResults(mappedRecognitions, currTimestamp, "red");
                            trackingOverlay.postInvalidate();
                            computingDetection = false;
                            //
                        }

                      
                        if (serverModelCheck) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                 //   inspktImage.setImageBitmap(cropBitmap);
                                    if (inFlag && !isSent) {
                                        focusAlert.setVisibility(View.GONE);
                                    }
                                }
                            });

                            if (inFlag && !isSent) {
                                //focusAlert.setVisibility(View.GONE);
                                ObImage(rgbFrameBitmap, cropBitmap);
                                //startCapture.setVisibility(View.VISIBLE);
                            }else{
                              if (lstInRules.size()==0 && lstOutRules.size()==0){
                                  if (results.size()>0){
                                      for(int i=0; i<results.size();i++){
                                          if (results.get(i).getTitle().contains("Prop65_Label") || (results.get(i).getTitle().contains("Prop65_LargeText"))){
                                              ObImage(rgbFrameBitmap, cropBitmap);
                                          }else {
                                              Toast.makeText(DetectorActivity.this, strFocusAlert, Toast.LENGTH_SHORT).show();
                                          }
                                      }
                                  }
                              }
                            }
                            //my changes
/*
                             else {
                                chek(results, cropBitmap, rgbFrameBitmap);
                            }
*/
                        } else {
                            chek(results, cropBitmap, rgbFrameBitmap);
                        }

                    }
                });
        // }
    }

    public void chek(List<Detector.Recognition> results, final Bitmap bitmap, final Bitmap rgbImage) {
        runOnUiThread(
                new Runnable() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void run() {
                        if (inFlag && !isSent) {
                            int confident;
                            final float confidence = results.get(0).getConfidence();
                            final String label = results.get(0).getTitle();
                            String strConf = String.format("%.2f", confidence * 100f);
                            float fConf = (Float.parseFloat(strConf) * 100);
                            if (passLable.contains(label))
                                confident = passConfidence;
                            else if (failLable.contains(label))
                                confident = failConfidence;
                            else
                                confident = negativeConfidence;

                            if ((fConf) > confident) {
                                if (!label.equals(negativeLable)) {
                                    imageitem = new Imageitem();
                                    if (passLable.contains(label)) {

                                        if (serverSecondaryCheck)
                                            PostImage(rgbImage, bitmap);
                                        else {
                                            passCallBool = true;
                                            imageitem.setCarViewName(label);
                                            imageitem.setConfidence(fConf);
                                            // imageitem.setByteImage(convertBitmapToByte(rgbFrameBitmap));
                                            if (cropBool)
                                                imageitem.setByteCropImage(convertBitmapToByte(bitmap));

                                            imageitem.setByteImage(convertBitmapToByte(Classifier.cropedImage(rgbImage, width, height, 0, 0, width, (height - bottomCropheight))));
                                            imageitem.setStatus("PASS");
                                            imageitem.setResultMsg(passMsg);
                                            resultTextViewOD.setTextColor(Color.parseColor(passMsgColor));
                                            resultTextViewOD.setText(passMsg);
                                            postInspkt(imageitem);
                                        }
                                       /* if(!colorCallBool) {
                                            postInspkt(imageitem);
                                        }else {
                                            if(getColorString(bitmap).equalsIgnoreCase("Blue")){
                                                postInspkt(imageitem);
                                            }else {
                                                postInspkt(imageitem);
                                            }
                                        }*/
                                    } else if (failLable.contains(label)) {
                                        String failLable = label;
                                        float failConf = fConf;
                                        byte[] failByteImage = convertBitmapToByte(Classifier.cropedImage(rgbImage, width, height, 0, 0, width, (height - bottomCropheight)));
                                        if (!isfailHandlerCalled) {
                                            failHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (!passCallBool) {

                                                        if (serverSecondaryCheck)
                                                            PostImage(rgbImage, bitmap);
                                                        else {
                                                            imageitem.setCarViewName(failLable);
                                                            imageitem.setConfidence(failConf);
                                                            if (cropBool)
                                                                imageitem.setByteCropImage(convertBitmapToByte(bitmap));

                                                            imageitem.setByteImage(failByteImage);

                                                             // imageitem.setBitmap(rgbFrameBitmap);
                                                            imageitem.setStatus("FAIL");
                                                            imageitem.setResultMsg(failMsg);
                                                            resultTextViewOD.setTextColor(Color.parseColor(failMsgColor));
                                                            resultTextViewOD.setText(failMsg);
                                                            postInspkt(imageitem);
                                                        }
                                                        isfailHandlerCalled = false;
                                                    }
                                                }
                                            }, waitTime);
                                            isfailHandlerCalled = true;
                                        }
                                    }
                                } else {
                                    resultTextViewOD.setTextColor(Color.parseColor(negativeMsgColor));
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        resultTextViewOD.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
                                    }
                                    resultTextViewOD.setText(negativeMsg);
                                }
                            }
                            // resultTextView.setText(getColorString(bitmap) +" ---- color");
                            // resultTextView.setText(fConf +" ---- "+label);
                          /*  resultTextView.setText(String.format("%.2f", results.get(0).getConfidence() * 100f)+"----"+
                                    results.get(0).getTitle()+"\n"+
                                    String.format("%.2f", results.get(1).getConfidence() * 100f)+"----"+
                                    results.get(1).getTitle()+"\n"+
                                    String.format("%.2f", results.get(2).getConfidence() * 100f)+"----"+
                                    results.get(2).getTitle()+"\n");*/
                        } else {
                          /*  resultTextView.setText("");
                            if(reTakeBool)
                                startVideo.setVisibility(View.VISIBLE);*/
                        }
                        inspktImage.setImageBitmap(bitmap);
                    }
                });
    }


    public void PostImage(final Bitmap rgbBitmap, final Bitmap bitmap) {
        isSent = true;
        byte[] byteImage = convertBitmapToByte(bitmap);
        byte[] srcByteImage = convertBitmapToByte(Classifier.cropedImage(rgbBitmap, width, height, 0, 0, width, (height - bottomCropheight)));

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);


        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        RequestBody requestBody = RequestBody
                .create(MediaType.parse("application/octet-stream"), byteImage);
        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(serverModelUrl)
                .build();
        ApiService service = retrofit.create(ApiService.class);

        Call<InspktResult> call = service.inspktImage(apiPredictionKey, requestBody);

        call.enqueue(new Callback<InspktResult>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<InspktResult> call, Response<InspktResult> response) {
                if (response.isSuccessful()) {
                    List<InspktResult.InspectResult> inspktResults;
                    //   if(response.body().getStatusCode()==200){
                    System.out.println("inspection result" + response.body().getInspectResult());
                    inspktResults = response.body().getInspectResult();
                    int confident;
                    final float confidence = inspktResults.get(0).getConfidence();
                    final String label = inspktResults.get(0).getLabel();
                    String strConf = String.format("%.2f", confidence * 100f);
                    float fConf = (Float.parseFloat(strConf));

                    if (passLable.contains(label))
                        confident = apiPassConfidence;
                    else if (failLable.contains(label))
                        confident = apiFailConfidence;
                    else
                        confident = apiNegativeConfidence;

                    if ((fConf) > confident) {
                        if (!label.equals(negativeLable)) {
                            imageitem = new Imageitem();
                            if (passLable.contains(label)) {
                                passCallBool = true;
                                imageitem.setCarViewName(label);
                                imageitem.setConfidence(fConf);
                                // imageitem.setByteImage(convertBitmapToByte(rgbFrameBitmap));
                                if (cropBool)
                                    imageitem.setByteCropImage(byteImage);

                                imageitem.setByteImage(srcByteImage);
                                imageitem.setStatus("PASS");
                                imageitem.setResultMsg(passMsg);
                                resultTextViewOD.setTextColor(Color.parseColor(passMsgColor));
                                resultTextViewOD.setText(passMsg);
                                postInspkt(imageitem);

                            } else if (failLable.contains(label)) {
                                String failLable = label;
                                float failConf = fConf;
                                //  byte[] failByteImage = convertBitmapToByte(Classifier.cropedImage(rgbBitmap, width, height, 0, 0, width, (height - bottomCropheight)));
                                //  if(!isfailHandlerCalled) {
                                        /*failHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {*/
                                //    if (!passCallBool) {
                                imageitem.setCarViewName(failLable);
                                imageitem.setConfidence(failConf);
                                if (cropBool)
                                    imageitem.setByteCropImage(byteImage);

                                imageitem.setByteImage(srcByteImage);

                                // imageitem.setBitmap(rgbFrameBitmap);
                                imageitem.setStatus("FAIL");
                                imageitem.setResultMsg(failMsg);
                                resultTextViewOD.setTextColor(Color.parseColor(failMsgColor));
                                resultTextViewOD.setText(failMsg);
                                postInspkt(imageitem);

                                //       }
                                //  }
                                       /* }, waitTime);
                                        isfailHandlerCalled = true;*/
                                //   }
                            }
                        } else {
                            resultTextViewOD.setTextColor(Color.parseColor(negativeMsgColor));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                resultTextViewOD.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
                            }
                            resultTextViewOD.setText(negativeMsg);
                            isSent = false;
                        }
                    } else {
                        isSent = false;
                    }
                 /*   resultTextView.setText(String.format("%.2f", inspktResults.get(0).getConfidence() * 100f)+"----"+
                            inspktResults.get(0).getLabel()+"\n"+
                            String.format("%.2f", inspktResults.get(1).getConfidence() * 100f)+"----"+
                            inspktResults.get(1).getLabel()+"\n"+
                            String.format("%.2f", inspktResults.get(2).getConfidence() * 100f)+"----"+
                            inspktResults.get(2).getLabel()+"\n");*/
                    //  }
                } else {
                    isSent = false;
                }
            }


            @Override
            public void onFailure(Call<InspktResult> call, Throwable t) {
                // handle execution failures like no internet connectivity
                //   BusProvider.getInstance().post(new ErrorEvent(-2,t.getMessage()));
                isSent = false;
            }
        });
    }


    private static byte[] convertBitmapToByte(Bitmap image) {
        // System.gc();
        Bitmap bmp = null;
        bmp = image;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    private static String encryptImage(byte[] imageArray) {
        String s = Base64.encodeToString(imageArray, Base64.NO_WRAP).trim();
        return s;
    }

    public void ObImage(final Bitmap rgbBitmap, final Bitmap bitmap) {
        // final Paint paint = new Paint();
        isSent = true;
        byte[] byteImage = convertBitmapToByte(bitmap);
        byte[] srcByteImage = convertBitmapToByte(Classifier.cropedImage(rgbBitmap, width, height, 0, 0, width, (height - bottomCropheight)));

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        SendImage sendImage = new SendImage(encryptImage(byteImage));
        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
       /* RequestBody requestBody = RequestBody
                .create(MediaType.parse("application/octet-stream"), byteImage);*/
        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(serverModelUrl/*"https://batteryterminalapi.azurewebsites.net/WWCV/ObjectDetection/BatteryTerminal/"*/)
                .build();
        ApiService service = retrofit.create(ApiService.class);

        Call<ApiInspktResult> call = service.apiInspktImage(apiPredictionKey, modelId, ModelCheckActivity.languageId, sendImage);

        call.enqueue(new Callback<ApiInspktResult>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<ApiInspktResult> call, Response<ApiInspktResult> response) {
              //  resultTextViewOD.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    String StatusCode = response.body().getStatusCode();
                    String confidence = response.body().getConfidence();
                    String negativeMsg = response.body().getMsg();
                    float fConf = Float.parseFloat(confidence);
                    if (StatusCode.equals("200")) {
                        //  paint.setColor(Color.GREEN);
                        Bitmap bitmap1=bitmap;
                        imageitem = new Imageitem();
                        imageitem.setCarViewName(response.body().lable);
                        imageitem.setConfidence(fConf);
                        // imageitem.setByteImage(convertBitmapToByte(rgbFrameBitmap));
                        imageitem.setByteCropImage(byteImage);
                        imageitem.setByteImage(srcByteImage);
                        imageitem.setStatus("PASS");
                        imageitem.setResultMsg(passMsg);
                      //  resultTextViewOD.setVisibility(View.VISIBLE);
                        resultTextViewOD.setTextColor(Color.parseColor(passMsgColor));
                        resultTextViewOD.setText(passMsg);
                        postInspkt(imageitem);
                    } else if (StatusCode.equals("201")) {
                        Bitmap bitmap1=bitmap;
                        //  paint.setColor(Color.RED);
                        imageitem = new Imageitem();
                        imageitem.setCarViewName(response.body().lable);
                        imageitem.setConfidence(fConf);
                        // imageitem.setByteImage(convertBitmapToByte(rgbFrameBitmap));
                        imageitem.setByteCropImage(byteImage);
                        imageitem.setByteImage(srcByteImage);
                        imageitem.setStatus("FAIL");
                        imageitem.setResultMsg(failMsg);
                       // resultTextViewOD.setVisibility(View.VISIBLE);
                        resultTextViewOD.setTextColor(Color.parseColor(failMsgColor));
                        resultTextViewOD.setText(failMsg);
                        postInspkt(imageitem);
                    } else if (StatusCode.equals("202")) {
                        Bitmap bitmap1=bitmap;
                        isSent = false;
                     //   resultTextViewOD.setVisibility(View.VISIBLE);
                        resultTextViewOD.setTextColor(Color.parseColor(negativeMsgColor));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            resultTextViewOD.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
                        }
                        // resultTextView.setText(apiDisplayMsg);
                        resultTextViewOD.setText(negativeMsg);
                        isCaptureStarted = false;
                        //startCapture.setVisibility(View.VISIBLE);
                    }
                } else {
                    isSent = false;
                    resultTextViewOD.setText("");
                    String message = response.message();
                    Toast.makeText(DetectorActivity.this, message.toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiInspktResult> call, Throwable t) {
                // handle execution failures like no internet connectivity
                //   BusProvider.getInstance().post(new ErrorEvent(-2,t.getMessage()));
                isSent = false;
            }
        });
    }

    public void postInspkt(Imageitem imageitem) {
        if (!isBeepSound) {
            ring.start();
            isBeepSound = true;
        }
        isSent = true;
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        //   processResponse(imageitem);
                        reTakeBool = false;
                        // showAlert(imageitem);
                        //  ConfirmationPopUp(imageitem);
                        if (imageitem.getStatus().equalsIgnoreCase("Pass")) {
                            if (retakePassConfirm)
                                showRetake(imageitem);
                            else
                                processResponse(imageitem);
                        } else if (imageitem.getStatus().equalsIgnoreCase("Fail")) {
                            if (retakeFailConfirm)
                                showRetake(imageitem);
                            else
                                processResponse(imageitem);
                        }

                    }
                });
    }

    void showRetake(final Imageitem result) {
        Imageitem imageitem = result;
        runOnUiThread(new Runnable() {
            @SuppressLint("ResourceAsColor")
            public void run() {
                retakeCard.setVisibility(View.VISIBLE);
                if (imageitem.getStatus().equalsIgnoreCase("Fail")) {
              /*inspktLyt.setBackgroundColor(Color.parseColor("#FF5722"));
              resultImageView.setBackgroundResource(R.drawable.ic_fail_white_24dp);
              retakeTextView.setText(failMsg + ""+reTakeMsg);*/
                    //new
                    txtModelName.setText(modelName);
                    retakeTextViewNew.setText(failMsg + "" + reTakeMsg);
                    retakeTextViewNew.setTextColor(R.color.red);
                } else if (imageitem.getStatus().equalsIgnoreCase("Pass")) {
              /*inspktLyt.setBackgroundColor(Color.parseColor("#8BC34A"));
              resultImageView.setBackgroundResource(R.drawable.ic_pass_white_24dp);
              retakeTextView.setText(passMsg+ ""+reTakeMsg);*/
                    //new
                    txtModelName.setText(modelName);
                    retakeTextViewNew.setText(passMsg + "" + reTakeMsg);
                    retakeTextViewNew.setTextColor(R.color.green);
                }
            }
        });
        // okImage.setOnClickListener(new View.OnClickListener() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retakeCard.setVisibility(View.GONE);
                processResponse(imageitem);
            }
        });
        // cancelImage.setOnClickListener(new View.OnClickListener() {
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retakeCard.setVisibility(View.GONE);
                isBeepSound = false;
                isSent = false;
                isCaptureStarted = false;
                reTakeBool = true;
                passCallBool = false;
                resultTextViewOD.setText("");
                /*runOnUiThread(new Runnable() {
                    public void run() {
                        resultTextViewOD.setText("");
                        //startCapture.setVisibility(View.VISIBLE);
                    }
                });*/
            }
        });
    }

    public void processResponse(final Imageitem imageItem) {
        if (imageItem != null && !isDetect) {
            // try {
            if (modelResultData == null)
                modelResultData = new ArrayList<>();
            mondelImage = imageItem.getByteImage();
            if (cropBool)
                modelResultData.add(new ParentResponseData.ModelResult(modelId, modelName, seqNo, modelVersion, AOI, imageItem.getStatus(),
                        imageItem.getResultMsg(), encryptImage(mondelImage), encryptImage(imageItem.getByteCropImage()), imageItem.getConfidence() + ""));
            else
                modelResultData.add(new ParentResponseData.ModelResult(modelId, modelName, seqNo, modelVersion, AOI, imageItem.getStatus(),
                        imageItem.getResultMsg(), encryptImage(mondelImage)));

            gson = new Gson();
            modelResultString = gson.toJson(modelResultData);
            PreferenceStorage.getInstance(this).setModelResult(modelResultString);


            isDetect = true;
            modelData.get(seqCount).setCheckStatus(true);
            if (seqBool) {
                gson = new Gson();
                //  stepGson = new Gson();
                modelString = gson.toJson(modelData);
                //   stepsBeanList = new ArrayList<>();
                for (int i = 0; i < stepData.size(); i++) {
                    if (i == seqCount) {
                        if (imageItem.getStatus().equals("PASS"))
                            stepData.get(i).setState(1);
                        else
                            stepData.get(i).setState(0);
                    }
                }
                gson = new Gson();
                String stepString = gson.toJson(stepData);
                PreferenceStorage.getInstance(this).setStep(stepString);
                PreferenceStorage.getInstance(this).setActivityModel(modelString);
                boolean nextBool = false;
                for (int i = 0; i < modelData.size(); i++) {
                    if (!modelData.get(i).isCheckStatus() && !nextBool) {
                        PreferenceStorage.getInstance(this).setOverlayUrl(modelData.get(i).getModelOverlayUrl());
                        PreferenceStorage.getInstance(this).setPreviewHeight(Integer.parseInt(modelData.get(i).getPreviewHeight()));
                        PreferenceStorage.getInstance(this).setPreviewWidth(Integer.parseInt(modelData.get(i).getPreviewWidth()));
                        PreferenceStorage.getInstance(this).setModelId(modelData.get(i).getModelId());
                        nextBool = true;
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.key_vin_no), vinNumber);
                Intent intent = new Intent(this, DetectorActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                pauseCall = true;
                modelResultData = null;
                modelResultString = null;
                mondelImage = null;
                modelData = null;
                finish();
            } else {
                isSent = true;
                ParentMetaDataStr = PreferenceStorage.getInstance(this).getActivity();
                gson = new Gson();
                Type metaType = new TypeToken<ParentMetaData>() {
                }.getType();
                parentData = gson.fromJson(ParentMetaDataStr, metaType);
                if (PreferenceStorage.getInstance(this).getVinNeed() == 1)
                    parentData.getMetaData().setObjectId(vinNumber);
                String activityResult = "PASS";
                String activityResultMsg = PreferenceStorage.getInstance(this).getActivityPassMsg();
                gson = new Gson();
                Type apiType = new TypeToken<ArrayList<ParentResponseData.ModelResult>>() {
                }.getType();
                modelResultDataApi = gson.fromJson(modelResultString, apiType);
                for (int i = 0; i < modelResultData.size(); i++) {
                    if (modelResultData.get(i).inspktResult.equals("FAIL")) {
                        activityResult = "FAIL";
                        activityResultMsg = PreferenceStorage.getInstance(this).getActivityFailMsg();
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
                if (getCurrentLocation() != null) {
                    lat = String.valueOf(getCurrentLocation().getLatitude());
                    lng = String.valueOf(getCurrentLocation().getLongitude());
                }

                parentResponseDataApi = new ParentResponseData(parentData.getMetaData()
                        , new ParentResponseData.ActivityResult(activityResult, activityResultMsg,
                        modelResultDataApi, lat, lng,
                        Utility.deviceDetail, String.valueOf(SDK_VERSION_CODE), Utility.getDeviceUDID(this)), parentData.getPrimaryData());
                //    gson = new Gson();
                //    String activityApiString = gson.toJson(parentResponseDataApi);
                //  Log.v("Activity Api Result", activityApiString);
                gson = new Gson();
                activityResultString = gson.toJson(parentResponseData);
                //  System.out.println("Activity Result" + activityResultString);
                // Details.result = activityResultString;


                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                if (Details.getInstance().getCallback() != null) {
                    System.out.println("Check: Response Success");
                    PreferenceStorage.getInstance(this).setModelId("0");
                    PreferenceStorage.getInstance(this).setActivityId("0");
                    Details.getInstance().getCallback().onSuccess(activityResultString);
                    modelResultData = null;
                    PreferenceStorage.getInstance(this).setActivityModel(null);
                } else {
                    System.out.println("Check: Response Fail");
                    Utility.exceptionLog(this,
                            Utility.getLog("Parent application call back is null"),
                            PreferenceStorage.getInstance(this).getUserId(),
                            PreferenceStorage.getInstance(this).getSiteId(),
                            PreferenceStorage.getInstance(this).getActivityId(),
                            PreferenceStorage.getInstance(this).getModelId(), "2");
                }
                runInBackground(
                        new Runnable() {
                            @Override
                            public void run() {
                                modelResultPresenter.modelResultAsyncTask(parentResponseDataApi);
                            }
                        });
                pauseCall = true;
                modelResultData = null;
                modelResultString = null;
                mondelImage = null;
                activityModelString = null;
                imageitem = null;
//            cropBitmap = null;
//            previewCropBitmap =null;
                //croppedBitmap = null;
                //rgbFrameBitmap = null;
                modelData = null;
                this.finish();
            }


           /* } catch (JSONException e) {
                e.printStackTrace();
            }*/

        }
    }


    @Override
    public synchronized void onResume() {
        super.onResume();
        // EventBus.getDefault().register(this);
        //initViewAction();
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    /*if(!pauseCall)
      closeActivity();*/
    }

    private void closeActivity() {
        if (PreferenceStorage.getInstance(this).getVinNeed() == 1) {
            Intent intent = new Intent(this, VinNumberActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        finish();
    }


    @Override
    protected int getLayoutId() {
        return R.layout.tfe_od_camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
   /* DESIRED_PREVIEW_SIZE = new Size(PreferenceStorage.getInstance(this).getPreviewWidth(),
            PreferenceStorage.getInstance(this).getPreviewHeight());*/
        return DESIRED_PREVIEW_SIZE;
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

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API;
    }

    @Override
    protected void setUseNNAPI(final boolean isChecked) {
        runInBackground(
                () -> {
                    try {
                        detector.setUseNNAPI(isChecked);
                    } catch (UnsupportedOperationException e) {
                        LOGGER.e(e, "Failed to set \"Use NNAPI\".");
                        runOnUiThread(
                                () -> {
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                });
    }

    @Override
    protected void setNumThreads(final int numThreads) {
        runInBackground(() -> detector.setNumThreads(numThreads));
    }
}
