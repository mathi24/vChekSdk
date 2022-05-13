package com.v_chek_host.vcheckhostsdk.utils;



import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

public class LoggingInterceptor implements Interceptor {
    public static final String TAG = "LoggingInterceptor:";

   // AndroidLogAdapter androidLogAdapter;

    public static String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long t1 = System.nanoTime();
        String requestLog = String.format("%nHost: %s %n",// on %s%n%s",
                request.url(), /*chain.connection(),*/ request.headers(),request.body());
        //YLog.d(String.format("Sending request %s on %s%n%s",
        //        request.url(), chain.connection(), request.headers()));
        if (request.method().compareToIgnoreCase("post") == 0) {
            requestLog =/*"\n"+*/requestLog +/*"\n"+*/bodyToString(request);
        }

        Response response = chain.proceed(request);
        long t2 = System.nanoTime();
        String responseLog = String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers());
        String bodyString = response.body().string();

        if (bodyString.startsWith("{") || bodyString.startsWith("[")) {
//            Logger.json(/*"\n"+responseLog+"\n"+*/bodyString);
           // Logger.json(/*"Response:\n"+*/bodyString);
          //  CamaraActivity.responceData=bodyString;
        } else {
//            Logger.d(TAG,/*"\n"+responseLog+"\n"+*/bodyString);
           // Logger.d(TAG,"Response:\n"+bodyString);

        }
        return response.newBuilder()
                .body(ResponseBody.create(response.body().contentType(), bodyString))
                .build();
        //return response;
    }
}