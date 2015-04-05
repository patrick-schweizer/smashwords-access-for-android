package com.unleashyouradventure.swaccess.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

public class StringUtils {
    public static boolean equals(String a, String b) {
        if (a == null)
            return b == null;
        else
            return a.equals(b);
    }

    public static String readAssetFileToString(Context context, String fileName) {
        StringBuilder b = new StringBuilder();
        try {
            InputStream input = context.getAssets().open(fileName);

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            while (null != (line = reader.readLine())) {
                b.append(line);
            }
        } catch (IOException e) {
            b.append(e.getMessage());
        }

        return b.toString();
    }
}
