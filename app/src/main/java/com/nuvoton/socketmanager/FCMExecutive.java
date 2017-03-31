package com.nuvoton.socketmanager;

import android.util.Log;

import com.google.common.net.InetAddresses;
import com.nuvoton.nuplayer.DeviceData;
import com.nuvoton.utility.EventMessageClass;
import com.nuvoton.utility.Miscellaneous;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Created by timsheu on 8/30/16.
 */

public class FCMExecutive {
    public interface FCMExecutiveInterface {
        void responseData(DeviceData deviceData);
        void ring(DeviceData deviceData);
    }

    public void setmInterface(FCMExecutiveInterface mInterface) {
        this.mInterface = mInterface;
    }

    //    private Context contextLocal;
    private FCMExecutiveInterface mInterface;
    private static final String TAG = "FCMExecutive";
    private EventMessageClass messageClass;
    private final int offset = 12;

    private static FCMExecutive manager = new FCMExecutive();
    
    private FCMExecutive(){
        messageClass = new EventMessageClass();
        Log.d(TAG, "instance initializer: created");
    }

    public static FCMExecutive getInstance(){
//        manager.contextLocal = context;
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
//                        ReadConfigure.getInstance(contextLocal, false).setTargetValue(entry, retrieveInfo);
                    }else{
                        Log.d(TAG, "retrivedMessage: key \"" + entry + "\" has null info");
                    }
                }
                modifyURLWithPublicIP();
            }
        }
    }

    private void modifyURLWithPublicIP(){
//        String oldURL = ReadConfigure.getInstance(contextLocal, false).getTargetValue("URL");
//        String newIP = ReadConfigure.getInstance(contextLocal, false).getTargetValue("PublicIPAddr");
//        String[] split = oldURL.split("/");
//        if (split.length > 1){
//            String oldIP = split[2];
//            String newURL = oldURL.replace(oldIP, newIP);
//            ReadConfigure.getInstance(contextLocal, false).setTargetValue("URL", newURL);
//            Log.d(TAG, "modifyURLWithPublicIP: new URL: " + newURL);
//        }
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
    
    private byte[] convertEndian(byte[] bytes){
        int length = bytes.length;
        byte[] returnByte = new byte[length];
        for (int i=0; i<length; i++){
            returnByte[length-1-i] = bytes[i];
        }
        return returnByte;
    }

    public void setupHeader(byte[] bytes){
        if (bytes.length == EventMessageClass.S_EVENTMSG_HEADER.SIZE){
            byte[] type = new byte[4];
            System.arraycopy(bytes, 0, type, 0, 4);
            long tempLong = 0L;
            for (int i=0; i<type.length; i++){
                tempLong |= (type[i] << i*8);
            }
            messageClass.sEventmsgLoginReq.sMsgHdr.u32SignWord = tempLong;
            messageClass.sEventmsgLoginResp.sMsgHdr.u32SignWord = tempLong;
            messageClass.sEventmsgEventNotify.sMsgHdr.u32SignWord = tempLong;

            Log.d(TAG, "setupHeader: messageClass.sEventmsgLoginResp.sMsgHdr.u32SignWord: " + tempLong);

            type = new byte[4];
            System.arraycopy(bytes, 4, type, 0, 4);
            tempLong = 0L;
            for (int i=0; i<type.length; i++){
                tempLong |= (type[i] << i*8);
            }
            messageClass.sEventmsgLoginReq.sMsgHdr.eMsgType = tempLong;
            messageClass.sEventmsgLoginResp.sMsgHdr.eMsgType = tempLong;
            messageClass.sEventmsgEventNotify.sMsgHdr.eMsgType = tempLong;
            Log.d(TAG, "setupHeader: messageClass.sEventmsgLoginResp.sMsgHdr.eMsgType: " + tempLong);

            type = new byte[4];
            System.arraycopy(bytes, 8, type, 0, 4);
            tempLong = 0L;
            for (int i=0; i<type.length; i++){
                tempLong |= (type[i] << i*8);
            }
            messageClass.sEventmsgLoginReq.sMsgHdr.u32MsgLen = tempLong;
            messageClass.sEventmsgLoginResp.sMsgHdr.u32MsgLen = tempLong;
            messageClass.sEventmsgEventNotify.sMsgHdr.u32MsgLen = tempLong;
            Log.d(TAG, "setupHeader: messageClass.sEventmsgLoginResp.sMsgHdr.u32MsgLen: " + tempLong);
        }
    }

    public void setupRemainNotifyData(byte[] bytes){
        byte[] tempByte = new byte[4];
        if (bytes.length == EventMessageClass.S_EVENTMSG_EVENT_NOTIFY.SIZE - EventMessageClass.S_EVENTMSG_HEADER.SIZE){
            System.arraycopy(bytes, offset, tempByte, 0, 65);
            long tempLong = 0L;
            char[] szUUID = Miscellaneous.getChars(tempByte, Miscellaneous.UTF_8_ENCODING);
            messageClass.sEventmsgEventNotify.szUUID = szUUID;

            System.arraycopy(bytes, 65+offset, tempByte, 0, 4);
            tempLong = 0L;
            for (int i=0; i<4; i++){
                tempLong |= (tempByte[i] << i*8);
            }
            messageClass.sEventmsgEventNotify.eEventType = tempLong;

            System.arraycopy(bytes, 69+offset, tempByte, 0, 4);
            tempLong = 0L;
            for (int i=0; i<4; i++){
                tempLong |= (tempByte[i] << i*8);
            }
            messageClass.sEventmsgEventNotify.u32EventSeqNo = tempLong;

            Log.d(TAG, "setupRemainNotifyData: " + messageClass.toString());

            String uuid = new String(messageClass.sEventmsgLoginReq.szUUID);
            List<DeviceData> result = DeviceData.find(DeviceData.class, "uuid = ?", uuid);
            if (result.size() > 0) {
                DeviceData one = result.get(0);
                if (result.size() > 1) {
                    Log.d(TAG, "setupRemainNotifyData: multiple records in SQLite !");
                }
                mInterface.ring(one);
            }
        }
    }


    public void setupRemainRequestData(byte[] bytes){
        byte[] tempByte = new byte[65];
        if (bytes.length == EventMessageClass.S_EVENTMSG_LOGIN_REQ.SIZE){
            // As header has been set, need not to manipulate it
            //System.arraycopy(bytes, offset, tempByte, 0, 65); 8 for default uuid size, otherwise it causes trouble
            System.arraycopy(bytes, offset, tempByte, 0, 8);
            messageClass.sEventmsgLoginReq.szUUID = Miscellaneous.getChars(tempByte, Miscellaneous.UTF_8_ENCODING);

            tempByte = new byte[4];
            System.arraycopy(bytes, 65+offset, tempByte, 0, 4);
            long tempLong = 0L;
            for (int i=0; i<4; i++){
                tempLong |= (tempByte[i] << i*8);
            }
            messageClass.sEventmsgLoginReq.eRole = tempLong;

            System.arraycopy(bytes, 324+offset, tempByte, 0, 4);
            tempLong = 0L;
            byte[] publicIP = tempByte.clone();
            messageClass.sEventmsgLoginReq.u32DevPrivateIP = publicIP;

            System.arraycopy(bytes, 328+offset, tempByte, 0, 4);
            tempLong = 0L;
            for (int i=0; i<4; i++){
                tempLong |= (tempByte[i] << i*8);
            }
            messageClass.sEventmsgLoginReq.u32DevHTTPPort = tempLong;

            System.arraycopy(bytes, 332+offset, tempByte, 0, 4);
            tempLong = 0L;
            for (int i=0; i<4; i++){
                tempLong |= (tempByte[i] << i*8);
            }
            messageClass.sEventmsgLoginReq.u32DevRTSPPort = tempLong;

            Log.d(TAG, "setupRemainData: " + messageClass.toString());

            String uuid = new String(messageClass.sEventmsgLoginReq.szUUID);
            List<DeviceData> result = DeviceData.find(DeviceData.class, "uuid = ?", "00000001");
            if (result.size() > 0){
                DeviceData one = result.get(0);
                one = updateValue(messageClass, one);
                mInterface.responseData(one);
            }else {

            }
        }
    }

    public void setupRemainResponseData(byte[] bytes){
        byte[] tempByte = new byte[4];
        if (bytes.length == EventMessageClass.S_EVENTMSG_LOGIN_RESP.SIZE - EventMessageClass.S_EVENTMSG_HEADER.SIZE){
            System.arraycopy(bytes, offset, tempByte, 0, 4);
            long tempLong = 0L;
            for (int i=0; i<4; i++){
                tempLong |= (tempByte[i] << i*8);
            }
            messageClass.sEventmsgLoginResp.eResult = tempLong;

            System.arraycopy(bytes, 4+offset, tempByte, 0, 4);
            tempLong = 0L;
            for (int i=0; i<4; i++){
                tempLong |= (tempByte[i] << i*8);
            }
            messageClass.sEventmsgLoginResp.bDevOnline = tempLong;

            System.arraycopy(bytes, 8+offset, tempByte, 0, 4);
            byte[] publicIP = tempByte.clone();
//            publicIP = convertEndian(publicIP);
            messageClass.sEventmsgLoginResp.u32DevPublicIP = publicIP;

            System.arraycopy(bytes, 12+offset, tempByte, 0, 4);
            byte[] privateIP = tempByte.clone();
//            privateIP = convertEndian(privateIP);
            messageClass.sEventmsgLoginResp.u32DevPrivateIP = privateIP;

            System.arraycopy(bytes, 16+offset, tempByte, 0, 4);
            tempLong = 0L;
            for (int i=0; i<4; i++){
                tempLong |= (tempByte[i] << i*8);
            }
            messageClass.sEventmsgLoginResp.u32DevHTTPPort = tempLong;

            System.arraycopy(bytes, 20+offset, tempByte, 0, 4);
            tempLong = 0L;
            for (int i=0; i<4; i++){
                tempLong |= (tempByte[i] << i*8);
            }
            messageClass.sEventmsgLoginResp.u32DevRTSPPort = tempLong;

            Log.d(TAG, "setupRemainResponseData: " + messageClass.toString());
        }
    }

    private DeviceData updateValue(EventMessageClass message, DeviceData data){
        DeviceData temp = data;
        if (data == null){
            temp = new DeviceData();
        }
        String uuid = new String(message.sEventmsgLoginReq.szUUID).substring(0, 8);
        String addr = Miscellaneous.ipConversionFromByteArray(message.sEventmsgLoginReq.u32DevPrivateIP);
        temp.setUuid(uuid);
        temp.setPrivateIP(addr);
        temp.setPublicIP(addr);
        temp.setHttpPort((int)message.sEventmsgLoginReq.u32DevHTTPPort);
        temp.setRtspPort((int)message.sEventmsgLoginReq.u32DevRTSPPort);
        temp.save();
        return temp;
    }

    public EventMessageClass getMessageClass(){
        return this.messageClass;
    }

}
