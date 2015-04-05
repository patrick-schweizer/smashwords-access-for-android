package com.unleashyouradventure.swaccess.readers;

import com.unleashyouradventure.swapi.retriever.Book.FileType;

public class CoolReader extends ReaderWithIntent {

    public CoolReader() {
        super("CoolReader", FileType.Epub);
    }

    @Override
    protected String getReaderUri() {
        return "org.coolreader";
    }

    @Override
    protected String getReaderClass() {
        return getReaderUri() + ".CoolReader";
    }

}
