package com.example.task2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements PaymentResultListener {
    private static final int SMART_CARD_VENDOR_ID = 10381;  // Replace with actual USB Token Vendor ID
    private static final int SMART_CARD_PRODUCT_ID = 64;    // Replace with actual Product ID

    private AlertDialog alertDialog;
    private UsbManager usbManager;
    private boolean isRazorpayActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        Button upiButton = findViewById(R.id.button);
        upiButton.setOnClickListener(view -> {
            if (isSmartCardConnected()) {
                Intent intent = new Intent(MainActivity.this, MainActivity3.class);
                startActivity(intent);
            } else {
                showUSBDisconnectedDialog();
            }
        });

        Button paymentGatewayButton = findViewById(R.id.button_payment_gateway);
        paymentGatewayButton.setOnClickListener(view -> {
            if (isSmartCardConnected()) {
                startPayment();
            } else {
                showUSBDisconnectedDialog();
            }
        });

        startUSBMonitoring();
    }

    // 🔹 1️⃣ Start Razorpay Payment
    private void startPayment() {
        isRazorpayActive = true;
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_aZuZDuCDDrZVvo"); // Replace with your Razorpay Key

        JSONObject options = new JSONObject();
        try {
            options.put("name", "FinTeQ Pay");
            options.put("description", "Test Payment");
            options.put("currency", "INR");
            options.put("amount", "10000"); // 10000 Paise = ₹100
            options.put("prefill.email", "test@example.com");
            options.put("prefill.contact", "9876543210");

            checkout.open(this, options);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // 🔹 2️⃣ Monitor USB Connection Continuously (Before & After Razorpay)
    private void startUSBMonitoring() {
        new Thread(() -> {
            while (true) {
                if (!isSmartCardConnected()) {
                    runOnUiThread(this::showUSBDisconnectedDialog);
                    if (isRazorpayActive) {
                        runOnUiThread(this::finishRazorpaySession);
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    // 🔹 3️⃣ Show USB Disconnection Alert
    private void showUSBDisconnectedDialog() {
        if (alertDialog == null || !alertDialog.isShowing()) {
            alertDialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("TrusToken Disconnected")
                    .setMessage("Please connect the USB token to proceed.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .create();
            alertDialog.show();
        }
    }

    // 🔹 4️⃣ Check if Smart Card USB Token is Connected
    private boolean isSmartCardConnected() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            if (device.getVendorId() == SMART_CARD_VENDOR_ID && device.getProductId() == SMART_CARD_PRODUCT_ID) {
                return true;
            }
        }
        return false;
    }

    // 🔹 5️⃣ Destroy Razorpay Payment Session if USB is Removed
    private void finishRazorpaySession() {
        isRazorpayActive = false;
        onPaymentError(0, "USB Token Disconnected! Payment Session Terminated.");
    }

    // 🔹 6️⃣ Razorpay Payment Callbacks
    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        isRazorpayActive = false;
        Toast.makeText(this, "Payment Successful! ID: " + razorpayPaymentID, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPaymentError(int code, String response) {
        isRazorpayActive = false;
        Toast.makeText(this, "Payment Failed: " + response, Toast.LENGTH_LONG).show();
    }
}
