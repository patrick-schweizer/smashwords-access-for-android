package com.unleashyouradventure.swaccess;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.novoda.imageloader.core.model.ImageTag;
import com.novoda.imageloader.core.model.ImageTagFactory;
import com.unleashyouradventure.swaccess.activity.booklist.BookListActivity;
import com.unleashyouradventure.swaccess.activity.booklist.SeriesList;
import com.unleashyouradventure.swaccess.readers.LeaveInSmashwordsFolder;
import com.unleashyouradventure.swaccess.readers.Reader;
import com.unleashyouradventure.swaccess.readers.Reader.CopyToReaderResult;
import com.unleashyouradventure.swaccess.util.AndroidHelper;
import com.unleashyouradventure.swaccess.util.BookOfTheDayHelper;
import com.unleashyouradventure.swaccess.util.Format;
import com.unleashyouradventure.swaccess.util.ProgressCallbackAsyncTask;
import com.unleashyouradventure.swapi.Smashwords;
import com.unleashyouradventure.swapi.model.ImageSize;
import com.unleashyouradventure.swapi.model.SwSeries;
import com.unleashyouradventure.swapi.retriever.Book;
import com.unleashyouradventure.swapi.retriever.Book.Download;
import com.unleashyouradventure.swapi.retriever.Book.FileType;
import com.unleashyouradventure.swapi.util.StringTrimmer;

public class BookActivity extends SherlockActivity {

    private ProgressDialog progress;

    public enum IntentParam {
        bookToShow;
    }

    private Book book = null;
    private Smashwords sw = SmashwordsAPIHelper.getSmashwords();
    private ImageTagFactory imageTagFactory;
    private BookOfTheDayHelper bookOfTheDayHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book);
        this.imageTagFactory = ImageTagFactory.newInstance(this, R.drawable.spinner_black_20);
        imageTagFactory.setErrorImageId(R.drawable.loading_error);
        bookOfTheDayHelper = new BookOfTheDayHelper(this);
        long bookId = getBookIdFromIntent();
        book = SmashwordsAPIHelper.getSmashwords().getBookRetriever().getCache().getBook(bookId);
        if (book == null) {
            // Loading complete book
            this.progress = AndroidHelper.createAndShowProgressDialog(this, "Loading book");
        } else {
            // Loading only book details
            displayBook();
        }
        new LoadBookTask(this.progress).execute(bookId);

    }

    private long getBookIdFromIntent() {
        final Intent intent = getIntent();
        final String action = intent.getAction();
        long id;
        if (Intent.ACTION_VIEW.equals(action)) {
            String bookId = new StringTrimmer(intent.getData().getPath()).getBeforeNext("?").getAfterLast("/").toString();
            id = Long.parseLong(bookId);
        } else {
            id = getIntent().getLongExtra(IntentParam.bookToShow.name(), 0);
        }
        return id;
    }

    private void displayBook() {

        // Reset
        TextView bookDetailsView = (TextView) findViewById(R.id.bookDetails);
        bookDetailsView.setText("");
        if (book == null)
            return;

        // Bar
        this.getSupportActionBar().setTitle(book.getTitle());

        // Progress for loading details
        findViewById(R.id.bookLoadProgress).setVisibility(book.isBookDetailsAdded() ? View.GONE : View.VISIBLE);

        // Image
        ImageView bookDetailImage = (ImageView) findViewById(R.id.bookDetailImage);
        ImageTag tag = imageTagFactory.build(book.getCover_url(ImageSize.full), this);
        ((ImageView) bookDetailImage).setTag(tag);
        SmashwordsAPIHelper.getImageLoader().getLoader().load(bookDetailImage);

        // Rating
        RatingBar ratingBar = (RatingBar) findViewById(R.id.bookRatingBar);
        ratingBar.setVisibility((book.getRating() > -1) ? View.VISIBLE : View.GONE);
        if (book.getRating() >= 0) {
            ratingBar.setRating((float) book.getRating());
        }

        // Book Details
        StringBuilder b = new StringBuilder();
        b.append(book.getTitle()).append("\n");
        b.append("by ").append(book.getFirstAuthorDisplayNameOrNull()).append("\n");
        String priceString = Format.getPrice(book.getPrice());
        b.append("Price: ").append(priceString).append("\n");
        bookDetailsView.setText(b.toString());

        // Buy
        Button bookButtonBuy = (Button) findViewById(R.id.bookButtonBuy);
        boolean showBuyButton = (book.isBookDetailsAdded() && book.canBookBeBought());
        bookButtonBuy.setVisibility(showBuyButton ? Button.VISIBLE : Button.GONE);
        bookButtonBuy.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent httpIntent = new Intent(Intent.ACTION_VIEW);
                httpIntent.setData(Uri.parse(book.getUrlForBookDetails() + "?ref=swaccess"));
                // httpIntent.setData(Uri.parse(book.getUrlForPuttingThisBookInShoppingCart()
                // + "?ref=swaccess"));
                startActivity(httpIntent);
            }
        });

        // Download Sample
        Button bookButtonDownloadSample = (Button) findViewById(R.id.bookButtonDownloadSample);
        bookButtonDownloadSample.setVisibility(showBuyButton ? Button.VISIBLE : Button.GONE);

        // Download
        Button bookButtonDownload = (Button) findViewById(R.id.bookButtonDownload);
        bookButtonDownload.setVisibility((book.isBookDetailsAdded() && book.canBookBeDownloaded()) ? Button.VISIBLE : Button.GONE);
        OnClickListener downloadListener = new OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(BookActivity.this);
                builder.setTitle("Pick a reader app");
                final List<Reader> availableReaders = Reader.getAvailableReadersForBookFormats(BookActivity.this, book);
                if (availableReaders.size() == 1 && availableReaders.get(0) instanceof LeaveInSmashwordsFolder) {
                    availableReaders.add(0, new DownloadNewReaderMarker());
                }
                final String[] items = getAvailableReadersAsStringArray(availableReaders);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int index) {
                        Reader reader = availableReaders.get(index);
                        if (reader instanceof DownloadNewReaderMarker) {
                            Intent intent = new Intent(BookActivity.this, AboutActivity.class);
                            BookActivity.this.startActivity(intent);
                            return;
                        }
                        FileType fileType = reader.getPreferredFileType(book.getFileTypes());
                        Download download = book.getDownloadLinkForNewestVersion(fileType);
                        progress = new ProgressDialog(BookActivity.this);
                        progress.setTitle("");
                        progress.setMessage("Downloading book");
                        progress.setIndeterminate(false);
                        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progress.show();
                        new DownloadAndAddBookToLibraryTask(reader, download).execute();
                    }
                });
                builder.create().show();
            }
        };
        bookButtonDownload.setOnClickListener(downloadListener);
        bookButtonDownloadSample.setOnClickListener(downloadListener);

        // Share
        Button shareButton = (Button) findViewById(R.id.bookButtonShare);
        shareButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String title = book.getTitle();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, title + " " + book.getUrlForBookDetails() + "?ref=swaccess");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
                startActivity(Intent.createChooser(intent, "Share " + title));
            }
        });

        // Book Description
        TextView bookDescriptionView = (TextView) findViewById(R.id.bookDescription);
        String description = book.getLong_description();
        if (description == null || description.length() == 0) {
            description = book.getShort_description();
        }
        bookDescriptionView.setText(description);

        // Series
        Button seriesButton = (Button) findViewById(R.id.bookSeries);
        boolean isPartOfSeries = book.getSeries() !=null && !book.getSeries().isEmpty();
        if(isPartOfSeries) {
            final Context context = this;;
            seriesButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    SwSeries series = book.getSeries().get(0);
                    Intent intent = new Intent(context, BookListActivity.class);
                    intent.putExtra(BookListActivity.IntentProperty.listType.name(), new SeriesList(series));
                    context.startActivity(intent);
                }
            });
        }
        // TODO: this is not working yet, disable
        // seriesButton.setVisibility(isPartOfSeries ? Button.VISIBLE : Button.GONE);
        seriesButton.setVisibility(Button.GONE);

    }

    public String[] getAvailableReadersAsStringArray(List<Reader> availableReaders) {

        String[] ids = new String[availableReaders.size()];
        for (int i = 0; i < availableReaders.size(); i++) {
            ids[i] = availableReaders.get(i).getName();
        }
        return ids;
    }

    private class DownloadAndAddBookToLibraryTask extends ProgressCallbackAsyncTask<Void, Void> {
        private String resultMessage;
        private File file;
        private Reader reader;
        private Download download;

        public DownloadAndAddBookToLibraryTask(Reader reader, Download download) {
            super(progress);
            this.reader = reader;
            this.download = download;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params) {

            String url = download.getUrl().toString();
            try {
                file = sw.getFile(SmashwordsAPIHelper.getSmashwordsFolder(), url, this);
            } catch (IOException e) {
                resultMessage = "Cannot download book: " + url;
                Log.i(BookActivity.class.getName(), resultMessage);
                return null;
            }
            resultMessage = "Downloaded book: " + book.getTitle();

            CopyToReaderResult endResult = reader.addBookToReader(file, BookActivity.this);
            resultMessage = endResult.toString();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            Toast.makeText(getApplicationContext(), resultMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private class LoadBookTask extends ProgressCallbackAsyncTask<Long, Book> {
        public LoadBookTask(ProgressDialog dialog) {
            super(dialog);
        }

        private String resultMessage = null;

        @Override
        protected Book doInBackground(Long... params) {

            Long bookId = params[0];
            Book result = null;

            try {
                result = SmashwordsAPIHelper.getSmashwords().getBookRetriever().getBookWithDetails(this, bookId);
                if (result != null && !result.isBookOwned()) {
                    bookOfTheDayHelper.storeBookOfTheDay(result);
                }
            } catch (UnknownHostException e) {
                resultMessage = "Please check your Internet connection.";
            } catch (IOException e) {
                resultMessage = e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Book result) {
            book = result;
            displayBook();
            if (progress != null) {
                progress.dismiss();
            }
            if (resultMessage != null)
                showToast(resultMessage);
        }

    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }

    private final class DownloadNewReaderMarker extends Reader {

        public DownloadNewReaderMarker() {
            super("No ebook readers found\n» download one", FileType.values());
        }

        @Override
        public boolean isReaderAvailable(Context context) {
            return true;
        }

        @Override
        public CopyToReaderResult addBookToReader(File file, Context context) {
            // do nothing, we are only a marker
            return null;
        }
    }
}
