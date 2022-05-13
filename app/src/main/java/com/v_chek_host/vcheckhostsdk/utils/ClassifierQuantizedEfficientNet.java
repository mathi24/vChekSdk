/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.v_chek_host.vcheckhostsdk.utils;

import android.app.Activity;

import com.v_chek_host.vcheckhostsdk.customview.Classifier;

import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.ops.NormalizeOp;

import java.io.IOException;
import java.util.List;

/** This TensorFlow Lite classifier works with the quantized EfficientNet model. */
public class ClassifierQuantizedEfficientNet extends Classifier {

  /**
   * The quantized model does not require normalization, thus set mean as 0.0f, and std as 1.0f to
   * bypass the normalization.
   */
  private static final float IMAGE_MEAN = 0.0f;

  private static final float IMAGE_STD = 1.0f;

 /* * Quantized MobileNet requires additional dequantization to the output probability. */
  private static final float PROBABILITY_MEAN = 0.0f;

  private static final float PROBABILITY_STD = 255.0f;

  /** Float MobileNet requires additional normalization of the used input. */
  /*private static final float IMAGE_MEAN = 127.5f;

  private static final float IMAGE_STD = 127.5f;

  *//**
   * Float model does not need dequantization in the post-processing. Setting mean and std as 0.0f
   * and 1.0f, repectively, to bypass the normalization.
   *//*
  private static final float PROBABILITY_MEAN = 0.0f;

  private static final float PROBABILITY_STD = 1.0f;*/

  /**
   * Initializes a {@code ClassifierQuantizedMobileNet}.
   *
   * @param activity
   */
  public ClassifierQuantizedEfficientNet(Activity activity, Device device, int numThreads, String s,String modelFileName,
                                         List<String> modelLabel)
      throws IOException {
    super(activity, device, numThreads,s,modelFileName,modelLabel);
  }

  @Override
  protected String getModelPath() {
    // you can download this file from
    // see build.gradle for where to obtain this file. It should be auto
    // downloaded into assets.
   // return "efficientnet-lite0-int8.tflite";
    return "model.tflite";
  }

  @Override
  protected String getLabelPath() {
 //   return "labels_without_background.txt";
    return "labelmap.txt";
  }

  @Override
  protected TensorOperator getPreprocessNormalizeOp() {
    return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
  }

  @Override
  protected TensorOperator getPostprocessNormalizeOp() {
    return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
  }
}
