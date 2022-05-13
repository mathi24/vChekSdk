package com.v_chek_host.vcheckhostsdk.model.api;

import android.app.Activity;

import androidx.annotation.NonNull;


import com.v_chek_host.vcheckhostsdk.model.entity.Result;
import com.v_chek_host.vcheckhostsdk.model.util.ToastUtils;

import okhttp3.Headers;


public class DefaultCallback<T extends Result> extends ForegroundCallback<T> {

    public DefaultCallback(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public final boolean onResultError(int code, Headers headers, Result.Error error) {
        if (code == 401) {
            return onResultAuthError(code, headers, error);
        } else {
            return onResultOtherError(code, headers, error);
        }
    }

    public boolean onResultAuthError(int code, Headers headers, Result.Error error) {
      /*  AlertDialogUtils.createBuilderWithAutoTheme(getActivity())
                .setMessage(R.string.access_token_out_of_date)
                .setPositiveButton(R.string.relogin, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LoginActivity.startForResult(getActivity());
                    }

                })
                .setNegativeButton(R.string.cancel, null)
                .show();*/
        return false;
    }

    public boolean onResultOtherError(int code, Headers headers, Result.Error error) {
        try {
            ToastUtils.with(getActivity()).show(error.getErrorMessage());
        }catch (Exception e){

        }
        return false;
    }

    @Override
    public boolean onCallException(Throwable t, Result.Error error) {
        try{
            ToastUtils.with(getActivity()).show(error.getErrorMessage());
        }catch (Exception e){

        }
        return false;
    }

}
