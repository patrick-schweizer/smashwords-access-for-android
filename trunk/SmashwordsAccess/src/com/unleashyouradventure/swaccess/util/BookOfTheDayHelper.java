package com.unleashyouradventure.swaccess.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.unleashyouradventure.swaccess.SmashwordsAPIHelper;
import com.unleashyouradventure.swapi.cache.Cache;
import com.unleashyouradventure.swapi.retriever.Book;

public class BookOfTheDayHelper {
    private final static Logger log = Logger.getLogger(BookOfTheDayHelper.class.getName());
    private final static int MAX_NUMBER_OF_BOOKS_IN_DB = 20;
    private Random random = new Random();
    private Context context;
    private List<Book> books = null;

    public BookOfTheDayHelper(Context context) {
        this.context = context;
    }

    public Book getBookOfTheDay() {
        if (this.books == null) {
            loadBooks();
        }
        int index = books.size() < 2 ? 0 : random.nextInt(this.books.size() - 1);
        Book book = books.get(index);
        // Add books to memory cache to speed up BookActivity
        Cache cache = SmashwordsAPIHelper.getSmashwords().getBookRetriever().getCache();
        Book cachedBook = cache.getBook(book.getId());
        if (cachedBook == null) {
            cache.putBook(book);
        }

        return book;
    }

    private void loadBooks() {
        try {
            DatabaseHandler db = new DatabaseHandler(context);
            books = db.getAllBooks();
            db.close();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot read book list", e);
            books = new ArrayList<Book>();
        }
        // fallback:
        if (books.isEmpty()) {
            books.addAll(getDefaultBooks());
        }
    }

    private List<Book> getDefaultBooks() {
        List<Book> books = new ArrayList<Book>();
        Book book = new Book();
        book.setAuthor("Sherrie McCarthy");
        book.setCoverUrl("http://cache.smashwire.com/bookCovers/2c0ef26c7bd3d766766f0a2f438dae991c18ada8");
        book.setPriceInCent(299);
        book.setTitle("Buying and Riding a Motorcycle in South East Asia");
        book.setId(142475);

        book.setAuthor("Patrick Schweizer");
        book.setCoverUrl("http://cache.smashwire.com/bookCovers/03d3d55ec4ab0bd29a36cffda2c129e5d452d4f5");
        book.setPriceInCent(299);
        book.setTitle("Fernweh - mit dem Motorrad um die Welt");
        book.setId(120327);

        book.setAuthor("Sherrie McCarthy");
        book.setCoverUrl("http://cache.smashwire.com/bookCovers/8e6db3a08d77a9717ba4ea7017cc23934d8ca9ef");
        book.setPriceInCent(399);
        book.setTitle("Iceland: A Stormy Motorcycle Adventure");
        book.setId(109660);

        book.setAuthor("Sherrie McCarthy");
        book.setCoverUrl("http://cache.smashwire.com/bookCovers/ea3ebad5e0bea7c7f73d0472c9a3dcc77c54a5f1");
        book.setPriceInCent(99);
        book.setTitle("The Unleash Your Adventure Packlist: What To Take, What To Leave, & The Hows & Whys Of Motorcycle Travel");
        book.setId(90235);

        books.add(book);
        return books;
    }

    public void storeBookOfTheDay(Book book) {
        if (book == null)
            return;
        new StoreBookToDB(context).execute(book);
    }

    private static class StoreBookToDB extends AsyncTask<Book, Void, Void> {

        private Context context;

        public StoreBookToDB(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Book... params) {
            Book book = params[0];
            DatabaseHandler db = new DatabaseHandler(context);
            db.deleteOldestBooks(MAX_NUMBER_OF_BOOKS_IN_DB - 1);
            if (db.exists(book.getId())) {
                db.close();
                return null;
            }
            db.addBook(book);
            db.close();
            return null;
        }
    }

    private static class DatabaseHandler extends SQLiteOpenHelper {

        // All Static variables
        // Database Version
        private static final int DATABASE_VERSION = 1;

        // Database Name
        private static final String DATABASE_NAME = "swAccess";

        // Contacts table name
        private static final String TABLE_BOOKS = "books";

        // columns names
        private static final String KEY_ID = "id";
        private static final String KEY_INSERT_DATE = "insert_date";
        private static final String KEY_BOOK = "book";

        private Gson gson = new Gson();

        public DatabaseHandler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Creating Tables
        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_BOOKS + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
                    + KEY_INSERT_DATE + " INTEGER," + KEY_BOOK + " TEXT" + ")";
            db.execSQL(CREATE_CONTACTS_TABLE);
        }

        // Upgrading database
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
            onCreate(db);
        }

        /**
         * All CRUD(Create, Read, Update, Delete) Operations
         */

        // Adding new contact
        void addBook(Book book) {
            String bookSerialzed = gson.toJson(book);
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_ID, book.getId());
            values.put(KEY_INSERT_DATE, System.currentTimeMillis());
            values.put(KEY_BOOK, bookSerialzed);

            // Inserting Row
            db.insert(TABLE_BOOKS, null, values);
        }

        public Book getBook(long id) {
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.query(TABLE_BOOKS, new String[] { KEY_ID, KEY_BOOK }, KEY_ID + "=?",
                    new String[] { String.valueOf(id) }, null, null, null, null);
            if (cursor != null)
                cursor.moveToFirst();
            String bookJSon = cursor.getString(2);
            Book book = gson.fromJson(bookJSon, Book.class);
            cursor.close();
            return book;
        }

        public boolean exists(long id) {
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.query(TABLE_BOOKS, new String[] { KEY_ID }, KEY_ID + "=?",
                    new String[] { String.valueOf(id) }, null, null, null, null);
            boolean result = cursor.moveToFirst();
            cursor.close();
            return result;
        }

        public List<Book> getAllBooks() {
            List<Book> bookList = new ArrayList<Book>();
            String selectQuery = "SELECT  * FROM " + TABLE_BOOKS;

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    String bookJSon = cursor.getString(2);
                    Book book = gson.fromJson(bookJSon, Book.class);
                    bookList.add(book);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return bookList;
        }

        public List<Long> getInsertDateList() {
            List<Long> dateList = new ArrayList<Long>();
            String selectQuery = "SELECT " + KEY_INSERT_DATE + " FROM " + TABLE_BOOKS + " ORDER BY " + KEY_INSERT_DATE
                    + " DESC";
            Cursor cursor = getWritableDatabase().rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                Long id;
                do {
                    id = cursor.getLong(0);
                    dateList.add(id);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return dateList;
        }

        public void deleteOldestBooks(int maxNumberOfBooksInDb) {

            List<Long> ids = getInsertDateList();
            if (ids.size() < maxNumberOfBooksInDb) {
                return; // nothing to do
            }
            Long lastDate = ids.get(maxNumberOfBooksInDb - 1);
            getWritableDatabase().delete(TABLE_BOOKS, KEY_INSERT_DATE + " < ?", new String[] { lastDate.toString() });
        }

        // public int updateContact(Contact contact) {
        // SQLiteDatabase db = this.getWritableDatabase();
        //
        // ContentValues values = new ContentValues();
        // values.put(KEY_NAME, contact.getName());
        // values.put(KEY_PH_NO, contact.getPhoneNumber());
        //
        // return db.update(TABLE_BOOKS, values, KEY_ID + " = ?", new String[] {
        // String.valueOf(contact.getID()) });
        // }

        public void deleteBook(Book book) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_BOOKS, KEY_ID + " = ?", new String[] { String.valueOf(book.getId()) });
        }

        public int getBookCount() {
            String countQuery = "SELECT  " + KEY_ID + " FROM " + TABLE_BOOKS;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(countQuery, null);
            cursor.close();
            return cursor.getCount();
        }
    }
}
