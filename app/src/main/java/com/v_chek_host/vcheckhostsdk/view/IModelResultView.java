package com.v_chek_host.vcheckhostsdk.view;

import androidx.annotation.NonNull;

import com.v_chek_host.vcheckhostsdk.model.entity.Result;

import retrofit2.Call;


public interface IModelResultView {

    void onModelResultError(@NonNull String message);

    void onModelResultOk( @NonNull Result.BaseResponse baseResponse);

    void onModelResultStart(@NonNull Call<Result.BaseResponse> call);

    void onModelResultFinish();

}
