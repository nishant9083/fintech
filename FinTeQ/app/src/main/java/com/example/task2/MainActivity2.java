
package com.example.task2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class MainActivity2 extends AppCompatActivity {

    private static final int SMART_CARD_VENDOR_ID = 10381;  // Replace with actual vendor ID for the smart card reader
    private static final int SMART_CARD_PRODUCT_ID = 64;
    private AlertDialog alertDialog;
    private boolean isRedirected = false; // Flag to track if we have already redirected

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Button yd = findViewById(R.id.button3);
        yd.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
            startActivity(intent);
        });

        Button y2 = findViewById(R.id.button2);
        y2.setOnClickListener(view -> {
            // UPI Payment URL
            EditText te1 = findViewById(R.id.te1);
            EditText te2 = findViewById(R.id.te2);
            EditText editTextNumber2 = findViewById(R.id.editTextNumber2);

            // Get the text from the views
            String text1 = te1.getText().toString();
            String text2 = te2.getText().toString();
            String numberText = editTextNumber2.getText().toString();

            String upiUrl = "upi://pay?pa=" + text1 + "&pn=" + text2 + "&am=" + numberText + "&cu=INR";

            // Create an implicit intent for UPI payment
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(upiUrl));
            startActivityForResult(intent, 1);  // Start the UPI payment process
        });

        UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);

        new Thread(() -> {
            while (true) {
                HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
                if (deviceList.isEmpty() || !isSmartCardReader(deviceList)) {
                    runOnUiThread(() -> {
                        showUSBDisconnectedDialog();
                        if (!isRedirected) {
                            redirectToApp();
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        if (alertDialog != null && alertDialog.isShowing()) {
                            alertDialog.dismiss(); // Close dialog when USB is connected
                        }
                    });
                }
                try {
                    Thread.sleep(100); // Check every second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    private boolean isSmartCardReader(HashMap<String, UsbDevice> deviceList) {
        for (UsbDevice device : deviceList.values()) {
            if (device.getVendorId() == SMART_CARD_VENDOR_ID && device.getProductId() == SMART_CARD_PRODUCT_ID) {
                return true;
            }
        }
        return false;
    }

    private void showUSBDisconnectedDialog() {
        if (alertDialog == null || !alertDialog.isShowing()) {
            alertDialog = new AlertDialog.Builder(MainActivity2.this)
                    .setTitle("TrustToken Disconnected")
                    .setMessage("Please connect the USB to proceed.")
                    .setCancelable(false)
                    .create();
            alertDialog.show();
        }
    }

    private void redirectToApp() {
        // If the TrustToken is disconnected, redirect back to the app
        isRedirected = true; // Mark that the app has been redirected
        Intent intent = new Intent(MainActivity2.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If GPay redirection is done, check TrustToken connection status and proceed
        if (requestCode == 1) {
            if (resultCode != RESULT_OK) {
                // Redirect to the app if GPay result is not OK or TrustToken is disconnected
                runOnUiThread(this::redirectToApp);
            }
        }
    }
}
