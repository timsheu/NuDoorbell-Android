package com.nuvoton.utility;

/**
 * Created by timsheu on 8/30/16.
 */

public class EventMessageClass {
    private static final String TAG = "EventMessageClass";
    public S_EVENTMSG_LOGIN_RESP response;
    public S_EVENTMSG_LOGIN_REQ request;
    public String TEST_UUID = "00000001";
    public enum E_EVENTMSG_RET_CODE{
        eEVENTMSG_RET_SUCCESS(0),
        eEVENTMSG_RET_UUID_INVALID(-1),
        eEVENTMSG_RET_ACCESS_LIMITED(-2);

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
        eEVENT_RING(1);			//Ex: mobile phone
        private long value;
        E_EVENT_TYPE(long value){
            this.value = value;
        }
        public long getEventType(){
            return this.value;
        }
    }

    public EventMessageClass(){
        response = new S_EVENTMSG_LOGIN_RESP();
        request = new S_EVENTMSG_LOGIN_REQ();
        request.sMsgHdr.eMsgType = E_EVENTMSG_TYPE.eEVENTMSG_LOGIN;
        request.sMsgHdr.u32MsgLen = 344;
        request.szUUID = TEST_UUID.toCharArray();
    }
    public static final int DEVICE_UUID_LEN = 64;
    public static final int CM_REGID_LEN    = 254;
    public static final class E_EVENTMSG_TYPE{
        static int
                    eEVENTMSG_LOGIN                 = 0x0100,		//Device/Client login message
                    eEVENTMSG_LOGIN_RESP            = 0x0101,		//Device/Client login response message

        //Device message
                    eEVENTMSG_EVENT_NOTIFY			= 0x0200,		//Device event notify message
                    eEVENTMSG_EVENT_NOTIFY_RESP		= 0x0201		//Device event notify response message

        //Client message	start from 0x0300

        ;
    }

    public class S_EVENTMSG_HEADER{
        public long eMsgType;				//E_EVENTMSG_TYPE
        public long u32MsgLen;			//include message header length
        public S_EVENTMSG_HEADER(){
        }
    }

    public class S_EVENTMSG_LOGIN_REQ{
        public S_EVENTMSG_HEADER sMsgHdr = new S_EVENTMSG_HEADER(); //8 bytes
        public char[] szUUID = new char[DEVICE_UUID_LEN + 1]; //64+1 bytes
        public long eRole;							//E_EVENTMSG_ROLE, 4 bytes
        public char[] szCloudRegID = new char[CM_REGID_LEN + 1];		// Used by client login request, 255 bytes
        public byte[] u32DevPrivateIP;					// Used by device. Device private IP address. 4 bytes
        public long u32DevHTTPPort;					// Used by device. Device http service port. 4 bytes
        public long u32DevRTSPPort;
        S_EVENTMSG_LOGIN_REQ(){

        }
    }

    public class S_EVENTMSG_LOGIN_RESP{
        public S_EVENTMSG_HEADER sMsgHdr = new S_EVENTMSG_HEADER();
        public long eResult;					// E_EVENTMSG_RET_CODE. eEVENTMSG_RET_SUCCESS: success; otherwise: failed.
        public long bDevOnline;				// BOOL. Report to client. Device online or not.
        public byte[] u32DevPublicIP;			// Report to client. Device public IP address.
        public byte[] u32DevPrivateIP;			// Report to client. Device private IP address.
        public long u32DevHTTPPort;			// Report to client. Device http service port.
        public long u32DevRTSPPort;			// Report to client. Device rtsp service port.
        S_EVENTMSG_LOGIN_RESP(){

        }

    }

    @Override
    public String toString(){
        String returnValue = "Request header: " +
                String.valueOf(request.sMsgHdr.eMsgType) + ", " +
                String.valueOf(request.sMsgHdr.u32MsgLen) +
                "\nRequest content: " +
                String.valueOf(request.szUUID) + ", " +
                String.valueOf(request.szCloudRegID) + ", " +
                String.valueOf(request.eRole) + ", " +
                "\n Response header: " +
                String.valueOf(response.sMsgHdr.eMsgType) + ", " +
                String.valueOf(response.sMsgHdr.u32MsgLen) + ", " +
                "\n Response header: " +
                String.valueOf(response.eResult) + ", " +
                String.valueOf(response.bDevOnline) + ", " +
                String.valueOf(response.u32DevPublicIP) + ", " +
                String.valueOf(response.u32DevPrivateIP) + ", " +
                String.valueOf(response.u32DevHTTPPort) + ", " +
                String.valueOf(response.u32DevRTSPPort)
                ;
        return returnValue;
    }
}
