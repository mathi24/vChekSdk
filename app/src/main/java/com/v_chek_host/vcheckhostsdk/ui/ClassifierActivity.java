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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.v_chek_host.vcheckhostsdk.BuildConfig;
import com.v_chek_host.vcheckhostsdk.Details;
import com.v_chek_host.vcheckhostsdk.ModelCheckActivity;
import com.v_chek_host.vcheckhostsdk.R;
import com.v_chek_host.vcheckhostsdk.VinNumberActivity;
import com.v_chek_host.vcheckhostsdk.customview.Classifier;
import com.v_chek_host.vcheckhostsdk.env.BorderedText;
import com.v_chek_host.vcheckhostsdk.env.Logger;
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
import com.v_chek_host.vcheckhostsdk.utils.ColorUtils;
import com.v_chek_host.vcheckhostsdk.utils.GPSTracker;
import com.v_chek_host.vcheckhostsdk.utils.PreferenceStorage;
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

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;
import static com.v_chek_host.vcheckhostsdk.BuildConfig.SDK_VERSION_CODE;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ClassifierActivity extends CameraActivity implements OnImageAvailableListener, IModelResultView {
    private static final Logger LOGGER = new Logger();
    private static Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    //private static final Size DESIRED_PREVIEW_SIZE= new Size(1360, 960);;
    private static final float TEXT_SIZE_DIP = 10;
    private Bitmap rgbFrameBitmap = null;
    private static Bitmap previewCropBitmap = null;
    private static Bitmap cropBitmap = null;
    private long lastProcessingTimeMs;
    private Integer sensorOrientation;
    private Classifier classifier;
    private BorderedText borderedText;
    AlertDialog confirmationDialog;

    /**
     * Input image size of the model along x axis.
     */
    private int imageSizeX;
    /**
     * Input image size of the model along y axis.
     */
    private int imageSizeY;
    public static boolean isVideoRecordStarted = false;

    protected static ImageView startVideo;
    protected static ImageView ivClose;
    boolean isSent = false;
    public byte[] mondelImage = null;
    public boolean isDetect = false;
    public String vinNumber = "";
    public String modelId;
    public String modelName;
    public String modelVersion;
    public String AOI;
    boolean isBeepSound = false;
    private boolean cropBool;
    private boolean previewCropBool;
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
    private List<String> passLable = new ArrayList<>();
    private String passMsg;
    private String passMsgColor;
    private List<String> failLable = new ArrayList<>();
    private List<String> colorLable = new ArrayList<>();
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
    private static String activityModelString;
    private String stepString;
    private static List<Result.ModelData> modelData;
    private static List<ParentResponseData.ModelResult> modelResultData = new ArrayList<>();
    private static List<ParentResponseData.ModelResult> modelResultDataApi = new ArrayList<>();
    private static ParentResponseData parentResponseData;
    private static ParentResponseData parentResponseDataApi;
    private static ParentMetaData parentData;
    private List<StepBean> stepData;
    private Gson gson;
    private MediaPlayer ring;
    private String apiDisplayMsg;
    /*Gson modelGson;
    Gson modelResultGson;
    Gson stepGson;*/
    private String modelString = null;
    private String modelResultString = null ;
    private String ParentMetaDataStr = null;
    private String activityResultString = null;
    private boolean dataBool;
    private boolean seqBool;
    private boolean passCallBool;
    private boolean serverModelCheck;
    private boolean serverSecondaryCheck;
    private boolean isfailHandlerCalled;
    private int waitTime = 0;
    private Handler failHandler;
    private IModelResultPresenter modelResultPresenter;
    private Boolean readNextBool;
    private Boolean reTakeBool = false;
    private Boolean pauseCall = false;
    private static Imageitem imageitem;
    Bundle bundle;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
        }
        bundle = getIntent().getExtras();
        ring = MediaPlayer.create(this, R.raw.long_beep);
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

      //  Toast.makeText(this,currentLocation.getLatitude() + "" +currentLocation.getLongitude(),Toast.LENGTH_SHORT).show();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tfe_ic_camera_connection_fragment;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        DESIRED_PREVIEW_SIZE = new Size(PreferenceStorage.getInstance(this).getPreviewWidth(),
                PreferenceStorage.getInstance(this).getPreviewHeight());
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    protected String getOverlayUrl() {
        return modelOverlayUrl;
    }

    @Override
    protected List<StepBean> getStepBean() {
        return stepsBeanList;
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        recreateClassifier(getModel(), getDevice(), getNumThreads());
        if (classifier == null) {
            LOGGER.e("No classifier on preview!");
            return;
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        //System.gc();
       // Runtime.getRuntime().freeMemory();
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    }

    @Override
    protected void processImage() {
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        final int cropSize = Math.min(previewWidth, previewHeight);

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        if (classifier != null) {
                            final long startTime = SystemClock.uptimeMillis();
                            if (previewBool /*&& !previewScan*/) {
                               // Bitmap previewCropBitmap;
                                if (previewCropBool) {
                                    previewCropBitmap = Classifier.cropedImage(rgbFrameBitmap, width, height, pcropX, pcropY, pcropWidth, pcropHeight);
                                } else {
                                    previewCropBitmap = rgbFrameBitmap;
                                }
                                final List<Classifier.Recognition> previewResults = classifier.recognizeImage(previewCropBitmap, sensorOrientation);
                                LOGGER.v("Detect: %s", previewResults);

                                runOnUiThread(
                                        new Runnable() {
                                            @SuppressLint("WrongConstant")
                                            @Override
                                            public void run() {

                                                inspktImage.setImageBitmap(previewCropBitmap);

                                                if (isVideoRecordStarted && !isSent) {
                                                    int confident;
                                                    final float confidence = previewResults.get(0).getConfidence();
                                                    final String label = previewResults.get(0).getTitle();
                                                    String strConf = String.format("%.2f", confidence * 100f);
                                                    //        String test = String.format("%.2f", (100 * confidence) + "%");
                                                    float fConf = (Float.parseFloat(strConf) * 100);
                                                    if (label.equals(previewLable))
                                                        confident = previewConfidence;
                                                    else if (failLable.contains(label))
                                                        confident = failConfidence;
                                                    else
                                                        confident = negativeConfidence;

                                                    resultTextView.setText("");
                                                    if ((fConf) > confident) {
                                                        if (!label.equals(negativeLable)) {
                                                            imageitem = new Imageitem();
                                                            if (label.equals(previewLable)) {
                                                                //    previewScan = true;
                                                                final List<Classifier.Recognition> results = classifier.recognizeImage(rgbFrameBitmap, sensorOrientation,
                                                                        cropBool, width, height, cropX, cropY, cropWidth, cropHeight);
                                                         //       Bitmap cropBitmap = null;
                                                                if (cropBool) {
                                                                    cropBitmap = Classifier.cropedImage(rgbFrameBitmap, width, height, cropX, cropY, cropWidth, cropHeight);
                                                                } else {
                                                                    cropBitmap = rgbFrameBitmap;
                                                                }
                                                                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                                                                LOGGER.v("Detect: %s", results);


                                                                if (serverModelCheck) {
                                                                    inspktImage.setImageBitmap(cropBitmap);
                                                                    ObImage(rgbFrameBitmap, cropBitmap);
                                                                    // isSent = true;
                                                                    // PostImage(rgbFrameBitmap,cropBitmap);

                                                                } else {
                                                                    chek(results, cropBitmap, rgbFrameBitmap);
                                                                   /* if(colorLable.contains(getColorString(cropBitmap))){
                                                                       // postInspkt(imageitem);
                                                                        resultTextView.setText(getColorString(cropBitmap) +" ---- color");
                                                                    }else {
                                                                     //   postInspkt(imageitem);
                                                                        resultTextView.setText(getColorString(cropBitmap) +" ---- color");
                                                                    }*/
                                                                }
                                                            } else if (failLable.contains(label)) {
                                                                String failLable = label;
                                                                float failConf = fConf;
                                                                byte[] failByteImage = convertBitmapToByte(Classifier.cropedImage(rgbFrameBitmap, width, height, 0, 0, width, (height - bottomCropheight)));
                                                                if (!isfailHandlerCalled) {
                                                                    failHandler.postDelayed(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            //  if (!passCallBool) {
                                                                            imageitem.setCarViewName(failLable);
                                                                            imageitem.setConfidence(failConf);
                                                                            if (cropBool)
                                                                                imageitem.setByteCropImage(convertBitmapToByte(previewCropBitmap));

                                                                            imageitem.setByteImage(failByteImage);

                                                                            // imageitem.setBitmap(rgbFrameBitmap);
                                                                            imageitem.setStatus("FAIL");
                                                                            imageitem.setResultMsg(failMsg);
                                                                            resultTextView.setTextColor(Color.parseColor(failMsgColor));
                                                                            resultTextView.setText(failMsg);
                                                                            if (!isBeepSound) {
                                                                                ring.start();
                                                                                isBeepSound = true;
                                                                            }
                                                                            isSent = true;
                                                                            runInBackground(
                                                                                    new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            //showAlert(imageitem);
                                                                                            showRetake(imageitem);
                                                                                          //  processResponse(imageitem);
                                                                                        }
                                                                                    });
                                                                            //   }
                                                                        }
                                                                    }, waitTime);
                                                                    isfailHandlerCalled = true;
                                                                }
                                                            }
                                                        } else {
                                                            resultTextView.setTextColor(Color.parseColor(negativeMsgColor));
                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                                resultTextView.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
                                                            }
                                                            resultTextView.setText(negativeMsg);
                                                        }
                                                    }

                                                }else {
                                                   /* resultTextView.setText("");
                                                    if(reTakeBool)
                                                        startVideo.setVisibility(View.VISIBLE);*/
                                                }
                                              /*  resultTextView.setText(String.format("%.2f", previewResults.get(0).getConfidence() * 100f)+"----"+
                                                        previewResults.get(0).getTitle()+"\n"+
                                                        String.format("%.2f", previewResults.get(1).getConfidence() * 100f)+"----"+
                                                        previewResults.get(1).getTitle()+"\n"+
                                                        String.format("%.2f", previewResults.get(2).getConfidence() * 100f)+"----"+
                                                        previewResults.get(2).getTitle()+"\n");*/
                                            }

                                        });

                            } else {
                                final List<Classifier.Recognition> results = classifier.recognizeImage(rgbFrameBitmap, sensorOrientation,
                                        cropBool, width, height, cropX, cropY, cropWidth, cropHeight);
                             //   Bitmap cropBitmap;
                                if (cropBool) {
                                    cropBitmap = Classifier.cropedImage(rgbFrameBitmap, width, height, cropX, cropY, cropWidth, cropHeight);
                                } else {
                                    cropBitmap = rgbFrameBitmap;
                                }
                                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                                LOGGER.v("Detect: %s", results);
                                //  chek(results,cropBitmap);
                                    if (serverModelCheck) {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                inspktImage.setImageBitmap(cropBitmap);
                                            }
                                        });
                                        // inspktImage.setImageBitmap(cropBitmap);
                                        // isSent = true;
                                        //  PostImage(rgbFrameBitmap,cropBitmap);
                                        if (isVideoRecordStarted && !isSent) {
                                            ObImage(rgbFrameBitmap, cropBitmap);
                                        }else {
                                           /* runOnUiThread(new Runnable() {
                                                public void run() {
                                                    resultTextView.setText("");
                                                    if(reTakeBool)
                                                        startVideo.setVisibility(View.VISIBLE);
                                                }
                                            });*/
                                        }
                                    } else {
                                    chek(results, cropBitmap, rgbFrameBitmap);
                                }
                            }
                        //    if (!isSent)
                                readyForNextImage();
                        }
                    }
                });
    }


    public void chek(List<Classifier.Recognition> results, final Bitmap bitmap, final Bitmap rgbImage) {
        runOnUiThread(
                new Runnable() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void run() {
                        if (isVideoRecordStarted && !isSent) {
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
                                            resultTextView.setTextColor(Color.parseColor(passMsgColor));
                                            resultTextView.setText(passMsg);
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
                                                            resultTextView.setTextColor(Color.parseColor(failMsgColor));
                                                            resultTextView.setText(failMsg);
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
                                    resultTextView.setTextColor(Color.parseColor(negativeMsgColor));
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        resultTextView.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
                                    }
                                    resultTextView.setText(negativeMsg);
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
                        }else {
                          /*  resultTextView.setText("");
                            if(reTakeBool)
                                startVideo.setVisibility(View.VISIBLE);*/
                        }
                        inspktImage.setImageBitmap(bitmap);
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
                        if(imageitem.getStatus().equalsIgnoreCase("Pass")) {
                            if (retakePassConfirm)
                                showRetake(imageitem);
                            else
                                processResponse(imageitem);
                        }else if(imageitem.getStatus().equalsIgnoreCase("Fail")) {
                            if (retakeFailConfirm)
                                showRetake(imageitem);
                            else
                                processResponse(imageitem);
                        }

                    }
                });
    }


    void showRetake(final Imageitem result){
        Imageitem imageitem=result;
        runOnUiThread(new Runnable() {
            public void run() {
                retakeCard.setVisibility(View.VISIBLE);
                if(imageitem.getStatus().equalsIgnoreCase("Fail")) {
                    inspktLyt.setBackgroundColor(Color.parseColor("#FF5722"));
                    resultImageView.setBackgroundResource(R.drawable.ic_fail_white_24dp);
                    retakeTextView.setText(failMsg + ""+reTakeMsg);
                }else if(imageitem.getStatus().equalsIgnoreCase("Pass")) {
                    inspktLyt.setBackgroundColor(Color.parseColor("#8BC34A"));
                    resultImageView.setBackgroundResource(R.drawable.ic_pass_white_24dp);
                    retakeTextView.setText(passMsg+ ""+reTakeMsg);
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
                isVideoRecordStarted = false;
                reTakeBool = true;
                passCallBool =false;
                runOnUiThread(new Runnable() {
                    public void run() {
                        resultTextView.setText("");
                        startVideo.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    public void ObImage(final Bitmap rgbBitmap, final Bitmap bitmap) {
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

        Call<ApiInspktResult> call = service.apiInspktImage(apiPredictionKey,modelId, ModelCheckActivity.languageId, sendImage);

        call.enqueue(new Callback<ApiInspktResult>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<ApiInspktResult> call, Response<ApiInspktResult> response) {
                if (response.isSuccessful()) {
                    String StatusCode = response.body().getStatusCode();
                    String confidence = response.body().getConfidence();
                    String negativeMsg = response.body().getMsg();
                    float fConf = Float.parseFloat(confidence);
                    if (StatusCode.equals("200")) {
                        imageitem = new Imageitem();
                        imageitem.setCarViewName(response.body().lable);
                        imageitem.setConfidence(fConf);
                        // imageitem.setByteImage(convertBitmapToByte(rgbFrameBitmap));
                        imageitem.setByteCropImage(byteImage);
                        imageitem.setByteImage(srcByteImage);
                        imageitem.setStatus("PASS");
                        imageitem.setResultMsg(passMsg);
                        resultTextView.setTextColor(Color.parseColor(passMsgColor));
                        resultTextView.setText(passMsg);
                        postInspkt(imageitem);
                    } else if (StatusCode.equals("201")) {
                        imageitem = new Imageitem();
                        imageitem.setCarViewName(response.body().lable);
                        imageitem.setConfidence(fConf);
                        // imageitem.setByteImage(convertBitmapToByte(rgbFrameBitmap));
                        imageitem.setByteCropImage(byteImage);
                        imageitem.setByteImage(srcByteImage);
                        imageitem.setStatus("FAIL");
                        imageitem.setResultMsg(failMsg);
                        resultTextView.setTextColor(Color.parseColor(failMsgColor));
                        resultTextView.setText(failMsg);
                        postInspkt(imageitem);
                    } else if (StatusCode.equals("202")) {
                        isSent = false;
                        resultTextView.setTextColor(Color.parseColor(negativeMsgColor));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            resultTextView.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
                        }
                       // resultTextView.setText(apiDisplayMsg);
                        resultTextView.setText(negativeMsg);
                        isVideoRecordStarted = false;
                        startVideo.setVisibility(View.VISIBLE);
                    }
                } else {
                    isSent = false;
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
                                resultTextView.setTextColor(Color.parseColor(passMsgColor));
                                resultTextView.setText(passMsg);
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
                                resultTextView.setTextColor(Color.parseColor(failMsgColor));
                                resultTextView.setText(failMsg);
                                postInspkt(imageitem);

                                //       }
                                //  }
                                       /* }, waitTime);
                                        isfailHandlerCalled = true;*/
                                //   }
                            }
                        } else {
                            resultTextView.setTextColor(Color.parseColor(negativeMsgColor));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                resultTextView.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
                            }
                            resultTextView.setText(negativeMsg);
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

    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    public String getColorString(Bitmap bitmap) {
        int colorBitmap = ClassifierActivity.getDominantColor(bitmap);
        int redValue = Color.red(colorBitmap);
        int blueValue = Color.blue(colorBitmap);
        int greenValue = Color.green(colorBitmap);

        ColorUtils colorUtils = new ColorUtils();
        String colorStr = colorUtils.getColorNameFromRgb(redValue, greenValue, blueValue);
        /*if(*//*colorStr.equalsIgnoreCase("Blue")*//*colorLable.contains(colorStr)){
                System.out.println("COLORSCAN_BLUE");
                if(blueValue>greenValue && blueValue > redValue){
                    if((blueValue-greenValue)>50){
                        colorStr = "Blue";
                    }else {
                        colorStr = "White";
                    }
                }else {
                    colorStr = "White";
                }
        }else if(colorLable.contains(colorStr)){
            if(greenValue>blueValue && greenValue > redValue) {
                colorStr = "Green";
            }else {
                colorStr = "White";
            }
        }else {
            if(redValue > blueValue && greenValue < redValue) {
                colorStr = "Red";
            }else {
                colorStr = "White";
            }
        }*/
        return colorStr;
    }

    @Override
    protected void onInferenceConfigurationChanged() {
        if (rgbFrameBitmap == null) {
            // Defer creation until we're getting camera frames.
            return;
        }
        final Classifier.Device device = getDevice();
        final Classifier.Model model = getModel();
        final int numThreads = getNumThreads();
        runInBackground(() -> recreateClassifier(model, device, numThreads));
    }

    private void recreateClassifier(Classifier.Model model, Classifier.Device device, int numThreads) {
        if (classifier != null) {
            LOGGER.d("Closing classifier.");
            classifier.close();
            classifier = null;
        }
        if (device == Classifier.Device.GPU
                && (model == Classifier.Model.QUANTIZED_MOBILENET || model == Classifier.Model.QUANTIZED_EFFICIENTNET)) {
            LOGGER.d("Not creating classifier: GPU doesn't support quantized models.");
            runOnUiThread(
                    () -> {
                        Toast.makeText(this, R.string.tfe_ic_gpu_quant_error, Toast.LENGTH_LONG).show();
                    });
            return;
        }
        try {
            LOGGER.d(
                    "Creating classifier (model=%s, device=%s, numThreads=%d)", model, device, numThreads);
            classifier = Classifier.create(this, model, device, numThreads, "new", modelFileName, modelLableName);
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.e(e, "Failed to create classifier.");
            runOnUiThread(
                    () -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    });
            return;
        }

        // Updates the input image size.
        imageSizeX = classifier.getImageSizeX();
        imageSizeY = classifier.getImageSizeY();
    }

    void initViewAction() {
        startVideo = findViewById(R.id.startCapture);
        ivClose = findViewById(R.id.image_close);
        isVideoRecordStarted = false;
        startVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVideoRecordStarted = true;
                startVideo.setVisibility(View.GONE);
                resultTextView.setText("");
            }
        });

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    chronometer.stop();
//                    stopVideo.setVisibility(View.GONE);
                    startVideo.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pauseCall=true;
                closeActivity();
            }
        });

    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(!pauseCall)
            closeActivity();
    }

    private void closeActivity() {
        if (PreferenceStorage.getInstance(this).getVinNeed() == 1) {
            Intent intent = new Intent(this, VinNumberActivity.class);
           // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        finish();
    }

    public static byte[] convertBitmapToByte(Bitmap image) {
       // System.gc();
        Bitmap bmp = null;
        bmp = image;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public static String encryptImage(byte[] imageArray) {
        String s = Base64.encodeToString(imageArray, Base64.NO_WRAP).trim();
        return s;
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
                Intent intent = new Intent(this, ClassifierActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                pauseCall=true;
                modelResultData = null;
                modelResultString = null;
                mondelImage = null;
                modelData =null;
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
                String lat="";
                String lng="";
                if(getCurrentLocation()!=null){
                    lat= String.valueOf(getCurrentLocation().getLatitude());
                    lng= String.valueOf(getCurrentLocation().getLongitude());
                }

                parentResponseDataApi = new ParentResponseData(parentData.getMetaData()
                        , new ParentResponseData.ActivityResult(activityResult, activityResultMsg,
                        modelResultDataApi,lat,lng,
                        Utility.deviceDetail,String.valueOf( SDK_VERSION_CODE),Utility.getDeviceUDID(this)), parentData.getPrimaryData());
            //    gson = new Gson();
            //    String activityApiString = gson.toJson(parentResponseDataApi);
             //  Log.v("Activity Api Result", activityApiString);
                gson = new Gson();
                activityResultString = gson.toJson(parentResponseData);
               //  System.out.println("Activity Result" + activityResultString);
               // Details.result = activityResultString;


                if (Details.getInstance().getCallback() != null) {
                    PreferenceStorage.getInstance(this).setModelId("0");
                    PreferenceStorage.getInstance(this).setActivityId("0");
                    Details.getInstance().getCallback().onSuccess(activityResultString);
                    modelResultData=null;
                    PreferenceStorage.getInstance(this).setActivityModel(null);
                } else {
                    Utility.exceptionLog(this,
                            Utility.getLog("Parent application call back is null"),
                            PreferenceStorage.getInstance(this).getUserId(),
                            PreferenceStorage.getInstance(this).getSiteId(),
                            PreferenceStorage.getInstance(this).getActivityId(),
                            PreferenceStorage.getInstance(this).getModelId(),"2");
                }
                runInBackground(
                        new Runnable() {
                            @Override
                            public void run() {
                                modelResultPresenter.modelResultAsyncTask(parentResponseDataApi);
                            }
                        });
                pauseCall=true;
                modelResultData = null;
                modelResultString = null;
                mondelImage = null;
                activityModelString =null;
                imageitem = null;
                cropBitmap = null;
                previewCropBitmap =null;
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
        initViewAction();
    }

    @Override
    public synchronized void onDestroy() {

        gson = null;
        parentResponseDataApi = null;
        parentResponseData = null;
        modelResultDataApi = null;
        modelResultString =null;
        modelString = null;
        activityResultString =null;
        ParentMetaDataStr =null;
        rgbFrameBitmap = null;

        if (classifier != null) {
            LOGGER.d("Closing classifier.");
            classifier.close();
            classifier = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        pauseCall=true;
        closeActivity();
    }

    @Override
    public void onModelResultError(@NonNull String message) {
      /*  Utility.exceptionLog(ClassifierActivity.this,
                Utility.getLog(message));*/
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

    /*public void ConfirmationPopUp(Imageitem result) {
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);
        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.confirm_dialog, viewGroup, false);

        TextView tvDamageHeader = (TextView) dialogView.findViewById(R.id.damage_txtheader);
        TextView txtModelName = dialogView.findViewById(R.id.txt_model_name);
        TextView txtModelResult = dialogView.findViewById(R.id.txt_model_result);
        Button btnRetake = dialogView.findViewById(R.id.btn_retake);
        Button btnContinue = dialogView.findViewById(R.id.btn_ok);
        ImageView resultImageView = dialogView.findViewById(R.id.inspkt_img);
        RelativeLayout inspktLyt= dialogView.findViewById(R.id.inspkt_lyt);
        txtModelName.setText(modelName);

        if(result.getStatus().equalsIgnoreCase("Fail")) {
            inspktLyt.setBackgroundColor(Color.parseColor("#FF5722"));
            resultImageView.setBackgroundResource(R.drawable.ic_fail_white_24dp);
            txtModelResult.setTextColor(Color.parseColor("#FF5722"));
            txtModelResult.setText(failMsg);
        }else if(result.getStatus().equalsIgnoreCase("Pass")) {
            inspktLyt.setBackgroundColor(Color.parseColor("#8BC34A"));
            resultImageView.setBackgroundResource(R.drawable.ic_pass_white_24dp);
            txtModelResult.setTextColor(Color.parseColor("#8BC34A"));
            txtModelResult.setText(passMsg);
        }
        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);
        builder.setCancelable(false);
        //finally creating the alert dialog and displaying it
        confirmationDialog = builder.create();
        confirmationDialog.show();
        btnRetake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmationDialog.dismiss();
                isBeepSound = false;
                isSent = false;
                isVideoRecordStarted = false;
                reTakeBool = true;
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmationDialog.dismiss();
                processResponse(result);
            }
        });
    }*/
}
