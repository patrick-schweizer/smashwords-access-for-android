package com.unleashyouradventure.swaccess.readers;

import java.io.File;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.unleashyouradventure.swaccess.util.AndroidHelper;
import com.unleashyouradventure.swapi.retriever.Book.FileType;

public abstract class ReaderWithIntent extends Reader {

    public ReaderWithIntent(String name, FileType... types) {
        super(name, types);
    }

    @Override
    public boolean isReaderAvailable(Context context) {
        return AndroidHelper.isAppInstalled(context, getReaderUri());
    }

    protected abstract String getReaderUri();

    @Override
    public CopyToReaderResult addBookToReader(File file, Context context) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setComponent(new ComponentName(getReaderUri(), getReaderClass()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.fromFile(file));
        CopyToReaderResult result;
        try {
            context.startActivity(intent);
            result = new CopyToReaderResult(true, "Book sent to " + getName());
        } catch (Exception e) {
            result = new CopyToReaderResult(true, e.getMessage());
        }
        return result;
    }

    protected abstract String getReaderClass();

    public String getReaderLink() {
        return "market://details?id=" + getReaderUri();
    }

}
