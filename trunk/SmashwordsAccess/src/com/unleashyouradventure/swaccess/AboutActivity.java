package com.unleashyouradventure.swaccess;

import com.unleashyouradventure.swaccess.util.AndroidHelper;

public class AboutActivity extends AbstractWebviewActivity {

    @Override
    protected String modifyHtml(String html) {
        html = html.replace("{version}", AndroidHelper.getVersion(this));
        return html;
    }

}
