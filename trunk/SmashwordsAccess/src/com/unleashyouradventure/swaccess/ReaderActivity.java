package com.unleashyouradventure.swaccess;

import com.unleashyouradventure.swaccess.readers.LeaveInSmashwordsFolder;
import com.unleashyouradventure.swaccess.readers.Reader;

public class ReaderActivity extends AbstractWebviewActivity {

    @Override
    protected String modifyHtml(String html) {
        html = html.replace("{readers}", getSupportedReaders());
        return html;
    }

    private String getSupportedReaders() {
        StringBuilder b = new StringBuilder();
        for (Reader reader : Reader.getAvailableReaders()) {
            if (reader instanceof LeaveInSmashwordsFolder)
                continue;
            b.append("<li>");
            String link = reader.getReaderLink();
            if (link != null) {
                b.append("<a href=\"");
                b.append(link).append("\">");
            }
            b.append(reader.getName());
            if (link != null) {
                b.append("</a>");
            }
            b.append(": <span class=\"");
            b.append(reader.isReaderAvailable(this) ? "found\">found " : "notfound\">not found");
            b.append("</span></li>");
        }
        return b.toString();
    }
}
