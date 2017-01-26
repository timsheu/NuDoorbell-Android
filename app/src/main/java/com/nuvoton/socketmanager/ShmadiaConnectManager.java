package com.nuvoton.socketmanager;

import android.content.Context;
import android.util.Log;

import com.google.common.primitives.Longs;
import com.nuvoton.utility.EventMessageClass;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Created by timsheu on 9/1/16.
 */

public class ShmadiaConnectManager {
    public interface ShmadiaConnectInterface {
        void announceIsConnected();
    }
    public ShmadiaConnectInterface shmadiaConnectInterface;
    private String tmp;
    private Thread thread;
    private Socket clientSocket;
    private BufferedWriter writer;
//    private BufferedReader reader;
    private InputStream inputStream;
    private Context contextLocal;
    private static final String TAG = "ShmadiaConnectManager";
    private boolean isConnedted = false;
    private static ShmadiaConnectManager manager = new ShmadiaConnectManager();
    private ShmadiaConnectManager(){

    }

    public static ShmadiaConnectManager getInstance(Context context){
        manager.contextLocal = context;
        Log.d(TAG, "getInstance: ");
        return manager;
    }

    private Runnable Connection = new Runnable() {
        @Override
        public void run() {
            try{
//                String url = ReadConfigure.getInstance(contextLocal, false).getTargetValue("PublicIPAddr");
                String url = "192.168.8.9";
                InetAddress serverIP = InetAddress.getByName(url);
//                String httpPort = ReadConfigure.getInstance(contextLocal, false).getTargetValue("HTTPPort");
                String httpPort = "5542";
                int serverPort = Integer.valueOf(httpPort);
                clientSocket = new Socket(serverIP, serverPort);
                writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
//                reader = new BufferedReader(new InputStreamReader((clientSocket.getInputStream())));
                inputStream = clientSocket.getInputStream();
                if (clientSocket.isConnected() && shmadiaConnectInterface != null){
                    shmadiaConnectInterface.announceIsConnected();
                }
                while (clientSocket.isConnected()){
                    byte[] tempByte = new byte[8];
                    int ret = inputStream.read(tempByte);
                    if (ret == 8){
                        FCMExecutive.getInstance().setupHeader(tempByte);
                    }
                    EventMessageClass messageClass = FCMExecutive.getInstance().getMessageClass();
                    int remainDataLength = (int)messageClass.sEventmsgLoginResp.sMsgHdr.u32MsgLen - 8;
                    tempByte = new byte[remainDataLength];
                    ret = inputStream.read(tempByte);
                    if (ret == remainDataLength){
                        FCMExecutive.getInstance().setupRemainResponseData(tempByte);
                    }
                    closeSocket();
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, "run: socket failed");
            }
        }
    };

    public void openSocket(){
        thread = new Thread(Connection);
        thread.start();
    }

    public void closeSocket(){
        try{
            writer.close();
//            reader.close();
            inputStream.close();
            clientSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void writeMessageToShmadia(EventMessageClass messageClass){
        byte[] outputData = new byte[344];
        byte[] bytes = Longs.toByteArray(messageClass.sEventmsgLoginReq.sMsgHdr.eMsgType);//ByteBuffer.allocate(Long.SIZE).putLong(messageClass.sEventmsgLoginReq.sMsgHdr.eMsgType).array();

        bytes = convertEndian(bytes);
        System.arraycopy(bytes, 0, outputData, 0, 4);

        bytes = Longs.toByteArray(messageClass.sEventmsgLoginReq.sMsgHdr.u32MsgLen);
        bytes = convertEndian(bytes);
        System.arraycopy(bytes, 0, outputData, 4, 4);

        ByteBuffer buffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(messageClass.sEventmsgLoginReq.szUUID));
        bytes = new byte[buffer.limit()];
        buffer.get(bytes);

        System.arraycopy(bytes, 0, outputData, 8, 8);

        bytes = Longs.toByteArray(messageClass.sEventmsgLoginReq.eRole);
        bytes = convertEndian(bytes);

        System.arraycopy(bytes, 0, outputData, 73, 4);

        buffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(messageClass.sEventmsgLoginReq.szCloudRegID));
        bytes = new byte[buffer.limit()];
        buffer.get(bytes);

        System.arraycopy(bytes, 0, outputData, 77, messageClass.sEventmsgLoginReq.szCloudRegID.length);

        String outputDataString = new String(outputData);
        if (clientSocket.isConnected()){
            Log.d(TAG, "writeMessage: socket connected, sending: " + outputDataString);
            try{
                writer.write(outputDataString);
                writer.flush();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private byte[] convertEndian(byte[] bytes){
        int length = bytes.length;
        byte[] returnByte = new byte[length];
        for (int i=0; i<length; i++){
            returnByte[7-i] = bytes[i];
        }
        return returnByte;
    }
}
