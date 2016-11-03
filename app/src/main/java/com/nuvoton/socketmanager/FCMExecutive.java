package com.nuvoton.socketmanager;

import android.content.Context;
import android.util.Log;

import com.google.common.primitives.Longs;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by timsheu on 8/30/16.
 */

public class FCMExecutive {
    private Context contextLocal;
    private static final String TAG = "FCMExecutive";
    private static String token;
    private EventMessageClass messageClass;

    private static FCMExecutive manager = new FCMExecutive();
    
    private FCMExecutive(){
        messageClass = new EventMessageClass();
        Log.d(TAG, "instance initializer: created");
    }

    public static FCMExecutive getInstance(Context context){
        manager.contextLocal = context;
        Log.d(TAG, "getInstance: ");
        return manager;
    }

    public void retrivedMessage(Map<String, Object> content){
        String messageType = "IP Data";
        if (content.size() > 0){
            for (Map.Entry<String, Object> entry : content.entrySet()) {
                if (entry.getKey().compareTo("aps") == 0){
                    messageType = "Ring";
                }
            }
            if (messageType.compareTo("Ring") == 0){
                Map<String, Object> apsAlert = (Map<String, Object>) content.get("aps");
                Map<String, String> alert = (Map<String, String>) apsAlert.get("alert");
                Log.d(TAG, "retrivedMessage: aps body" + alert.get("body"));
            }else if(messageType.compareTo("IP Data") == 0){
                String[] messageInfo = new String[]{"PublicIPAddr", "PrivateIPAddr", "HTTPPort", "RTSPPort"};
                String retrieveInfo;
                for (String entry: messageInfo) {
                    retrieveInfo = (String) content.get(entry);
                    if (retrieveInfo != null){
                        ReadConfigure.getInstance(contextLocal, false).setTargetValue(entry, retrieveInfo);
                    }else{
                        Log.d(TAG, "retrivedMessage: key \"" + entry + "\" has null info");
                    }
                }
                modifyURLWithPublicIP();
            }
        }
    }

    private void modifyURLWithPublicIP(){
        String oldURL = ReadConfigure.getInstance(contextLocal, false).getTargetValue("URL");
        String newIP = ReadConfigure.getInstance(contextLocal, false).getTargetValue("PublicIPAddr");
        String[] split = oldURL.split("/");
        if (split.length > 1){
            String oldIP = split[2];
            String newURL = oldURL.replace(oldIP, newIP);
            ReadConfigure.getInstance(contextLocal, false).setTargetValue("URL", newURL);
            Log.d(TAG, "modifyURLWithPublicIP: new URL: " + newURL);
        }
    }

    public void retrieveString(String message){
        if (message.length() == 36){
            byte [] bytes = message.getBytes();
            for (int i=0; i<36; i++){
                Log.d(TAG, "retrieveString: " + String.valueOf(bytes[i]));
            }
            Log.d(TAG, "retrieveString: ");
            long temp = 0L;
            int j=0;
            for (int i=j; i<4+j; i++){
                temp = temp | (bytes[3+j-i] << i-j);
            }
            Log.d(TAG, "retrieveString: header type " + String.valueOf(temp));

            temp = 0L;
            j=4;
            for (int i=j; i<4+j; i++){
                temp = temp | (bytes[3+j-i] << i-j);
            }
            Log.d(TAG, "retrieveString: header length " + String.valueOf(temp));

            temp = 0L;
            j=8;
            for (int i=j; i<4+j; i++){
                temp = temp | (bytes[3+j-i] << i-j);
            }
            Log.d(TAG, "retrieveString: result: " + String.valueOf(temp));

            temp = 0L;
            j=12;
            for (int i=j; i<4+j; i++){
                temp = temp | (bytes[3+j-i] << i-j);
            }
            Log.d(TAG, "retrieveString: online: " + String.valueOf(temp));

            temp = 0L;
            j=16;
            for (int i=j; i<4+j; i++){
                temp = temp | (bytes[3+j-i] << i-j);
            }
            Log.d(TAG, "retrieveString: u32DevPublicIP: " + String.valueOf(temp));

            temp = 0L;
            j=20;
            for (int i=j; i<4+j; i++){
                temp = temp | (bytes[3+j-i] << i-j);
            }
            Log.d(TAG, "retrieveString: u32DevPrivateIP: " + String.valueOf(temp));

            temp = 0L;
            j=24;
            for (int i=j; i<4+j; i++){
                temp = temp | (bytes[3+j-i] << i-j);
            }
            Log.d(TAG, "retrieveString: u32DevHTTPPort: " + String.valueOf(temp));

            temp = 0L;
            j=28;
            for (int i=j; i<4+j; i++){
                temp = temp | (bytes[3+j-i] << i-j);
            }
            Log.d(TAG, "retrieveString: u32DevRTSPPort: " + String.valueOf(temp));

        }else{
            Log.d(TAG, "retrieveString: wrong string length: " + message.length());
        }
    }

    public void setToken(String string){
        this.token = string;
    }

    public String getToken(){
        return token;
    }

    private byte[] convertEndian(byte[] bytes){
        int length = bytes.length;
        byte[] returnByte = new byte[length];
        for (int i=0; i<length; i++){
            returnByte[length-1-i] = bytes[i];
        }
        return returnByte;
    }

    public void setupHeader(byte[] bytes){
        if (bytes.length == 8){
            byte[] type = new byte[4];
            System.arraycopy(bytes, 0, type, 0, 4);
            long tempLong = 0L;
            for (int i=0; i<type.length; i++){
                tempLong |= (type[i] << i*8);
            }
            messageClass.response.sMsgHdr.eMsgType = tempLong;
            Log.d(TAG, "setupHeader: messageClass.response.sMsgHdr.eMsgType: " + tempLong);
            type = new byte[4];
            System.arraycopy(bytes, 4, type, 0, 4);
            tempLong = 0L;
            for (int i=0; i<type.length; i++){
                tempLong |= (type[i] << i*8);
            }
            messageClass.response.sMsgHdr.u32MsgLen = tempLong;
            Log.d(TAG, "setupHeader: messageClass.response.sMsgHdr.u32MsgLen: " + tempLong);
        }
    }

    public void setupRemainRequestData(byte[] bytes){
        byte[] tempByte = new byte[4];
        if (bytes.length == 336){
            System.arraycopy(bytes, 324, tempByte, 0, 4);
            long tempLong = 0L;
            byte[] publicIP = tempByte.clone();
            messageClass.response.u32DevPublicIP = publicIP;
            messageClass.response.u32DevPrivateIP = publicIP;

            System.arraycopy(bytes, 328, tempByte, 0, 4);
            tempLong = 0L;
            for (int i=0; i<4; i++){
                tempLong |= (tempByte[i] << i*8);
            }
            messageClass.response.u32DevHTTPPort = tempLong;

            System.arraycopy(bytes, 332, tempByte, 0, 4);
            tempLong = 0L;
            for (int i=0; i<4; i++){
                tempLong |= (tempByte[i] << i*8);
            }
            messageClass.response.u32DevRTSPPort = tempLong;

            Log.d(TAG, "setupRemainData: " + messageClass.toString());

            ReadConfigure.getInstance(contextLocal, false).updateDoorBellDevice(messageClass);
        }
    }

    public void setupRemainResponseData(byte[] bytes){
        byte[] tempByte = new byte[4];
        if (bytes.length == 24){
            System.arraycopy(bytes, 0, tempByte, 0, 4);
            long tempLong = 0L;
            for (int i=0; i<4; i++){
                tempLong |= (tempByte[i] << i*8);
            }
            messageClass.response.eResult = tempLong;

            System.arraycopy(bytes, 4, tempByte, 0, 4);
            tempLong = 0L;
            for (int i=0; i<4; i++){
                tempLong |= (tempByte[i] << i*8);
            }
            messageClass.response.bDevOnline = tempLong;

            System.arraycopy(bytes, 8, tempByte, 0, 4);
            byte[] publicIP = tempByte.clone();
//            publicIP = convertEndian(publicIP);
            messageClass.response.u32DevPublicIP = publicIP;

            System.arraycopy(bytes, 12, tempByte, 0, 4);
            byte[] privateIP = tempByte.clone();
//            privateIP = convertEndian(privateIP);
            messageClass.response.u32DevPrivateIP = privateIP;

            System.arraycopy(bytes, 16, tempByte, 0, 4);
            tempLong = 0L;
            for (int i=0; i<4; i++){
                tempLong |= (tempByte[i] << i*8);
            }
            messageClass.response.u32DevHTTPPort = tempLong;

            System.arraycopy(bytes, 20, tempByte, 0, 4);
            tempLong = 0L;
            for (int i=0; i<4; i++){
                tempLong |= (tempByte[i] << i*8);
            }
            messageClass.response.u32DevRTSPPort = tempLong;

            Log.d(TAG, "setupRemainData: " + messageClass.toString());

            ReadConfigure.getInstance(contextLocal, false).updateDoorBellDevice(messageClass);
        }
    }

    public EventMessageClass getMessageClass(){
        return this.messageClass;
    }
}
