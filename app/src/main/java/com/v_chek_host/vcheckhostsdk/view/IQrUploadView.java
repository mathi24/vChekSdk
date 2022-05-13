package com.v_chek_host.vcheckhostsdk.view;

import androidx.annotation.NonNull;


import com.v_chek_host.vcheckhostsdk.model.entity.Result;

import retrofit2.Call;


public interface IQrUploadView {

    void onUploadError(@NonNull String message);

    void onUploadOk( @NonNull Result.QRUploadResponse uploadInfo);

    void onUploadStart(@NonNull Call<Result.QRUploadResponse> call);

    void onUploadFinish();

}
