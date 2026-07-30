package com.gag4.g4wallet.engine;

import com.gag4.g4wallet.utils.Logger;

import java.util.function.Consumer;

/**
 * Component 1: POS Offline Capability Tester
 * Combines Passive (Xposed sniffer) + Active (HCE behavioral test)
 */
public class OfflineTester {

    private final Consumer<String> logCallback;
    private final Consumer<String> statusCallback;

    public OfflineTester(Consumer<String> log, Consumer<String> status) {
        this.logCallback = log;
        this.statusCallback = status;
    }

    public String runCombinedDiagnostic() {
        log("=== OFFLINE DIAGNOSTIC START ===");
        status("Scanning...");

        String passiveVerdict = passiveScan();
        log("Passive verdict: " + passiveVerdict);

        String activeVerdict = activeTest();
        log("Active verdict: " + activeVerdict);

        if (passiveVerdict.contains("DECLARES") && activeVerdict.contains("TC")) {
            return "✅ OFFLINE SUPPORTED (declared + behavior)";
        } else if (passiveVerdict.contains("DECLARES") && !activeVerdict.contains("TC")) {
            return "⚠️ POS DECLARES OFFLINE BUT DOES NOT USE (config?)";
        } else {
            return "❌ POS DOES NOT SUPPORT OFFLINE";
        }
    }

    private String passiveScan() {
        log(">>> Passive Scan: Intercepting APDUs... (simulated)");
        return "POS DECLARES OFFLINE (9F33 bit 7=1)";
    }

    private String activeTest() {
        log(">>> Active Test: Starting HCE... (simulated)");
        return "POS requested TC (P1=0x80)";
    }

    private void log(String msg) {
        if (logCallback != null) logCallback.accept(msg);
        Logger.log(msg);
    }

    private void status(String msg) {
        if (statusCallback != null) statusCallback.accept(msg);
    }
}
