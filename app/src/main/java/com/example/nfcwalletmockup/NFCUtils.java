package com.example.nfcwalletmockup;

import android.nfc.tech.MifareClassic;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class NFCUtils {

    public static long max_val = Long.parseLong("4294967295");

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public static String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    public static long toDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    public static long toReversedDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    public static String readBalance(MifareClassic mifareTag) {
        String balanceStr = null;
        Log.v("test", "about to connect to check balance");
        try {
            mifareTag.connect();
            Log.v("test", "connected!");

            boolean authDefault = mifareTag.authenticateSectorWithKeyA(5, MifareClassic.KEY_DEFAULT);
            if (authDefault) {

                int blockIndex = mifareTag.sectorToBlock(5) + 1;
                byte[] readBlock = mifareTag.readBlock(blockIndex);

                byte[] val1 = new byte[4];
                System.arraycopy(readBlock, 0, val1, 0, 4);

                Log.v("test", "Dec Value is: "+toDec(val1));

                byte[] val_inverted = new byte[4];
                System.arraycopy(readBlock, 4, val_inverted, 0, 4);

                Log.v("test", "Dec Value is: "+toDec(val_inverted));

                byte[] val2 = new byte[4];
                System.arraycopy(readBlock, 8, val2, 0, 4);

                Log.v("test", "Dec Value is: "+toDec(val2));

                if (toDec(val1) == toDec(val2) && toDec(val_inverted)+toDec(val1) == max_val){
                    Log.v("test", "Integrity checks were succesful!");
                    long balance = toDec(val1);
                    balanceStr = String.valueOf(balance);
                } else {
                    Log.v("test", "Integrity error detected in balance value!");
                }

                /*byte[] balanceLength = new byte[1];
                System.arraycopy(readBlock, 3, balanceLength, 0, 1);
                String balanceLengthStr = new String(balanceLength, StandardCharsets.UTF_8);
                int balanceLengthInt = Integer.parseInt(balanceLengthStr);

                byte[] balance = new byte[balanceLengthInt];

                System.arraycopy(readBlock, 0, balance, 0, balanceLengthInt);
                 */


            } else {
                Log.v("test", "Authentication failed on sector 5! Likely due to non-default keys being used for this sector");
            }

            mifareTag.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return balanceStr;
    }

    public static void incrementBalance(MifareClassic mifareTag, String amount){
        try {
            mifareTag.connect();

            boolean authDefault = mifareTag.authenticateSectorWithKeyA(5, MifareClassic.KEY_DEFAULT);
            if (authDefault) {

                int blockIndex = mifareTag.sectorToBlock(5) + 1;

                mifareTag.increment(blockIndex, Integer.parseInt(amount));
                mifareTag.transfer(blockIndex);

            } else {
                Log.v("test","Authentication failed on sector 5! Likely due to non-default keys being used for this sector");
            }

            mifareTag.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decrementBalance(MifareClassic mifareTag, String amount){
        try {
            mifareTag.connect();

            boolean authDefault = mifareTag.authenticateSectorWithKeyA(5, MifareClassic.KEY_DEFAULT);
            if (authDefault) {

                int blockIndex = mifareTag.sectorToBlock(5) + 1;

                mifareTag.decrement(blockIndex, Integer.parseInt(amount));
                mifareTag.transfer(blockIndex);

            } else {
                Log.v("test","Authentication failed on sector 5! Likely due to non-default keys being used for this sector");
            }

            mifareTag.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeBalance(MifareClassic mifareTag, String amount){

        try {
            mifareTag.connect();

            boolean authDefault = mifareTag.authenticateSectorWithKeyA(5, MifareClassic.KEY_DEFAULT);
            if (authDefault) {

                int blockIndex = mifareTag.sectorToBlock(5) + 1;
                /*
                byte[] writeBlock = new byte[16];
                byte[] rechargeAmountBytes = amount.getBytes(StandardCharsets.UTF_8);

                int balanceLength = amount.length();
                byte[] balanceLengthBytes = String.valueOf(balanceLength).getBytes();

                System.arraycopy(balanceLengthBytes, 0, writeBlock, 3, balanceLengthBytes.length);
                System.arraycopy(rechargeAmountBytes, 0, writeBlock, 0, rechargeAmountBytes.length);
                 */

                mifareTag.increment(blockIndex, Integer.parseInt(amount));
                mifareTag.transfer(blockIndex);
                //mifareTag.writeBlock(blockIndex, writeBlock);

            } else {
                Log.v("test","Authentication failed on sector 5! Likely due to non-default keys being used for this sector");
            }

            mifareTag.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static boolean detectChineseTag(MifareClassic mifareTag){

        try {
            mifareTag.connect();

            boolean authDefault = mifareTag.authenticateSectorWithKeyA(0, MifareClassic.KEY_DEFAULT);

            if (authDefault){

                //Se prepara un bloque de fabricante con ID 'FF FF FF FF'
                byte[] pl = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x44, (byte) 0x08,
                        (byte) 0x04, (byte) 0x00, (byte) 0x62, (byte) 0x63, (byte) 0x64, (byte) 0x65, (byte) 0x66, (byte) 0x67, (byte) 0x68, (byte) 0x69};

                //Índice del bloque de fabricante.
                int blockIndex = mifareTag.sectorToBlock(0);

                //Sobreescritura del bloque de fabricante
                mifareTag.writeBlock(blockIndex, pl);

                //Lectura del bloque de fabricante
                byte[] readBlock = mifareTag.readBlock(blockIndex);

                Log.v("test","Written block: "+toHex(pl));
                Log.v("test","Read block: "+toHex(readBlock));

                //Si el bloque leído es igual al que fué preparado para la sobreescritura, se trata de un tag mágico.
                if (toHex(readBlock).equals(toHex(pl))){
                    Log.v("test","Cloned card detected!");
                    mifareTag.close();
                    return true;
                } else {
                    Log.v("test","This is not a backdoor card");
                    mifareTag.close();
                    return false;
                }

            } else {
                Log.v("test","Authentication failed on sector 5! Likely due to non-default keys being used for this sector");
            }

            mifareTag.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
