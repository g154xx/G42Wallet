package com.gag4.g4wallet;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText etPan, etExpiry, etServiceCode, etCountry, etAmount, etAid, etKeyHex;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        etPan = findViewById(R.id.et_pan);
        etExpiry = findViewById(R.id.et_expiry);
        etServiceCode = findViewById(R.id.et_service_code);
        etCountry = findViewById(R.id.et_country);
        etAmount = findViewById(R.id.et_amount);
        etAid = findViewById(R.id.et_aid);
        etKeyHex = findViewById(R.id.et_key_hex);
        btnSave = findViewById(R.id.btn_save_settings);

        // Încărcare valori salvate
        SharedPreferences prefs = getSharedPreferences("G4WalletPrefs", MODE_PRIVATE);
        etPan.setText(prefs.getString("pan", "1234567890123456"));
        etExpiry.setText(prefs.getString("expiry", "2612"));
        etServiceCode.setText(prefs.getString("service_code", "101"));
        etCountry.setText(prefs.getString("country", "0840")); // 0840 = România
        etAmount.setText(prefs.getString("amount", "000000000500")); // 5.00 RON
        etAid.setText(prefs.getString("aid", "A0000000041010")); // Mastercard
        etKeyHex.setText(prefs.getString("emv_key_hex", "0123456789ABCDEF0123456789ABCDEF"));

        btnSave.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("pan", etPan.getText().toString());
            editor.putString("expiry", etExpiry.getText().toString());
            editor.putString("service_code", etServiceCode.getText().toString());
            editor.putString("country", etCountry.getText().toString());
            editor.putString("amount", etAmount.getText().toString());
            editor.putString("aid", etAid.getText().toString());
            editor.putString("emv_key_hex", etKeyHex.getText().toString());
            editor.apply();
            finish();
        });
    }
}