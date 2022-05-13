package com.v_chek_host.vcheckhostsdk.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FindViewModel {

    @SerializedName("TagName")
    @Expose
    private String tagName;
    @SerializedName("Confidence")
    @Expose
    private Double confidence;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

}
