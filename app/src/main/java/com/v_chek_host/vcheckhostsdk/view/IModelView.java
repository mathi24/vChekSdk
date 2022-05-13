package com.v_chek_host.vcheckhostsdk.view;

import androidx.annotation.NonNull;

import com.v_chek_host.vcheckhostsdk.model.entity.Result;

import retrofit2.Call;


public interface IModelView {

    void onModelError(@NonNull String message);

    void onModelOk( @NonNull Result.ModelResponse modelInfo);

    void onModelStart(@NonNull Call<Result.ModelResponse> call);

    void onModelFinish();

}
