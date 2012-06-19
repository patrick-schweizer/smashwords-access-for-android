package com.unleashyouradventure.swaccess;

import android.app.Application;

public class SmashwordsAccessApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initSingletons();
    }

    protected void initSingletons() {
        SmashwordsAPIHelper.init(this);
    }
}
