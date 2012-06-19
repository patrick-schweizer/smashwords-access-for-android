package com.unleashyouradventure.swaccess.readers;

import com.unleashyouradventure.swapi.retriever.Book.FileType;

public class FBReader extends ReaderWithIntent {

    final static String FB_READER_URI = "org.geometerplus.zlibrary.ui.android";

    FBReader() {
        super("FBReader", FileType.Epub, FileType.MOBI, FileType.RTF);
    }

    @Override
    protected String getReaderUri() {
        return FB_READER_URI;
    }

    @Override
    protected String getReaderClass() {
        return "org.geometerplus.android.fbreader.FBReader";
    }
}
