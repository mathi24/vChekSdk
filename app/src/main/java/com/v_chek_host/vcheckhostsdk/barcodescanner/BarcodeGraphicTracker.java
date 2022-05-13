package com.v_chek_host.vcheckhostsdk.barcodescanner.camera;

import android.content.Context;

import androidx.annotation.UiThread;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import com.v_chek_host.vcheckhostsdk.barcodescanner.BarcodeGraphic;

public class BarcodeGraphicTracker extends Tracker<Barcode> {
private GraphicOverlay<BarcodeGraphic> mOverlay;
private BarcodeGraphic mGraphic;

private BarcodeUpdateListener mBarcodeUpdateListener;

/**
 * Consume the item instance detected from an Activity or Fragment level by implementing the
 * BarcodeUpdateListener interface method onBarcodeDetected.
 */
public interface BarcodeUpdateListener {
    @UiThread
    void onBarcodeDetected(Barcode barcode);
}

    public BarcodeGraphicTracker(GraphicOverlay<BarcodeGraphic> mOverlay, BarcodeGraphic mGraphic,
                                 Context context) {
        this.mOverlay = mOverlay;
        this.mGraphic = mGraphic;
        if (context instanceof BarcodeUpdateListener) {
            this.mBarcodeUpdateListener = (BarcodeUpdateListener) context;
        } else {
            throw new RuntimeException("Hosting activity must implement BarcodeUpdateListener");
        }
    }

    /**
     * Start tracking the detected item instance within the item overlay.
     */
    @Override
    public void onNewItem(int id, Barcode item) {
        mGraphic.setId(id);
        mBarcodeUpdateListener.onBarcodeDetected(item);
    }

    /**
     * Update the position/characteristics of the item within the overlay.
     */
    @Override
    public void onUpdate(Detector.Detections<Barcode> detectionResults, Barcode item) {
        mOverlay.add(mGraphic);
        mGraphic.updateItem(item);
    }

    /**
     * Hide the graphic when the corresponding object was not detected.  This can happen for
     * intermediate frames temporarily, for example if the object was momentarily blocked from
     * view.
     */
    @Override
    public void onMissing(Detector.Detections<Barcode> detectionResults) {
        mOverlay.remove(mGraphic);
    }

    /**
     * Called when the item is assumed to be gone for good. Remove the graphic annotation from
     * the overlay.
     */
    @Override
    public void onDone() {
        mOverlay.remove(mGraphic);
    }
}