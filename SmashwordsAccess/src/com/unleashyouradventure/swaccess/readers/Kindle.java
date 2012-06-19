package com.unleashyouradventure.swaccess.readers;

import com.unleashyouradventure.swapi.retriever.Book.FileType;

public class Kindle extends ReaderWithFolder {

    protected Kindle() {
        super("Kindle", FileType.MOBI);
    }

    @Override
    protected String getFileDirName() {
        return "kindle";
    }

    public String getReaderLink() {
        return "market://details?id=com.amazon.kindle";
    }
}
