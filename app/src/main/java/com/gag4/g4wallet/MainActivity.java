package com.gag4.g4wallet;

import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gag4.g4wallet.engine.OfflineForce;
import com.gag4.g4wallet.engine.OfflineTester;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;
    private TextView tvLog;
    private OfflineTester offlineTester;
    private OfflineForce offlineForce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tv_status);
        tvLog = findViewById(R.id.tv_log);

        offlineTester = new OfflineTester(this::appendLog, this::updateStatus);
        offlineForce = new OfflineForce(this::appendLog, this::updateStatus);

        findViewById(R.id.btn_diagnostic).setOnClickListener(v -> {
            appendLog(">>> Starting POS Diagnostic...");
            updateStatus("Scanning...");
            new Thread(() -> {
                String result = offlineTester.runCombinedDiagnostic();
                runOnUiThread(() -> {
                    appendLog(">>> Result: " + result);
                    updateStatus("Done: " + result);
                });
            }).start();
        });

        findViewById(R.id.btn_card_info).setOnClickListener(v ->
            Toast.makeText(this, "Card info - future extension", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btn_load_keys).setOnClickListener(v -> {
            offlineForce.loadKeys("0123456789ABCDEF0123456789ABCDEF");
            appendLog(">>> Keys loaded (mock).");
            Toast.makeText(this, "Keys loaded", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_generate_track).setOnClickListener(v -> {
            String track = offlineForce.generateTrack2("1234567890123456", "2612", "101");
            appendLog(">>> Track2 generated: " + track);
            Toast.makeText(this, "Track2: " + track, Toast.LENGTH_LONG).show();
        });

        findViewById(R.id.btn_settings).setOnClickListener(v ->
            Toast.makeText(this, "Settings - coming soon", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btn_network).setOnClickListener(v ->
            Toast.makeText(this, "Network info - placeholder", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btn_about).setOnClickListener(v ->
            Toast.makeText(this, "G42Wallet v1.0\nEMV Offline Tester", Toast.LENGTH_LONG).show()
        );

        findViewById(R.id.btn_health_card).setOnClickListener(v ->
            Toast.makeText(this, "Health Card - future extension", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btn_id_card).setOnClickListener(v ->
            Toast.makeText(this, "ID Card - future extension", Toast.LENGTH_SHORT).show()
        );
    }

    private void updateStatus(String msg) {
        runOnUiThread(() -> tvStatus.setText("Status: " + msg));
    }

    private void appendLog(String msg) {
        runOnUiThread(() -> {
            String current = tvLog.getText().toString();
            tvLog.setText(current + msg + "\n");
            ScrollView sv = findViewById(R.id.scrollView);
            if (sv != null) sv.fullScroll(View.FOCUS_DOWN);
        });
    }
}
