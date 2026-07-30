package com.gag4.g4wallet.utils;

public class Logger {
    private static LogCallback callback;

    public interface LogCallback {
        void onLog(String msg);
    }

    public static void setCallback(LogCallback cb) {
        callback = cb;
    }

    public static void log(String msg) {
        if (callback != null) {
            callback.onLog(msg);
        }
        System.out.println("[G4Wallet] " + msg);
    }
}
