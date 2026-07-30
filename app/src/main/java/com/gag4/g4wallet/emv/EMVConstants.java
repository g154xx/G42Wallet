package com.gag4.g4wallet.emv;

public class EMVConstants {
    public static final String AID_MASTERCARD = "A0000000041010";
    public static final String AID_VISA = "A0000000031010";

    public static final int TAG_FCI_TEMPLATE = 0x6F;
    public static final int TAG_AID = 0x84;
    public static final int TAG_PDOL = 0x9F38;
    public static final int TAG_AUC = 0x9F07;
    public static final int TAG_AIP = 0x82;
    public static final int TAG_AFL = 0x94;

    public static final int SW_SUCCESS = 0x9000;
    public static final int SW_CONDITIONS_NOT_SATISFIED = 0x6985;
    public static final int SW_FUNCTION_NOT_SUPPORTED = 0x6A81;
    public static final int SW_FILE_NOT_FOUND = 0x6A82;
    public static final int SW_INCORRECT_DATA = 0x6A80;
}
