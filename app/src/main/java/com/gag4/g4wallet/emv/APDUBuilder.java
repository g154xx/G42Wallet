package com.gag4.g4wallet.emv;

import com.gag4.g4wallet.utils.HexUtils;

public class APDUBuilder {
    public static byte[] buildSelectAid(String aidHex) {
        byte[] aid = HexUtils.fromHex(aidHex);
        byte[] cmd = new byte[6 + aid.length];
        cmd[0] = 0x00;
        cmd[1] = (byte) 0xA4;
        cmd[2] = 0x04;
        cmd[3] = 0x00;
        cmd[4] = (byte) aid.length;
        System.arraycopy(aid, 0, cmd, 5, aid.length);
        cmd[cmd.length - 1] = 0x00;
        return cmd;
    }

    public static byte[] buildGpo(byte[] pdolData) {
        byte[] cmd = new byte[5 + pdolData.length + 1];
        cmd[0] = (byte) 0x80;
        cmd[1] = (byte) 0xA8;
        cmd[2] = 0x00;
        cmd[3] = 0x00;
        cmd[4] = (byte) pdolData.length;
        System.arraycopy(pdolData, 0, cmd, 5, pdolData.length);
        cmd[cmd.length - 1] = 0x00;
        return cmd;
    }

    public static byte[] buildGenerateAc(byte p1, byte[] cdolData) {
        byte[] cmd = new byte[5 + cdolData.length + 1];
        cmd[0] = (byte) 0x80;
        cmd[1] = (byte) 0xAE;
        cmd[2] = p1;
        cmd[3] = 0x00;
        cmd[4] = (byte) cdolData.length;
        System.arraycopy(cdolData, 0, cmd, 5, cdolData.length);
        cmd[cmd.length - 1] = 0x00;
        return cmd;
    }
}
