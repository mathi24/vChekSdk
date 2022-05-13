package com.v_chek_host.vcheckhostsdk.model.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by NTL on 15-Dec-16.
 */

    public final class HandlerUtils {

        private HandlerUtils() {}

        private static final Handler handler = new Handler(Looper.getMainLooper());

        public static boolean post(Runnable r) {
            return handler.post(r);
        }

        public static boolean postDelayed(Runnable r, long delayMillis) {
            return handler.postDelayed(r, delayMillis);
        }

    }

