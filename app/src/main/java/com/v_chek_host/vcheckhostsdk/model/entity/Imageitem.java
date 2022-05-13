package com.v_chek_host.vcheckhostsdk.model.entity;

import android.graphics.Bitmap;

import java.util.Arrays;

public class Imageitem {

    String carViewName;
    Bitmap bitmap;
    float confidence;
    byte[] byteImage;
    byte[] byteCropImage;
    String code;
    String status;
    String color;
    String resultMsg;

    public String getCarViewName() {
        return carViewName;
    }

    public void setCarViewName(String carViewName) {
        this.carViewName = carViewName;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public byte[] getByteImage() {
        return byteImage;
    }

    public void setByteImage(byte[] byteImage) {
        this.byteImage = byteImage;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public byte[] getByteCropImage() {
        return byteCropImage;
    }

    public void setByteCropImage(byte[] byteCropImage) {
        this.byteCropImage = byteCropImage;
    }

    @Override
    public String toString() {
        return "Imageitem{" +
                "carViewName='" + carViewName + '\'' +
                ", bitmap=" + bitmap +
                ", confidence=" + confidence +
                ", byteImage=" + Arrays.toString(byteImage) +
                ", code='" + code + '\'' +
                ", status='" + status + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
