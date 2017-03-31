package com.nuvoton.utility;

import android.util.Log;

import com.google.common.net.InetAddresses;

import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Created by cchsu20 on 30/03/2017.
 */

public class Miscellaneous {
    public static String ASCII_ENCODING = "US-ASCII";
    public static String UTF_8_ENCODING = "UTF-8";
    private static String TAG = "Miscellaneous";
    public static String ipConversionFromByteArray(byte[] array){
        InetAddress address = null;
        try {
            address = InetAddresses.fromLittleEndianByteArray(array);
            Log.d(TAG, "ipConversionFromByteArray, address: " + address.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        return address.toString().substring(1);
    }

    public static String ipConversionFromInt(long number){
        InetAddress address = null;
        try {
            byte[] temp = new byte[4];
            for (int i = 0; i < 4; i++) {
                temp[i] = (byte)((number >> i*8) & 0xFF);
            }
            address = InetAddresses.fromLittleEndianByteArray(temp);
            Log.d(TAG, "ipConversionFromInt, address: " + address.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        return address.toString().substring(1);
    }

    static public char[] getChars (byte[] bytes, String encoding) {
        //encoding: UTF-8 or US-ASCII
        Charset cs = Charset.forName (encoding);
        ByteBuffer bb = ByteBuffer.allocate (bytes.length);
        bb.put (bytes);
        bb.flip ();
        CharBuffer cb = cs.decode (bb);

        return cb.array();
    }
}
