package com.v_chek_host.vcheckhostsdk.model.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SendImage implements Serializable {

    @SerializedName("Binary")
    public String imageString;

    public SendImage (String imageString){
        this.imageString = imageString;
    }

    public String getImageString() {
        return imageString;
    }
}
