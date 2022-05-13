package com.v_chek_host.vcheckhostsdk.presenter.implement;

import android.app.Activity;

import androidx.annotation.NonNull;


import com.v_chek_host.vcheckhostsdk.model.api.ApiClient;
import com.v_chek_host.vcheckhostsdk.model.api.DefaultCallback;
import com.v_chek_host.vcheckhostsdk.model.entity.ApiRequest;
import com.v_chek_host.vcheckhostsdk.model.entity.Result;
import com.v_chek_host.vcheckhostsdk.presenter.contract.IUploadPresenter;
import com.v_chek_host.vcheckhostsdk.view.IQrUploadView;

import okhttp3.Headers;
import retrofit2.Call;


public class UploadPresenter implements IUploadPresenter {

    private final Activity activity;
    private final IQrUploadView iQrUploadView;

    public UploadPresenter(@NonNull Activity activity, @NonNull IQrUploadView qrUploadView) {
        this.activity = activity;
        this.iQrUploadView = qrUploadView;
    }

    @Override
    public void uploadAsyncTask(final String vinNo, final String image) {

            Call<Result.QRUploadResponse> call = ApiClient.service.setUpload(new ApiRequest.
                    QrUpload("1","1","2","9",
                    "1","2",image,
                    "","","","",vinNo));
        iQrUploadView.onUploadStart(call);
            call.enqueue(new DefaultCallback<Result.QRUploadResponse>(activity) {
                @Override
                public boolean onResultOk(int code, Headers headers, Result.QRUploadResponse loginInfo) {
                    iQrUploadView.onUploadOk( loginInfo);
                    return false;
                }

                @Override
                public boolean onResultAuthError(int code, Headers headers, Result.Error error) {
                    iQrUploadView.onUploadError("Error");
                    return false;
                }

                @Override
                public void onFinish() {
                    iQrUploadView.onUploadFinish();
                }

            });

    }

}
