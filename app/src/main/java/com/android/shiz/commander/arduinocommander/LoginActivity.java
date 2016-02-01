package com.android.shiz.commander.arduinocommander;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;


/**
 * Класс экрана авторизации через логин и пароль.
 */
public class LoginActivity extends AppCompatActivity {

    private final String LOG_TAG = LoginActivity.class.getSimpleName();
    private SharedPreferences prefs;

    /**
     * Переменная класса фоновой задачи.
     */
    private UserLoginTask mAuthTask = null;

    // Объекты графичесого интерфейса.
    private AutoCompleteTextView mLoginView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    /**
     * Метод создания актинвости.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginView = (AutoCompleteTextView) findViewById(R.id.login);
        File sdcard = Environment.getExternalStorageDirectory();
        File loginsFile = new File(sdcard, "logins.txt");
        if(!loginsFile.exists()) {
            try {
                loginsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Инициализация свойств текствью для ввода пароля.
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // Инициализация свойств кнопки для старта функции авторизации.
        Button mLoginSignInButton = (Button) findViewById(R.id.login_sign_in_button);
        mLoginSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        prefs = getSharedPreferences("Easykill", MODE_PRIVATE);
    }


    /**
     * Попытка авторизовать аккаунт по логину и пароля с фоормы.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Сброс ошибок.
        mLoginView.setError(null);
        mPasswordView.setError(null);

        // Считывание логина и пароля с формы.
        String login = mLoginView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Проверка пароля на валидность.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Проверка логина на валиднотсь.
        if (TextUtils.isEmpty(login)) {
            mLoginView.setError(getString(R.string.error_field_required));
            focusView = mLoginView;
            cancel = true;
        } else if (!isLoginValid(login)) {
            mLoginView.setError(getString(R.string.error_invalid_login));
            focusView = mLoginView;
            cancel = true;
        }

        // Проверка на прерывание фоновой задачи.
        if (cancel) {
           // В случае ошибки авторизации фокус переводится на поле, где была ошибка.
            focusView.requestFocus();
        } else {
            // Показ спинера прогресса и запуск фоновой задачи авторизации.
            showProgress(true);
            mAuthTask = new UserLoginTask(login, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Провекра валидности логина.
     * @param email
     * @return
     */
    private boolean isLoginValid(String email) {
        return true;
    }

    /**
     * Проверка валидности пароля.
     * @param password
     * @return
     */
    private boolean isPasswordValid(String password) {
        return password.length() > 0;
    }

    /**
     * Показывает прогресс и срывает активность авторизации пользователя.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // Скрытие спинера прогресса.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Класс асихронной задачи авторизации пользователя.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mLogin;
        private final String mPassword;

        // Конструктор класса фоновой задачи.
        UserLoginTask(String email, String password) {
            mLogin = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String[] credentials = new String[0]; //= LoginDataChecker.getCredentials();

            if (prefs.getBoolean("FirstTime", true) == true) {
                LoginDataChecker.setLoginCheck(true);
                credentials = LoginDataChecker.getCredentials();

            } else {
                Log.d(LOG_TAG, "Too many times!");
                LoginDataChecker.setLoginCheck(false);
            }

            // Условие проверки списка на ненулевое количество элементов.
            if (credentials.length != 0)
                // Проверка введённых учётных данных на совпадение с имеющимися.
                for (String credential : credentials) {
                    String[] pieces = credential.split(":");

                    Log.d(LOG_TAG, pieces[0] + " - " + pieces[1] + " - " + mLogin + " - " + mPassword);

                    // Условие проверки логина.
                    if (pieces[0].equals(mLogin)) {
                        // Account exists, return true if the password matches.
                        Log.d(LOG_TAG, "The first time.");
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("FirstTime", false);
                        editor.commit();
                        return pieces[1].equals(mPassword);
                    }
                }
            return false;
        }

        /**
         * Метод, выполняемы после завершения основной задачи.
         * @param success
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            // В случае успешной проверки логина и пароля запуск главной активности.
            if (success) {
//                finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                mLoginView.setText("");
                mPasswordView.setText("");
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        /**
         * Метод проверки флага прерывания выполнения задачи.
         */
        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

