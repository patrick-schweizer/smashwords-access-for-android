package com.unleashyouradventure.swaccess.readers;

import java.io.File;

import android.content.Context;
import android.os.Environment;

import com.unleashyouradventure.swapi.retriever.Book.FileType;

public abstract class ReaderWithFolder extends Reader {

    protected ReaderWithFolder(String name, FileType... types) {
        super(name, types);
    }

    @Override
    public boolean isReaderAvailable(Context context) {
        return getFileDir().exists();
    }

    protected abstract String getFileDirName();

    protected File getFileDir() {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        return new File(sdCardRoot, getFileDirName());
    }

    @Override
    public CopyToReaderResult addBookToReader(File file, Context context) {
        File newFile = new File(getFileDir(), file.getName());
        boolean copyOK = file.renameTo(newFile);

        if (copyOK) {
            String msg = "Moved book to " + getName() + " directory (" + newFile.getAbsolutePath() + ")";
            return new CopyToReaderResult(false, msg);
        } else {
            String msg = "Can not copy book to " + newFile;
            return new CopyToReaderResult(false, msg);
        }
    }

}
