package com.v_chek_host.vcheckhostsdk.utils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ExceptionLog implements Serializable {

    @SerializedName("Message")
    private String message;

    @SerializedName("StatusCode")
    private int statusCode;

    public void setMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return message;
    }

    public void setStatusCode(int statusCode){
        this.statusCode = statusCode;
    }

    public int getStatusCode(){
        return statusCode;
    }

    @Override
    public String toString(){
        return
                "ExceptionLog{" +
                        "message = '" + message + '\'' +
                        ",statusCode = '" + statusCode + '\'' +
                        "}";
    }
}