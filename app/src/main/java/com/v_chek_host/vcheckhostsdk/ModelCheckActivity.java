package com.v_chek_host.vcheckhostsdk;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.Normalizer2;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.v_chek_host.vcheckhostsdk.database.SQLiteHandler;
import com.v_chek_host.vcheckhostsdk.downloadutil.CheckForSDCard;
import com.v_chek_host.vcheckhostsdk.downloadutil.Utils;
import com.v_chek_host.vcheckhostsdk.model.entity.ModelStatus;
import com.v_chek_host.vcheckhostsdk.model.entity.ParentMetaData;
import com.v_chek_host.vcheckhostsdk.model.entity.Result;
import com.v_chek_host.vcheckhostsdk.presenter.contract.IModelPresenter;
import com.v_chek_host.vcheckhostsdk.presenter.implement.ModelPresenter;
import com.v_chek_host.vcheckhostsdk.ui.ClassifierActivity;


import com.v_chek_host.vcheckhostsdk.ui.DetectorActivity;
import com.v_chek_host.vcheckhostsdk.ui.VchekErrorActivity;

import com.v_chek_host.vcheckhostsdk.utils.GPSTracker;
import com.v_chek_host.vcheckhostsdk.utils.PreferenceStorage;
import com.v_chek_host.vcheckhostsdk.utils.Utility;
import com.v_chek_host.vcheckhostsdk.utils.bean.StepBean;
import com.v_chek_host.vcheckhostsdk.view.IModelView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import retrofit2.Call;

import static android.content.ContentValues.TAG;

public class ModelCheckActivity extends AppCompatActivity implements IModelView {

    public static String activityId = "";
    public static String languageId = "";
    IModelPresenter modelPresenter;
    List<Result.ModelData> modelDataList;
    Result.ModelData modelData;
    private ParentMetaData parentData;
    SQLiteHandler sqLiteHandler;
    TextView responseText;
    int modelCount;
    int modelCheckCount;
    Handler mainHandler;
    protected CoordinatorLayout coordinatorLayout;
    Gson metaGson;
    Gson modelGson;
    Gson stepGson;
    String modelString;
    int isVinNeed = 0;
    int vinMaxLength;
    private static List<StepBean> stepsBeanList = null;
    String width,height;
    File externalFilesDir ;
    Bundle bundle;
    GPSTracker tracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_check);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        modelPresenter = new ModelPresenter(this, this);
        responseText = findViewById(R.id.text_config);
        sqLiteHandler = new SQLiteHandler(this);
        bundle = getIntent().getExtras();
        externalFilesDir = getExternalFilesDir(null);
        metaGson = new Gson();
        if (bundle != null) {
            String activityString ;
            if(getIntent().getStringExtra("input_data")!=null) {
                activityString = getIntent().getStringExtra("input_data");
            }else {
                Intent in = getIntent();
                Uri data = in.getData();
                activityString = data.getQueryParameter("input_data");
            }
            PreferenceStorage.getInstance(this).setActivity(activityString);
            Type metaType = new TypeToken<ParentMetaData>() {
            }.getType();
            parentData = metaGson.fromJson(activityString, metaType);
            activityId = parentData.getMetaData().getActivityId();
            languageId = parentData.getMetaData().getLanguageId();
            PreferenceStorage.getInstance(this).setUserId(parentData.getMetaData().getUserId());
            PreferenceStorage.getInstance(this).setActivityId(activityId);
            PreferenceStorage.getInstance(this).setSiteId(parentData.getMetaData().getSiteId());
            PreferenceStorage.getInstance(this).setModelId("0");
        }
        width = Utility.getWidth(this);
        height = Utility.getHeight(this);
        tracker = new GPSTracker(this);
      //  activityId = "19";
       // activityId = "2";
        //activityId = "36";
        //activityId = "53";
       // languageId = "en_US";

     /*    activityId = "36";
       */
        checkPermissions();
        try {
            CaocConfig.Builder.create()
                    .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
                    .enabled(true)
                    .showRestartButton(false)
                    .minTimeBetweenCrashesMs(1)
                    .errorActivity(VchekErrorActivity.class)
                    .showErrorDetails(false)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   private Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            if (modelCount == modelCheckCount) {
                Intent intent;
                PreferenceStorage.getInstance(ModelCheckActivity.this).setModelResult(null);
                if (PreferenceStorage.getInstance(ModelCheckActivity.this).getVinNeed() == 1) {
                    intent = new Intent(ModelCheckActivity.this, VinNumberActivity.class);
                    startActivity(intent);
                } else {
                    //intent = new Intent(ModelCheckActivity.this, ClassifierActivity.class);
                  //  intent = new Intent(ModelCheckActivity.this, TestClassifierActivity.class);
                   // intent = new Intent(ModelCheckActivity.this, DefaultClassifierActivity.class);
                    intent = new Intent(ModelCheckActivity.this, DetectorActivity.class);
                    startActivity(intent);
                }
                finish();
            } else {
                mainHandler.postDelayed(myRunnable, 2);
            }

        } // This is your code
    };

    @Override
    public void onModelError(@NonNull String message) {
        snackBarRefresh(this, coordinatorLayout, message);
    }

    @Override
    public void onModelOk(@NonNull Result.ModelResponse modelInfo) {
        if (modelInfo.getmResponseCode().equals("200")) {
            modelDataList = modelInfo.getModelData();
            responseText.setText(modelInfo.getVinDetails().getmActivityResponseMsg());
            modelCount = modelDataList.size();
            isVinNeed = modelInfo.getVinDetails().getIsVinMandat();
            vinMaxLength = modelInfo.getVinDetails().getVinMaxLength();
            PreferenceStorage.getInstance(ModelCheckActivity.this).setVinNeed(isVinNeed);
            PreferenceStorage.getInstance(ModelCheckActivity.this).setVinCompare(modelInfo.getVinDetails().getIsVinCompare());
            PreferenceStorage.getInstance(ModelCheckActivity.this).setVinMaxLength(vinMaxLength);
            PreferenceStorage.getInstance(ModelCheckActivity.this).setActivityPassMsg(modelInfo.getVinDetails().getmActivityPassMsg());
            PreferenceStorage.getInstance(ModelCheckActivity.this).setActivityFailMsg(modelInfo.getVinDetails().getmActivityFailMsg());
            modelCheck(modelDataList);
            mainHandler.postDelayed(myRunnable, 2);
            modelGson = new Gson();
            stepGson = new Gson();
            modelString = modelGson.toJson(modelDataList);
            PreferenceStorage.getInstance(ModelCheckActivity.this).setActivityModel(modelString);
            if (modelCount > 0) {
                stepsBeanList = new ArrayList<>();
                for (int i = 0; i < modelCount; i++) {
                    String stepName = modelDataList.get(i).getStepLable();
                    stepsBeanList.add(new StepBean(stepName, -1));
                }
                String stepString = stepGson.toJson(stepsBeanList);
                PreferenceStorage.getInstance(ModelCheckActivity.this).setStep(stepString);
                PreferenceStorage.getInstance(ModelCheckActivity.this).setOverlayUrl(modelDataList.get(0).getModelOverlayUrl());
                PreferenceStorage.getInstance(ModelCheckActivity.this).setPreviewHeight(Integer.parseInt(modelDataList.get(0).getPreviewHeight()));
                PreferenceStorage.getInstance(ModelCheckActivity.this).setPreviewWidth(Integer.parseInt(modelDataList.get(0).getPreviewWidth()));
                PreferenceStorage.getInstance(ModelCheckActivity.this).setModelId(modelDataList.get(0).getModelId());
            }
            // List<ModelStatus> modelStatuses = sqLiteHandler.getAllContacts();
        }
    }

    @Override
    public void onModelStart(@NonNull Call<Result.ModelResponse> call) {

    }

    @Override
    public void onModelFinish() {

    }


    public void snackBarRefresh(Context context, CoordinatorLayout coordinatorLayout, final String msg) {
        final Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(Color.BLUE);
        snackbar.setAction(context.getString(R.string.refresh), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
                if (Utility.isConnectingToInternet(ModelCheckActivity.this)) {
                    if (tracker.getGpsStatus()) {
                        modelPresenter.modelAsyncTask(activityId, languageId, width, height);
                    } else {
                        snackBarRefresh(ModelCheckActivity.this, coordinatorLayout, getString(R.string.error_msg_gps_problem));
                    }
                }else
                    snackBarRefresh(ModelCheckActivity.this, coordinatorLayout, getString(R.string.error_msg_network_problem));
            }
        });
        snackbar.setActionTextColor(Color.GREEN);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.DKGRAY);
        TextView textView = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }



    public void modelCheck(List<Result.ModelData> modelInfo) {
        if (modelInfo.size() > 0) {
            for (int i = 0; i < modelInfo.size(); i++) {
                modelData = modelInfo.get(i);
                int modelId = Integer.parseInt(modelData.getModelId());
                int modelVersion = Integer.parseInt(modelData.getModelVersion());
                if (sqLiteHandler.getModelDownloadStatus(1, modelId, modelVersion)) {
                    if (sqLiteHandler.getModelDownloadStatus(2, modelId, modelVersion)) {
                        if(modelExist(modelData.getModelFileName()))
                           modelCheckCount++;
                        else
                          new DownloadingTask().execute(new MyTaskParams(modelData.getModelDownloadUrl(), modelData.getModelFileName(),
                                    modelData.getModelId(), modelData.getModelVersion()));
                    } else {
                        new DownloadingTask().execute(new MyTaskParams(modelData.getModelDownloadUrl(), modelData.getModelFileName(),
                                modelData.getModelId(), modelData.getModelVersion()));
                    }
                } else {
                    sqLiteHandler.addContact(new ModelStatus(Integer.parseInt(modelData.getModelId()), modelData.getModelName(),
                            Integer.parseInt(modelData.getModelType()), Integer.parseInt(modelData.getModelVersion()),
                            Integer.parseInt(modelData.getPreviewWidth()), Integer.parseInt(modelData.getPreviewHeight())
                            , modelData.getModelFileName(), modelData.getModelDownloadUrl(), 0));
                    new DownloadingTask().execute(new MyTaskParams(modelData.getModelDownloadUrl(), modelData.getModelFileName(),
                            modelData.getModelId(), modelData.getModelVersion()));
                }

            }
        } else {

        }
    }


    public class MyTaskParams {
        String modelUrl;
        String modelName;
        String modelId;
        String modelVersion;

        public MyTaskParams(String modelUrl, String modelName, String modelId, String modelVersion) {
            this.modelUrl = modelUrl;
            this.modelName = modelName;
            this.modelId = modelId;
            this.modelVersion = modelVersion;
        }
    }

   /* private class DownloadingTask extends AsyncTask<MyTaskParams, Void, Void> {
        // SQLiteHandler  db = new SQLiteHandler(getApplicationContext());
        File apkStorage = null;
        File outputFile = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (outputFile != null) {
                    modelCheckCount++;
                    //  Toast.makeText(getApplicationContext(), "Download Completed", Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "Download Failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Download Failed with Exception - " + e.getLocalizedMessage());
            }
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(MyTaskParams... params) {
            try {
                URL url = new URL(params[0].modelUrl);//Create Download URl
                HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
                c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
                c.connect();//connect the URL Connection

                //If Connection response is not OK then show Logs
                if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + c.getResponseCode()
                            + " " + c.getResponseMessage());

                }


                //Get File if SD card is present
                if (new CheckForSDCard().isSDCardPresent()) {

                    apkStorage = new File(
                            Environment.getExternalStorageDirectory() + "/"
                                    + Utils.downloadDirectory);
                } else {
                    //  Toast.makeText(getApplicationContext(), "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();
                }
                //If File is not present create directory
                if (!apkStorage.exists()) {
                    apkStorage.mkdir();
                    Log.e(TAG, "Directory Created.");
                }

                outputFile = new File(apkStorage, params[0].modelName);//Create Output file in Main File

                //Create New File if not present
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                    Log.e(TAG, "File Created");
                }

                FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

                InputStream is = c.getInputStream();//Get InputStream for connection

                byte[] buffer = new byte[1024];//Set buffer type
                int len1 = 0;//init length
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);//Write new file
                }

                //Close all connection after doing task
                fos.close();
                is.close();
                sqLiteHandler.updateDownloadStatus(Integer.parseInt(params[0].modelId), Integer.parseInt(params[0].modelVersion));

            } catch (Exception e) {

                //Read exception if something went wrong
                e.printStackTrace();
                outputFile = null;
                snackBarRefresh(ModelCheckActivity.this, coordinatorLayout, getString(R.string.error_msg_not_downloaded));
                Log.e(TAG, "Download Error Exception " + e.getMessage());
            }

            return null;
        }
    }*/
   private class DownloadingTask extends AsyncTask<MyTaskParams, Void, Void> {
        // SQLiteHandler  db = new SQLiteHandler(getApplicationContext());
        String apkStorage = null;
        File externalFilesDir = getExternalFilesDir(null);
        File outputFile = null;
        String dir = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (outputFile != null) {
                    modelCheckCount++;
                    //  Toast.makeText(getApplicationContext(), "Download Completed", Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "Download Failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Download Failed with Exception - " + e.getLocalizedMessage());
            }
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(MyTaskParams... params) {
            try {
                URL url = new URL(params[0].modelUrl);//Create Download URl
                HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
                c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
                c.connect();//connect the URL Connection

                //If Connection response is not OK then show Logs
                if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + c.getResponseCode()
                            + " " + c.getResponseMessage());

                }


                if (null != externalFilesDir) {
                    dir = externalFilesDir.getAbsolutePath();
                }
                String packageName = getPackageName();
                if (!TextUtils.isEmpty(dir)) {
                    if (!dir.endsWith(File.separator)) {
                        apkStorage = dir + File.separator ;
                    } else {
                        apkStorage = dir + packageName + File.separator;
                    }
                    try {
                        outputFile = new File(apkStorage,params[0].modelName);
                        if (!outputFile.exists()) {
                            outputFile.createNewFile();
                            Log.e(TAG, "File Created");
                        }

                       /* FileOutputStream outputStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.flush();
                        outputStream.close();*/

                        FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

                        InputStream is = c.getInputStream();//Get InputStream for connection

                        byte[] buffer = new byte[1024];//Set buffer type
                        int len1 = 0;//init length
                        while ((len1 = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len1);//Write new file
                        }

                        //Close all connection after doing task
                        fos.close();
                        is.close();
                        sqLiteHandler.updateDownloadStatus(Integer.parseInt(params[0].modelId), Integer.parseInt(params[0].modelVersion));

                    } catch (Exception e) {
                        //  ToastUtil.showToast(context, e.getMessage(), true);
                        e.printStackTrace();
                        outputFile = null;
                        snackBarRefresh(ModelCheckActivity.this, coordinatorLayout, getString(R.string.error_msg_not_downloaded));
                        Log.e(TAG, "Download Error Exception " + e.getMessage());
                    }
                }


            } catch (Exception e) {

                //Read exception if something went wrong
                e.printStackTrace();
                outputFile = null;
                snackBarRefresh(ModelCheckActivity.this, coordinatorLayout, getString(R.string.error_msg_not_downloaded));
                Log.e(TAG, "Download Error Exception " + e.getMessage());
            }

            return null;
        }
    }

    private boolean modelExist(String modelName) {
        boolean modelExist = true;
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

            File file = new File(filePath, modelName);
            if (!file.exists()) {
                return false;
            }
        }
        return modelExist;
    }
    /*private boolean modelExist(String modelName){
        boolean modelExist = true;
        File modelDir = null;
        File modelFile = null;
        if (new CheckForSDCard().isSDCardPresent()) {
            modelDir = new File(
                    Environment.getExternalStorageDirectory() + "/"
                            + Utils.downloadDirectory);
        } else {
            //  Toast.makeText(getApplicationContext(), "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();
        }
        //If File is not present create directory
        if (!modelDir.exists()) {
           return false;
        }

        modelFile = new File(modelDir, modelName);//Create Output file in Main File

        //Create New File if not present
        if (!modelFile.exists()) {
            return false;
        }

        return modelExist;
    }*/

    private void checkPermissions() {
        Dexter.withActivity(ModelCheckActivity.this)
                .withPermissions(
                        Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION/*,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE*/
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            // do you work now
                            mainHandler = new Handler(getMainLooper());
                            if (Utility.isConnectingToInternet(ModelCheckActivity.this)) {
                                if(tracker.getGpsStatus()){
                                modelPresenter.modelAsyncTask(activityId, languageId,width,height);
                                }else {
                                    snackBarRefresh(ModelCheckActivity.this, coordinatorLayout, getString(R.string.error_msg_gps_problem));
                                }
                            } else
                                snackBarRefresh(ModelCheckActivity.this, coordinatorLayout, getString(R.string.error_msg_network_problem));

                        }

                        List<PermissionDeniedResponse> deniedResponses = report.getDeniedPermissionResponses();

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permenantly, navigate user to app settings
                            showSettingsDialog();
                        }else {
                            if (!deniedResponses.isEmpty()) {
                                showSettingsDialog();
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ModelCheckActivity.this);
        builder.setTitle(getString(R.string.txt_need_permissions));
        builder.setMessage(getString(R.string.txt_app_settings_permissions));
        builder.setPositiveButton(getString(R.string.txt_goto_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton(getString(R.string.txt_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                checkPermissions();
            }
        });
        builder.show();

    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            //TODO something;
            checkPermissions();
        }
    }

    @Override
    protected void onDestroy() {
        tracker.stopUsingGPS();
        super.onDestroy();
        if(mainHandler!=null)
        mainHandler.removeCallbacks(myRunnable);

    }


}