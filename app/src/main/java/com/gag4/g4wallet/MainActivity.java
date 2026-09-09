package com.gag4.g4wallet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.gag4.g4wallet.nfc.HceCardService;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "hce_channel";
    private static final int NOTIFICATION_ID = 1;

    // HARDCODATE
    private static final String HARDCODED_AMOUNT = "000000000500"; // 5.00 RON
    private static final String HARDCODED_TRACK2 = "5312570022406247=27102010000000002112?";
    private static final String HARDCODED_KEY = "0123456789ABCDEF0123456789ABCDEF"; // Înlocuiește cu cheia ta reală!

    private TextView tvResult;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvResult = findViewById(R.id.tv_result);

        // Pornim testul automat la lansare
        runOfflineTest();
    }

    private void runOfflineTest() {
        tvResult.setText("Se verifică...");
        tvResult.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // Pornim HCE-ul
        startHceService();

        // Simulăm un test de 5 secunde (timp real de handshake)
        handler.postDelayed(() -> {
            // Rezultatul testului – înlocuiește cu logica reală de verificare AIP
            boolean supportsOffline = checkOfflineSupport();

            if (supportsOffline) {
                tvResult.setText("✅ fonduri insuficiente");
                tvResult.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            } else {
                tvResult.setText("❌ fonduri insuficiente");
                tvResult.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            }

            // Oprim HCE-ul
            stopHceService();
        }, 5000);
    }

    private boolean checkOfflineSupport() {
        // Aici, în loc de simulare, trebuie să verifici dacă POS-ul a trimis GPO cu AIP corect
        // Pentru test, folosim un mock care returnează mereu true
        // În realitate, ar trebui să verifici răspunsurile primite de HCE
        // De exemplu, dacă HceCardService a primit GPO cu P1=0x80, atunci offline este suportat
        return true; // <-- Înlocuiește cu logica reală
    }

    // ---------- HCE Service Control ----------
    private void startHceService() {
        Intent serviceIntent = new Intent(this, HceCardService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        Notification notification = buildNotification();
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, notification);
    }

    private void stopHceService() {
        Intent serviceIntent = new Intent(this, HceCardService.class);
        stopService(serviceIntent);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Offline Tester")
                .setContentText("HCE emulation is running...")
                .setSmallIcon(android.R.drawable.ic_menu_agenda)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "HCE Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopHceService();
    }
}