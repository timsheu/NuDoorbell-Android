package com.nuvoton.socketmanager;

import android.content.Context;
import android.util.Log;

import com.nuvoton.nuplayer.DeviceData;
import com.nuvoton.utility.EventMessageClass;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;

/**
 * Created by cchsu20 on 10/27/16.
 */

public class BroadcastReceiver implements FCMExecutive.FCMExecutiveInterface{

    @Override
    public void responseData(DeviceData deviceData) {
        broadcastInterface.signalDataHandled(deviceData);
        Log.d(TAG, "responseData: " + deviceData.toString());
    }

    public interface BroadcastInterface {
        void signalDataHandled(DeviceData deviceData);
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
    private static String TAG = "BroadcastReceiver";
    private static BroadcastReceiver bcrReceiver = new BroadcastReceiver();
    private DatagramSocket socket;
    private BroadcastReceiver(){

    }

    public static BroadcastReceiver getInstance(Context context){
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
            final int SIZE = 344;
            final int headerSize = 8;
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
                    FCMExecutive.getInstance().setmInterface(BroadcastReceiver.this);
                    EventMessageClass messageClass = FCMExecutive.getInstance().getMessageClass();
                    int remainDataLength = (int)messageClass.sEventmsgLoginResp.sMsgHdr.u32MsgLen - headerSize;
                    if (messageClass.sEventmsgHeader.eMsgType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_EVENT_NOTIFY){
                        Log.d(TAG, "run: ring: " + String.valueOf(remainDataLength+headerSize) );
                        String uuid = new String(messageClass.sEventmsgEventNotify.szUUID);
                        List<DeviceData> list = DeviceData.findWithQuery(DeviceData.class, "uuid = ?", uuid);
                        switch (list.size()){
                            case 0:
                                Log.d(TAG, "run: no such uuid in DeviceData ORM");
                                break;
                            case 1:
                                Log.d(TAG, "run: ring form uuid: " + uuid);
                                DeviceData deviceData = list.get(0);
                                broadcastInterface.signalDataHandled(deviceData);
                                break;
                            default:
                                Log.d(TAG, "run: multiple SQL entries!!");
                                break;
                        }
                    }else if(messageClass.sEventmsgHeader.eMsgType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_LOGIN){
                        Log.d(TAG, "run: login: " + String.valueOf(remainDataLength+headerSize) );
                        tempByte = new byte[remainDataLength];
                        System.arraycopy(buffer, 8, tempByte, 0, remainDataLength);
                        FCMExecutive.getInstance().setupRemainRequestData(tempByte);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private int determineMessageLength(EventMessageClass eventMessageClass){
        int length = 0;
        long messageType = eventMessageClass.sEventmsgHeader.eMsgType;
        if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_EVENT_NOTIFY){
            length = eventMessageClass.sEventmsgEventNotify.messageLength();
        }else if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_EVENT_NOTIFY_RESP){
            length = eventMessageClass.sEventmsgEventNotifyResp.messageLength();
        }else if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_LOGIN){
            length = eventMessageClass.sEventmsgLoginReq.messageLength();
        }else if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_LOGIN_RESP){
            length = eventMessageClass.sEventmsgLoginResp.messageLength();
        }else if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_FW_DOWNLOAD){
            length = eventMessageClass.sEventmsgFwDownload.messageLength();
        }else if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_FW_DOWNLOAD_RESP){
            length = eventMessageClass.sEventmsgFwDownloadResp.messageLength();
        }else if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_GET_FW_VER){
            length = eventMessageClass.sEventmsgGetFwVer.messageLength();
        }else if (messageType == EventMessageClass.E_EVENTMSG_TYPE.eEVENTMSG_GET_FW_VER_RESP){
            length = eventMessageClass.sEventmsgGetFwVerResp.messageLength();
        }
        return length;
    }

    public void setBroadcastInterface(BroadcastInterface broadcastInterface) {
        this.broadcastInterface = broadcastInterface;
    }
}
