package com.unleashyouradventure.swaccess;

import com.unleashyouradventure.swaccess.activity.booklist.BookListActivity;
import com.unleashyouradventure.swapi.load.PageLoader;
import com.unleashyouradventure.swapi.retriever.BookList;

import java.io.IOException;
import java.io.Serializable;

public abstract class ListType implements Serializable {
    protected final String title;

    public ListType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public abstract BookList load(PageLoader.ProgressCallback progressCallback) throws IOException;

    public void showOptions(BookListActivity context) {
        // default: do nothing
    }

    public boolean hasOptions() {
        return false;
    }

    public boolean isReadyForLoading() {
        return true;
    }
}
