package com.v_chek_host.vcheckhostsdk.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.v_chek_host.vcheckhostsdk.R;

public class RectangleOverLayBARCodeView extends LinearLayout {

    private Bitmap bitmap;

    public RectangleOverLayBARCodeView(Context context) {

        super(context);

    }

    public RectangleOverLayBARCodeView(Context context, AttributeSet attrs) {

        super(context, attrs);

    }

    public RectangleOverLayBARCodeView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

    }

    public RectangleOverLayBARCodeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        super(context, attrs, defStyleAttr, defStyleRes);

    }

    @Override

    protected void dispatchDraw(Canvas canvas) {

        super.dispatchDraw(canvas);

        if (bitmap == null) {

            createWindowFrame();

        }

        canvas.drawBitmap(bitmap, 0, 0, null);

    }

    protected void createWindowFrame() {

        setWillNotDraw(false);

        setLayerType(LAYER_TYPE_HARDWARE, null);

        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

        Canvas osCanvas = new Canvas(bitmap);

        RectF outerRectangle = new RectF(0, 0, getWidth(), getHeight());

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(getResources().getColor(R.color.scannerBGColor));

        osCanvas.drawRect(outerRectangle, paint);

        paint.setColor(Color.TRANSPARENT);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));

        float x1 = getWidth() / 20;

        float x2 = getWidth() - x1;

        float heightDiff = x2 - x1;

        float y1 = (getHeight() / 2) - (heightDiff / 5);

        float y2 = (getHeight() / 2) + (heightDiff / 5);

        RectF rect = new RectF(x1, y1, x2, y2);

        osCanvas.drawRoundRect(rect, 10, 10, paint);

        RectF rectBorder = new RectF(x1 + 10, y1 + 10, x2 - 10, y2 - 10);

      //  paint.setStyle(Paint.Style.STROKE);

       // paint.setStrokeWidth(20);

      //  paint.setColor(Color.WHITE);

        osCanvas.drawRoundRect(rectBorder, 90, 90, paint);

    }

    @Override

    public boolean isInEditMode() {

        return true;

    }

    @Override

    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        super.onLayout(changed, l, t, r, b);

        bitmap = null;

    }

}