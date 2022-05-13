package com.v_chek_host.vcheckhostsdk.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.v_chek_host.vcheckhostsdk.BuildConfig;
import com.v_chek_host.vcheckhostsdk.appinterface.Api;
import com.v_chek_host.vcheckhostsdk.model.api.ApiDefine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.v_chek_host.vcheckhostsdk.model.api.ApiClient.createUserAgentInterceptor;

//import com.aimagix.vehicleinspection.classes.MyApplication;

public class Utility {
    public static Boolean isConnectingToInternet(final Activity activity, final Context ctx) {

        long startTime = Calendar.getInstance().getTimeInMillis();
        Boolean isConnected = false;
        int check = 0;
        try {
            ConnectivityManager connectivity = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            isConnected = info != null && info.isAvailable() && info.isConnected();

            if (isConnected == true) {
                check = 1;
            }

            switch (check) {
                case 0:
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // your stuff to update the UI
                            Toast.makeText(ctx, "No internet connection", Toast.LENGTH_SHORT).show();
                            // customToast(ctx, ctx.getString(R.string.error_no_internet));
//                            Snackbar snackbar = Snackbar.make(activity.getCurrentFocus(),
//                                    ctx.getString(R.string.error_no_internet), Snackbar.LENGTH_LONG);
//                            snackbar.show();
                        }
                    });
                    break;
                case 1:
                    //  Log.d(TAG,"Internet Connection available");
                    Log.d("isConnected: ", "" + isConnected);
                    long endtime = Calendar.getInstance().getTimeInMillis();
                    long totalTime = endtime - startTime;
                    System.out.println("Time taken for conectivity check: " + totalTime);
            }
            return isConnected;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isConnected;
    }

    public static boolean isConnectingToInternet(Activity ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    public static Bitmap rotateImageIfRequired(Context context, Bitmap img) throws IOException {
        if (img.getHeight() < img.getWidth()) {
            return rotateImage(img, 90);//(img, 270);
        }
        return img;
    }

    public static Bitmap imageRotation(Context context, String path, Bitmap bitmap) {

        Bitmap rotatedBitmap = null;
        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);


            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    System.out.println("imagerotation " + 90);
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    System.out.println("imagerotation " + 180);
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    System.out.println("imagerotation " + 270);
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotatedBitmap;
    }

    public static String getWidth(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
       // int height = metrics.heightPixels;

        return  width+"" ;
    }

    public static String getHeight(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int height = metrics.heightPixels;
        return  height +"";
    }

    public static String deviceDetail = Build.MANUFACTURER
            + ", " + Build.MODEL +
            ", " + Build.VERSION.RELEASE+
            ", SDK_Ver : " + BuildConfig.SDK_VERSION_CODE
            + ", " + osName();

    public static String getDeviceUDID(Context ctx) {
        return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getDeviceIMEI(Context ctx) {
        String ts = Context.TELEPHONY_SERVICE;
        TelephonyManager mTelephonyMgr = (TelephonyManager) ctx.getSystemService(ts);
      //  @SuppressLint("MissingPermission") String imsi = mTelephonyMgr.getSubscriberId();
        @SuppressLint("MissingPermission") String imei = mTelephonyMgr.getDeviceId();
        return imei;
    }

    @SuppressLint("MissingPermission")
    public static String getDeviceId(Context context) {

        String deviceId;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephony.getDeviceId() != null) {
                deviceId = mTelephony.getDeviceId();
            } else {
                deviceId = Settings.Secure.getString(
                        context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            }
        }

        return deviceId;
    }

    @SuppressLint("MissingPermission")
    public static String getIMEIDeviceId(Context context) {

        String deviceId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return "";
                }
            }
            assert mTelephony != null;
            if (mTelephony.getDeviceId() != null)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    deviceId = mTelephony.getImei();
                }else {
                    deviceId = mTelephony.getDeviceId();
                }
            } else {
                deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }
        Log.d("deviceId", deviceId);
        return deviceId;
    }
    static String osName() {
        String osName = "";

        Field[] fields = Build.VERSION_CODES.class.getFields();
//        String codeName = "UNKNOWN";
        for (Field field : fields) {
            try {
                if (field.getInt(Build.VERSION_CODES.class) == Build.VERSION.SDK_INT) {
                    osName = field.getName();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return osName;
    }

    static boolean isExceptionAPILoading = false;


//        InputStream input = context.getContentResolver().openInputStream(selectedImage);

//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        img.compress(Bitmap.CompressFormat.JPEG /*PNG*/, 100 /*ignored for PNG*/, bos);
//        ExifInterface ei=null;
//        if (Build.VERSION.SDK_INT > 23) {
//            byte[] bitmapdata = bos.toByteArray();
//            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
//            ei = new ExifInterface(bs);
//        } else {
//            try {
//                ContentValues values = new ContentValues();
//                values.put(MediaStore.Images.Media.TITLE, "Title");
//                values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
//                Uri selectedImage = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
////            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), img, "Title", null);
////            if (img != null){
////                App.bitmapValue = "bmp height: "+img.getHeight()+"bmp height: "+img.getWidth();
////            }else{
////                    App.bitmapValue = "bmp null";
////            }
////            App.uriPath=selectedImage==null?"null":selectedImage.getPath();
////            Uri selectedImage = Uri.parse(path);
//                ei = new ExifInterface(selectedImage.getPath());
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//        int orientation = 0;
//        if(img.getHeight()<img.getWidth()){
//            return rotateImage(img, 90);//(img, 270);
//        }

//        if (ei != null) {
//            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//        }
//        switch (orientation) {
//            case ExifInterface.ORIENTATION_UNDEFINED:
//                return rotateImage(img, 90);//(img, 270);
//            case ExifInterface.ORIENTATION_ROTATE_90:
//                return rotateImage(img, 90);
//            case ExifInterface.ORIENTATION_ROTATE_180:
//                return rotateImage(img, 180);
//            case ExifInterface.ORIENTATION_ROTATE_270:
//                return rotateImage(img, 270);
//            default:
//                return img;
//        }

//        return img;
//    }


    public static Bitmap rotateImage(Bitmap imge, int degree) {
        Bitmap workingBitmap = Bitmap.createBitmap(imge);
        Bitmap img = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
//        Canvas canvas = new Canvas(img);
//        canvas.save(); //save the position of the canvas
//        int h=img.getHeight();
//        int w=img.getWidth();
//
//        canvas.rotate(degree, w + (w/ 2), h + (h/ 2)); //rotate the canvas
//        canvas.drawBitmap(img, w, h, null); //draw the image on the rotated canvas
//        canvas.restore();  // restore the canvas position.
//        AppData.getInstance().imageRes = "Before rotate wxh:" + img.getWidth() + "x" + img.getHeight() + ", ";
//        img = ImageUtils.resize(img, 375, 1000);
//        img = ImageUtils.resize(img, 275, 275);
        //        img = ImageUtils.resize(img, 500, 1000);
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        img = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
//        img=ImageUtils.resize(img, 375, 1000);
//        AppData.getInstance().imageRes = AppData.getInstance().imageRes+ " After rotate wxh:" + img.getWidth() + "x" + img.getHeight() + ", ";
//        img.recycle();
        return /*img*/ img;
    }

    public static String compressImage(String imageUri, Context ctx) {

        String filePath = getRealPathFromURI(imageUri,ctx);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 1000.0f;
        float maxWidth = 1000.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public static String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    private static String getRealPathFromURI(String contentURI, Context context) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public static String getLog(String errors){
        String logData = "";
        logData = logData + "Error log taken at: " +
                getCurrentTimeStamp("yyyy-MM-dd HH:mm:ss") + "\n\n";
        logData = logData+" \nApplication name: Vchek SDK ";
        logData=logData+errors;
        logData = logData + "------------------------------------------------" + "\n\n\n";
        return logData;
    }

    private static String getCurrentTimeStamp(String timeFormat) {
        String currentTimeStamp = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(timeFormat,
                    java.util.Locale.getDefault());
            currentTimeStamp = dateFormat.format(new Date());
        } catch (Exception e) {
            Log.e("FileLog", Log.getStackTraceString(e));
        }
        return currentTimeStamp;
    }

    public static void exceptionLog(Activity activity, String exception,String userIdStr,String siteIdStr
            ,String activityIdStr,String modelIdStr,String type) {
        System.out.println("Exception log API called");
        if (!isExceptionAPILoading) {
            if (Utility.isConnectingToInternet(activity, activity)) {
                OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
                httpClient.readTimeout(80, TimeUnit.SECONDS);
                httpClient.connectTimeout(80, TimeUnit.SECONDS);
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                //logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                httpClient.addInterceptor(new LoggingInterceptor());
                httpClient.addInterceptor(createUserAgentInterceptor());
//            encryptImage(buf);
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BuildConfig.BASE_URL)
                        .client(httpClient.build())
                        .addConverterFactory(GsonConverterFactory.create()) //Here we are using the GsonConverterFactory to directly convert json data to object
                        .build();
                Api api = retrofit.create(Api.class);
                isExceptionAPILoading = true;
                System.out.println("SDK Exception log API called internet avail" +userIdStr +" "+ deviceDetail + " "+ exception);
                Call<ExceptionLog> call = api.exceptionLogAPI(
                        StrConstant.API_KEY,
                        3,
                        userIdStr,
                        deviceDetail,siteIdStr,
                        activityIdStr,
                        modelIdStr,type,exception
                );
                call.enqueue(new Callback<ExceptionLog>() {
                    @Override
                    public void onResponse(Call<ExceptionLog> call, Response<ExceptionLog> response) {
                        isExceptionAPILoading = false;
                        System.out.println("SDK Exception log API onResponse:" + response.code() + "");
                        int serverCode = response.code();
                        if (serverCode == 200 && response.body() != null) {
                            // progressDialog.dismiss();
//                        PastVehicleHistoryModel pastVehicleHistoryModel = response.body();
                            int responceCode = response.body().getStatusCode();
                            if (responceCode == 200) {
                            } else {
//                            Toast.makeText(getContext(), R.string.txt_something_went_wrong_please_try_again_later, Toast.LENGTH_SHORT).show();
                            }
                        } else {
//                        Toast.makeText(getContext(), R.string.txt_something_went_wrong_please_try_again_later, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onFailure(Call<ExceptionLog> call, Throwable t) {
                        isExceptionAPILoading = false;
                        System.out.println("Exception log API Failure:" + t.getMessage());
                        if (Objects.equals(t.getMessage(), "timeout")) {
//                        Toast.makeText(getContext(), R.string.txt_poor_internet_connection_please_check_your_internet_and_try_again, Toast.LENGTH_SHORT).show();
                        } else {
//                        Toast.makeText(getContext(), R.string.txt_something_went_wrong_please_try_again_later, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
