package com.unleashyouradventure.swaccess;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.novoda.imageloader.core.model.ImageTag;
import com.novoda.imageloader.core.model.ImageTagFactory;
import com.unleashyouradventure.swaccess.util.AndroidHelper;
import com.unleashyouradventure.swaccess.util.Format;
import com.unleashyouradventure.swaccess.util.ProgressCallbackAsyncTask;
import com.unleashyouradventure.swaccess.util.StringUtils;
import com.unleashyouradventure.swapi.load.PageLoader;
import com.unleashyouradventure.swapi.load.PageLoader.ProgressCallback;
import com.unleashyouradventure.swapi.retriever.Book;
import com.unleashyouradventure.swapi.retriever.BookCategory;
import com.unleashyouradventure.swapi.retriever.BookList;
import com.unleashyouradventure.swapi.retriever.BookListRetriever;
import com.unleashyouradventure.swapi.retriever.BookListRetriever.Length;
import com.unleashyouradventure.swapi.retriever.BookListRetriever.Price;
import com.unleashyouradventure.swapi.retriever.BookListRetriever.Sortby;

public class BookListActivity extends SherlockListActivity {

    public enum IntentProperty {
        listType, searchTerm
    }

    private ListView lv;
    private ArrayAdapter<Book> listAdapter;
    private ProgressDialog progress;
    private ImageTagFactory imageTagFactory;
    private BookList booklist;
    private ListType listType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listType = (ListType) getIntent().getSerializableExtra(IntentProperty.listType.name());
        listAdapter = new BookListAdapter(this, R.layout.book_list_item);
        setListAdapter(listAdapter);
        lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Book book = listAdapter.getItem(position);
                if (book instanceof DummyBookForLoadList) {
                    new LoadMoreTask().execute();
                } else {
                    Intent intent = new Intent(lv.getContext(), BookActivity.class);
                    intent.putExtra(BookActivity.IntentParam.bookToShow.name(), book.getId());
                    lv.getContext().startActivity(intent);
                }
            }
        });
        this.imageTagFactory = new ImageTagFactory(this, R.drawable.spinner_black_20);
        showList(new BookList());
        if (this.listType.isReadyForLoading()) {
            reloadList();
        } else {
            this.listType.showOptions(this);
        }
        this.getSupportActionBar().setTitle(this.listType.getTitle());
    }

    private void showList(BookList result) {
        this.getSupportActionBar().setTitle(this.listType.getTitle());
        this.listAdapter.clear();
        for (Book book : result)
            listAdapter.add(book);
        if (result.hasMoreElementsToLoad()) {
            listAdapter.add(new DummyBookForLoadList());
        }
        listAdapter.notifyDataSetChanged();
    }

    void reloadList() {
        this.progress = AndroidHelper.createAndShowProgressDialog(this, "Downloading book list");
        new LoadListTask(progress).execute(listType);
    }

    // legacy code: for the options button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_MENU)) {
            this.listType.showOptions(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (this.listType.hasOptions()) {
            MenuItem item = menu.add(0, 1, 0, "Options");
            item.setIcon(android.R.drawable.ic_menu_preferences);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
            return false;
        }
        this.listType.showOptions(this);
        return true;
    }

    private class BookListAdapter extends ArrayAdapter<Book> {

        LayoutInflater inflater;

        public BookListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Book book = getItem(position);

            if (book instanceof DummyBookForLoadList) {
                convertView = inflater.inflate(R.layout.book_list_item_more, null);
                return convertView;
            }

            convertView = inflater.inflate(R.layout.book_list_item, null);

            ImageView bookImageView = (ImageView) convertView.findViewById(R.id.book_list_image);
            ImageTag tag = imageTagFactory.build(book.getCoverUrl(Book.ImageSize.thumb));
            ((ImageView) bookImageView).setTag(tag);
            SmashwordsAPIHelper.getImageLoader().getLoader().load(bookImageView);

            TextView tv = (TextView) convertView.findViewById(R.id.book_list_text);
            String priceString = Format.getPrice(book.getPriceInCent());
            tv.setText(book.getTitle() + "\n" + book.getAuthor() + "\n" + priceString);

            return convertView;
        }
    }

    private class LoadListTask extends ProgressCallbackAsyncTask<ListType, BookList> {
        public LoadListTask(ProgressDialog dialog) {
            super(dialog);
        }

        private String resultMessage = null;

        @Override
        protected BookList doInBackground(ListType... params) {

            ListType type = params[0];
            if (type instanceof LibraryList
                    && !SmashwordsAPIHelper.getSmashwords().getLogin().areCredentialsWellFormed()) {
                resultMessage = "In order to view your library you must enter your username and password under preferences.";
                return new BookList();
            }
            try {
                return type.load(this);
            } catch (UnknownHostException e) {
                resultMessage = "Please check your Internet connection.";
            } catch (IOException e) {
                resultMessage = e.getMessage();
            }
            return new BookList();
        }

        @Override
        protected void onPostExecute(BookList result) {
            booklist = result;
            showList(result);
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (resultMessage != null)
                showToast(resultMessage);
        }
    }

    private class LoadMoreTask extends AsyncTask<Void, Integer, Void> {
        private String resultMessage = null;

        @Override
        protected void onPreExecute() {
            TextView bookListMoreText = (TextView) BookListActivity.this.findViewById(R.id.bookListItemMoreText);
            bookListMoreText.setVisibility(View.GONE);
            View bookListItemMoreLoadImage = BookListActivity.this.findViewById(R.id.bookListItemMoreLoadImage);
            bookListItemMoreLoadImage.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                BookListRetriever retriever = SmashwordsAPIHelper.getSmashwords().getBookListRetriever();
                retriever.getMoreBooks(PageLoader.PROGRESS_CALLBACK_DUMMY, booklist);

            } catch (UnknownHostException e) {
                resultMessage = "Please check your Internet connection.";
            } catch (IOException e) {
                resultMessage = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            listAdapter.clear();
            for (Book book : booklist)
                listAdapter.add(book);
            if (booklist.hasMoreElementsToLoad()) {
                listAdapter.add(new DummyBookForLoadList());
            }
            listAdapter.notifyDataSetChanged();
            if (resultMessage != null)
                showToast(resultMessage);
        }

    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }

    /** Marker class, indicates the load more entry */
    private static class DummyBookForLoadList extends Book {
    }

    public static abstract class ListType implements Serializable {
        protected final String title;

        public ListType(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public abstract BookList load(ProgressCallback progressCallback) throws IOException;

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

    public static class SearchList extends ListType {

        private String searchTerm;

        public SearchList() {
            super("Book Search");
        }

        public BookList load(ProgressCallback progressCallback) throws IOException {
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

    public static class LibraryList extends ListType {

        public LibraryList() {
            super("My Library");
        }

        public BookList load(ProgressCallback progressCallback) throws IOException {
            BookListRetriever retriever = SmashwordsAPIHelper.getSmashwords().getBookListRetriever();
            return retriever.getBooksFromLibary(progressCallback);
        }
    }

    public static class CategoryList extends ListType {

        private BookCategory category;
        private Sortby sortby;
        private Price price;
        private Length length;

        public CategoryList() {
            super("Category");
        }

        public String getTitle() {
            String txt = title;
            if (category != null) {
                txt += " » ";
                txt += category.toString();
            }
            return txt;
        }

        public BookList load(ProgressCallback progressCallback) throws IOException {
            BookListRetriever retriever = SmashwordsAPIHelper.getSmashwords().getBookListRetriever();
            return retriever.getBooksByCategory(progressCallback, category, sortby, price, length);
        }

        @Override
        public boolean isReadyForLoading() {
            return this.category != null;
        }

        @Override
        public void showOptions(final BookListActivity context) {

            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Search for Books");
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);

            // Set an EditText view to get user input
            final Spinner categorySpinner = new Spinner(context);
            ArrayAdapter<BookCategory> categoryAdapter = new ArrayAdapter<BookCategory>(context,
                    android.R.layout.simple_spinner_item);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            BookCategory root = SmashwordsAPIHelper.getSmashwords().getBookCategoryRetriever().getRootCategory();
            categoryAdapter.add(root);
            addCategories(categoryAdapter, root.getChildren().get(0).getChildren());// Fiction
            addCategories(categoryAdapter, root.getChildren().get(1).getChildren());// Non-Fiction
            categorySpinner.setAdapter(categoryAdapter);
            setSelection(categorySpinner, categoryAdapter, category);
            layout.addView(categorySpinner);

            final Spinner sortbySpinner = new Spinner(context);
            ArrayAdapter<Sortby> sortbyAdapter = new ArrayAdapter<Sortby>(context,
                    android.R.layout.simple_spinner_item, Sortby.values());
            sortbyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sortbySpinner.setAdapter(sortbyAdapter);
            setSelection(sortbySpinner, sortbyAdapter, sortby);
            layout.addView(sortbySpinner);

            final Spinner priceSpinner = new Spinner(context);
            ArrayAdapter<Price> priceAdapter = new ArrayAdapter<Price>(context, android.R.layout.simple_spinner_item,
                    Price.values());
            priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            priceSpinner.setAdapter(priceAdapter);
            setSelection(priceSpinner, priceAdapter, price);
            layout.addView(priceSpinner);

            final Spinner lengthSpinner = new Spinner(context);
            ArrayAdapter<Length> lengthAdapter = new ArrayAdapter<Length>(context,
                    android.R.layout.simple_spinner_item, Length.values());
            lengthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            lengthSpinner.setAdapter(lengthAdapter);
            setSelection(lengthSpinner, lengthAdapter, length);
            layout.addView(lengthSpinner);
            alert.setView(layout);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    BookCategory newCategory = (BookCategory) categorySpinner.getSelectedItem();
                    Sortby newSortby = (Sortby) sortbySpinner.getSelectedItem();
                    Price newPrice = (Price) priceSpinner.getSelectedItem();
                    Length newLength = (Length) lengthSpinner.getSelectedItem();

                    boolean reloadRequired = !(newCategory.equals(category)) || sortby != newSortby
                            || price != newPrice || length != newLength;
                    if (reloadRequired) {
                        category = newCategory;
                        sortby = newSortby;
                        price = newPrice;
                        length = newLength;
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

        private <T> void setSelection(Spinner spinner, ArrayAdapter<T> adapter, T selection) {
            if (selection == null)
                return;
            spinner.setSelection(adapter.getPosition(selection));
        }

        private void addCategories(ArrayAdapter<BookCategory> categoryAdapter, List<BookCategory> categories) {
            for (BookCategory cat : categories) {
                categoryAdapter.add(cat);
            }

        }
    }
}