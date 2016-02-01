package com.android.shiz.commander.arduinocommander;

        import android.content.BroadcastReceiver;
        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.content.ServiceConnection;
        import android.content.pm.ActivityInfo;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.IBinder;
        import android.os.Message;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.widget.Button;
        import android.widget.CompoundButton;
        import android.widget.Switch;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.lang.ref.WeakReference;
        import java.util.Set;

/**
 * Created by OldMan on 30.01.2016.
 */
public class MainActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener {
    static boolean stateUSB; // Состояние USB.
    private UsbService usbService;

    private MyHandler mHandler;
    private TextView led1TextView, led2TextView, textViewButton;
    private Switch led1Switch, led2Switch;
    private Button sendButton;
    private boolean isUsbReceiver = false;
    /*
    * Здесь принимаются сообщения от сервиса USB.
    */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // Разрешение USB предоставлено.
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // Разрешение USB не представлено.
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // USB не соединено.
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB соединено.
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB не подддреживается.
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    // Инициализация объекта класса USB-сервиса.
    private final ServiceConnection usbConnection = new ServiceConnection() {
        // Событие соединения с сервисом.
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        // Событие разединения с сервисом.
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    // Метод создания объекта класса активности.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean tablet = getResources().getBoolean(R.bool.tablet);
        // Проверка типа устройства (планшет - смартофон).
        if (!tablet)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        mHandler = new MyHandler(this);

        // Объекты класса графического интерфейса.
        led1Switch = (Switch) findViewById(R.id.switch1);
        led2Switch = (Switch) findViewById(R.id.switch2);
        led1TextView = (TextView) findViewById(R.id.textView);
        led2TextView = (TextView) findViewById(R.id.textView2);
        textViewButton = (TextView) findViewById(R.id.textView4);
        led1Switch.setOnCheckedChangeListener(this);
        led2Switch.setOnCheckedChangeListener(this);

        Log.d(MainActivity.this.toString(), "NOsavedInstanceState");
        setUiEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
            setFilters();  // Старт прослушки сообщений от сервиса.
            startService(UsbService.class, usbConnection, null); // Запуск сервиса.
            isUsbReceiver = true;
       if (stateUSB == true)
            setUiEnabled(true);
    }

    // Прерывание процесса выполнения.
    @Override
    public void onPause() {
        super.onPause();
        setUiEnabled(false);
    }

    // Завершения выполнения активности
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterUSB();
    }

    /**
     * Прекращение подписки на сообщения от ресивера и сервиса.
     */
    private void unregisterUSB() {
            unregisterReceiver(mUsbReceiver);
            unbindService(usbConnection);
            isUsbReceiver = false;
    }

    /**
     * Установка переключателей доступными при установлении соединения.
     *
     * @param bool
     */
    public void setUiEnabled(boolean bool) {
        led1Switch.setEnabled(bool);
        led2Switch.setEnabled(bool);
    }

    /**
     * Событие переключения кнопок.
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String sendStr = null;
        switch (buttonView.getId()) {
            case R.id.switch1:
                if (isChecked) {
                    led1TextView.setText("ON"); // LED 1 включён.
                    sendDataToArduino("1");
                } else {
                    led1TextView.setText("OFF"); // LED 1 выключен.
                    sendDataToArduino("4");
                }
                break;
            case R.id.switch2:
                if (isChecked) {
                    led2TextView.setText("ON"); // LED 2 включён.
                    sendDataToArduino("2");
                } else {
                    led2TextView.setText("OFF"); // LED 2 выключён.
                    sendDataToArduino("5");
                }
                break;
            default:
                break;
        }
    }

    // Метод отправки в Ардуино.
    private void sendDataToArduino(String str) {
        if (usbService != null) { // При установлении связи с USB-сервисом отправка данных.
            usbService.write(str.getBytes());
        }
    }

    // Старт севриса.
    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    // Установки фильтров для получаемых событий.
    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /**
    * Данные, получаемы через последовательный порт, отображются через этот порт.
    */
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity; // Ссылка на активити для связи.

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        // Метод обработки получаемых сообщений.
        @Override
        public void handleMessage(Message msg) {
           switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = ((String) msg.obj).replaceAll("\\D+", "");
                    if(data.length() > 0) {
                        // Установка статусов для LEDs.
                         switch (data) {
                            case "1":
                                mActivity.get().led1TextView.setText("Ok!");
                                break;
                            case "4":
                                mActivity.get().led1TextView.setText("Not Ok!");
                                break;
                            case "2":
                                mActivity.get().led2TextView.setText("Ok!");
                                break;
                            case "5":
                                mActivity.get().led2TextView.setText("Not Ok!");
                                 break;
                            case "3":
                                mActivity.get().textViewButton.setText("Ok!");
                                break;
                            case "6":
                                mActivity.get().textViewButton.setText("Not Ok!");
                                break;
                        }
                    }
                        break;

                case UsbService.MESSAGE_ACTION_USB_READY:
                    String ready = (String) msg.obj;
                    // Установка статуса USB.
                    switch (ready) {
                        case "1":
                            mActivity.get().setUiEnabled(true);
                            stateUSB = true;
                            break;
                        case "0":
                            mActivity.get().setUiEnabled(false);
                            stateUSB = false;
                            break;

                    }
            }
        }
    }
}