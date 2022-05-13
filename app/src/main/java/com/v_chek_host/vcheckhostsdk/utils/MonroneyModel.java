package com.v_chek_host.vcheckhostsdk.utils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MonroneyModel implements Serializable {

    @SerializedName("MetaData")
    @Expose
    private MetaData metaData;
    @SerializedName("Results")
    @Expose
    private Results results;

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) {
        this.results = results;
    }

    public class MetaData {

        @SerializedName("Name")
        @Expose
        private String name;
        @SerializedName("Version")
        @Expose
        private String version;
        @SerializedName("URI")
        @Expose
        private String uRI;



        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getURI() {
            return uRI;
        }

        public void setURI(String uRI) {
            this.uRI = uRI;
        }

        public String getuRI() {
            return uRI;
        }

        public void setuRI(String uRI) {
            this.uRI = uRI;
        }

        @Override
        public String toString() {
            return "MetaData{" +
                    "name='" + name + '\'' +
                    ", version='" + version + '\'' +
                    ", uRI='" + uRI + '\'' +
                    '}';
        }
    }

    public class Results {

        @SerializedName("Code")
        @Expose
        private String code;
        @SerializedName("Color")
        @Expose
        private String color;
        @SerializedName("QCheck")
        @Expose
        private String qCheck;
        @SerializedName("VIN")
        @Expose
        private String vIN;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getQCheck() {
            return qCheck;
        }

        public void setQCheck(String qCheck) {
            this.qCheck = qCheck;
        }

        public String getVIN() {
            return vIN;
        }

        public void setVIN(String vIN) {
            this.vIN = vIN;
        }

        @Override
        public String toString() {
            return "Results{" +
                    "code='" + code + '\'' +
                    ", color='" + color + '\'' +
                    ", qCheck='" + qCheck + '\'' +
                    ", vIN='" + vIN + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "MonroneyModel{" +
                "metaData=" + metaData +
                ", results=" + results +
                '}';
    }
}
