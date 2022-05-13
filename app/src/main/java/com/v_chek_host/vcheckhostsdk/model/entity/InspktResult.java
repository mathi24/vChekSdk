package com.v_chek_host.vcheckhostsdk.model.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class InspktResult {

    @SerializedName("id")
    public String iterationId;

    @SerializedName("predictions")
    public List<InspectResult> inspectResult;


    public List<InspectResult> getInspectResult() {
        return inspectResult;
    }

    public class InspectResult implements Serializable {

        @SerializedName("tagName")
        public String label;

        @SerializedName("probability")
        public float confidence;

        @SerializedName("tagId")
        public String tagId;

        public String getLabel() {
            return label;
        }

        public float getConfidence() {
            return confidence;
        }

        public String getTagId() {
            return tagId;
        }
    }
}
