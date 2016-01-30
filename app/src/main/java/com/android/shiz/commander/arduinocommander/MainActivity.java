package com.android.shiz.commander.arduinocommander;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
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

    /*
        * Notifications from UsbService will be received here.
        */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private UsbService usbService;

    private MyHandler mHandler;
    private TextView led1TextView, led2TextView, led3TextView;
    private Switch led1Switch, led2Switch;
    private Button sendButton;

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new MyHandler(this);

        led1Switch = (Switch) findViewById(R.id.switch1);
        led2Switch = (Switch) findViewById(R.id.switch2);
        led1TextView = (TextView) findViewById(R.id.textView);
        led2TextView = (TextView) findViewById(R.id.textView2);
        led3TextView = (TextView) findViewById(R.id.textView4);
        led1Switch.setOnCheckedChangeListener(this);
        led2Switch.setOnCheckedChangeListener(this);

        setUiEnabled(false);

    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
//        setUiEnabled(true);

    }

    @Override
    public void onPause() {
        super.onPause();
//        setUiEnabled(false);

//        unregisterReceiver(mUsbReceiver);
//        unbindService(usbConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    /**
     * Set switches enabled, when connection is opened.
     *
     * @param bool
     */
    public void setUiEnabled(boolean bool) {
        led1Switch.setEnabled(bool);
        led2Switch.setEnabled(bool);
    }

    /**
     * 1ON - turn LED #1
     * 1OFF - turn off LED #1
     * 2ON - turn LED #2
     * 2OFF - turn off LED #2
     *
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String sendStr = null;
        switch (buttonView.getId()) {
            case R.id.switch1:
                if (isChecked) {
                    led1TextView.setText("ON"); // test
                    led3TextView.setText("wait"); // test
                    sendDataToArduino("1");
                    Toast.makeText(MainActivity.this, "send 1", Toast.LENGTH_SHORT).show();
                } else {
                    led1TextView.setText("OFF"); // test
                    led3TextView.setText("wait"); // test
                    sendDataToArduino("4");
                }
                break;
            case R.id.switch2:
                if (isChecked) {
                    led2TextView.setText("ON"); // test
                    led3TextView.setText("wait"); // test
                    sendDataToArduino("2");
                    Toast.makeText(MainActivity.this, "send 2", Toast.LENGTH_SHORT).show();

                } else {
                    led2TextView.setText("OFF"); // test
                    led3TextView.setText("wait"); // test
                    sendDataToArduino("5");
                }
                break;
            default:
                break;
        }
    }

    private void sendDataToArduino(String str) {
        if (usbService != null) { // if UsbService was correctly binded, Send data
            usbService.write(str.getBytes());
        }
    }

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

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
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
                            mActivity.get().led3TextView.setText("Ok!");
                            break;
                        case "6":
                            mActivity.get().led3TextView.setText("Not Ok!");
                            break;
                    }
                    break;
                case UsbService.MESSAGE_ACTION_USB_READY:
                    String ready = (String) msg.obj;
                    switch (ready) {
                        case "1":
                            mActivity.get().setUiEnabled(true);

                            break;
                        case "0":
                            mActivity.get().setUiEnabled(false);
                            break;

                    }
            }
        }
    }}