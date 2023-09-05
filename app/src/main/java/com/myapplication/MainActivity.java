package com.myapplication;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.rokid.glass.instruct.InstructLifeManager;
import com.rokid.glass.instruct.entity.EntityKey;
import com.rokid.glass.instruct.entity.IInstructReceiver;
import com.rokid.glass.instruct.entity.InstructEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    //        private InstructLifeManager mLifeManager;
    private static final int REQUEST_PERMISSION_CODE = 123;
    public BluetoothDevice device;
    public BluetoothSocket bluetoothSocket;
    public Button translateButton, connectButton, historyButton, disConnectButton;
    public Spinner outputLanguageSpinner;
    public ImageView bluetoothStatus;
    public boolean isHistoryClicked;
    String selectedLanguage;
    InputStream inputStream;
    BluetoothAdapter bluetoothAdapter;
    UUID uuid;
    BluetoothServerSocket mmServerSocket;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        translateButton = findViewById(R.id.translateButton);
        connectButton = findViewById(R.id.connectButton);
//        historyButton = findViewById(R.id.historyButton);
        disConnectButton = findViewById(R.id.disConnectButton);

        // Set up the OnClickListener for connectButton
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bluetoothSocket == null || !bluetoothSocket.isConnected()){
                    Log.d("bluetoothSocket","bluetoothSocket is null" );
                    Log.d("isConnected: ", "The bluetoothSocket is not Connected");
                    connectCellphone();
                    if(bluetoothSocket != null){
                        Log.d("isConnected: ", "The bluetoothSocket is Connected successfully");
                    }
                }
                else{
                    Log.d("isConnected: ", "The bluetoothSocket has already been Connected");
                    Log.d("bluetoothSocket",bluetoothSocket.toString());
                }
            }
        });
        disConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    closeSocket();
//                    bluetoothSocket = null;
                    showToast("DisConnected");
                    Log.d("BluetoothSocket",bluetoothSocket.toString());
                    updateBluetoothStatus(false);
                } catch (IOException e) {
                    e.getMessage();
                }
            }
        });

        // Set up the OnClickListener for translateButton
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
                    // The Bluetooth socket is closed or not connected
                    // You might want to handle this case accordingly
                    Toast.makeText(MainActivity.this,"Please connect to sender",Toast.LENGTH_LONG).show();
                }else{
                    Log.d("start translate","start translate, go to second page");
                    goToTranslatePage("translate");
                }
            }
        });

//        // Set up the OnClickListener for historyButton
//        historyButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d("start translate","start translate, go to second page");
//                goToTranslatePage("history");
//            }
//        });

        // Initialize UI components
        outputLanguageSpinner = findViewById(R.id.outputLanguageSpinner);
        bluetoothStatus = findViewById(R.id.bluetoothStatusLight);

        // Add Indonesian and Vietnamese options to the output spinner： ベトナム語： Vietnamese， インドネシア語：Indonesian
        String[] outputLanguages = {"ベトナム語" , "インドネシア語"}; //"Vietnamese" , "Indonesian"
        ArrayAdapter<String> outputAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, outputLanguages);
        outputLanguageSpinner.setAdapter(outputAdapter);

        // Set up the OnItemSelectedListener for outputLanguageSpinner
        outputLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item
                selectedLanguage = (String) parent.getItemAtPosition(position);
                // Do something with the selected language
                Toast.makeText(getApplicationContext(), "Selected output language: " + selectedLanguage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected (if needed)
            }
        });
        configInstruct();
    }

    private void goToTranslatePage(String action) {
        if(action.equals("translate")){
            if (bluetoothSocket != null) {
                BluetoothSocketManager.setBluetoothSocket(bluetoothSocket);
            }
            Intent intent = new Intent(MainActivity.this, TranslatePage.class);
            intent.putExtra("selectedLanguage",selectedLanguage);
            startActivity(intent);
        }
        if(action.equals("history")){
            isHistoryClicked = true;
            if (bluetoothSocket != null) {
                BluetoothSocketManager.setBluetoothSocket(bluetoothSocket);
            }
            Intent intent = new Intent(MainActivity.this, TranslatePage.class);
            intent.putExtra("isHistoryClicked",isHistoryClicked);
            startActivity(intent);
        }
    }
    public void configInstruct() {
        InstructLifeManager mLifeManager = new InstructLifeManager(this, getLifecycle(), mInstructLifeListener);
        mLifeManager.addInstructEntity(
                new InstructEntity()
                        .addEntityKey(R.id.connectButton)
                        .addEntityKey(new EntityKey(EntityKey.Language.ja, "セツゾク"))  // pronunciation: Setsuzoku
                        .setShowTips(true)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                Log.d(TAG, "链接 触发");
                                connectButton.performClick();
                            }
                        })
        );
        mLifeManager.addInstructEntity(
                new InstructEntity()
                        .addEntityKey(R.id.translateButton)
                        .addEntityKey(new EntityKey(EntityKey.Language.ja, "ホンヤク")) // pronunciation: Honyaku
                        .setShowTips(true)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                Log.d(TAG, "翻译 触发");
                                translateButton.performClick();
                            }
                        })
        );
        mLifeManager.addInstructEntity(
                new InstructEntity()
                        .addEntityKey(R.id.disConnectButton)
                        .addEntityKey(new EntityKey(EntityKey.Language.ja, "セツダン")) // pronunciation: Setsudan
                        .setShowTips(true)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                Log.d(TAG, "断开连接 触发");
                                disConnectButton.performClick();
                            }
                        })
        );
//        mLifeManager.addInstructEntity(
//                new InstructEntity()
//                        .addEntityKey(R.id.historyButton)
//                        .addEntityKey(new EntityKey(EntityKey.Language.ja, "りれき")) // pronunciation: Rireki
//                        .setShowTips(true)
//                        .setCallback(new IInstructReceiver() {
//                            @Override
//                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
//                                Log.d(TAG, "历史 触发");
//                                historyButton.performClick();
//                            }
//                        })
//        );
        mLifeManager.addInstructEntity(
                new InstructEntity()
                        .addEntityKey(R.id.outputLanguageSpinner)
                        .addEntityKey(new EntityKey(EntityKey.Language.ja, "しゅつりょくげんご")) // pronunciation: Shutsuryoku Gengo
                        .setShowTips(true)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                Log.d(TAG, "output language 触发");
                                outputLanguageSpinner.performClick();
                            }
                        })
        );
    }
    private InstructLifeManager.IInstructLifeListener mInstructLifeListener = new InstructLifeManager.IInstructLifeListener() {
        @Override
        public boolean onInterceptCommand(String command) {
            if ("需要拦截的指令".equals(command)) {
                return true;
            }
            return false;
        }
        @Override
        public void onTipsUiReady() {
            Log.d("AudioAi", "onTipsUiReady Call ");
        }

        @Override
        public void onHelpLayerShow(boolean show) {

        }
    };
    private void showToast(String msg) {
        Log.d("toast","showToast");
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
    private void connectCellphone() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Your existing code to get the Bluetooth adapter and check permissions...
        checkConnectPermission();

        // Get a list of paired devices and try to connect to one of them
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        String deviceName;
        String deviceMacAddress = null;
        for (BluetoothDevice device : bondedDevices) {
            deviceName = device.getName();
            Log.d("deviceName", deviceName);
            deviceMacAddress = device.getAddress();
            Log.d("deviceMacAddress", deviceMacAddress);
        }
        if (bondedDevices.size() > 0) {
            device = bondedDevices.iterator().next();
            Log.d("device", device.toString());
        }

        // Start a separate thread to connect and handle the timeout
        Thread bluetoothServerThread = new BluetoothServerThread();

        // Start connectThread
        try {
            // Start the connection thread
            bluetoothServerThread.start();
        } catch (Exception e) {
            Log.e("error", "Error creating socket: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public class BluetoothServerThread extends Thread {
        private static final String TAG = "BluetoothServerThread";
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        private BluetoothAdapter mBluetoothAdapter;

        public BluetoothServerThread() {
            BluetoothServerSocket tmp = null;
            try {
                checkConnectPermission();
                // Create a BluetoothServerSocket using the UUID
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("MyApp", MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {

            // Keep listening until an exception occurs or a socket is returned
            while (true) {
                try {
                    Log.d(TAG, "Listening for incoming connections...");
                    bluetoothSocket = mmServerSocket.accept();
                    BluetoothSocketManager.setBluetoothSocket(bluetoothSocket);
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (bluetoothSocket != null) {
                    // A connection was accepted

                    updateBluetoothStatus(true);
                    Log.d("bluetoothSocket","connect finished:" + bluetoothSocket.toString());

                    Log.d(TAG, "Socket connected.");
                    // You can now handle the connected socket in a separate thread
                    // For example, create a new thread to handle the communication
                    // BluetoothCommunicationThread communicationThread = new BluetoothCommunicationThread(socket);
                    // communicationThread.start();
                    break; // Optionally, you can stop listening for more connections
                }
            }
        }

        // Call this method from your activity to stop listening for connections
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket's close() method failed", e);
            }
        }
    }

    private void updateBluetoothStatus(boolean isConnected) {
        if (isConnected) {
            bluetoothStatus.setImageResource(R.drawable.button_green);
        } else {
            bluetoothStatus.setImageResource(R.drawable.button_red);
        }
    }
    private void checkConnectPermission () {
        // 检查是否已经有了权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // 如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_CONNECT)) {
                // 在这里向用户解释为什么你需要这个权限，并再次请求
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_CODE);

            } else {
                // 直接请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_CODE);
            }
        } else {
            // 已经有权限，你可以在这里执行后续的操作
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        try {
//            closeSocket(); // Close the Bluetooth socket
//            Log.d("onDestroy","translator has been onDestroy");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    @SuppressLint("SuspiciousIndentation")
    public void closeSocket() throws IOException {

        if (inputStream != null) {
            inputStream.close();
            Log.e("stream close", "stream close");
        }

//        if (bluetoothSocket != null || bluetoothSocket.isConnected()) {
//            bluetoothSocket.close();
            bluetoothSocket = null;
            mmServerSocket = null;
        Log.e("socket close", "socket close");
        updateBluetoothStatus(false);
//        }
    }

}