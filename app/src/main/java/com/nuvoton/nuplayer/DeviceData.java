package com.nuvoton.nuplayer;

import com.orm.SugarRecord;

/**
 * Created by cchsu20 on 16/12/2016.
 */

/*
 DeviceData extends SugarRecord which is an ORM implementation to SQLite.

 New:
    DeviceData data = new DeviceData(...)
    data.save();

 Load:
    DeviceData data = DeviceData.findById(DeviceData.class, serial)

 Update:
    DeviceData data = DeviceData.findById(DeviceData.class, serial)
    data.deviceType = "NuWicam"
    data.int = 8080
    data.save()

 Delete:
    DeviceData data = ...
    data.delete()

 Bulk operation:
    List<DeviceData> devices = DeviceData.listAll(DeviceData.class) --- to get device list
    devices.deleteAll(DeviceData.class) --- to delete all entries
 */

public class DeviceData extends SugarRecord{
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    String uuid;                        // Device UUID for recognition
    String name;                        // Device custom name
    String deviceType;                  // NuDoorbell, SkyEye, NuWiCam
    String publicIP;                    // public ip of the device
    String privateIP;                   // private ip of the device
    int httpPort;                       // http port of the device
    int rtspPort;                       // rtsp port of the device
    String fcmToken;                    // Token was sent from Fire Base Messaging server
    Boolean isVoiceUploadHttp;          // True: HTTP, false: tcp socket
    Boolean isAdaptive;                 // True: In adaptive mode, false: not in adaptive mode
    Boolean isFixedQuality;             // True: In fixed quality mode, false: not in fixed quality mode
    Boolean isFixedBitrate;             // True: In fixed bit rate mode, false: not in fixed bitrate mode
    Boolean isTCPTransmission;          // True: RTSP with TCP, false: RTSP with UDP
    Boolean isMute;                     // True: device is muted, false: device is not muted
    Boolean isStorageAvailable;         // True: device has storage, false: device has not enough storage
    Boolean isRecorderOn;               // True: device recorder is on, false: device recorder is off
    Boolean isPAL;                      // True: flicker is PAL(50Hz), false: flicker is NTSC(60Hz)
    String resolution;                  // Device resolution: QVGA, VGA, 360p, 720p
    int encodeQuality;                  // Device encode quality, range: 1-52
    int bitRate;                        // Device bit rate, range: 1000-8000
    int fps;                            // Device frame rate per second: 1-30
    String ssid;                        // Device SSID in soft AP mode
    String password;                    // Device password in soft AP mode
    String history1;                    // Device history list
    String history2;                    // Device history list
    String history3;                    // Device history list
    String history4;                    // Device history list
    String history5;                    // Device history list

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getPublicIP() {
        return publicIP;
    }

    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }

    public String getPrivateIP() {
        return privateIP;
    }

    public void setPrivateIP(String privateIP) {
        this.privateIP = privateIP;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getRtspPort() {
        return rtspPort;
    }

    public void setRtspPort(int rtspPort) {
        this.rtspPort = rtspPort;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Boolean getVoiceUploadHttp() {
        return isVoiceUploadHttp;
    }

    public void setVoiceUploadHttp(Boolean voiceUploadHttp) {
        isVoiceUploadHttp = voiceUploadHttp;
    }

    public Boolean getAdaptive() {
        return isAdaptive;
    }

    public void setAdaptive(Boolean adaptive) {
        isAdaptive = adaptive;
    }

    public Boolean getFixedQuality() {
        return isFixedQuality;
    }

    public void setFixedQuality(Boolean fixedQuality) {
        isFixedQuality = fixedQuality;
    }

    public Boolean getFixedBitrate() {
        return isFixedBitrate;
    }

    public void setFixedBitrate(Boolean fixedBitrate) {
        isFixedBitrate = fixedBitrate;
    }

    public Boolean getTCPTransmission() {
        return isTCPTransmission;
    }

    public void setTCPTransmission(Boolean TCPTransmission) {
        isTCPTransmission = TCPTransmission;
    }

    public Boolean getMute() {
        return isMute;
    }

    public void setMute(Boolean mute) {
        isMute = mute;
    }

    public Boolean getStorageAvailable() {
        return isStorageAvailable;
    }

    public void setStorageAvailable(Boolean storageAvailable) {
        isStorageAvailable = storageAvailable;
    }

    public Boolean getRecorderOn() {
        return isRecorderOn;
    }

    public void setRecorderOn(Boolean recorderOn) {
        isRecorderOn = recorderOn;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public int getEncodeQuality() {
        return encodeQuality;
    }

    public void setEncodeQuality(int encodeQuality) {
        this.encodeQuality = encodeQuality;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHistory1() {
        return history1;
    }

    public void setHistory1(String history1) {
        this.history1 = history1;
    }

    public String getHistory2() {
        return history2;
    }

    public void setHistory2(String history2) {
        this.history2 = history2;
    }

    public String getHistory3() {
        return history3;
    }

    public void setHistory3(String history3) {
        this.history3 = history3;
    }

    public String getHistory4() {
        return history4;
    }

    public void setHistory4(String history4) {
        this.history4 = history4;
    }

    public String getHistory5() {
        return history5;
    }

    public void setHistory5(String history5) {
        this.history5 = history5;
    }

    public void setIsPAL(Boolean isPAL){
        this.isPAL = isPAL;
    }

    public DeviceData(){
        uuid = "00000001";
        name = "SkyEye";
        deviceType = "SkyEye";
        publicIP = "192.168.100.1";
        privateIP = "192.168.100.1";
        httpPort = 80;
        rtspPort = 554;
        fcmToken = "";
        isVoiceUploadHttp = false;
        isAdaptive = false;
        isFixedQuality = false;
        isFixedBitrate = false;
        isTCPTransmission = true;
        isMute = false;
        isStorageAvailable = false;
        isRecorderOn = false;
        isPAL = false;
        resolution = "QVGA";
        encodeQuality = 30;
        bitRate = 1024;
        fps = 20;
        ssid = "SkyEye";
        password = "12345678";
        history1 = "192.168.100.1";
        history2 = "";
        history3 = "";
        history4 = "";
        history5 = "";
    }

    public DeviceData(String uuid, String name, String deviceType, String publicIP, String privateIP, int httpPort, int rtspPort, String fcmToken, Boolean isVoiceUploadHttp, Boolean isAdaptive, Boolean isFixedQuality, Boolean isFixedBitrate, Boolean isTCPTransmission, Boolean isMute, Boolean isStorageAvailable, Boolean isRecorderOn, String resolution, int encodeQuality, int bitRate, int fps, String ssid, String password, String history1, String history2, String history3, String history4, String history5) {
        this.uuid = uuid;
        this.deviceType = deviceType;
        this.publicIP = publicIP;
        this.privateIP = privateIP;
        this.httpPort = httpPort;
        this.rtspPort = rtspPort;
        this.fcmToken = fcmToken;
        this.isVoiceUploadHttp = isVoiceUploadHttp;
        this.isAdaptive = isAdaptive;
        this.isFixedQuality = isFixedQuality;
        this.isFixedBitrate = isFixedBitrate;
        this.isTCPTransmission = isTCPTransmission;
        this.isMute = isMute;
        this.isStorageAvailable = isStorageAvailable;
        this.isRecorderOn = isRecorderOn;
        this.resolution = resolution;
        this.encodeQuality = encodeQuality;
        this.bitRate = bitRate;
        this.fps = fps;
        this.ssid = ssid;
        this.password = password;
        this.history1 = history1;
        this.history2 = history2;
        this.history3 = history3;
        this.history4 = history4;
        this.history5 = history5;
        this.name = name;
    }
}
