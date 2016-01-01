package com.unleashyouradventure.swaccess.activity.booklist;

import com.unleashyouradventure.swaccess.ListType;
import com.unleashyouradventure.swaccess.SmashwordsAPIHelper;
import com.unleashyouradventure.swapi.load.PageLoader;
import com.unleashyouradventure.swapi.model.SwSeries;
import com.unleashyouradventure.swapi.retriever.BookList;
import com.unleashyouradventure.swapi.retriever.BookListRetriever;

import java.io.IOException;

import lombok.RequiredArgsConstructor;

public class SeriesList extends ListType {
    private final SwSeries series;


    public SeriesList(SwSeries series) {
        super(series.getName());
        this.series = series;
    }

    public BookList load(PageLoader.ProgressCallback progressCallback) throws IOException {
        BookListRetriever retriever = SmashwordsAPIHelper.getSmashwords().getBookListRetriever();
        BookList bookList = retriever.getBooksBySeries(progressCallback, series);
        return bookList;
    }
}
