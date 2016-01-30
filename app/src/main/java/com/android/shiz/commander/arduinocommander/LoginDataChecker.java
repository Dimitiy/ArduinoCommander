package com.android.shiz.commander.arduinocommander;

/**
 * Created by OldMan on 30.01.2016.
 */
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class LoginDataChecker {
    private static boolean loginCheck;
    private static final String LOG_TAG = LoginDataChecker.class.getSimpleName();

    private static String[] credentials;

    public static String[] getCredentials() {
        Log.d("CHecked", "Ch: " + loginCheck);
        if (!loginCheck) {
            return null;
        }

        credentials = readLoginsFromFile();

        return credentials;
    }

    private static String[] readLoginsFromFile() {
        File sdcard = Environment.getExternalStorageDirectory();
        File loginsFile = new File(sdcard, "logins.txt");

        Log.d(LOG_TAG, "file: " + loginsFile.getAbsolutePath());

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(loginsFile));
            String line;
            int i = 0;
            ArrayList<String> list = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
//                credentials[i] = line;
                list.add(line);
                Log.d(LOG_TAG, "str " + i + " - " + line);
                i++;
            }
            credentials = list.toArray(new String[list.size()]);
        } catch (IOException e) {
            Log.d(LOG_TAG, "IOException");

            credentials = new String[0];
        }

        return credentials;
    }

    public static void setLoginCheck(boolean b) {
        loginCheck = b;
    }
}