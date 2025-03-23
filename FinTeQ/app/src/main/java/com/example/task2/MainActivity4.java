package com.example.task2;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;

public class MainActivity4 extends AppCompatActivity {
    private static final int SMART_CARD_VENDOR_ID = 10381;  // Replace with actual vendor ID for the smart card reader
    private static final int SMART_CARD_PRODUCT_ID = 64;
    private static final String FIXED_PASSWORD = "123456";  // Set fixed password here

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main4);

        TextInputEditText passwordField = findViewById(R.id.passwordField);
        Button loginButton = findViewById(R.id.button13);

        loginButton.setOnClickListener(view -> {
            String enteredPassword = passwordField.getText().toString().trim();
            if (FIXED_PASSWORD.equals(enteredPassword)) {
                Toast.makeText(MainActivity4.this, "Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity4.this, MainActivity.class)); // Go to MainActivity
            } else {
                Toast.makeText(MainActivity4.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
            }
        });

        // USB Detection
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        new Thread(() -> {
            while (true) {
                HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
                if (deviceList.isEmpty()) {
                    runOnUiThread(this::showUSBDisconnectedDialog);
                } else {
                    UsbDevice device = deviceList.values().iterator().next();
                    if (!isSmartCardReader(device)) {
                        runOnUiThread(this::showUSBDisconnectedDialog);
                    } else {
                        runOnUiThread(() -> {
                            if (alertDialog != null && alertDialog.isShowing()) {
                                alertDialog.dismiss(); // Close dialog when USB is connected
                            }
                        });
                    }
                }
                try {
                    Thread.sleep(100); // Check every second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();

        // Adjust UI for system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean isSmartCardReader(UsbDevice device) {
        return device.getVendorId() == SMART_CARD_VENDOR_ID && device.getProductId() == SMART_CARD_PRODUCT_ID;
    }

    private void showUSBDisconnectedDialog() {
        if (alertDialog == null || !alertDialog.isShowing()) {
            alertDialog = new AlertDialog.Builder(MainActivity4.this)
                    .setTitle("TrusToken Disconnected")
                    .setMessage("Please connect the USB to proceed.")
                    .setCancelable(false) // Prevent dialog dismissal
                    .setPositiveButton("OK", (dialog, which) -> {finishAffinity(); finish();})
                    .create();
            alertDialog.show();
        }
    }
}
