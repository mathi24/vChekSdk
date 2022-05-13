package com.v_chek_host.vcheckhostsdk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.v_chek_host.vcheckhostsdk.R;
import com.v_chek_host.vcheckhostsdk.utils.PreferenceStorage;
import com.v_chek_host.vcheckhostsdk.utils.Utility;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

public class VchekErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vchek_custom_error);
        System.out.println("SDK Exception log arrived");
        TextView errorDetailsText = findViewById(R.id.error_details);
        errorDetailsText.setText(CustomActivityOnCrash.getStackTraceFromIntent(getIntent()));
        String errors= CustomActivityOnCrash.getStackTraceFromIntent(getIntent());
        errors=errors!=null?errors:"";

        System.out.println("SDK Exception log arrived");
        Utility.exceptionLog(this, Utility.getLog(errors),
                PreferenceStorage.getInstance(this).getUserId(),
                PreferenceStorage.getInstance(this).getSiteId(),
                PreferenceStorage.getInstance(this).getActivityId(),
                PreferenceStorage.getInstance(this).getModelId(),"2");
//        new SaveErrorLog(CustomErrorActivity.this).execute(logData);

        Button restartButton = findViewById(R.id.restart_button);
        final CaocConfig config = CustomActivityOnCrash.getConfigFromIntent(getIntent());

        if (config == null) {
            //This should never happen - Just finish the activity to avoid a recursive crash.
            finish();
            return;
        }
        if (config.isShowRestartButton() && config.getRestartActivityClass() != null) {
            restartButton.setText("Restart app");
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomActivityOnCrash.restartApplication(VchekErrorActivity.this, config);
                }
            });
        } else {
            restartButton.setText("Restart app");
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    startActivity(LaunchIntent);
                }
            });
        }
    }
}