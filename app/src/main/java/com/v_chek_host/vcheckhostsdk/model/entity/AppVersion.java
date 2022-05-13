package com.v_chek_host.vcheckhostsdk.model.entity;

import com.google.gson.annotations.SerializedName;


public class AppVersion {

	@SerializedName("status")
	private String mStatus;


	@SerializedName("appversion")
	private String mMessage;


	public String getStatus() {
		return mStatus;
	}

	public String getmMessage() {
		return mMessage;
	}



}
