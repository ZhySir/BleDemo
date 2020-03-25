package com.zhy.bledemo.common;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.exceptions.BleException;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Created by zhy on 2020/3/16.
 */
public class MainApplication extends Application {

    private static MainApplication mainApplication;
    public static RxBleClient rxBleClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mainApplication = this;
        rxBleClient = RxBleClient.create(this);

        RxJavaPlugins.setErrorHandler(throwable -> {
            if (throwable instanceof UndeliverableException && throwable.getCause() instanceof BleException) {
                Log.v("SampleApplication", "Suppressed UndeliverableException: " + throwable.toString());
                return; // ignore BleExceptions as they were surely delivered at least once
            }
            // add other custom handlers if needed
            throw new RuntimeException("Unexpected Throwable in RxJavaPlugins error handler", throwable);
        });
    }

    public static Context getContext() {
        if (mainApplication != null) {
            return mainApplication;
        }
        throw new NullPointerException("u should init first");
    }

}
