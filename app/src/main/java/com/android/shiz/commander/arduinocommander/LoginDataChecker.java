package com.android.shiz.commander.arduinocommander;

/**
 * Created by OldMan on 30.01.2016.
 */
import android.util.Log;


public class LoginDataChecker {
    private static boolean loginCheck;

    public static String[] getCredentials() {
        Log.d("CHecked", "Ch: " + loginCheck);
        if (!loginCheck) {
            return null;
        }

        String[] credentials = new String[] {
                "root:root"
        };

        return credentials;
    }

    public static void setLoginCheck(boolean b) {
        loginCheck = b;
    }
}