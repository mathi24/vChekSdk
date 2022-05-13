package com.v_chek_host.vcheckhostsdk.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.v_chek_host.vcheckhostsdk.ModelCheckActivity;
import com.v_chek_host.vcheckhostsdk.R;
import com.v_chek_host.vcheckhostsdk.VinNumberActivity;
import com.v_chek_host.vcheckhostsdk.utils.Utility;

public class StartActivity extends AppCompatActivity {

    private Button btnStart;
    EditText etxtActivityID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
       /* Intent intent = new Intent(getBaseContext(), ModelDownloadService.class);
        startService(intent);*/
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        String toastMsg;
        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                toastMsg = "Large screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                toastMsg = "Normal screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                toastMsg = "Small screen";
                break;
            default:
                toastMsg = "Screen size is neither large, normal or small";
        }
      //  Toast.makeText(this, getScreenResolution(this), Toast.LENGTH_LONG).show();
        Toast.makeText(this, Utility.getDeviceUDID(this), Toast.LENGTH_LONG).show();
      //  Toast.makeText(this, Utility.getIMEIDeviceId(this), Toast.LENGTH_LONG).show();
        btnStart=findViewById(R.id.btn_start);
        etxtActivityID=findViewById(R.id.etxt_activity_id);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String actId = etxtActivityID.getText().toString();
              //  Intent intent= new Intent(StartActivity.this, VinNumberActivity.class);//weel_lock:96,kickPlate : 21
                String metaData = "{\"MetaData\":{\"ActivityId\":\""+actId+"\",\"ConnString\":" +
                        "\"HIYE581JrCIwtuR0UXCdlXljFopuDPFT2+ATxXLOEVp+wFDj4lL96UIkItYpDDt+HWg0WIQyq" +
                        "TGq5P5IIbLU4QBxx/I+O9ZLULMlMuS2jB2q4quB2Zo/yaxIiDm3jbRDWlXbSpISSv7gksMil2n" +
                        "YCwjdJZGiqpTHqqYFpSJS/HJ9jF5tlpWcg7b6a4vUqcJTsysAK0vKY97P3eS8zwVINQ\\u003d\\u003d\"," +
                        "\"Emp_ID\":\"000\",\"Emp_Name\":\"Demo\",\"LanguageID\":\"en_US\"," +
                        "\"ObjectIDValue\":\"YESY\",\"ObjectIDName\":\"VIN\",\"ParentId\":\"1\"," +
                        "\"SiteCode\":\"CAN\",\"Site-ID\":\"000\",\"TenantId\":\"1\",\"User-ID\":\"17\"," +
                        "\"User_Name\":\"DemoAdmin\"},\"Primary\":{\"Params\":[{\"Name\":\"Acc.Cd.\"," +
                        "\"Value\":\"WheelLock\"},{\"Name\":\"Acc.Desc.\",\"Value\":\"WheelLock\"}]}}";
                Bundle bundle = new Bundle();
                bundle.putString("input_data", metaData);
                Intent intent= new Intent(StartActivity.this, ModelCheckActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private static String getScreenResolution(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        return "{" + width + "," + height + "}";
    }
}