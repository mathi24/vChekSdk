package com.v_chek_host.vcheckhostsdk.model.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ApiInspktResult {

    @SerializedName("StatusCode")
    public String statusCode;

    @SerializedName("Lable")
    public String lable;

    @SerializedName("Confidence")
    public String confidence;

    @SerializedName("Message")
    public String msg;

    public String getStatusCode() {
        return statusCode;
    }

    public String getLable() {
        return lable;
    }

    public String getConfidence() {
        return confidence;
    }

    public String getMsg() {
        return msg;
    }
}
