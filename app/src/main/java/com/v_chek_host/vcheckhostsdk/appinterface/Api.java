package com.v_chek_host.vcheckhostsdk.appinterface;



import com.v_chek_host.vcheckhostsdk.model.PushMonroneyDataModel;
import com.v_chek_host.vcheckhostsdk.utils.ExceptionLog;
import com.v_chek_host.vcheckhostsdk.utils.MonroneyModel;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface Api {
    @FormUrlEncoded
    @POST("GlobalExceptionLog")
    Call<ExceptionLog> exceptionLogAPI(
            @Header("apikey") String apikey,
            @Field("app_id") int app_id,
            @Field("user_id") String user_id,
            @Field("device_details") String device_details,
            @Field("site_id") String site_id,
            @Field("activity_id") String activity_id,
            @Field("ml_model_id") String ml_model_id,
            @Field("mobile_type") String mobile_type,
            @Field("exception_details") String exception_details);

}
