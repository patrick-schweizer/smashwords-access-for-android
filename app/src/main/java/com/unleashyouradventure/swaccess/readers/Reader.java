package com.unleashyouradventure.swaccess.readers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;

import com.unleashyouradventure.swapi.retriever.Book;
import com.unleashyouradventure.swapi.retriever.Book.FileType;

public abstract class Reader {

    private static Map<String, Reader> readers = new HashMap<String, Reader>();
    private static List<Reader> orderedReaderList = new ArrayList<Reader>();
    static {
        addReader(new CoolReader());
        addReader(new FBReader());
        addReader(new Kindle());
        addReader(new PageTurner());
        addReader(new LeaveInSmashwordsFolder());
    }

    public Reader(String name, FileType[] types) {
        this.name = name;
        for (FileType type : types) {
            this.preferredFileTypes.add(type);
        }
    }

    private static void addReader(Reader reader) {
        readers.put(reader.getId(), reader);
        orderedReaderList.add(reader);
    }

    public static List<Reader> getAvailableReaders() {
        List<Reader> availableReaders = new ArrayList<Reader>();
        for (Reader reader : orderedReaderList) {
            availableReaders.add(reader);
        }
        return availableReaders;
    }

    public static List<Reader> getAvailableReadersForBookFormats(Context context, Book book) {
        List<Reader> availableReaders = new ArrayList<Reader>();
        for (Reader reader : orderedReaderList) {
            for (FileType type : book.getFileTypes()) {
                if (reader.isReaderAvailable(context) && reader.acceptFileType(type)) {
                    availableReaders.add(reader);
                    break;
                }
            }
        }
        return availableReaders;
    }

    protected List<FileType> preferredFileTypes = new ArrayList<FileType>();
    protected String name;

    public String getId() {
        return getClass().getSimpleName();
    }

    public abstract boolean isReaderAvailable(Context context);

    public String getName() {
        return name;
    }

    public boolean acceptFileType(Book.FileType type) {
        return preferredFileTypes.contains(type);
    }

    public FileType getPreferredFileType(Set<FileType> types) {
        for (FileType prefered : preferredFileTypes) {
            if (types.contains(prefered)) {
                return prefered;
            }
        }
        return null;
    }

    public abstract CopyToReaderResult addBookToReader(File file, Context context);

    public class CopyToReaderResult {

        private static final long serialVersionUID = 1L;
        final boolean success;
        final String message;

        public CopyToReaderResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public String toString() {
            return this.message;
        }
    }

    public static Reader getReader(String id) {
        return readers.get(id);
    }

    public String getReaderLink() {
        return null;
    }
}
