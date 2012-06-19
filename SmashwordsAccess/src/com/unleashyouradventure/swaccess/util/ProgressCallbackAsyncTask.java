package com.unleashyouradventure.swaccess.util;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.unleashyouradventure.swapi.load.PageLoader.ProgressCallback;

public abstract class ProgressCallbackAsyncTask<Params, Result> extends AsyncTask<Params, Integer, Result> implements
        ProgressCallback {

    private ProgressDialog dialog;
    private String currentMessage;

    public ProgressCallbackAsyncTask(ProgressDialog dialog) {
        this.dialog = dialog;

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (dialog != null) {
            dialog.setProgress(values[0]);
            dialog.setMessage(currentMessage);
        }
    }

    @Override
    public void setProgress(int progressInPercent) {
        this.publishProgress(progressInPercent);
    }

    @Override
    public void setCurrentAction(String action) {
        this.currentMessage = action;
    }

}
