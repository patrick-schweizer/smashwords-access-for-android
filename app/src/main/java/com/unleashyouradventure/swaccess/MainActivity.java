package com.unleashyouradventure.swaccess;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import com.actionbarsherlock.app.SherlockActivity;
import com.novoda.imageloader.core.model.ImageTag;
import com.novoda.imageloader.core.model.ImageTagFactory;
import com.unleashyouradventure.swaccess.activity.booklist.BookListActivity;
import com.unleashyouradventure.swaccess.activity.booklist.BookListActivity.CategoryList;
import com.unleashyouradventure.swaccess.activity.booklist.BookListActivity.LibraryList;
import com.unleashyouradventure.swaccess.activity.booklist.SearchList;
import com.unleashyouradventure.swaccess.util.AppRater;
import com.unleashyouradventure.swaccess.util.BookOfTheDayHelper;
import com.unleashyouradventure.swapi.model.ImageSize;
import com.unleashyouradventure.swapi.retriever.Book;

public class MainActivity extends SherlockActivity {
    private ImageTagFactory imageTagFactory;
    private GridView menu;
    private BookOfTheDayHelper bookOfTheDayHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        menu = (GridView) findViewById(R.id.mainGrid);
        menu.setAdapter(new MenuItem(this, R.layout.main_item));

        // Book of the day
        bookOfTheDayHelper = new BookOfTheDayHelper(this);
        this.imageTagFactory = ImageTagFactory.newInstance(this, R.drawable.spinner_black_20);
        imageTagFactory.setErrorImageId(R.drawable.loading_error);

        // Rating reminder
        // debug AppRater.showRateDialog(this, null);
        AppRater.app_launched(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new ShowBookOfTheDayTask(this).execute();
    }

    public class MenuItem extends ArrayAdapter<MainActivityMenyEntry> {

        public MenuItem(Context context, int textViewResourceId) {
            super(context, textViewResourceId, MainActivityMenyEntry.values());
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = createRow(parent);
            TextView tv = (TextView) row.findViewById(R.id.text);
            MainActivityMenyEntry entry = MainActivityMenyEntry.values()[position];
            tv.setText(getContext().getText(entry.getTextId()));
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

        private MainActivityMenyEntry entry;

        private StartActivityOnClickListener(MainActivityMenyEntry entry) {
            this.entry = entry;
        }

        @Override
        public void onClick(View v) {
            Intent intent = null;
            if (entry == MainActivityMenyEntry.myLibrary) {
                intent = new Intent(menu.getContext(), BookListActivity.class);
                intent.putExtra(BookListActivity.IntentProperty.listType.name(), new LibraryList());
            } else if (entry == MainActivityMenyEntry.settings) {
                intent = new Intent(menu.getContext(), PreferencesActivity.class);
            } else if (entry == MainActivityMenyEntry.search) {
                intent = new Intent(menu.getContext(), BookListActivity.class);
                intent.putExtra(BookListActivity.IntentProperty.listType.name(), new SearchList());
            } else if (entry == MainActivityMenyEntry.category) {
                intent = new Intent(menu.getContext(), BookListActivity.class);
                intent.putExtra(BookListActivity.IntentProperty.listType.name(), new CategoryList());
            } else if (entry == MainActivityMenyEntry.reader) {
                intent = new Intent(menu.getContext(), ReaderActivity.class);
            } else if (entry == MainActivityMenyEntry.help) {
                String url = "http://unleashyouradventure.com/smashwords-access-help-faq/";
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
            }else {
                intent = new Intent(menu.getContext(), AboutActivity.class);
            }
            menu.getContext().startActivity(intent);
        }
    }

    private class ShowBookOfTheDayTask extends AsyncTask<Void, Void, Book> {
        private final Context context;

        public ShowBookOfTheDayTask(Context context) {
            this.context = context;
        }

        @Override
        protected Book doInBackground(Void... params) {
            final Book book = bookOfTheDayHelper.getBookOfTheDay();
            return book;
        }

        @Override
        protected void onPostExecute(final Book book) {
            ImageView bookDetailImage = (ImageView) findViewById(R.id.mainBookOfTheDayImage);
            ImageTag tag = imageTagFactory.build(book.getCover_url(ImageSize.full), context);
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