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

/**
 * Класс проверки логина и пароля на корректность.
 */
public class LoginDataChecker {
    private static boolean loginCheck; // Статус проверки.
    private static final String LOG_TAG = LoginDataChecker.class.getSimpleName();

    private static String[] credentials; // Строки "логин:пароль" из файла.

    public static String[] getCredentials() {
        Log.d("CHecked", "Ch: " + loginCheck);
        if (!loginCheck) {
            return null;
        }

        credentials = readLoginsFromFile(); // Чтение данных авторизации из файла.

        return credentials;
    }

    // Метод чтения строк с файла.
    private static String[] readLoginsFromFile() {
        File sdcard = Environment.getExternalStorageDirectory(); // Ссылка на внешнее хранилище.
        File loginsFile = new File(sdcard, "logins.txt"); // Ссылка на файл с авторизационными данными.

        Log.d(LOG_TAG, "file: " + loginsFile.getAbsolutePath());

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(loginsFile));
            String line;
            int i = 0;
            ArrayList<String> list = new ArrayList<>();
            // Построчное считывания с файла.
            while ((line = bufferedReader.readLine()) != null) {
                list.add(line);
                Log.d(LOG_TAG, "str " + i + " - " + line);
                i++;
            }
            credentials = list.toArray(new String[list.size()]); // Преобразование списка строк в массив.
        } catch (IOException e) {
            Log.d(LOG_TAG, "IOException");

            credentials = new String[0]; // В случае ошибок возвращаем пустой массив строк.
        }

        return credentials;
    }

    public static void setLoginCheck(boolean b) {
        loginCheck = b;
    }
}