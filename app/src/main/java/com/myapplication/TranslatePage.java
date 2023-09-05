package com.myapplication;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TranslatePage extends AppCompatActivity {
    private BluetoothSocket bluetoothSocket;
    InputStream inputStream;
    public EditText noteEditText;
    StringBuilder scannedDataBuilder = new StringBuilder();
    private List<String> history = new ArrayList<>(); // Declare a history list
    String selectedLanguage;
    public boolean isHistoryClicked = false;
    String scannedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_page);
        noteEditText = findViewById(R.id.translationEditText);
//         Retrieve the BluetoothSocket from the BluetoothSocketManager
        bluetoothSocket = BluetoothSocketManager.getBluetoothSocket();

        Intent intent = getIntent();
        if(intent.hasExtra("isHistoryClicked")){
            Log.d("history",history.toString());
            history();
        }else{
            selectedLanguage = (String)intent.getSerializableExtra("selectedLanguage");
            // The Bluetooth socket is open and connected
            // You can perform Bluetooth communication here
            if(bluetoothSocket != null){
                ReadDataTaskThread readDataTaskThread = new ReadDataTaskThread();
                readDataTaskThread.start();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TranslatePage", "onResume called");

        if (bluetoothSocket != null) {
            ReadDataTaskThread readDataTaskThread = new ReadDataTaskThread();
            readDataTaskThread.start();
        }

        // Process and display Bluetooth data when the activity is resumed
        if (scannedData != null) {
            processBluetoothData(scannedData);
        }

    }
    public void history(){
            if(history.isEmpty()){
                Log.d("history: ", " History is empty");
                noteEditText.setText("歴史はありません"); //  There is no history
            }else{
                StringBuilder historyText = new StringBuilder();
                for (String entry : history) {
                    historyText.append(entry).append("\n");
                }
                Log.d("history: ", "History has entries");
                noteEditText.setText(historyText.toString());
            }
    }
    private void processBluetoothData(String data) {
        // Process the input based on the selected language
        String processedOutput = "";
        if (selectedLanguage.equals("ベトナム語")) {
            processedOutput = getVietnamese(data);
        } else if (selectedLanguage.equals("インドネシア語")) {
            processedOutput = getIndonesian(data);
        }
        Log.d("finalProcessedOutput", processedOutput);
        // Append the processed output to history, for future searches
        history.add(processedOutput);

        // Add a delay before setting text in milliseconds (e.g., 100 milliseconds)
        String finalProcessedOutput = processedOutput;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Update the noteEditText with the processed output
                noteEditText.setText(finalProcessedOutput);
                Log.d("noteEditText", "noteEditText has been updated");
            }
        }, 100);

        // Update the noteEditText with the processed output
        noteEditText.setText(processedOutput);
        Log.d("noteEditText", "noteEditText has been updated");

        // Force the view to redraw
        noteEditText.invalidate();
        noteEditText.requestLayout();
    }

    class ReadDataTaskThread extends Thread {

        @Override
        public void run() {
            try {
                Log.d("trying to read data:", "trying to read data:");
                Log.d("entering reading data:", "entering reading data");
                inputStream = bluetoothSocket.getInputStream();
                Log.d("inputStream:", "inputStream     " + inputStream.toString());
                byte[] buffer = new byte[1024];
                int bytesRead;
                int available = inputStream.available();
                Log.d("available:" ,"" + available);
                while(available == 0){
                    Thread.sleep(100);
                }
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    final String scannedData = new String(buffer, 0, bytesRead); // Define scannedData here
                    Log.d("bytesRead2:", bytesRead + "");
                    if(scannedDataBuilder != null){
                        Log.d("scannedDataBuilder", "scannedDataBuilder: " + scannedDataBuilder.toString());
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            processBluetoothData(scannedData);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static String getVietnamese(String input){
        if(input == null || input.length() == 0){
            return "";
        }
        return input.split("\\$")[0];
    }

    public static String getIndonesian(String input){
        if(input == null || input.length() == 0){
            return "";
        }
        return input.split("\\$")[1];
    }

}
