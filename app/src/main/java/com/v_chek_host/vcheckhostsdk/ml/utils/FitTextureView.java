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

package com.v_chek_host.vcheckhostsdk.ml.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.view.WindowManager;

/** A {@link TextureView} that can be adjusted to a specified aspect ratio. */
public class FitTextureView extends TextureView {
  private int ratioWidth = 0;
  private int ratioHeight = 0;
  public DisplayMetrics mMetrics = new DisplayMetrics();

  public FitTextureView(final Context context) {
    this(context, null);
  }

  public FitTextureView(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public FitTextureView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
  }

  /**
   * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
   * calculated from the parameters. Note that the actual sizes of parameters don't matter, that is,
   * calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
   *
   * @param width Relative horizontal size
   * @param height Relative vertical size
   */
  public void setAspectRatio(final int width, final int height) {
    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("Size cannot be negative.");
    }
    ratioWidth = width;
    ratioHeight = height;
    requestLayout();
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    final int width = MeasureSpec.getSize(widthMeasureSpec);
    final int height = MeasureSpec.getSize(heightMeasureSpec);
    if (0 == width || 0 == height) {
      setMeasuredDimension(width, height);
    } else {
      WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
      windowManager.getDefaultDisplay().getMetrics(mMetrics);
      double ratio = (double)width / (double)height;
      double invertedRatio = (double)height / (double)width;
      double portraitHeight = width * invertedRatio;
      double portraitWidth = width * (mMetrics.heightPixels / portraitHeight);
      double landscapeWidth = height * ratio;
      double landscapeHeight = (mMetrics.widthPixels / landscapeWidth) * height;

      if (width < height * width / height) {
        setMeasuredDimension((int)portraitWidth, mMetrics.heightPixels);
      } else {
        setMeasuredDimension(mMetrics.widthPixels, (int)landscapeHeight);
      }
    }
   /* if (0 == ratioWidth || 0 == ratioHeight) {
      setMeasuredDimension(width, height);
    } else {
      if (width < height * ratioWidth / ratioHeight) {
        setMeasuredDimension(width, width * ratioHeight / ratioWidth);
      } else {
        setMeasuredDimension(height * ratioWidth / ratioHeight, height);
      }*/

    /*  if (width < height * ratioWidth / ratioHeight) {
        setMeasuredDimension(height * ratioWidth / ratioHeight, height);
      } else {
        setMeasuredDimension(width, width * ratioHeight / ratioWidth);
      }*/
    }

}
