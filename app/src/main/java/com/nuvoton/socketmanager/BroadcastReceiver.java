package com.nuvoton.socketmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;

/**
 * Created by cchsu20 on 10/27/16.
 */

public class BroadcastReceiver {
    private Thread thread;
    private InputStream inputStream;
    private Context contextLocal;
    private static String TAG = "BroadcastReceiver";
    private static BroadcastReceiver bcrReceiver = new BroadcastReceiver();

    private BroadcastReceiver(){

    }

    public static BroadcastReceiver getInstance(Context context){
        bcrReceiver.contextLocal = context;
        return bcrReceiver;
    }

    public void openUDPSocket(){
        thread = new Thread(OpenUDPSocket);
        thread.start();
    }

    private Runnable OpenUDPSocket = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: ");
            final int SIZE = 344;
            byte buffer[] = new byte[SIZE];
            int udpPort = 5543;
            try{
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                DatagramSocket socket = new DatagramSocket(udpPort);
                socket.receive(packet);
                byte[] tempByte = new byte[8];
                System.arraycopy(buffer, 0, tempByte, 0, 8);
                FCMExecutive.getInstance(contextLocal).setupHeader(tempByte);
                EventMessageClass messageClass = FCMExecutive.getInstance(contextLocal).getMessageClass();
                int remainDataLength = (int)messageClass.response.sMsgHdr.u32MsgLen - 8;
                if (remainDataLength == 73){
                    socket.close();
                    bcrReceiver.openUDPSocket();
                    Log.d(TAG, "run: length incorrect: " + String.valueOf(remainDataLength+8) );
                    return;
                }
                Log.d(TAG, "run: length correct: " + String.valueOf(remainDataLength+8) );
                tempByte = new byte[remainDataLength];
                System.arraycopy(buffer, 8, tempByte, 0, remainDataLength);
                FCMExecutive.getInstance(contextLocal).setupRemainRequestData(tempByte);
                socket.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };


}
