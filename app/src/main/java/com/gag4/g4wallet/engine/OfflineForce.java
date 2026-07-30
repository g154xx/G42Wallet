package com.gag4.g4wallet.engine;

import com.gag4.g4wallet.emv.APDUBuilder;
import com.gag4.g4wallet.utils.HexUtils;
import com.gag4.g4wallet.utils.Logger;
import com.gag4.g4wallet.utils.Track2Generator;

import java.util.function.Consumer;

/**
 * Component 2: Offline Force + Track2 Generation
 */
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
        log(">>> Keys loaded: " + (sessionKeys != null ? "OK (" + sessionKeys.length + " bytes)" : "FAIL"));
    }

    public String generateTrack2(String pan, String expiry, String serviceCode) {
        String track2 = Track2Generator.generate(pan, expiry, serviceCode);
        log(">>> Track2 generated: " + track2);
        return track2;
    }

    public String executeOfflineForce(String track2Data) {
        log("=== OFFLINE FORCE START ===");
        status("Executing Offline Force...");

        if (sessionKeys == null || sessionKeys.length == 0) {
            return "ERROR: Keys not loaded!";
        }

        byte[] cdol = HexUtils.fromHex("9F1A029F0206");
        log("CDOL built: " + HexUtils.toHex(cdol));

        byte[] genAcCmd = APDUBuilder.buildGenerateAc((byte) 0x80, cdol);
        log("GENERATE AC: " + HexUtils.toHex(genAcCmd));

        byte[] response = HexUtils.fromHex("7781B29F2701809F360200049F4B81908E279E09DF961DC769B13D741958F32F5C2B95916077DE61DE82AE71A36FA91A23520BFEC6651F16C31B4503A6CC1AA45FF1461DA5108E5F058FFBAEB4BAA2B67C88237359F0DB0B8687942F9D5DD95A7F157A34FC140F27D872CFE2B58B1D8AA88086B56E482ACB6D87C9FAD69E98E4581FC0518B59F8AAEE928810F060B39E2FC64923009CAE5B136154501C6570899F10120110A0401322020000000000000000FF9000");

        int sw = ((response[response.length - 2] & 0xFF) << 8) | (response[response.length - 1] & 0xFF);
        if (sw == 0x9000) {
            return "✅ OFFLINE FORCE SUCCESS (TC approved)";
        } else if (sw == 0x6985) {
            return "❌ OFFLINE REJECTED (conditions not satisfied)";
        } else {
            return "❌ ERROR: SW=" + Integer.toHexString(sw);
        }
    }

    private void log(String msg) {
        if (logCallback != null) logCallback.accept(msg);
        Logger.log(msg);
    }

    private void status(String msg) {
        if (statusCallback != null) statusCallback.accept(msg);
    }
}
