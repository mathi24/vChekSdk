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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.ImageReader.OnImageAvailableListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Base64;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.v_chek_host.vcheckhostsdk.R;
import com.v_chek_host.vcheckhostsdk.VinNumberActivity;
import com.v_chek_host.vcheckhostsdk.ml.adapter.ListviewAdapter;
import com.v_chek_host.vcheckhostsdk.ml.adapter.OcrListAdapter;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import okhttp3.internal.cache.DiskLruCache;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class VinCompareCaptureActivity extends VinCompareTrainCameraActivity implements OnImageAvailableListener {

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
    public TextView tvScanTitle, tvScanSubTitle;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
     /*   DESIRED_PREVIEW_SIZE = new Size(getScreenHeight(getApplicationContext()),
                getScreenWidth(getApplicationContext()));
        System.out.println("DESIRED_PREVIEW_SIZE " + DESIRED_PREVIEW_SIZE);*/
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

    private static Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
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
                            isVideoRecordStarted=false;
                            if (rgbFrameBitmap != null) {
                                /*Bitmap reSizedBitmap= rgbFrameBitmap;
                                reSizedBitmap = rotate(reSizedBitmap, 90);*/
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
                                    /*if (rgbFrameBitmap != null && !rgbFrameBitmap.isRecycled()) {
                                        rgbFrameBitmap.recycle();
                                        rgbFrameBitmap = null;
                                    }*/
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
                           // Toast.makeText(VinCompareCaptureActivity.this, barcodes.get(0).getDisplayValue(), Toast.LENGTH_SHORT).show();
                            Intent data = new Intent();
                            // data.putExtra(BarcodeObject, best);
                            data.putExtra("scan_no", barcodes.get(0).getDisplayValue());
                            //setResult(CommonStatusCodes.SUCCESS, data);
                            setResult(RESULT_OK, data);
                            finish();
                        } else {
                            isVideoRecordStarted = false;
                            startVideo.setVisibility(View.VISIBLE);
                            Toast.makeText(VinCompareCaptureActivity.this, "Not found", Toast.LENGTH_SHORT).show();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (rgbFrameBitmap != null && !rgbFrameBitmap.isRecycled()) {
                                        rgbFrameBitmap.recycle();
                                        rgbFrameBitmap = null;
                                    }
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
                        Toast.makeText(VinCompareCaptureActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (rgbFrameBitmap != null && !rgbFrameBitmap.isRecycled()) {
                                    rgbFrameBitmap.recycle();
                                    rgbFrameBitmap = null;
                                }
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
                                //    showOcrList(visionText);
                                } else {
                                    isVideoRecordStarted = false;
                                    startVideo.setVisibility(View.VISIBLE);
                                    Toast.makeText(VinCompareCaptureActivity.this, "Not found", Toast.LENGTH_SHORT).show();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (rgbFrameBitmap != null && !rgbFrameBitmap.isRecycled()) {
                                                rgbFrameBitmap.recycle();
                                                rgbFrameBitmap = null;
                                            }
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
                                        Toast.makeText(VinCompareCaptureActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (rgbFrameBitmap != null && !rgbFrameBitmap.isRecycled()) {
                                                    rgbFrameBitmap.recycle();
                                                    rgbFrameBitmap = null;
                                                }
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

   /* public void showListOfOcrText(Text visionText) {

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
            ocrList.setOcrText(input);
            ocrListList.add(ocrList);
        }
        if (ocrListList.size() > 0) {
            ocrListList.get(0).setSelected(true);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        OcrListAdapter ocrListAdapter = new OcrListAdapter(ocrListList, getApplicationContext());
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
                        if (rgbFrameBitmap != null && !rgbFrameBitmap.isRecycled()) {
                            rgbFrameBitmap.recycle();
                            rgbFrameBitmap = null;
                        }
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
                        System.out.println("SelectedItem " + ocrList.getOcrText());
                        Intent data = new Intent();
                        // data.putExtra(BarcodeObject, best);
                        data.putExtra("scan_no", ocrList.getOcrText());
                        //setResult(CommonStatusCodes.SUCCESS, data);
                        setResult(RESULT_OK, data);
                        finish();
                        break;
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
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }*/

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
            ocrList.setOcrText(input);
            ocrListList.add(ocrList);
        }
        if (ocrListList.size() > 0) {
            ocrListList.get(0).setSelected(true);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        OcrListAdapter ocrListAdapter = new OcrListAdapter(ocrListList, VinCompareCaptureActivity.this,0);
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
                        if (rgbFrameBitmap != null && !rgbFrameBitmap.isRecycled()) {
                            rgbFrameBitmap.recycle();
                            rgbFrameBitmap = null;
                        }
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
                        System.out.println("SelectedItem " + ocrList.getOcrText());
                        Intent data = new Intent();
                        // data.putExtra(BarcodeObject, best);
                        data.putExtra("scan_no", ocrList.getOcrText());
                        //setResult(CommonStatusCodes.SUCCESS, data);
                        setResult(RESULT_OK, data);
                        finish();
                        break;
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

    /*public void showOcrList(Text visionText) {

        List<OcrListAdapter.OcrList> ocrListList = new ArrayList<>();
        ViewGroup viewGroup = findViewById(android.R.id.content);
        ListviewAdapter adpter;
        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.ocr_list_view, viewGroup, false);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        ListView listView =dialogView.findViewById(R.id.listview);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit);
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            OcrListAdapter.OcrList ocrList = new OcrListAdapter.OcrList();
            ocrList.setOcrText(block.getText());
            ocrListList.add(ocrList);
        }
        if (ocrListList.size() > 0) {
            ocrListList.get(0).setSelected(true);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        OcrListAdapter ocrListAdapter = new OcrListAdapter(ocrListList, getApplicationContext());
        recyclerView.setAdapter(ocrListAdapter);
        adpter=new ListviewAdapter(this,ocrListList);
        listView.setAdapter(adpter);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                isVideoRecordStarted = false;
                startVideo.setVisibility(View.VISIBLE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (rgbFrameBitmap != null && !rgbFrameBitmap.isRecycled()) {
                            rgbFrameBitmap.recycle();
                            rgbFrameBitmap = null;
                        }
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
                        System.out.println("SelectedItem " + ocrList.getOcrText());
                        Intent data = new Intent();
                        // data.putExtra(BarcodeObject, best);
                        data.putExtra("scan_no", ocrList.getOcrText());
                        //setResult(CommonStatusCodes.SUCCESS, data);
                        setResult(RESULT_OK, data);
                        finish();
                        break;
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
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }*/

    @Override
    protected void onInferenceConfigurationChanged() {
        if (rgbFrameBitmap == null) {
            // Defer creation until we're getting camera frames.
            return;
        }
      /*  final Classifier.Device device = getDevice();
        final Classifier.Model model = getModel();
        final int numThreads = getNumThreads();*/
        //  runInBackground(() -> recreateClassifier(model, device, numThreads));
    }

    /*private void recreateClassifier(Classifier.Model model, Classifier.Device device, int numThreads) {
     */
    /* if (classifier != null) {
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
        imageSizeY = classifier.getImageSizeY();*//*
    }*/

    void initViewAction() {
        startVideo = findViewById(R.id.startCapture);
        ivClose = findViewById(R.id.image_close);
        tvSingleAndMulti = findViewById(R.id.tvSingleAndMulti);
        ivCapturePrevieImageView = findViewById(R.id.ivCapturePreview);
        ivCapturePrevieImageView2 = findViewById(R.id.ivCapturePreview2);
        SwitchMaterial switchVideo = findViewById(R.id.video_switch);
        ImageView ivVideo = findViewById(R.id.ivVideo);
        tvScanTitle = findViewById(R.id.tvScanTitle);
        tvScanSubTitle = findViewById(R.id.tvScanSubtitle);
        rectangleOverLayBARCodeView = (RectangleOverLayBARCodeView) findViewById(R.id.brCodeOverlay);
        rectangleOverLayQRCodeView = (RectangleOverLayQRCodeView) findViewById(R.id.qrCodeOverLay);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        tvScanTitle.setText("Scan Barcode");
        tvScanSubTitle.setText("Capture the Barcode from the\nShipping label");
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
            Intent intent = new Intent(VinCompareCaptureActivity.this, PreviewImageActivity.class);
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
                tvScanTitle.setText("Scan Barcode");
                tvScanSubTitle.setText("Capture the Barcode from the\nShipping label");
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
                tvScanTitle.setText("Scan Text");
                tvScanSubTitle.setText("Capture the Text from the Shipping label");
                return true;
            } else if (itemId == R.id.navigation_qr2) {
                rectangleOverLayBARCodeView.setVisibility(View.GONE);
                rectangleOverLayQRCodeView.setVisibility(View.VISIBLE);
                selectedItem = 2;
                tvScanTitle.setText("Scan QR code");
                tvScanSubTitle.setText("Capture the QR code from the\nShipping label");
                return true;
            }
            return false;
        }
    };

    @Override
    public synchronized void onPause() {
        super.onPause();
        finish();
    }

}
