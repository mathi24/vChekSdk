package com.v_chek_host.vcheckhostsdk.ml;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.v_chek_host.vcheckhostsdk.R;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class PreviewImageActivity extends BaseActivity {
    private ImageView imageView;
    private Button btnCancel, btnOk;
    private String imageFile;
    private File file;
    private ProgressBar progressBar;
    public Context ctx;
    public ImageView ivBack;
    public Button btnAdd;
    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);
        ctx = PreviewImageActivity.this;
        Bundle extras = getIntent().getExtras();
        imageFile = extras.getString("file");
        Bitmap btImage=extras.getParcelable("cropimage");
        file = new File(imageFile);

        imageView = findViewById(R.id.image_view);
        btnCancel = findViewById(R.id.btn_cancel);
        btnOk = findViewById(R.id.btn_ok);
        progressBar = findViewById(R.id.progress_bar);
        ivBack = findViewById(R.id.iv_back);
        btnAdd = findViewById(R.id.btnAdd);
        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        imageView.setImageBitmap(btImage);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //    Intent intent = new Intent(PreviewImageActivity.this, DataCollectionCamaraActivity.class);
                Intent intent = new Intent(PreviewImageActivity.this, VinCompareCaptureActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  if (Utility.isConnectingToInternet(PreviewImageActivity.this, ctx)) {
                    progressBar.setVisibility(View.VISIBLE);
                    callMlModelIMageInsert();
                }*/
            }
        });
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* btnAdd.setEnabled(false);
                progressDialog = new ProgressDialog(ctx);
                progressDialog.setMessage("Loading..");
                progressDialog.setCancelable(false);
                progressDialog.show();*/
               // callSingleTrainingModel(file);
            }
        });
    }

  /*  private void insertImageApiCall() {

        try {

            //  String partsTag = TextUtils.isEmpty(etPartsTag.getText().toString().trim()) ? "" : etPartsTag.getText().toString().trim();
            //  String damageTag = TextUtils.isEmpty(etDamageTags.getText().toString().trim()) ? "" : etDamageTags.getText().toString().trim();
            // String partsTag = "";
            // String damageTag = "";
            */
    /*for (int i = 0; i < selectedChips.size(); i++) {
                partsTag = partsTag + selectedChips.get(i).getPartPrimaryId() + ",";
            }
            for (int j = 0; j < damageSelectedChips.size(); j++) {
                damageTag = damageTag + damageSelectedChips.get(j).getDamagePrimaryId() + ",";
            }
            if (!TextUtils.isEmpty(partsTag)) {
                partsTag = partsTag.substring(0, partsTag.length() - 1);
            }
            if (!TextUtils.isEmpty(damageTag)) {
                damageTag = damageTag.substring(0, damageTag.length() - 1);
            }*/
    /*
            InputStream in = new FileInputStream(file.getPath());
            Log.v("filepathhhhh", file.getPath());
            byte[] buf = new byte[in.available()];
            while (in.read(buf) != -1) ;
            final RequestBody requestBody = RequestBody
                    .create(MediaType.parse("application/octet-stream"), buf);
            Call<UploadingPhotoImageModel> call = ApiClient.getInstance().getApi().insertCollectionImage(StrConstants.APIvalue, requestBody);
            call.enqueue(new Callback<UploadingPhotoImageModel>() {
                @Override
                public void onResponse(Call<UploadingPhotoImageModel> call, Response<UploadingPhotoImageModel> response) {
                    int statusCode = response.code();
                    progressBar.setVisibility(View.GONE);
                    Log.v("phottttttttt", statusCode + "");
                    if (statusCode == StrConstants.STATUS_CODE_200) {
                        //Navigation.findNavController(view).popBackStack(R.id.photoBasedDataCameraFragment, true);
                        Intent intent = new Intent(PreviewImageActivity.this, DataCollectionActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PreviewImageActivity.this, R.string.txt_something_went_wrong_please_try_again_later, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<UploadingPhotoImageModel> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Log.v("errorMag", t.getMessage());
                    if (t.getMessage().equals("timeout")) {
                        Toast.makeText(PreviewImageActivity.this, R.string.txt_poor_internet_connection_please_check_your_internet_and_try_again, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(PreviewImageActivity.this, R.string.txt_something_went_wrong_please_try_again_later, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void callMlModelIMageInsert() {
        try {
            InputStream in = new FileInputStream(file.getPath());
            Log.v("filepathhhhh", file.getPath());
            byte[] buf = new byte[in.available()];
            while (in.read(buf) != -1) ;
            String value = Base64.encodeToString(buf, Base64.DEFAULT);
            System.out.println("sdfffff " + V2ModelTrainingDataSetActivity.vImageResult + "");
            Call<ResponseBody> call = ApiClient.getInstance().getApi().insertMlModelImage(StrConstants.API_VALUE,
                    String.valueOf(SharedPreferenceManager.getModelId(this)), value, V2ModelTrainingDataSetActivity.vImageResult);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    progressBar.setVisibility(View.GONE);
                    int statusCode = response.code();
                    if (statusCode == 200) {
                        finish();
                    } else {
                        Toast.makeText(PreviewImageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PreviewImageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (FileNotFoundException e) {
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
        } catch (IOException e) {
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
        }

    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

   /* public void callSingleTrainingModel(File file) {
        String base64Image = base64Image(file.getAbsolutePath());
        Call<V3SingleTrainingModelResponce> call = ApiClient.getInstance().getApi().
                singleInsertTrainingModel(StrConstants.API_VALUE, String.valueOf(V3PassAndFailActivity.modelID)
                        , SharedPreferenceManager.getUserid(ctx), base64Image, V3PassAndFailActivity.isPassAndFail ? "1" : "0");
        call.enqueue(new Callback<V3SingleTrainingModelResponce>() {
            @Override
            public void onResponse(Call<V3SingleTrainingModelResponce> call, Response<V3SingleTrainingModelResponce> response) {
                btnAdd.setEnabled(true);
                progressDialog.dismiss();
                int statusCode = response.code();
                if (statusCode == 200) {
                    if (file.exists()) {
                        file.delete();
                    }
                    finish();
                }
            }

            @Override
            public void onFailure(Call<V3SingleTrainingModelResponce> call, Throwable t) {
                btnAdd.setEnabled(true);
                progressDialog.dismiss();
            }
        });
    }*/

    public String base64Image(String filePath) {
        Bitmap bm = BitmapFactory.decodeFile(filePath);
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, bOut);
        String base64Image = Base64.encodeToString(bOut.toByteArray(), Base64.DEFAULT);
        return base64Image;
    }
}