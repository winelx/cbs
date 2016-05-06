package com.sitech.prm.hn.unicomclient.net;

import android.app.Application;
import android.content.Context;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by 杨静威 on 2016/3/2.
 */
public class CiatWildApplication extends Application {
    private Context applicationContext;
    private static CiatWildApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        instance=this;
        SDKInitializer.initialize(this);
    }

    public static CiatWildApplication getInstance() {
        return instance;
    }

}
