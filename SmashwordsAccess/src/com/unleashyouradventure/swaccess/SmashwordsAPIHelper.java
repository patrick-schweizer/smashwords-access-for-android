package com.unleashyouradventure.swaccess;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.LoaderSettings;
import com.novoda.imageloader.core.LoaderSettings.SettingsBuilder;
import com.novoda.imageloader.core.cache.LruBitmapCache;
import com.unleashyouradventure.swapi.Smashwords;
import com.unleashyouradventure.swapi.cache.Cache;
import com.unleashyouradventure.swapi.cache.InMemoryCache;
import com.unleashyouradventure.swapi.load.LoginHelper;
import com.unleashyouradventure.swapi.load.PageLoader;
import com.unleashyouradventure.swapi.retriever.BookListRetriever.AdultContent;

public class SmashwordsAPIHelper {

    static final Logger LOG = Logger.getLogger(SmashwordsAPIHelper.class.getName());

    // Singletons
    private static Smashwords sw = createSmashwords();
    private static ImageManager imageManager;

    private static Smashwords createSmashwords() {
        Smashwords newSW = new Smashwords(null, null, new PageLoader());
        Cache cache = new InMemoryCache();
        newSW.setCache(cache);
        return newSW;
    }

    private static void createImageLoader(Context context) {

        LoaderSettings settings = new SettingsBuilder().withDisconnectOnEveryCall(true).build(context);
        imageManager = new ImageManager(context, settings);

        settings = new SettingsBuilder().withDisconnectOnEveryCall(true).withAsyncTasks(false).withCacheManager(new LruBitmapCache(context)).build(context);

    }

    public static ImageManager getImageLoader() {
        return imageManager;
    }

    public static void init(SmashwordsAccessApplication applicationContext) {
        reconfigurePreferences(applicationContext);
        createImageLoader(applicationContext);
    }

    public static void reconfigurePreferences(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        configureLogin(context, sharedPrefs);
        AdultContent adultContent = getAdultContent(sharedPrefs);
        sw.getLoader().setAdultContent(adultContent);
    }

    private static void configureLogin(Context context, SharedPreferences sharedPrefs) {
        LoginHelper login = sw.getLogin();
        String email = sharedPrefs.getString(PreferencesActivity.name.sw_email.toString(), null);
        String password = sharedPrefs.getString(PreferencesActivity.name.sw_password.toString(), null);
        boolean needsLogout = login.isLoggedIn();
        boolean changed = login.configure(email, password);
        boolean valid = login.areCredentialsWellFormed();
        if (changed && valid) {
            new LoginTask(context, login, needsLogout).execute();
        }
    }

    private static AdultContent getAdultContent(SharedPreferences sharedPrefs) {
        String swAdultFilterString = sharedPrefs.getString(PreferencesActivity.name.sw_adultfilter.name(), AdultContent.swdefault.name());
        for (AdultContent filter : AdultContent.values()) {
            if (filter.name().equals(swAdultFilterString)) {
                return filter;
            }
        }
        return AdultContent.swdefault;
    }

    public static Smashwords getSmashwords() {
        return sw;
    }

    public static File getSmashwordsFolder() {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File file = new File(sdCardRoot, "smashwords");
        if (!file.exists()) {
            boolean successful = file.mkdir();
            if (!successful) {
                LOG.log(Level.WARNING, "Cannot create directory " + file.getAbsolutePath());
            }
        }
        return file;
    }

    private final static class LoginTask extends AsyncTask<Void, Void, String> {
        private final Context context;
        private final LoginHelper login;
        private final boolean needsLogout;

        public LoginTask(Context context, LoginHelper login, boolean needsLogout) {
            this.context = context;
            this.login = login;
            this.needsLogout = needsLogout;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result;
            try {
                if (needsLogout) {
                    login.logOut();
                }
                boolean loggedIn = login.logIn();
                if (loggedIn) {
                    result = (String) context.getResources().getText(R.string.SmashwordsAPIHelper_Result_success);
                } else {
                    result = (String) context.getResources().getText(R.string.SmashwordsAPIHelper_Result_failed);
                }
            } catch (IOException e) {
                result = (String) context.getResources().getText(R.string.SmashwordsAPIHelper_Result_IOException);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            showToast(context, result);
        }
    }

    private static void showToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }
}
