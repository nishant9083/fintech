package com.example.task2;

import static com.example.task2.MessageDecoder.decodeMessage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketService extends Service {
    private static final String TAG = "WebSocketService";
    private static final String CHANNEL_ID = "WebSocketServiceChannel";

    public static final String ACTION_WEBSOCKET_MESSAGE = "com.example.task2.WEBSOCKET_MESSAGE";
    public static final String ACTION_SEND_MESSAGE = "com.example.task2.SEND_MESSAGE";

    private WebSocketClient webSocketClient;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, getNotification());
        connectWebSocket();
    }

    private void connectWebSocket() {
        try {
            URI uri = new URI("ws://localhost:49152");
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "WebSocket Opened");
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Message received: " + message);
                    try {
                        JSONObject decodedJson = decodeMessage(message);
                        Log.d(TAG, "Decoded Message: " + decodedJson.toString());

                        // Send received message to activities via broadcast
                        Intent intent = new Intent(ACTION_WEBSOCKET_MESSAGE);
                        intent.putExtra("message", decodedJson.toString());
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                    } catch (JSONException e) {
                        Log.e(TAG, "Error decoding message", e);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket Closed: " + reason);
                    reconnectWebSocket();
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket Error: ", ex);
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid WebSocket URI", e);
        } catch (Exception e) {
            Log.e(TAG, "Error in connecting to WebSocket", e);
        }
    }

    private void reconnectWebSocket() {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                connectWebSocket();
            } catch (InterruptedException e) {
                Log.e(TAG, "Reconnect interrupted", e);
            }
        }).start();
    }

    private Notification getNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("WebSocket Service")
                .setContentText("Listening for WebSocket events...")
//                .setSmallIcon(R.drawable.ic_notification)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "WebSocket Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_SEND_MESSAGE.equals(intent.getAction())) {
            String messageToSend = intent.getStringExtra("message");
            if (messageToSend != null && webSocketClient != null && webSocketClient.isOpen()) {
                webSocketClient.send(messageToSend);
                Log.d(TAG, "Sent message: " + messageToSend);
                Toast.makeText(this, "Sent Message", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "WebSocket not connected or message is null");
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
