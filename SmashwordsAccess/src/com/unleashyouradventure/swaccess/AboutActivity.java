package com.unleashyouradventure.swaccess;

import android.content.pm.PackageManager.NameNotFoundException;

public class AboutActivity extends AbstractWebviewActivity {

    @Override
    protected String modifyHtml(String html) {
        html = html.replace("{version}", getVersion());
        return html;
    }

    private String getVersion() {
        String version;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            version = "???";
        }
        return version;
    }
}
