package com.unleashyouradventure.swaccess.readers;

import com.unleashyouradventure.swapi.retriever.Book.FileType;

public class PageTurner extends ReaderWithIntent {

    PageTurner() {
        super("PageTurner", FileType.Epub);
    }

    @Override
    protected String getReaderUri() {
        return "net.nightwhistler.pageturner";
    }

    @Override
    protected String getReaderClass() {
        return getReaderUri() + ".activity.ReadingActivity";
    }

    public String getReaderLink() {
        return "market://details?id=net.nightwhistler.pageturner.ads";
    }
}
