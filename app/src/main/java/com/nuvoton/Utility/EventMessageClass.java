package com.nuvoton.utility;

import android.app.usage.UsageEvents;

/**
 * Created by timsheu on 8/30/16.
 */

public class EventMessageClass {
    private static final String TAG = "EventMessageClass";
    public static S_EVENTMSG_HEADER sEventmsgHeader;
    public S_EVENTMSG_LOGIN_RESP sEventmsgLoginResp;
    public S_EVENTMSG_LOGIN_REQ sEventmsgLoginReq;
    public S_EVENTMSG_EVENT_NOTIFY sEventmsgEventNotify;
    public S_EVENTMSG_EVENT_NOTIFY_RESP sEventmsgEventNotifyResp;
    public S_EVENTMSG_GET_FW_VER sEventmsgGetFwVer;
    public S_EVENTMSG_GET_FW_VER_RESP sEventmsgGetFwVerResp;
    public S_EVENTMSG_FW_DOWNLOAD_RESP sEventmsgFwDownloadResp;
    public S_EVENTMSG_FW_DOWNLOAD sEventmsgFwDownload;


    public String TEST_UUID = "00000001";
    public enum E_EVENTMSG_RET_CODE{
        eEVENTMSG_RET_SUCCESS(0),
        eEVENTMSG_RET_UUID_INVALID(-1),
        eEVENTMSG_RET_ACCESS_LIMITED(-2),
        eEVENTMSG_RET_FW_VER_UNKNOWN(-3),
        eEVENTMSG_RET_FW_DOWNLOAD_FAIL(-4);

        private long value;
        E_EVENTMSG_RET_CODE(long value){
            this.value = value;
        }
        public long getRetCode(){
            return this.value;
        }
    }

    public enum E_EVENTMSG_ROLE{
        eEVENTMSG_ROLE_DEVICE(0),			//Ex: IP camera, Doorbell
        eEVENTMSG_ROLE_USER(1);			//Ex: mobile phone
        private long value;
        E_EVENTMSG_ROLE(long value){
            this.value = value;
        }
        public long getRole(){
            return this.value;
        }
    }

    public enum E_EVENT_TYPE{
        eEVENT_MOTION_DET(0),			//Ex: IP camera, Doorbell
        eEVENT_RING(1),           		//Ex: mobile phone
        eEVENT_BATTERY_LOW(2);
        private long value;
        E_EVENT_TYPE(long value){
            this.value = value;
        }
        public long getEventType(){
            return this.value;
        }
    }

    public enum E_FW_DOWNLOAD_TYPE{
        eFW_DOWNLOAD_TYPE_NATIVE(0),			//NuDoorbell
        eFW_DOWNLOAD_TYPE_WIFI(1),				//RAK439
        eFW_DOWNLOAD_TYPE_NANO(2),				//NANO
        eFW_DOWNLOAD_TYPE_CNT(3);
        private long value;
        E_FW_DOWNLOAD_TYPE(long value){
            this.value = value;
        }
        public long getFirmwareDownloadType(){
            return this.value;
        }
    }

    public EventMessageClass(){
        sEventmsgHeader = new S_EVENTMSG_HEADER();

        sEventmsgLoginResp = new S_EVENTMSG_LOGIN_RESP();

        sEventmsgLoginReq = new S_EVENTMSG_LOGIN_REQ();
        sEventmsgLoginReq.sMsgHdr.eMsgType = E_EVENTMSG_TYPE.eEVENTMSG_LOGIN;
        sEventmsgLoginReq.sMsgHdr.u32MsgLen = 344;
        sEventmsgLoginReq.szUUID = TEST_UUID.toCharArray();

        sEventmsgEventNotify = new S_EVENTMSG_EVENT_NOTIFY();
        sEventmsgEventNotify.sMsgHdr.eMsgType = E_EVENTMSG_TYPE.eEVENTMSG_EVENT_NOTIFY;
        sEventmsgEventNotify.sMsgHdr.u32MsgLen = 85;
        sEventmsgEventNotify.szUUID = TEST_UUID.toCharArray();

        sEventmsgEventNotifyResp = new S_EVENTMSG_EVENT_NOTIFY_RESP();
        sEventmsgEventNotifyResp.sMsgHdr.eMsgType = E_EVENTMSG_TYPE.eEVENTMSG_EVENT_NOTIFY_RESP;
        sEventmsgEventNotifyResp.sMsgHdr.u32MsgLen = 16;

        sEventmsgGetFwVer = new S_EVENTMSG_GET_FW_VER();
        sEventmsgGetFwVer.sMsgHdr.eMsgType = E_EVENTMSG_TYPE.eEVENTMSG_GET_FW_VER;
        sEventmsgGetFwVer.sMsgHdr.u32MsgLen = 81;

        sEventmsgGetFwVerResp = new S_EVENTMSG_GET_FW_VER_RESP();
        sEventmsgGetFwVerResp.sMsgHdr.eMsgType = E_EVENTMSG_TYPE.eEVENTMSG_GET_FW_VER;
        sEventmsgGetFwVer.sMsgHdr.u32MsgLen = 20;

        sEventmsgFwDownload = new S_EVENTMSG_FW_DOWNLOAD();
        sEventmsgFwDownload.sMsgHdr.eMsgType = E_EVENTMSG_TYPE.eEVENTMSG_FW_DOWNLOAD;
        sEventmsgFwDownload.sMsgHdr.u32MsgLen = 81;

        sEventmsgFwDownloadResp = new S_EVENTMSG_FW_DOWNLOAD_RESP();
        sEventmsgFwDownloadResp.sMsgHdr.eMsgType = E_EVENTMSG_TYPE.eEVENTMSG_FW_DOWNLOAD_RESP;
        sEventmsgFwDownloadResp.sMsgHdr.u32MsgLen = 20;

    }
    public static final int DEVICE_UUID_LEN = 64;
    public static final int CM_REGID_LEN    = 254;
    private static final long SIGNATURE_WORD = 0x525566;

    public static final class E_EVENTMSG_TYPE{
        public static int
                    eEVENTMSG_LOGIN                 = 0x0100,		//Device/Client login message
                    eEVENTMSG_LOGIN_RESP            = 0x0101,		//Device/Client login sEventmsgLoginResp message

        //Device message
                    eEVENTMSG_EVENT_NOTIFY			= 0x0200,		//Device event notify message
                    eEVENTMSG_EVENT_NOTIFY_RESP		= 0x0201,   	//Device event notify sEventmsgLoginResp message

        //Client message	start from 0x0300
                    eEVENTMSG_GET_FW_VER			= 0x0300,		//Client get firmware version number sEventmsgLoginReq
                    eEVENTMSG_GET_FW_VER_RESP		= 0x0301,		//Client get firmware version number sEventmsgLoginResp

                    eEVENTMSG_FW_DOWNLOAD			= 0x0302,		//Client firmware download sEventmsgLoginReq
                    eEVENTMSG_FW_DOWNLOAD_RESP		= 0x0303		//Client firmware download sEventmsgLoginResp

        ;
    }

    public class S_EVENTMSG_HEADER{ // 12 bytes
        private final int SIZE = 12;
        public long u32SignWord;
        public long eMsgType;				//E_EVENTMSG_TYPE
        public long u32MsgLen;			//include message header length
        public S_EVENTMSG_HEADER(){

        }
        public int messageLength(){
            return SIZE;
        }
    }

    public class S_EVENTMSG_LOGIN_REQ{
        private final int SIZE = 348;
        public S_EVENTMSG_HEADER sMsgHdr; //12 bytes
        public char[] szUUID = new char[DEVICE_UUID_LEN + 1]; //64+1 bytes
        public long eRole;							//E_EVENTMSG_ROLE, 4 bytes
        public char[] szCloudRegID = new char[CM_REGID_LEN + 1];		// Used by client login sEventmsgLoginReq, 255 bytes
        public byte[] u32DevPrivateIP;					// Used by device. Device private IP address. 4 bytes
        public long u32DevHTTPPort;					// Used by device. Device http service port. 4 bytes
        public long u32DevRTSPPort;
        S_EVENTMSG_LOGIN_REQ(){
            sMsgHdr = EventMessageClass.sEventmsgHeader;
        }
        public int messageLength(){
            return SIZE;
        }
    }

    public class S_EVENTMSG_LOGIN_RESP{
        private final int SIZE = 36;
        public S_EVENTMSG_HEADER sMsgHdr;
        public long eResult;					// E_EVENTMSG_RET_CODE. eEVENTMSG_RET_SUCCESS: success; otherwise: failed.
        public long bDevOnline;				// BOOL. Report to client. Device online or not.
        public byte[] u32DevPublicIP;			// Report to client. Device public IP address.
        public byte[] u32DevPrivateIP;			// Report to client. Device private IP address.
        public long u32DevHTTPPort;			// Report to client. Device http service port.
        public long u32DevRTSPPort;			// Report to client. Device rtsp service port.
        S_EVENTMSG_LOGIN_RESP(){
            sMsgHdr = EventMessageClass.sEventmsgHeader;
        }
        public int messageLength(){
            return SIZE;
        }
    }

    public class S_EVENTMSG_EVENT_NOTIFY{
        private final int SIZE = 85;
        public S_EVENTMSG_HEADER sMsgHdr;
        public char[] szUUID = new char[DEVICE_UUID_LEN + 1]; //64+1 bytes
        public long eEventType;
        public long u32EventSeqNo;
        S_EVENTMSG_EVENT_NOTIFY(){
            sMsgHdr = EventMessageClass.sEventmsgHeader;
        }
        public int messageLength(){
            return SIZE;
        }
    }

    public class S_EVENTMSG_EVENT_NOTIFY_RESP{
        private final int SIZE = 16;
        public S_EVENTMSG_HEADER sMsgHdr;
        public long eResult;
        S_EVENTMSG_EVENT_NOTIFY_RESP(){
            sMsgHdr = EventMessageClass.sEventmsgHeader;
        }
        public int messageLength(){
            return SIZE;
        }
    }

    public class S_EVENTMSG_GET_FW_VER{
        private final int SIZE = 81;
        public S_EVENTMSG_HEADER sMsgHdr;
        public char[] szUUID = new char[DEVICE_UUID_LEN + 1]; //64+1 bytes
        public long eFirmwareType;
        S_EVENTMSG_GET_FW_VER(){
            sMsgHdr = EventMessageClass.sEventmsgHeader;
        }
        public int messageLength(){
            return SIZE;
        }
    }

    public class S_EVENTMSG_GET_FW_VER_RESP{
        private final int SIZE = 20;
        public S_EVENTMSG_HEADER sMsgHdr;
        public long eResult;
        public long u32FWVer;
        S_EVENTMSG_GET_FW_VER_RESP(){
            sMsgHdr = EventMessageClass.sEventmsgHeader;
        }
        public int messageLength(){
            return SIZE;
        }
    }

    public class S_EVENTMSG_FW_DOWNLOAD{
        private final int SIZE = 81;
        public S_EVENTMSG_HEADER sMsgHdr;
        public char[] szUUID = new char[DEVICE_UUID_LEN + 1]; //64+1 bytes
        public long eFirmwareType;
        S_EVENTMSG_FW_DOWNLOAD(){
            sMsgHdr = EventMessageClass.sEventmsgHeader;
        }
        public int messageLength(){
            return SIZE;
        }
    }

    public class S_EVENTMSG_FW_DOWNLOAD_RESP{
        private final int SIZE = 20;
        public S_EVENTMSG_HEADER sMsgHdr;
        public long eResult;
        public long u32FWLen;
        S_EVENTMSG_FW_DOWNLOAD_RESP(){
            sMsgHdr = EventMessageClass.sEventmsgHeader;
        }
        public int messageLength(){
            return SIZE;
        }
    }

    @Override
    public String toString(){
        String returnValue = "Request header: " +
                String.valueOf(sEventmsgLoginReq.sMsgHdr.eMsgType) + ", " +
                String.valueOf(sEventmsgLoginReq.sMsgHdr.u32MsgLen) +
                "\nRequest content: " +
                String.valueOf(sEventmsgLoginReq.szUUID) + ", " +
                String.valueOf(sEventmsgLoginReq.szCloudRegID) + ", " +
                String.valueOf(sEventmsgLoginReq.eRole) + ", " +
                "\n Response header: " +
                String.valueOf(sEventmsgLoginResp.sMsgHdr.eMsgType) + ", " +
                String.valueOf(sEventmsgLoginResp.sMsgHdr.u32MsgLen) + ", " +
                "\n Response header: " +
                String.valueOf(sEventmsgLoginResp.eResult) + ", " +
                String.valueOf(sEventmsgLoginResp.bDevOnline) + ", " +
                String.valueOf(sEventmsgLoginResp.u32DevPublicIP) + ", " +
                String.valueOf(sEventmsgLoginResp.u32DevPrivateIP) + ", " +
                String.valueOf(sEventmsgLoginResp.u32DevHTTPPort) + ", " +
                String.valueOf(sEventmsgLoginResp.u32DevRTSPPort)
                ;
        return returnValue;
    }
}
