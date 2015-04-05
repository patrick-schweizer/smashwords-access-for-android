package com.unleashyouradventure.swaccess.readers;

import com.unleashyouradventure.swapi.retriever.Book.FileType;

public class PageTurnerPro extends ReaderWithIntent {

    PageTurnerPro() {
        super("PageTurner Pro", FileType.Epub);
    }

    @Override
    protected String getReaderUri() {
        return "net.nightwhistler.pageturner.pro";
    }

    @Override
    protected String getReaderClass() {
        return getReaderUri() + ".activity.ReadingActivity";
    }

    public String getReaderLink() {
        return "market://details?id=net.nightwhistler.pageturner.pro";
    }
}
