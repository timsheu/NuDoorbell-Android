package com.nuvoton.socketmanager;

import android.content.Context;
import android.util.Log;

import com.google.common.primitives.Longs;
import com.nuvoton.nuplayer.TokenHandler;
import com.nuvoton.utility.EventMessageClass;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Created by timsheu on 9/1/16.
 */

public class ShmadiaConnectManager {
    private String tmp;
    private Thread thread;
    private Socket clientSocket;
    private DataOutputStream outputStream;
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
                String url = "192.168.8.9";
                String httpPort = "5542";
                int serverPort = Integer.valueOf(httpPort);
                clientSocket = new Socket(url, serverPort);
                outputStream = new DataOutputStream(clientSocket.getOutputStream());
                connectShmadiaServer();
//                reader = new BufferedReader(new InputStreamReader((clientSocket.getInputStream())));
                inputStream = clientSocket.getInputStream();
                if (clientSocket.isConnected()){
                    byte[] tempByte = new byte[EventMessageClass.S_EVENTMSG_HEADER.SIZE];
                    int ret = inputStream.read(tempByte);
                    if (ret == EventMessageClass.S_EVENTMSG_HEADER.SIZE){
                        FCMExecutive.getInstance().setupHeader(tempByte);
                    }
                    EventMessageClass messageClass = FCMExecutive.getInstance().getMessageClass();
                    int remainDataLength = (int)messageClass.sEventmsgLoginResp.sMsgHdr.u32MsgLen - EventMessageClass.S_EVENTMSG_HEADER.SIZE;
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
            outputStream.close();
//            reader.close();
            inputStream.close();
            clientSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public byte[] convertMessageToString(EventMessageClass messageClass){
        byte[] outputData = new byte[EventMessageClass.S_EVENTMSG_LOGIN_REQ.SIZE];

        byte[] bytes = Longs.toByteArray(messageClass.sEventmsgLoginReq.sMsgHdr.u32SignWord);
        bytes = convertEndian(bytes);
        System.arraycopy(bytes, 0, outputData, 0, 4);

        bytes = Longs.toByteArray(messageClass.sEventmsgLoginReq.sMsgHdr.eMsgType);//ByteBuffer.allocate(Long.SIZE).putLong(messageClass.sEventmsgLoginReq.sMsgHdr.eMsgType).array();
        bytes = convertEndian(bytes);
        System.arraycopy(bytes, 0, outputData, 4, 4);

        bytes = Longs.toByteArray(messageClass.sEventmsgLoginReq.sMsgHdr.u32MsgLen);
        bytes = convertEndian(bytes);
        System.arraycopy(bytes, 0, outputData, 8, 4);

        ByteBuffer buffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(messageClass.sEventmsgLoginReq.szUUID));
        bytes = new byte[buffer.limit()];
        buffer.get(bytes);

        System.arraycopy(bytes, 0, outputData, 12, 8);

        bytes = Longs.toByteArray(messageClass.sEventmsgLoginReq.eRole);
        bytes = convertEndian(bytes);

        System.arraycopy(bytes, 0, outputData, 77, 4);

        buffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(messageClass.sEventmsgLoginReq.szCloudRegID));
        bytes = new byte[buffer.limit()];
        buffer.get(bytes);

        System.arraycopy(bytes, 0, outputData, 81, messageClass.sEventmsgLoginReq.szCloudRegID.length);
        return outputData;
    }

    private byte[] convertEndian(byte[] bytes){
        int length = bytes.length;
        byte[] returnByte = new byte[length];
        for (int i=0; i<length; i++){
            returnByte[7-i] = bytes[i];
        }
        return returnByte;
    }

    public void connectShmadiaServer(){
        String refreshedToken = TokenHandler.getToken();
        EventMessageClass messageClass = new EventMessageClass();
        char[] uuidArray = messageClass.TEST_UUID.toCharArray();
        messageClass.sEventmsgLoginReq.szUUID = uuidArray;
        messageClass.sEventmsgLoginReq.eRole = EventMessageClass.E_EVENTMSG_ROLE.eEVENTMSG_ROLE_USER.getRole();
        char[] tokenArray = refreshedToken.toCharArray();
        messageClass.sEventmsgLoginReq.szCloudRegID = tokenArray;
        byte[] message = convertMessageToString(messageClass);
        if (clientSocket.isConnected()){
            Log.d(TAG, "writeMessage: socket connected, sending: " + messageClass.toString());
            try{
                outputStream.write(message, 0, message.length);
                outputStream.flush();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        Log.d(TAG, "sendRegistrationToServer: " + messageClass.toString());
    }
}
