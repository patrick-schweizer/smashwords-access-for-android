package com.unleashyouradventure.swaccess.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class AndroidHelper {

    public static boolean isAppInstalled(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
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

    public static String getVersion(Context context) {
        String version;
        try {
            version = getPackageInfo(context).versionName;
        } catch (NameNotFoundException e) {
            version = "???";
        }
        return version;
    }

    public static String getName(Context context) {
        String name;
        try {
            name = context.getPackageManager()
                    .getApplicationLabel(getPackageInfo(context.getApplicationContext()).applicationInfo).toString();
        } catch (NameNotFoundException e) {
            name = "???";
        }
        return name;
    }

    public static PackageInfo getPackageInfo(Context context) throws NameNotFoundException {
        return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
    }
}
