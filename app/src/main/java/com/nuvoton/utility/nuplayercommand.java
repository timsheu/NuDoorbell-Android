package com.nuvoton.utility;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by cchsu20 on 06/01/2017.
 */

public class NuPlayerCommand {
    public static String setResolution(String type, int value, int width, int height){
        String command = "/server.command?command=set_resol&pipe=0";
        command = command + "&type=" + type;
        command = command + "&value=" + String.valueOf(value);
        command = command + "&width=" + String.valueOf(width);
        command = command + "&height=" + String.valueOf(height);
        return command;
    }

    public static String setResolution(String type, int value){
        String command = "/server.command?command=set_resol&pipe=0";
        command = command + "&type=" + type;
        command = command + "&value=" + String.valueOf(value);
        return command;
    }

    public static String getResolution(String type){
        String command = "/server.command?command=get_resol&pipe=0";
        command = command + "&type=" + type;
        return command;
    }

    public static String setEncodeQuality(String type, int value){
        String command = "/server.command?command=set_enc_quality&pipe=0";
        command = command + "&type=" + type;
        command = command + "&value=" + String.valueOf(value);
        return command;
    }

    public static String getEncodeQuality(String type){
        String command = "/server.command?command=get_enc_quality&pipe=0";
        command = command + "&type=" + type;
        return command;
    }

    public static String setEncodeBitrate(String type, int value){
        String command = "/server.command?command=set_enc_bitrate&pipe=0";
        command = command + "&type=" + type;
        command = command + "&value=" + String.valueOf(value);
        return command;
    }

    public static String getEncodeBitrate(String type){
        String command = "/server.command?command=get_enc_bitrate&pipe=0";
        command = command + "&type=" + type;
        return command;
    }

    public static String setFps(String type, int value){
        String command = "/server.command?command=set_max_fps&pipe=0";
        command = command + "&type=" + type;
        command = command + "&value=" + String.valueOf(value);
        return command;
    }

    public static String getFps(String type){
        String command = "/server.command?command=get_max_fps&pipe=0";
        command = command + "&type=" + type;
        return command;
    }

    public static String mute(boolean isMute){
        String command = "/server.command?command=enable_mute";
        if (isMute){
            command += "&value=1";
        }else {
            command += "&value=0";
        }
        return command;
    }

    public static String checkStorage(){
        String command = "/GetStorageCapacity.ncgi";
        return command;
    }

    public static String snapshot(){
        String command = "/server.command?command=snapshot&pipe=0";
        return command;
    }

    public static String recorderStatus(String type){
        String command = "/server.command?command=is_pipe_record&pipe=0";
        command = command + "&type=" + type;
        return command;
    }

    public static String startRecorder(String type){
        String command = "/server.command?command=start_record_pipe&pipe=0";
        command = command + "&type=" + type;
        return command;
    }

    public static String stopRecorder(String type){
        String command = "/server.command?command=stop_record_pipe&pipe=0";
        command = command + "&type=" + type;
        return command;
    }

    public static String listWifiParameters(){
        String command = "/param.cgi?action=list&group=wifi";
        return command;
    }

    public static String updateWifiParameters(String apSsid, String apPassword){
        String command = "/param.cgi?action=update&group=wifi";
        command = command + "&AP_SSID=" + apSsid + "&AP_AUTH_KEY=" + apPassword;
        return command;
    }

    public static String updatePluginParameters(ArrayList<Map<String, String>> parameters){
        String command = "/param.cgi?action=update&group=plugin";
        for (Map<String, String> p: parameters) {
            String name = p.get("name");
            String param = p.get("param");
            String value = p.get("param");
            command = command + "&name=" + name + " &param=" + param + "&value=" + value;
        }
        return command;
    }

    public static String listFile(){
        String command = "/param.cgi?action=list&group=file";
        return command;
    }

    public static String reboot(){
        String command = "/restart.cgi";
        return command;
    }

    public static String firmwareUpdate(){
        String command = "/firmwareupgrade.cgi";
        return command;
    }
}
