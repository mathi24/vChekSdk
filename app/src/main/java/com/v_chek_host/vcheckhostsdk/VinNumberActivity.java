package com.v_chek_host.vcheckhostsdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.v_chek_host.vcheckhostsdk.barcodescanner.BarcodeCaptureActivity;
import com.v_chek_host.vcheckhostsdk.ml.VinCompareCaptureActivity;
import com.v_chek_host.vcheckhostsdk.ml.VinCompareResultActivity;
import com.v_chek_host.vcheckhostsdk.ui.ClassifierActivity;
import com.v_chek_host.vcheckhostsdk.ui.DetectorActivity;
import com.v_chek_host.vcheckhostsdk.ui.ScanVinNumberActivity;
import com.v_chek_host.vcheckhostsdk.ui.VchekErrorActivity;
import com.v_chek_host.vcheckhostsdk.ui.VinCompareActivity;
import com.v_chek_host.vcheckhostsdk.utils.PreferenceStorage;
import com.v_chek_host.vcheckhostsdk.utils.Utility;


import java.util.regex.Pattern;

import cat.ereza.customactivityoncrash.config.CaocConfig;

public class VinNumberActivity extends AppCompatActivity {
      private EditText etVinNumber;
  //  public TextInputEditText etVinNumber;
    public TextInputLayout textInputVinNo;
    public ImageView scanImageView;
    public static String activityModelString = "";
    static Pattern regex = Pattern.compile("[$&+,:;=\\\\?@#|/'<>\".^*()%!-]");
    static Pattern iregex = Pattern.compile("[IW]");
    private Context ctx;
    private Activity activity;
    public static int vinMaxLength;
    int LAUNCH_SCAN_ACTIVITY = 203;
    protected Button btnProceed;
    public static InputFilter vinFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            boolean keepOriginal = true;
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (!regex.matcher(String.valueOf(c)).matches()) // put your condition here
                {
                    if (end == (vinMaxLength + 1) & i == 0) {
                        if (!iregex.matcher(String.valueOf(c)).matches()) {
                            sb.append(c);
                        } else {
                            keepOriginal = false;
                        }
                    } else
                        sb.append(c);
                } else
                    keepOriginal = false;
            }

            if (keepOriginal)
                return null;
            else {
                if (source instanceof Spanned) {
                    SpannableString sp = new SpannableString(sb);
                    TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                    return sp;
                } else {
                    return sb;
                }
            }
        }
    };

    private TextView txtData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vin_number);
        ctx = VinNumberActivity.this;
        activity = VinNumberActivity.this;
        // etVinNumber = (EditText) findViewById(R.id.et_vin_number);
        etVinNumber = findViewById(R.id.et_vin_number);
        scanImageView = findViewById(R.id.image_scan);
       // textInputVinNo = (TextInputLayout) findViewById(R.id.txt_input_vin_no);
        txtData = (TextView) findViewById(R.id.txt_data);
        btnProceed = findViewById(R.id.btn_proceed);
        activityModelString = PreferenceStorage.getInstance(VinNumberActivity.this).getActivityModel();
        vinMaxLength = PreferenceStorage.getInstance(this).getVinMaxLenght();
//        txtData.setText(Details.result);
//        gotoCaptureScreen("");
        if (PreferenceStorage.getInstance(this).isVinCompare() == 1) {
            etVinNumber.setHint("Enter Cargo Number");
        } else {
            etVinNumber.setHint("Enter VIN Number");
        }
        etVinNumber.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.
                LengthFilter(vinMaxLength), vinFilter});
        etVinNumber.requestFocus();
        etVinNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //  if (s.toString().length() >= vinMaxLength) {
                String vinNumber = etVinNumber.getText().toString();
                if (PreferenceStorage.getInstance(VinNumberActivity.this).isVinCompare() == 1) {
                    //  gotoCompareScreen(vinNumber);
                    if (vinNumber.length() >= 3) {
                        btnProceed.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (vinNumber.length() >= vinMaxLength) {
                        Utility.hideKeyboard(activity);
                        gotoCaptureScreen(vinNumber);
                        // checkPermissionsAgin(vinNumber);
                    }
                }
                //   }
            }
        });

        scanImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(VinNumberActivity.this, ScanVinNumberActivity.class);
                startActivity(intent);*/


                //Intent i = new Intent(VinNumberActivity.this, BarcodeCaptureActivity.class);
                Intent i = new Intent(VinNumberActivity.this, VinCompareCaptureActivity.class);
                startActivityForResult(i, LAUNCH_SCAN_ACTIVITY);
            }
        });


        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoCompareScreen(etVinNumber.getText().toString());
            }
        });

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check that it is the SecondActivity with an OK result
        if (requestCode == LAUNCH_SCAN_ACTIVITY) {
            if (resultCode == RESULT_OK) { // Activity.RESULT_OK

                // get String data from Intent
                String returnString = data.getStringExtra("scan_no");

                // set text view with string
                etVinNumber.setText(returnString);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Details.result = "";
    }

    void gotoCaptureScreen(String vinNumber) {
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.key_vin_no), vinNumber);
        //Intent intent = new Intent(VinNumberActivity.this, ClassifierActivity.class);
        Intent intent = new Intent(VinNumberActivity.this, DetectorActivity.class);
        intent.putExtras(bundle);
        // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    void gotoCompareScreen(String vinNumber) {
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.key_vin_no), vinNumber);
       // Intent intent = new Intent(VinNumberActivity.this, VinCompareActivity.class);
        Intent intent = new Intent(VinNumberActivity.this, VinCompareResultActivity.class);
        //Intent intent = new Intent(VinNumberActivity.this, DetectorActivity.class);
        intent.putExtras(bundle);
        // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}