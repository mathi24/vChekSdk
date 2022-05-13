package com.v_chek_host.vcheckhostsdk;

public interface JSONObjectCallBack {
    public void onSuccess(String response);

    public void onFailure(String errorCode);
}
