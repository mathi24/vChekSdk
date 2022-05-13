package com.v_chek_host.vcheckhostsdk.presenter.implement;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.v_chek_host.vcheckhostsdk.model.api.ApiClient;
import com.v_chek_host.vcheckhostsdk.model.api.DefaultCallback;
import com.v_chek_host.vcheckhostsdk.model.entity.ApiRequest;
import com.v_chek_host.vcheckhostsdk.model.entity.Result;
import com.v_chek_host.vcheckhostsdk.presenter.contract.IModelPresenter;
import com.v_chek_host.vcheckhostsdk.presenter.contract.IUploadPresenter;
import com.v_chek_host.vcheckhostsdk.view.IModelView;
import com.v_chek_host.vcheckhostsdk.view.IQrUploadView;

import okhttp3.Headers;
import retrofit2.Call;


public class ModelPresenter implements IModelPresenter {

    private final Activity activity;
    private final IModelView iModelView;

    public ModelPresenter(@NonNull Activity activity, @NonNull IModelView iModelView) {
        this.activity = activity;
        this.iModelView = iModelView;
    }

    @Override
    public void modelAsyncTask(final String activityId, final String langId,final String width,final String height) {

            Call<Result.ModelResponse> call = ApiClient.service.getModel(new ApiRequest.
                    GetModel(activityId,langId,width,height));
        iModelView.onModelStart(call);
            call.enqueue(new DefaultCallback<Result.ModelResponse>(activity) {
                @Override
                public boolean onResultOk(int code, Headers headers, Result.ModelResponse modeInfo) {
                    iModelView.onModelOk( modeInfo);
                    return false;
                }

                @Override
                public boolean onResultAuthError(int code, Headers headers, Result.Error error) {
                    iModelView.onModelError(error.getErrorMessage());
                    return false;
                }

                @Override
                public void onFinish() {
                    iModelView.onModelFinish();
                }

            });

    }

}
