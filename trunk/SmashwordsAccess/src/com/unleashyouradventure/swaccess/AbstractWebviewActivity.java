package com.unleashyouradventure.swaccess;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.jsing.common.string.StringTrimmer;
import com.unleashyouradventure.swaccess.util.StringUtils;

public abstract class AbstractWebviewActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webview = new WebView(this);
        setContentView(webview);
        String name = getName();
        String html = StringUtils.readAssetFileToString(this, "html/" + name + ".html");
        html = modifyHtml(html);
        webview.loadDataWithBaseURL("file:///android_asset/html/", html, "text/html", "UTF-8", null);
    }

    protected String modifyHtml(String html) {
        // default: do nothing
        return html;
    }

    protected String getName() {
        String name = new StringTrimmer(this.getClass().getSimpleName()).getBeforeNext("Activity").toString();
        name = name.toLowerCase();
        return name;
    }
}
