package com.unleashyouradventure.swaccess.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AndroidHelper {

    public static boolean isAppInstalled(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            PackageInfo info = pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            pm.getPackageInfo(uri, PackageManager.GET_INTENT_FILTERS);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    public static ProgressDialog createAndShowProgressDialog(Context context, String title) {
        ProgressDialog progress = new ProgressDialog(context);
        progress.setTitle("");
        progress.setMessage(title);
        progress.setIndeterminate(false);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();
        return progress;
    }
}
