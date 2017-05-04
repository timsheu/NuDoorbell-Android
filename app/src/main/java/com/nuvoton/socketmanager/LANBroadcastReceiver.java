package com.nuvoton.socketmanager;

import android.content.Context;
import android.util.Log;

import com.google.common.net.InetAddresses;
import com.nuvoton.nuplayer.DeviceData;
import com.nuvoton.utility.EventMessageClass;
import com.nuvoton.utility.Miscellaneous;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

/**
 * Created by cchsu20 on 10/27/16.
 */

public class LANBroadcastReceiver implements FCMExecutive.FCMExecutiveInterface{

    @Override
    public void ring(DeviceData deviceData) {

    }

    @Override
    public void responseData(DeviceData deviceData) {
        broadcastInterface.signalDataHandled(deviceData, "Data from response received!");
        Log.d(TAG, "responseData: " + deviceData.toString());
    }

    public interface BroadcastInterface {
        void signalDataHandled(DeviceData deviceData, String message);
    }

    public void setOn(boolean on) {
        isOn = on;
    }
    private DeviceData responseData;
    private boolean isOn = false;
    public BroadcastInterface broadcastInterface;
    private Thread thread;
    private InputStream inputStream;
    private Context contextLocal;
    private static String TAG = "LANBroadcastReceiver";
    private static LANBroadcastReceiver bcrReceiver = new LANBroadcastReceiver();
    private DatagramSocket socket;
    private LANBroadcastReceiver(){

    }

    public static LANBroadcastReceiver getInstance(Context context){
        bcrReceiver.contextLocal = context;
        return bcrReceiver;
    }

    public void openUDPSocket(){
        isOn = true;
        thread = new Thread(OpenUDPSocket);
        thread.start();
    }

    public void closeUDPSocket(){
        Log.d(TAG, "closeUDPSocket: ");
        if (socket.isConnected()){
            socket.disconnect();
            socket.close();
            socket = null;
        }
        thread.interrupt();
    }

    private Runnable OpenUDPSocket = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: ");
            final int SIZE = EventMessageClass.S_EVENTMSG_HEADER.SIZE;
            final int headerSize = SIZE;
            byte buffer[] = new byte[SIZE];
            int udpPort = 5543;
            try{
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket = new DatagramSocket(udpPort);
                while (isOn){
                    socket.receive(packet);
                    byte[] tempByte = new byte[headerSize];
                    System.arraycopy(buffer, 0, tempByte, 0, headerSize);
                    FCMExecutive.getInstance().setupHeader(tempByte);
                    FCMExecutive.getInstance().setmInterface(LANBroadcastReceiver.this);
                    EventMessageClass messageClass = FCMExecutive.getInstance().getMessageClass();
                    int completeDataLength = (int)messageClass.sEventmsgEventNotify.sMsgHdr.u32MsgLen;
                    if (messageClass.sEventmsgEventNotify.sMsgHdr.eMsgType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_EVENT_NOTIFY){
                        Log.d(TAG, "run: ring: " + String.valueOf(completeDataLength+headerSize) );
                        byte[] remainBuffer = new byte[completeDataLength];
                        DatagramPacket remainPacket = new DatagramPacket(remainBuffer, remainBuffer.length);
                        socket.receive(remainPacket);
                        FCMExecutive.getInstance().setupRemainResponseData(remainPacket.getData());
                        String uuid = new String(messageClass.sEventmsgEventNotify.szUUID);
                        //use default uuid
                        List<DeviceData> list = DeviceData.findWithQuery(DeviceData.class, "UUID = ?", "00000001");
                        switch (list.size()){
                            case 0:
                                Log.d(TAG, "run: no such uuid in DeviceData ORM");
                                break;
                            case 1:
                                Log.d(TAG, "run: ring form uuid: " + uuid);
                                DeviceData deviceData = list.get(0);
                                broadcastInterface.signalDataHandled(deviceData, "Ring from UUID: " + uuid);
                                break;
                            default:
                                Log.d(TAG, "run: multiple SQL entries!!");
                                break;
                        }
                    }else if(messageClass.sEventmsgLoginReq.sMsgHdr.eMsgType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_LOGIN){
                        byte[] remainBuffer = new byte[completeDataLength];
                        DatagramPacket remainPacket = new DatagramPacket(remainBuffer, remainBuffer.length);
                        socket.receive(remainPacket);
                        Log.d(TAG, "run, login: " + String.valueOf(remainPacket) );
                        tempByte = new byte[completeDataLength];
                        System.arraycopy(remainBuffer, 0, tempByte, 0, completeDataLength);
                        FCMExecutive.getInstance().setupRemainRequestData(tempByte);
                        EventMessageClass message = FCMExecutive.getInstance().getMessageClass();
                        Log.d(TAG, "run, after setup remain: " + message.sEventmsgLoginReq.toString());
                        byte[] test = new byte[65];
                        test[0] = 30;
                        test[1] = 30;
                        test[2] = 30;
                        test[3] = 30;
                        test[4] = 30;
                        test[5] = 30;
                        test[6] = 30;
                        test[7] = 31;
                        char[] testChar = Miscellaneous.getChars(test, Miscellaneous.ASCII_ENCODING);
                        String testString = String.valueOf(testChar);
                        List<DeviceData> list = DeviceData.find(DeviceData.class, "uuid = ?", "00000001");
                        if (list.size() > 0){
                            DeviceData deviceData = list.get(0);
                            InetAddress address = InetAddresses.fromLittleEndianByteArray(message.sEventmsgLoginReq.u32DevPrivateIP);
                            Log.d(TAG, "run, addrees: " + address.toString());
                            broadcastInterface.signalDataHandled(deviceData, "Login from " + address.toString().substring(1));
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

//    private int determineMessageLength(EventMessageClass eventMessageClass){
//        int length = 0;
//        long messageType = eventMessageClass.sEventmsgHeader.eMsgType;
//        if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_EVENT_NOTIFY){
//            length = eventMessageClass.sEventmsgEventNotify.messageLength();
//        }else if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_EVENT_NOTIFY_RESP){
//            length = eventMessageClass.sEventmsgEventNotifyResp.messageLength();
//        }else if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_LOGIN){
//            length = eventMessageClass.sEventmsgLoginReq.messageLength();
//        }else if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_LOGIN_RESP){
//            length = eventMessageClass.sEventmsgLoginResp.messageLength();
//        }else if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_FW_DOWNLOAD){
//            length = eventMessageClass.sEventmsgFwDownload.messageLength();
//        }else if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_FW_DOWNLOAD_RESP){
//            length = eventMessageClass.sEventmsgFwDownloadResp.messageLength();
//        }else if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_GET_FW_VER){
//            length = eventMessageClass.sEventmsgGetFwVer.messageLength();
//        }else if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_GET_FW_VER_RESP){
//            length = eventMessageClass.sEventmsgGetFwVerResp.messageLength();
//        }
//        return length;
//    }

    public void setBroadcastInterface(BroadcastInterface broadcastInterface) {
        this.broadcastInterface = broadcastInterface;
    }

}
