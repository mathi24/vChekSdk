package com.v_chek_host.vcheckhostsdk.fragment;

/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import com.v_chek_host.vcheckhostsdk.R;
import com.v_chek_host.vcheckhostsdk.customview.AutoFitTextureView;
import com.v_chek_host.vcheckhostsdk.env.ImageUtils;
import com.v_chek_host.vcheckhostsdk.env.Logger;
import com.v_chek_host.vcheckhostsdk.ui.ClassifierActivity;
import com.v_chek_host.vcheckhostsdk.utils.HorizontalStepView;
import com.v_chek_host.vcheckhostsdk.utils.PreferenceStorage;
import com.v_chek_host.vcheckhostsdk.utils.bean.StepBean;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("ValidFragment")
public class LegacyCameraConnectionFragment extends Fragment {
  private static final Logger LOGGER = new Logger();
  /** Conversion from screen rotation to JPEG orientation. */
  private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
  private View view;
  private boolean isFlashOn = false;
  private HorizontalStepView stepView;
  private String stepString;
  private static ImageView imageView;
  private static ImageView overlayImage;
  private Gson stepGson;
  static {
    ORIENTATIONS.append(Surface.ROTATION_0, 90);
    ORIENTATIONS.append(Surface.ROTATION_90, 0);
    ORIENTATIONS.append(Surface.ROTATION_180, 270);
    ORIENTATIONS.append(Surface.ROTATION_270, 180);
  }

  private Camera camera;
  Camera.Parameters parameters;
  private Camera.PreviewCallback imageListener;
  private Size desiredSize;
  private String overlayUrl;
  private List<StepBean> stepBeanList;
  /** The layout identifier to inflate for this Fragment. */
  private int layout;
  /** An {@link AutoFitTextureView} for camera preview. */
  private AutoFitTextureView textureView;
  /**
   * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a {@link
   * TextureView}.
   */
  private final TextureView.SurfaceTextureListener surfaceTextureListener =
      new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(
                final SurfaceTexture texture, final int width, final int height) {

          int index = getCameraId();
          camera = Camera.open(index);

          try {
             parameters = camera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes != null
                && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
              parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            List<Camera.Size> cameraSizes = parameters.getSupportedPreviewSizes();
            Size[] sizes = new Size[cameraSizes.size()];
            //my changes
            int mFrameWidth = (int) cameraSizes.get(0).width;
            int mFrameHeight = (int) cameraSizes.get(0).height;
            Size previewSize =new Size(mFrameWidth,mFrameHeight);//end

           /* int i = 0;
            for (Camera.Size size : cameraSizes) {
              sizes[i++] = new Size(size.width, size.height);
            }
            Size previewSize =
                CameraConnectionFragment.chooseOptimalSize(
                    sizes, desiredSize.getWidth(), desiredSize.getHeight());*/
            parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
            camera.setDisplayOrientation(0);
            camera.setParameters(parameters);
            camera.setPreviewTexture(texture);
          } catch (IOException exception) {
            camera.release();
          }

          camera.setPreviewCallbackWithBuffer(imageListener);
          Camera.Size s = camera.getParameters().getPreviewSize();
        //  camera.addCallbackBuffer(new byte[ImageUtils.getYUVByteSize(s.height, s.width)]);
          camera.addCallbackBuffer(new byte[ImageUtils.getYUVByteSize(s.width,s.height)]);

          textureView.setAspectRatio(s.width,s.height);

          camera.startPreview();
        }

        @Override
        public void onSurfaceTextureSizeChanged(
                final SurfaceTexture texture, final int width, final int height) {}

        @Override
        public boolean onSurfaceTextureDestroyed(final SurfaceTexture texture) {
          return true;
        }

        @Override
        public void onSurfaceTextureUpdated(final SurfaceTexture texture) {}
      };
  /** An additional thread for running tasks that shouldn't block the UI. */
  private HandlerThread backgroundThread;

  @SuppressLint("ValidFragment")
  public LegacyCameraConnectionFragment(
          final Camera.PreviewCallback imageListener, final int layout, final Size desiredSize) {
    this.imageListener = imageListener;
    this.layout = layout;
    this.desiredSize = desiredSize;
  }

  @SuppressLint("ValidFragment")
  public LegacyCameraConnectionFragment(
          final Camera.PreviewCallback imageListener, final int layout, final Size desiredSize,
          final String overlayUrl,
          final List<StepBean> stepBeanList) {
    this.imageListener = imageListener;
    this.layout = layout;
    this.desiredSize = desiredSize;
    this.overlayUrl = overlayUrl;
    this.stepBeanList = stepBeanList;
  }

  @Override
  public View onCreateView(
          final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    view = inflater.inflate(layout, container, false);
    imageView = view.findViewById(R.id.img_flash_light);
    stepView = view.findViewById(R.id.stepview);
    overlayImage = view.findViewById(R.id.overlay_image);
    overlayImage.setVisibility(View.GONE);
    imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        boolean hasFlash = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (hasFlash) {
          // EventBus.getDefault().post(StrConstant.FLASH_LIGHT_ON);
          parameters = camera.getParameters();
          /* parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);*/
          List<String> focusModes = parameters.getSupportedFocusModes();
          if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
          } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
          }
                        /*if (parameters.getMaxNumFocusAreas() > 0) {
                            parameters.setFocusAreas(null);
                        }*/
          if (!isFlashOn) {
            isFlashOn = true;
            imageView.setImageResource(R.drawable.ic_baseline_flash_on_24);
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setDisplayOrientation(0);
            camera.setParameters(parameters);
          } else {
            isFlashOn = false;
            imageView.setImageResource(R.drawable.ic_baseline_flash_off_24);
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
          }
          camera.setDisplayOrientation(0);
          camera.setParameters(parameters);

        }
        // Toast.makeText(getActivity(), "hheeee", Toast.LENGTH_SHORT).show();

      }
    });

   // orderStatus(2);
    /*if(stepBeanList!=null)
    statusView(stepBeanList);*/

    stepGson = new Gson();
    Type stepType = new TypeToken<ArrayList<StepBean>>() {
    }.getType();
    stepString=PreferenceStorage.getInstance(getActivity()).getStep();
    stepBeanList = stepGson.fromJson(stepString, stepType);
    if(stepBeanList.size()>1)
    statusView(stepBeanList);
    overlayUrl = PreferenceStorage.getInstance(getActivity()).getOverLayUrl();
    if(overlayUrl != null)
    Picasso.get()
            .load(overlayUrl)
            .into(overlayImage, new Callback() {
              @Override
              public void onSuccess() {
                //Toast.makeText(getActivity(),"success",Toast.LENGTH_SHORT).show();
              }

              @Override
              public void onError(Exception e) {
               // Toast.makeText(getActivity(),"exc "+e,Toast.LENGTH_LONG).show();
              }
            });
    return view;
  }

  @Override
  public void onViewCreated(final View view, final Bundle savedInstanceState) {
    textureView = (AutoFitTextureView) view.findViewById(R.id.texture);
  }

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public void onResume() {
    super.onResume();
    startBackgroundThread();
    // When the screen is turned off and turned back on, the SurfaceTexture is already
    // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
    // a camera and start preview from here (otherwise, we wait until the surface is ready in
    // the SurfaceTextureListener).

    if (textureView.isAvailable()) {
      if (camera != null) {
        camera.startPreview();
      }
    } else {
      textureView.setSurfaceTextureListener(surfaceTextureListener);
    }
  }

  @Override
  public void onPause() {
    stopCamera();
    stopBackgroundThread();
    super.onPause();
  }

  /** Starts a background thread and its {@link Handler}. */
  private void startBackgroundThread() {
    backgroundThread = new HandlerThread("CameraBackground");
    backgroundThread.start();
  }

  /** Stops the background thread and its {@link Handler}. */
  private void stopBackgroundThread() {
    backgroundThread.quitSafely();
    try {
      backgroundThread.join();
      backgroundThread = null;
    } catch (final InterruptedException e) {
      LOGGER.e(e, "Exception!");
    }
  }

  protected void stopCamera() {
    if (camera != null) {
      camera.stopPreview();
      camera.setPreviewCallback(null);
      camera.release();
      camera = null;
    }
  }

  private int getCameraId() {
    CameraInfo ci = new CameraInfo();
    for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
      Camera.getCameraInfo(i, ci);
      if (ci.facing == CameraInfo.CAMERA_FACING_BACK) return i;
    }
    return -1; // No camera found
  }
  /*public void orderStatus(int o){
    switch (o){
      case 1:
        statusView(1,-1,-1,-1,"Checking",2);
        break;
      case 2:
        statusView(1,-1,-1,-1,"Checking",3);
        break;
      case 3:
        statusView(1,0,-1,-1,"Rejected",4);
        break;
      case 4:
        statusView(1,1,1,-1,"Assigned",5);
        break;
      case 5:
        statusView(1,1,1,1,"Assigned",6);
        break;
      case 8:
        statusView(1,0,-1,-1,"Cancelled",7);
        break;
    }
  }*/

  public void statusView(List<StepBean> stepBeanList){
    stepView.setStepViewTexts(stepBeanList)
            .setTextSize(14)//set textSize
            .setStepsViewIndicatorCompletedLineColor(ContextCompat.getColor(getActivity(), R.color.teal))//StepsViewIndicator
            .setStepsViewIndicatorUnCompletedLineColor(ContextCompat.getColor(getActivity(), R.color.teal))//StepsViewIndicator
            .setStepViewComplectedTextColor(ContextCompat.getColor(getActivity(), R.color.teal))//StepsView text
            .setStepViewUnComplectedTextColor(ContextCompat.getColor(getActivity(), R.color.teal))//StepsView text
            .setStepsViewIndicatorCompleteIcon(ContextCompat.getDrawable(getActivity(), R.drawable.complted))//StepsViewIndicator CompleteIcon
            .setStepsViewIndicatorDefaultIcon(ContextCompat.getDrawable(getActivity(), R.drawable.default_icon))//StepsViewIndicator DefaultIcon
            .setStepsViewIndicatorAttentionIcon(ContextCompat.getDrawable(getActivity(), R.drawable.attention));//StepsViewIndicator AttentionIcon

  }
}
