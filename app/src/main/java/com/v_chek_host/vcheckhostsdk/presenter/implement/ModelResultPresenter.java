package com.v_chek_host.vcheckhostsdk.presenter.implement;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.v_chek_host.vcheckhostsdk.model.api.ApiClient;
import com.v_chek_host.vcheckhostsdk.model.api.DefaultCallback;
import com.v_chek_host.vcheckhostsdk.model.entity.ApiRequest;
import com.v_chek_host.vcheckhostsdk.model.entity.ParentResponseData;
import com.v_chek_host.vcheckhostsdk.model.entity.Result;
import com.v_chek_host.vcheckhostsdk.presenter.contract.IModelPresenter;
import com.v_chek_host.vcheckhostsdk.presenter.contract.IModelResultPresenter;
import com.v_chek_host.vcheckhostsdk.view.IModelResultView;
import com.v_chek_host.vcheckhostsdk.view.IModelView;

import okhttp3.Headers;
import retrofit2.Call;


public class ModelResultPresenter implements IModelResultPresenter {

    private final Activity activity;
    private final IModelResultView modelResultView;

    public ModelResultPresenter(@NonNull Activity activity, @NonNull IModelResultView modelResultView) {
        this.activity = activity;
        this.modelResultView = modelResultView;
    }

    @Override
    public void modelResultAsyncTask(final ParentResponseData parentResponseData) {

            Call<Result.BaseResponse> call = ApiClient.service.setModelResult(parentResponseData);
        modelResultView.onModelResultStart(call);
            call.enqueue(new DefaultCallback<Result.BaseResponse>(activity) {
                @Override
                public boolean onResultOk(int code, Headers headers, Result.BaseResponse baseResponse) {
                    modelResultView.onModelResultOk( baseResponse);
                    return false;
                }

                @Override
                public boolean onResultAuthError(int code, Headers headers, Result.Error error) {
                    modelResultView.onModelResultError(error.getErrorMessage());
                    return false;
                }

                @Override
                public void onFinish() {
                    modelResultView.onModelResultFinish();
                }

            });

    }

}
