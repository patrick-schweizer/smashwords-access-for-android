package com.unleashyouradventure.swaccess.readers;

import java.io.File;

import android.content.Context;
import android.content.Intent;

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

    @Override
    public CopyToReaderResult addBookToReader(File file, Context context) {
        CopyToReaderResult result = super.addBookToReader(file, context);
        // Now launch Kindle
        Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage("com.amazon.kindle");
        context.startActivity(LaunchIntent);
        return result;
    }
}
