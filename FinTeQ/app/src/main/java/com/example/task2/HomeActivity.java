package com.example.task2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "OtherActivity";
    private TextView serverStatus;
    private TextView tokenStatus;
    private EditText pin, secondEditText;
    private Spinner firstSpinner, secondSpinner;
    private Button loginButton;
    private String appId;
    private String tokenSerialNumber;
    private ProgressBar progressBar;


    private final ActivityResultLauncher<Intent> requestOverlayPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (Settings.canDrawOverlays(this)) {
                    // Permission granted, proceed with creating the overlay
                    Toast.makeText(this, "Overlay permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    // Permission denied
                    Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show();
                }
            });


    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WebSocketService.ACTION_WEBSOCKET_MESSAGE.equals(intent.getAction())) {
                String receivedMessage = intent.getStringExtra("message");
                try {
                    JSONObject jsonObject = new JSONObject(receivedMessage);
                    String tag = jsonObject.getString("tag");
                    if (tag.equals("81")) {
                        serverStatus.setText("Server Status: Connected");
                        appId = jsonObject.getString("appId");
                        // Handle login message
                    } else if (tag.equals("91")) {
                        tokenStatus.setText("Token Status: Connected");
                        tokenSerialNumber = jsonObject.getString("tokenSerialNumber");
                        // Handle logout message
                        Toast.makeText(context, tokenSerialNumber, Toast.LENGTH_SHORT).show();
                    }
                    else if(tag.equals("98")){
                        int rt = jsonObject.getInt("statusCode");
//                        System.out.println(rt);
//                        Toast.makeText(context, tag, Toast.LENGTH_SHORT).show();
                        if(rt == 0){
                            Intent intent1 = new Intent(HomeActivity.this, MainActivity.class);
                            startActivity(intent1);
                        }
                        if(rt == 12){
                            tokenStatus.setText("Token Status: Disconnected");
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                finally{
                    progressBar.setVisibility(View.GONE); // Hide loader
                    loginButton.setEnabled(true);
                }
                Log.d(TAG, "Message received in activity: " + receivedMessage);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Request the permission
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
            );
            requestOverlayPermissionLauncher.launch(intent);
        } else {
            // Permission already granted or not needed, proceed with creating the overlay
            Toast.makeText(this, "Overlay permission already granted", Toast.LENGTH_SHORT).show();
        }


        // Start WebSocket Service
        Intent serviceIntent = new Intent(this, WebSocketService.class);
        startService(serviceIntent);

        // Register BroadcastReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
                messageReceiver, new IntentFilter(WebSocketService.ACTION_WEBSOCKET_MESSAGE));

        serverStatus = findViewById(R.id.serverStatus   );
        pin = findViewById(R.id.pin);
//        secondEditText = findViewById(R.id.secondEditText);
        firstSpinner = findViewById(R.id.firstSpinner);
        secondSpinner = findViewById(R.id.secondSpinner);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        tokenStatus = findViewById(R.id.tokenStatus);

        // Setting up the first dropdown (RAF, HCF)
        String[] options1 = {"RAF", "HCF"};
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options1);
        firstSpinner.setAdapter(adapter1);

        // Setting up the second dropdown (ECC, RSA)
        String[] options2 = {"ECC", "RSA"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options2);
        secondSpinner.setAdapter(adapter2);

        // Button Click Event
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loginPin = pin.getText().toString();
//                String text2 = secondEditText.getText().toString();
                String algo = firstSpinner.getSelectedItem().toString();
                String fm = secondSpinner.getSelectedItem().toString();
                JSONObject jsonObject = new JSONObject();
                try {
                    if(tokenSerialNumber != null && appId != null){
                        jsonObject.put("tag", "96");
                        jsonObject.put("pin", loginPin);
                        jsonObject.put("ct", algo.equals("RAF") ? 1 : 2);
                        jsonObject.put("AlgoType", fm.equals("ECC") ? 2 : 1);
                        jsonObject.put("appId", appId);
                        jsonObject.put("tokenSerialNumber", tokenSerialNumber);
                        System.out.println(jsonObject);
                        byte[] tlvData = TLVEncoder.encodeToTLV((byte) -106, jsonObject);
                        System.out.println(Arrays.toString(tlvData));
                        sendMessageToWebSocket(Arrays.toString(tlvData));
                        progressBar.setVisibility(View.VISIBLE);
                        loginButton.setEnabled(false);
                    }
                    else{
                        Toast.makeText(HomeActivity.this, "Reconnect Token", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


            }
        });
    }

    private void sendMessageToWebSocket(String message) {
        Intent intent = new Intent(this, WebSocketService.class);
        intent.setAction(WebSocketService.ACTION_SEND_MESSAGE);
        intent.putExtra("message", message);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent serviceIntent = new Intent(this, WebSocketService.class);
        stopService(serviceIntent);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
    }
}