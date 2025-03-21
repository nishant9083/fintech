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

import java.util.HashMap;

public class MainActivity3 extends AppCompatActivity {
    private static final int SMART_CARD_VENDOR_ID = 10381;
    private static final int SMART_CARD_PRODUCT_ID = 64;
    private AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);

        Button y = findViewById(R.id.button7);
        y.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Create an implicit intent
                Intent intent = new Intent(MainActivity3.this,MainActivity.class);
                //intent.setData(Uri.parse(upiUrl));
                startActivity(intent);

            }
        });


        Button y1 = findViewById(R.id.button5);
        y1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity3.this,MainActivity2.class);
                //intent.setData(Uri.parse(upiUrl));
                startActivity(intent);

            }
        });


        Button y2 = findViewById(R.id.button4);
        y2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity3.this,MainActivity2.class);
                //intent.setData(Uri.parse(upiUrl));
                startActivity(intent);

            }
        });

        Button y3 = findViewById(R.id.button6);
        y3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity3.this,MainActivity2.class);
                //intent.setData(Uri.parse(upiUrl));
                startActivity(intent);
            }
        });



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
            alertDialog = new AlertDialog.Builder(MainActivity3.this)
                    .setTitle("TrusToken Disconnected")
                    .setMessage("Please connect the USB to proceed.")
                    .setCancelable(false) // Prevent dialog dismissal
                    .create();
            alertDialog.show();
        }
    }

}