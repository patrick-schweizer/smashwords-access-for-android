package com.unleashyouradventure.swaccess.activity.booklist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

import com.unleashyouradventure.swaccess.ListType;
import com.unleashyouradventure.swaccess.SmashwordsAPIHelper;
import com.unleashyouradventure.swaccess.util.StringUtils;
import com.unleashyouradventure.swapi.load.PageLoader;
import com.unleashyouradventure.swapi.retriever.BookList;
import com.unleashyouradventure.swapi.retriever.BookListRetriever;

import java.io.IOException;

public class SearchList extends ListType {

    private String searchTerm;

    public SearchList() {
        super("Book Search");
    }

    public BookList load(PageLoader.ProgressCallback progressCallback) throws IOException {
        BookListRetriever retriever = SmashwordsAPIHelper.getSmashwords().getBookListRetriever();
        BookList bookList = retriever.getBooksBySearch(progressCallback, searchTerm);
        return bookList;
    }

    @Override
    public void showOptions(final BookListActivity context) {

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Search for Books");

        // Set an EditText view to get user input
        final EditText input = new EditText(context);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newSearchTerm = input.getText().toString();
                boolean reloadRequired = !(StringUtils.equals(newSearchTerm, searchTerm));
                if (reloadRequired) {
                    searchTerm = newSearchTerm;
                    context.reloadList();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    @Override
    public boolean hasOptions() {
        return true;
    }

    public boolean isReadyForLoading() {
        return this.searchTerm != null && this.searchTerm.trim().length() > 0;
    }

    public String getTitle() {
        String txt = title;
        if (this.searchTerm != null) {
            txt += " » ";
            txt += searchTerm;
        }
        return txt;
    }
}
