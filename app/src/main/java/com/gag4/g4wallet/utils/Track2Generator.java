package com.gag4.g4wallet.utils;

public class Track2Generator {
    public static String generate(String pan, String expiry, String serviceCode) {
        String panWithLuhn = pan;
        if (!isLuhnValid(pan)) {
            int checkDigit = calculateLuhnCheckDigit(pan.substring(0, pan.length() - 1));
            panWithLuhn = pan.substring(0, pan.length() - 1) + checkDigit;
        }
        StringBuilder track2 = new StringBuilder();
        track2.append(panWithLuhn);
        track2.append('D');
        track2.append(expiry);
        track2.append(serviceCode);
        while (track2.length() < 40) {
            track2.append('0');
        }
        return track2.toString();
    }

    public static boolean isLuhnValid(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    public static int calculateLuhnCheckDigit(String numberWithoutCheck) {
        int sum = 0;
        boolean alternate = true;
        for (int i = numberWithoutCheck.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(numberWithoutCheck.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit;
    }
}
