package com.v_chek_host.vcheckhostsdk.model.api;



import com.v_chek_host.vcheckhostsdk.model.entity.ApiInspktResult;
import com.v_chek_host.vcheckhostsdk.model.entity.InspktResult;
import com.v_chek_host.vcheckhostsdk.model.entity.ParentResponseData;
import com.v_chek_host.vcheckhostsdk.model.entity.SendImage;
import com.v_chek_host.vcheckhostsdk.utils.ExceptionLog;
import com.v_chek_host.vcheckhostsdk.model.PushMonroneyDataModel;
import com.v_chek_host.vcheckhostsdk.model.entity.ApiRequest;
import com.v_chek_host.vcheckhostsdk.model.entity.Result;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface ApiService {

    @POST("vTallyOnStop/")
    Call<Result.QRUploadResponse> setUpload(@Body ApiRequest.QrUpload jsonObjectTitle);


    @POST("sp_get_all_models_by_activity/")
    Call<Result.ModelResponse> getModel(@Body ApiRequest.GetModel jsonObjectTitle);

    @POST("vchek_result_update/")
    Call<Result.BaseResponse> setModelResult(@Body ParentResponseData jsonObjectTitle);

    @Headers({"Content-Type: application/octet-stream"})
    @POST(".")
    Call<InspktResult> inspktImage(@Header("Prediction-Key") String apikey,@Body RequestBody sendImage);

    @Headers({"Content-Type: application/json"})
    @POST(".")
    Call<ApiInspktResult> apiInspktImage(@Header("Prediction-Key") String apikey, @Header("model_id") String modelId,
                                        @Header("locale_id") String localId, @Body SendImage sendImage);

    @FormUrlEncoded
    @POST("GlobalExceptionLog")
    Call<ExceptionLog> exceptionLogAPI(
            @Header("apikey") String apikey,
            @Field("app_id") int app_id,
            @Field("user_id") String user_id,
            @Field("device_details") String device_details,
            @Field("exception_details") String exception_details);

   /* @FormUrlEncoded
    @POST("PushMonroneyDataV3")
    Call<PushMonroneyDataModel> pushMonroneyDataV3(
            @Header("apikey") String apikey,
            @Field("VIN") String vin,
            @Field("employee_id") String employee_id,
            @Field("user_id") String user_id,
            @Field("parent_id") String parent_id,
            @Field("ml_model_id") String ml_model_id,
            @Field("ConnectionString") String ConnectionString,
            @Field("qc_result") String qcResult,
            @Field("Image") String image,
            @Field("qc_json_result") String qcJsonResult,
            @Field("SiteCode") String siteCode,
            @Field("SiteEmployeeCode") String siteEmployeeCode
    );*/


//
//    @GET("topics")
//    Call<Result.Data<List<Topic>>> getTopicList(
//            @Query("tab") TabType tab,
//            @Query("page") Integer page,
//            @Query("limit") Integer limit,
//            @Query("mdrender") Boolean mdrender
//    );
}


