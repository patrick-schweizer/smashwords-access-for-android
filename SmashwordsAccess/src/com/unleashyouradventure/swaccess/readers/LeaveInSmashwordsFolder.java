package com.unleashyouradventure.swaccess.readers;

import java.io.File;

import android.content.Context;

import com.unleashyouradventure.swapi.retriever.Book.FileType;

public class LeaveInSmashwordsFolder extends Reader {

    public LeaveInSmashwordsFolder() {
        super("Leave book in Smashwords folder", new FileType[] { FileType.Epub, FileType.PDF, FileType.MOBI });
    }

    @Override
    public boolean isReaderAvailable(Context context) {
        return true;
    }

    @Override
    public CopyToReaderResult addBookToReader(File file, Context context) {
        return new CopyToReaderResult(true, "The book file was downloaded to: " + file.getAbsolutePath());
    }
}
