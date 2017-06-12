package com.nuvoton.utility;

/**
 * Created by cchsu20 on 09/01/2017.
 */

public class NuDoorbellCommand {
    public static String setResolution(String resolution){
        String command = "/server.command?command=set_resol&pipe=0&type=h264";
        String value;
        if (resolution.compareTo("1080p") == 0){
            value = "3";
        }else if (resolution.compareTo("720p") == 0){
            value = "2";
        }else if (resolution.compareTo("VGA") == 0){
            value = "1";
        }else{
            value = "0";
        }
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

    public static String updateFlicker(String type){
        String command = "/param.cgi?action=update&group=plugin&name=video_in&param=port0_flicker";
        String hzValue = "50";
        if (type.compareTo("NTSC") == 0){
            hzValue = "60";
        }
        command = command + "&value=" + hzValue;
        return command;
    }

    public static String listVideoInputParameters(){
        String command = "/param.cgi?action=list&group=plugin&name=video_in";
        return command;
    }

    public static String listNetworkParameters(){
        String command = "/param.cgi?action=list&group=plugin&name=network";
        return command;
    }

    public static String setAEWindow(int startX, int endX, int startY, int endY){
        String command = "/server.command?command=set_sensor_ae_window&pipe=0" +
                "&start_x=" + startX + "&end_x=" + endX +
                "&start_y=" + startY + "&end_y=" + endY ;
        return command;
    }
}
