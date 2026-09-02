package com.gag4.g4wallet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import com.google.android.material.navigation.NavigationView;

import com.gag4.g4wallet.engine.OfflineForce;
import com.gag4.g4wallet.engine.OfflineTester;
import com.gag4.g4wallet.nfc.HceCardService;
import com.gag4.g4wallet.utils.ConfigManager;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "hce_channel";
    private static final int NOTIFICATION_ID = 1;

    private TextView tvStatus;
    private TextView tvLog;
    private Button btnStartHce;
    private OfflineTester offlineTester;
    private OfflineForce offlineForce;
    private boolean isHceRunning = false;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_diagnostic) {
                runDiagnostic();
            } else if (id == R.id.nav_card_info) {
                showCardInfo();
            } else if (id == R.id.nav_load_keys) {
                loadKeys();
            } else if (id == R.id.nav_generate_track) {
                generateTrack2();
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.nav_load_config) {
                loadConfig();
            } else if (id == R.id.nav_network) {
                Toast.makeText(this, "Network info placeholder", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_about) {
                Toast.makeText(this, "G4² Wallet v2.0\nEMV Offline Tester", Toast.LENGTH_LONG).show();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        tvStatus = findViewById(R.id.tv_status);
        tvLog = findViewById(R.id.tv_log);
        btnStartHce = findViewById(R.id.btn_start_hce);

        offlineTester = new OfflineTester(this::appendLog, this::updateStatus);
        offlineForce = new OfflineForce(this::appendLog, this::updateStatus);

        btnStartHce.setOnClickListener(v -> {
            if (isHceRunning) {
                stopHceService();
            } else {
                startHceService();
            }
        });

        createNotificationChannel();
    }

    // ---------- Menu Actions ----------
    private void runDiagnostic() {
        appendLog(">>> Starting POS Diagnostic...");
        updateStatus("Scanning...");
        startHceService();
        new Thread(() -> {
            String result = offlineTester.runCombinedDiagnostic();
            runOnUiThread(() -> {
                appendLog(">>> Result: " + result);
                updateStatus("Done: " + result);
            });
        }).start();
    }

    private void showCardInfo() {
        Toast.makeText(this, "Card Info: " + ConfigManager.getCardInfo(this), Toast.LENGTH_LONG).show();
    }

    private void loadKeys() {
        SharedPreferences prefs = getSharedPreferences("G4WalletPrefs", MODE_PRIVATE);
        String keyHex = prefs.getString("emv_key_hex", "0123456789ABCDEF0123456789ABCDEF");
        offlineForce.loadKeys(keyHex);
        appendLog(">>> Keys loaded from settings.");
        Toast.makeText(this, "Keys loaded", Toast.LENGTH_SHORT).show();
    }

    private void generateTrack2() {
        SharedPreferences prefs = getSharedPreferences("G4WalletPrefs", MODE_PRIVATE);
        String pan = prefs.getString("pan", "1234567890123456");
        String expiry = prefs.getString("expiry", "2612");
        String serviceCode = prefs.getString("service_code", "101");
        String track2 = offlineForce.generateTrack2(pan, expiry, serviceCode);
        appendLog(">>> Track2 generated: " + track2);
        Toast.makeText(this, "Track2: " + track2, Toast.LENGTH_LONG).show();
    }

    private void loadConfig() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        startActivityForResult(Intent.createChooser(intent, "Select config file"), 1001);
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

        isHceRunning = true;
        btnStartHce.setText("Stop HCE");
        btnStartHce.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_dark));
        updateStatus("HCE ACTIVE - Apropie telefonul de POS");
        appendLog(">>> HCE service started.");
        Toast.makeText(this, "HCE emulation started", Toast.LENGTH_SHORT).show();
    }

    private void stopHceService() {
        Intent serviceIntent = new Intent(this, HceCardService.class);
        stopService(serviceIntent);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);

        isHceRunning = false;
        btnStartHce.setText("Start HCE");
        btnStartHce.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
        updateStatus("HCE stopped");
        appendLog(">>> HCE service stopped.");
        Toast.makeText(this, "HCE emulation stopped", Toast.LENGTH_SHORT).show();
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("G4² Wallet")
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

    // ---------- UI Helpers ----------
    private void updateStatus(String msg) {
        runOnUiThread(() -> tvStatus.setText("Status: " + msg));
    }

    private void appendLog(String msg) {
        runOnUiThread(() -> {
            String current = tvLog.getText().toString();
            tvLog.setText(current + msg + "\n");
            ScrollView sv = findViewById(R.id.scrollView);
            if (sv != null) sv.fullScroll(ScrollView.FOCUS_DOWN);
        });
    }

    // ---------- Menu Hamburger ----------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            try {
                ConfigManager.loadConfig(this, data.getData());
                appendLog(">>> Config loaded from file.");
                Toast.makeText(this, "Config loaded successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                appendLog(">>> Config load error: " + e.getMessage());
                Toast.makeText(this, "Error loading config", Toast.LENGTH_SHORT).show();
            }
        }
    }
}