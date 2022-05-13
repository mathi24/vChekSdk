package com.v_chek_host.vcheckhostsdk.app;


import android.util.ArrayMap;

public class AppController implements Thread.UncaughtExceptionHandler {

    private static AppController instance;

    public static final String TAG = AppController.class.getSimpleName();
    public ArrayMap<String,Boolean> vindetails;
    public AppController() {
              instance =this;
          }

    /**
     * Returns a singleton instance of this class
     */
    public static synchronized AppController getInstance() {
        if(instance == null){
            instance = new AppController();
        }
        return instance;
    }


    @Override
    public void uncaughtException(Thread thread, Throwable e) {
        System.exit(1);
    }

}
