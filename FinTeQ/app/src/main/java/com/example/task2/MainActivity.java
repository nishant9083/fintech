package com.example.task2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements PaymentResultListener {
    private static final int SMART_CARD_VENDOR_ID = 10381;  // Replace with actual USB Token Vendor ID
    private static final int SMART_CARD_PRODUCT_ID = 64;    // Replace with actual Product ID

    private boolean isRazorpayActive = false;
    private UsbManager usbManager;
    private Handler usbCheckHandler;
//    private ProgressBar progressBar;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        usbCheckHandler = new Handler();
//        progressBar = findViewById(R.id.progressBar);
        fragmentManager = getSupportFragmentManager();

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
//        progressBar.setVisibility(View.VISIBLE);  // Show loader

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

    // 🔹 2️⃣ USB Monitoring using Handler
    private final Runnable usbCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isSmartCardConnected()) {
                runOnUiThread(() -> {
                    if (isRazorpayActive) {
                        finishRazorpaySession();
                    }
                    showUSBDisconnectedDialog();
                });
            }
            usbCheckHandler.postDelayed(this, 500); // Check every 500ms
        }
    };

    private void startUSBMonitoring() {
        usbCheckHandler.post(usbCheckRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        usbCheckHandler.removeCallbacks(usbCheckRunnable);
    }

    // 🔹 3️⃣ Show USB Disconnection Alert **Above** Razorpay
    private void showUSBDisconnectedDialog() {
        if (fragmentManager.findFragmentByTag("USB_DIALOG") == null) {
            USBDisconnectedDialog dialog = new USBDisconnectedDialog();
            dialog.show(fragmentManager, "USB_DIALOG");
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

    // 🔹 5️⃣ Close Razorpay Payment Screen on USB Removal
    private void finishRazorpaySession() {
        isRazorpayActive = false;
//        progressBar.setVisibility(View.GONE);  // Hide loader
        runOnUiThread(() -> onPaymentError(0, "USB Token Disconnected! Payment Terminated."));
    }

    // 🔹 6️⃣ Razorpay Payment Callbacks
    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        isRazorpayActive = false;
//        progressBar.setVisibility(View.GONE);  // Hide loader
        Toast.makeText(this, "Payment Successful! ID: " + razorpayPaymentID, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPaymentError(int code, String response) {
        isRazorpayActive = false;
//        progressBar.setVisibility(View.GONE);  // Hide loader
        Toast.makeText(this, "Payment Failed: " + response, Toast.LENGTH_LONG).show();
    }
}
