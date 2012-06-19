package com.unleashyouradventure.swaccess;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.novoda.imageloader.core.model.ImageTag;
import com.novoda.imageloader.core.model.ImageTagFactory;
import com.unleashyouradventure.swaccess.BookListActivity.CategoryList;
import com.unleashyouradventure.swaccess.BookListActivity.LibraryList;
import com.unleashyouradventure.swaccess.BookListActivity.SearchList;
import com.unleashyouradventure.swaccess.util.BookOfTheDayHelper;
import com.unleashyouradventure.swapi.retriever.Book;
import com.unleashyouradventure.swapi.retriever.Book.ImageSize;

public class MainActivity extends Activity {

    enum Entry {
        myLibrary("My Library", android.R.drawable.ic_menu_gallery), search("Search", android.R.drawable.ic_menu_search), category(
                "By Category", android.R.drawable.ic_menu_compass), settings("Preferences",
                android.R.drawable.ic_menu_preferences), reader("Readers", android.R.drawable.ic_menu_preferences),

        about("About", android.R.drawable.ic_menu_info_details);
        private final String text;
        private final int iconId;

        Entry(String text, int iconId) {
            this.text = text;
            this.iconId = iconId;
        }

        public String getText() {
            return text;
        }

        public int getIconId() {
            return iconId;
        }

    }

    private ImageTagFactory imageTagFactory;
    private GridView menu;
    private BookOfTheDayHelper bookOfTheDayHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        menu = (GridView) findViewById(R.id.mainGrid);
        menu.setAdapter(new MenuItem(this, R.layout.main_item));

        // Book of the day
        bookOfTheDayHelper = new BookOfTheDayHelper(this);
        this.imageTagFactory = new ImageTagFactory(this, R.drawable.spinner_black_20);
        imageTagFactory.setErrorImageId(R.drawable.loading_error);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new ShowBookOfTheDayTask().execute();
    }

    public class MenuItem extends ArrayAdapter<Entry> {

        public MenuItem(Context context, int textViewResourceId) {
            super(context, textViewResourceId, Entry.values());
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = createRow(parent);
            TextView tv = (TextView) row.findViewById(R.id.text);
            Entry entry = Entry.values()[position];
            tv.setText(entry.getText());
            tv.setCompoundDrawablesWithIntrinsicBounds(0, entry.getIconId(), 0, 0);
            tv.setOnClickListener(new StartActivityOnClickListener(entry));
            return row;
        }

        private View createRow(ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.main_item, parent, false);
            return row;
        }

    }

    private class StartActivityOnClickListener implements OnClickListener {

        private Entry entry;

        private StartActivityOnClickListener(Entry entry) {
            this.entry = entry;
        }

        @Override
        public void onClick(View v) {
            Intent intent = null;
            if (entry == Entry.myLibrary) {
                intent = new Intent(menu.getContext(), BookListActivity.class);
                intent.putExtra(BookListActivity.IntentProperty.listType.name(), new LibraryList());
            } else if (entry == Entry.settings) {
                intent = new Intent(menu.getContext(), PreferencesActivity.class);
            } else if (entry == Entry.search) {
                intent = new Intent(menu.getContext(), BookListActivity.class);
                intent.putExtra(BookListActivity.IntentProperty.listType.name(), new SearchList());
            } else if (entry == Entry.category) {
                intent = new Intent(menu.getContext(), BookListActivity.class);
                intent.putExtra(BookListActivity.IntentProperty.listType.name(), new CategoryList());
            } else if (entry == Entry.reader) {
                intent = new Intent(menu.getContext(), ReaderActivity.class);
            } else {
                intent = new Intent(menu.getContext(), AboutActivity.class);
            }
            menu.getContext().startActivity(intent);
        }
    }

    private class ShowBookOfTheDayTask extends AsyncTask<Void, Void, Book> {

        @Override
        protected Book doInBackground(Void... params) {
            final Book book = bookOfTheDayHelper.getBookOfTheDay();
            return book;
        }

        @Override
        protected void onPostExecute(final Book book) {
            ImageView bookDetailImage = (ImageView) findViewById(R.id.mainBookOfTheDayImage);
            ImageTag tag = imageTagFactory.build(book.getCoverUrl(ImageSize.thumb));
            ((ImageView) bookDetailImage).setTag(tag);
            SmashwordsAPIHelper.getImageLoader().getLoader().load(bookDetailImage);
            bookDetailImage.setClickable(true);
            bookDetailImage.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(menu.getContext(), BookActivity.class);
                    intent.putExtra(BookActivity.IntentParam.bookToShow.name(), Long.valueOf(book.getId()));
                    menu.getContext().startActivity(intent);
                }
            });
        }
    }
}