package com.v_chek_host.vcheckhostsdk.ml;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.v_chek_host.vcheckhostsdk.ui.VchekErrorActivity;

import cat.ereza.customactivityoncrash.config.CaocConfig;

public class BaseActivity extends AppCompatActivity {
    ProgressDialog progressBar;

   BaseActivity activity;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity=this;
        try {
            CaocConfig.Builder.create()
                    .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
                    .enabled(true)
                    .showRestartButton(false)
                    .minTimeBetweenCrashesMs(1)
                    .errorActivity(VchekErrorActivity.class)
                    .showErrorDetails(false)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
