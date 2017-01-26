package com.nuvoton.utility;

/**
 * Created by cchsu20 on 09/01/2017.
 */

public class NuDoorbellCommand {
    public static String setResolution(String value){
        String command = "/server.command?command=set_resol&pipe=0&type=h264";
        command = command + "&value=" + value;
        return command;
    }

    public static String setEncodeBitrate(String value){
        String command = "/server.command?command=set_enc_bitrate&pipe=0&type=h264";
        command = command + "&value=" + value;
        return command;
    }

    public static String startRecordPipe(){
        String command = "/server.command?command=start_record_pipe&pipe=0&type=h264";
        return command;
    }

    public static String isPipeRecord(){
        String command = "/server.command?command=is_pipe_record&pipe=0&type=h264";
        return command;
    }

    public static String stopRecordPipe(){
        String command = "/server.command?command=stop_record&pipe=0&type=h264";
        return command;
    }

    public static String snapshot(){
        String command = "/server.command?command=snapshot&pipe=0";
        return command;
    }

    public static String enableMute(boolean isMute){
        String command = "/server.command?command=enable_mute&value=";
        if (isMute){
            command += "1";
        }else {
            command += "0";
        }
        return command;
    }

    public static String wifiSetup(String apSsid, String apPassword){
        String command = "/server.command?command=wifi_setup";
        command = command + "&ssid=" + apSsid + "&password=" + apPassword;
        return command;
    }

    public static String restoreFactory(){
        String command = "/server.command?command=restore_factory";
        return command;
    }

    public static String restart(){
        String command = "/server.command?command=restart";
        return command;
    }
}
