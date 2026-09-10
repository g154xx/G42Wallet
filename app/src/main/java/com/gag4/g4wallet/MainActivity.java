package com.gag4.g4wallet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.gag4.g4wallet.nfc.HceCardService;
import com.gag4.g4wallet.utils.ResultCallback;

public class MainActivity extends AppCompatActivity implements ResultCallback {

    private static final String CHANNEL_ID = "hce_channel";
    private static final int NOTIFICATION_ID = 1;

    private TextView tvResult;
    private Button btnStart, btnStop;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvResult = findViewById(R.id.tv_result);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);

        // Verificăm dacă NFC este activat
        if (!isNfcEnabled()) {
            Toast.makeText(this, "NFC este dezactivat. Activează-l din setări.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }

        // Verificăm dacă aplicația este setată ca default payment
        if (!isDefaultPaymentApp()) {
            Toast.makeText(this, "Setează această aplicație ca plată contactless implicită.", Toast.LENGTH_LONG).show();
            openDefaultPaymentSettings();
        }

        btnStart.setOnClickListener(v -> {
            startHceService();
            btnStart.setVisibility(android.view.View.GONE);
            btnStop.setVisibility(android.view.View.VISIBLE);
            tvResult.setText("Se verifică...");
            tvResult.setTextColor(getResources().getColor(android.R.color.darker_gray));
        });

        btnStop.setOnClickListener(v -> {
            stopHceService();
            btnStart.setVisibility(android.view.View.VISIBLE);
            btnStop.setVisibility(android.view.View.GONE);
            tvResult.setText("Apropie telefonul de POS");
            tvResult.setTextColor(getResources().getColor(android.R.color.darker_gray));
        });

        createNotificationChannel();
    }

    private boolean isNfcEnabled() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        return adapter != null && adapter.isEnabled();
    }

    private boolean isDefaultPaymentApp() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return true;
        }
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter == null) return false;
        CardEmulation cardEmulation = CardEmulation.getInstance(adapter);
        if (cardEmulation == null) return false;
        // Verificăm dacă aplicația este selectată ca default pentru HCE
        // Pentru simplitate, returnăm mereu true și lăsăm utilizatorul să verifice manual
        return true;
    }

    private void openDefaultPaymentSettings() {
        Intent intent = new Intent(Settings.ACTION_NFC_PAYMENT_SETTINGS);
        startActivity(intent);
    }

    // ---------- HCE Service Control ----------
    private void startHceService() {
        HceCardService.setCallback(this); // setăm callback static (fără putExtra)

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
        HceCardService.setCallback(null);
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

    // ---------- Callback pentru HCE ----------
    @Override
    public void onOfflineDetected(boolean supportsOffline) {
        runOnUiThread(() -> {
            if (supportsOffline) {
                tvResult.setText("✅ fonduri insuficiente");
                tvResult.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            } else {
                tvResult.setText("❌ fonduri insuficiente");
                tvResult.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            }
            // Oprim HCE-ul automat după rezultat
            stopHceService();
            btnStart.setVisibility(android.view.View.VISIBLE);
            btnStop.setVisibility(android.view.View.GONE);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopHceService();
        HceCardService.setCallback(null);
    }
}