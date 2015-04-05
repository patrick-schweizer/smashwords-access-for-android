package com.unleashyouradventure.swaccess.readers;

import com.unleashyouradventure.swapi.retriever.Book.FileType;

public class PageTurner extends ReaderWithIntent {

    PageTurner() {
        super("PageTurner", FileType.Epub);
    }

    @Override
    protected String getReaderUri() {
        return "net.nightwhistler.pageturner.ads";
    }

    @Override
    protected String getReaderClass() {
        return "net.nightwhistler.pageturner.activity.ReadingActivity";
    }

    public String getReaderLink() {
        return "market://details?id=net.nightwhistler.pageturner.ads";
    }
}
