package com.unleashyouradventure.swaccess.readers;

import com.unleashyouradventure.swapi.retriever.Book.FileType;

public class KoboReader extends ReaderWithIntent {

    KoboReader() {
        super("Kobo", FileType.Epub);
    }

    @Override
    protected String getReaderUri() {
        return "com.kobobooks.android";
    }

    @Override
    protected String getReaderClass() {
        return "com.kobobooks.android.screens.Welcome";
    }

}
