package com.v_chek_host.vcheckhostsdk.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.v_chek_host.vcheckhostsdk.model.entity.ParentMetaData;

/**
 * Created by Quintus Labs on 18-Feb-2019.
 * www.quintuslabs.com
 */
public class PreferenceStorage {



    public static final String PARENT_DATA = "parent_data";
    public static final String KEY_USER_ADDRESS = "user_address";
    public static final String KEY_CURRENT_ACTIVITY= "current_activity";
    public static final String KEY_CURRENT_ACTIVITY_MODELS = "activity_models";
    public static final String KEY_MODELS_STEP = "activity_models_step";
    public static final String KEY_MODELS_RESULT = "activity_models_result";
    public static final String KEY_OVERLAY_URL = "activity_models_overlay";
    public static final String KEY_MODEL_PREVIEW_HEIGHT = "model_preview_height";
    public static final String KEY_MODEL_PREVIEW_WIDTH = "model_preview_width";
    public static final String KEY_VIN_NEED = "vin_need";
    public static final String KEY_VIN_COMPARE = "vin_compare";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_SITE_ID = "site_id";
    public static final String KEY_ACTIVITY_ID = "activity_id";
    public static final String KEY_MODEL_ID = "model_id";
    public static final String KEY_VIN_MAX_LENGHT = "vin_max_lenght";
    public static final String KEY_ACTIVITY_PASS_MSG = "activity_pass_img";
    public static final String KEY_ACTIVITY_FAIL_MSG = "activity_fail_img";
    private static final String IS_USER_LOGIN = "IsUserLoggedIn";

    private static final String MODEL_IMAGE_WIDTH= "MODEL_IMAGE_WIDTH";
    private static final String MODEL_IMAGE_HEIGHT= "MODEL_IMAGE_HEIGHT";
    private static final String MODEL_BOTTOM_CROP_HEIGHT= "MODEL_BOTTOM_CROP_HEIGHT";
    private static final String MODEL_OVERLAY= "MODEL_OVERLAY";


    private static PreferenceStorage instance = null;
    SharedPreferences sharedPreferences;
    Editor editor;
    int PRIVATE_MODE = 0;
    Context _context;

    public PreferenceStorage(Context context) {
        sharedPreferences = context.getSharedPreferences("vCheckPreferences", 0);
    }

    public static PreferenceStorage getInstance(Activity context) {
        if (instance == null) {
            synchronized (PreferenceStorage.class) {
                if (instance == null) {
                    instance = new PreferenceStorage(context);
                }
            }
        }
        return instance;
    }

    public void saveParentData(String parentDataJson){
        editor = sharedPreferences.edit();
        editor.putString(PARENT_DATA, parentDataJson);
        editor.commit();
    }

    public ParentMetaData getParentData(){
        Gson gson = new Gson();
        String json = sharedPreferences.getString(PARENT_DATA, "");
        if(json.equalsIgnoreCase("")){
            return null;
        }
        ParentMetaData obj = gson.fromJson(json, ParentMetaData.class);
        return obj;
    }


    public void logoutUser() {
        editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    public boolean checkLogin() {
        // Check login status
        return !this.isUserLoggedIn();
    }


    public boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(IS_USER_LOGIN, false);
    }


    public String getActivity() {
        if (sharedPreferences.contains(KEY_CURRENT_ACTIVITY))
            return sharedPreferences.getString(KEY_CURRENT_ACTIVITY, null);
        else return null;
    }


    public void setActivity(String activity) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CURRENT_ACTIVITY, activity);
        editor.commit();
    }


    public String getActivityModel() {
        if (sharedPreferences.contains(KEY_CURRENT_ACTIVITY_MODELS))
            return sharedPreferences.getString(KEY_CURRENT_ACTIVITY_MODELS, null);
        else return null;
    }



    public void setActivityModel(String model) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CURRENT_ACTIVITY_MODELS, model);
        editor.commit();
    }

    public String getStep() {
        if (sharedPreferences.contains(KEY_MODELS_STEP))
            return sharedPreferences.getString(KEY_MODELS_STEP, null);
        else return null;
    }


    public void setStep(String modelStep) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_MODELS_STEP, modelStep);
        editor.commit();
    }

    public String getModelResult() {
        if (sharedPreferences.contains(KEY_MODELS_RESULT))
            return sharedPreferences.getString(KEY_MODELS_RESULT, "[]");
        else return null;
    }


    public void setModelResult(String modelResult) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_MODELS_RESULT, modelResult);
        editor.commit();
    }


    public String getOverLayUrl() {
        if (sharedPreferences.contains(KEY_OVERLAY_URL))
            return sharedPreferences.getString(KEY_OVERLAY_URL, null);
        else return null;
    }


    public void setOverlayUrl(String modelOverlay) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_OVERLAY_URL, modelOverlay);
        editor.commit();
    }

    public String getUserId() {
        if (sharedPreferences.contains(KEY_USER_ID))
            return sharedPreferences.getString(KEY_USER_ID, "0");
        else return null;
    }


    public void setUserId(String userId) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.commit();
    }

    public String getSiteId() {
        if (sharedPreferences.contains(KEY_SITE_ID))
            return sharedPreferences.getString(KEY_SITE_ID, "0");
        else return null;
    }


    public void setSiteId(String siteId) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SITE_ID, siteId);
        editor.commit();
    }

    public String getActivityId() {
        if (sharedPreferences.contains(KEY_ACTIVITY_ID))
            return sharedPreferences.getString(KEY_ACTIVITY_ID, "0");
        else return null;
    }


    public void setActivityId(String activityId) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACTIVITY_ID, activityId);
        editor.commit();
    }

    public String getModelId() {
        if (sharedPreferences.contains(KEY_MODEL_ID))
            return sharedPreferences.getString(KEY_MODEL_ID, "0");
        else return null;
    }


    public void setModelId(String modelId) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_MODEL_ID, modelId);
        editor.commit();
    }
    public String getActivityPassMsg() {
        if (sharedPreferences.contains(KEY_ACTIVITY_PASS_MSG))
            return sharedPreferences.getString(KEY_ACTIVITY_PASS_MSG, null);
        else return null;
    }


    public void setActivityPassMsg(String msg) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACTIVITY_PASS_MSG, msg);
        editor.commit();
    }

    public String getActivityFailMsg() {
        if (sharedPreferences.contains(KEY_ACTIVITY_FAIL_MSG))
            return sharedPreferences.getString(KEY_ACTIVITY_FAIL_MSG, null);
        else return null;
    }


    public void setActivityFailMsg(String msg) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACTIVITY_FAIL_MSG, msg);
        editor.commit();
    }

    public int getPreviewWidth() {
        return sharedPreferences.getInt(KEY_MODEL_PREVIEW_WIDTH, 640);
    }

    public void setPreviewWidth(final int width)
    {
        Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_MODEL_PREVIEW_WIDTH, width);
        editor.apply();
    }

    public int getPreviewHeight() {
        return sharedPreferences.getInt(KEY_MODEL_PREVIEW_HEIGHT, 480);
    }

    public void setPreviewHeight(final int height)
    {
        Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_MODEL_PREVIEW_HEIGHT, height);
        editor.apply();
    }

    public int getVinNeed() {
        return sharedPreferences.getInt(KEY_VIN_NEED, 1);
    }

    public void setVinNeed(final int vinNeed)
    {
        Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_VIN_NEED, vinNeed);
        editor.apply();
    }

    public int isVinCompare() {
        return sharedPreferences.getInt(KEY_VIN_COMPARE, 0);
    }

    public void setVinCompare(final int vinCompare)
    {
        Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_VIN_COMPARE, vinCompare);
        editor.apply();
    }

    public int getVinMaxLenght() {
        return sharedPreferences.getInt(KEY_VIN_MAX_LENGHT, 1);
    }

    public void setVinMaxLength(final int vinMaxLength)
    {
        Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_VIN_MAX_LENGHT, vinMaxLength);
        editor.apply();
    }

    public void deleteCart() {
        Editor editor = sharedPreferences.edit();
        editor.remove("CART");
        editor.commit();
    }


    public String getOrder() {
        if (sharedPreferences.contains("ORDER"))
            return sharedPreferences.getString("ORDER", null);
        else return null;
    }


    public void setOrder(String order) {
        Editor editor = sharedPreferences.edit();
        editor.putString("ORDER", order);
        editor.commit();
    }

    public void deleteOrder() {
        Editor editor = sharedPreferences.edit();
        editor.remove("ORDER");
        editor.commit();
    }
    public static int getModelImageHeight(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int imageHeight = sp.getInt(MODEL_IMAGE_HEIGHT, 480);
        return imageHeight;
    }

    public static void setModelImageHeight(Context context, int modelImageHeight) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(MODEL_IMAGE_HEIGHT,modelImageHeight).commit();
    }

    public static int getModelImageWidth(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int imageWidth = sp.getInt(MODEL_IMAGE_WIDTH, 640);
        return imageWidth;
    }

    public static void setModelImageWidth(Context context, int modelImageWidth) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(MODEL_IMAGE_WIDTH,modelImageWidth).commit();
    }

    public static int getModelBottomCropHeight(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int cropHeight = sp.getInt(MODEL_BOTTOM_CROP_HEIGHT, 100);
        return cropHeight;
    }

    public static void setModelBottomCropHeight(Context context, int bottomCropHeight) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(MODEL_BOTTOM_CROP_HEIGHT,bottomCropHeight).commit();
    }
    public static String getOverlayUrl(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String activityName = sp.getString(MODEL_OVERLAY, null);
        return activityName;
    }

    public static void setOverlayUrl(Context context, String overlay) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(MODEL_OVERLAY, overlay).commit();
    }
}
