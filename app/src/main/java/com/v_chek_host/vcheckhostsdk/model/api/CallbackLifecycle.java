package com.v_chek_host.vcheckhostsdk.model.api;







import com.v_chek_host.vcheckhostsdk.model.entity.Result;

import okhttp3.Headers;


public interface CallbackLifecycle<T extends Result> {

    boolean onResultOk(int code, Headers headers, T result);

    boolean onResultError(int code, Headers headers, Result.Error error);

    boolean onCallCancel();

    boolean onCallException(Throwable t, Result.Error error);

    void onFinish();

}
