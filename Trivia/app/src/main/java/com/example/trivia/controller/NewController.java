package com.example.trivia.controller;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class NewController extends Application {

    public static final String TAG = NewController.class.getSimpleName();
    private static NewController instance;
    private RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static synchronized NewController getInstance() {
//        if (instance == null) {
//            instance = new NewController(context);
//        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return requestQueue;
    }


//    public <T> void addToRequestQueue(Request<T> req, String tag) {
//        req.getTag().(TextUtils.isEmpty(tag) ? TAG : tag );
//        getRequestQueue().add(req);
//    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequest(Object tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }
}
