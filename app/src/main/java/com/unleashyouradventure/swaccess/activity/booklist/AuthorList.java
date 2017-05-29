package com.unleashyouradventure.swaccess.activity.booklist;

import com.unleashyouradventure.swaccess.ListType;
import com.unleashyouradventure.swaccess.SmashwordsAPIHelper;
import com.unleashyouradventure.swapi.load.PageLoader;
import com.unleashyouradventure.swapi.model.SwAuthor;
import com.unleashyouradventure.swapi.model.SwPerson;
import com.unleashyouradventure.swapi.model.SwSeries;
import com.unleashyouradventure.swapi.retriever.BookList;
import com.unleashyouradventure.swapi.retriever.BookListRetriever;

import java.io.IOException;

public class AuthorList extends ListType {
    private final SwPerson person;


    public AuthorList(SwPerson person) {
        super(person.getAccount().getDisplay_name());
        this.person = person;
    }

    public BookList load(PageLoader.ProgressCallback progressCallback) throws IOException {
        BookListRetriever retriever = SmashwordsAPIHelper.getSmashwords().getBookListRetriever();
        BookList bookList = retriever.getBooksByAuthor(progressCallback, person);
        return bookList;
    }
}
