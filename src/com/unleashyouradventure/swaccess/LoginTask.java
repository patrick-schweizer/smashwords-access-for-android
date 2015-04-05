package com.unleashyouradventure.swaccess;

import android.content.Context;
import android.os.AsyncTask;

import com.unleashyouradventure.swapi.load.LoginHelper;

public class LoginTask extends AsyncTask<Void, Void, String> {

    private LoginHelper login;
    private Context context;

    LoginTask(LoginHelper login, Context context) {
        this.login = login;
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        // TODO Auto-generated method stub
        return null;
    }

}
