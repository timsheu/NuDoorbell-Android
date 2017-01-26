package com.nuvoton.utility;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Created by cchsu20 on 09/01/2017.
 */

public class NuWicamCommand {
    public static String listWifiParamters(){
        String command = "/param.cgi?action=list&group=wifi";
        return command;
    }

    public static String updateWifiParameters(String apSsid, String apPassword){
        String command = "/param.cgi?action=update&group=wifi";
        command = command + "&AP_SSID=" + apSsid + "&AP_AUTH_KEY=" + apPassword;
        return command;
    }

    public static String listStreamParameters(){
        String command = "/param.cgi?action=list&group=stream";
        return command;
    }

    public static String updateStreamParameters(ArrayList<Map<String, String>> parameters){
        String command = "/param.cgi?action=update&group=stream";
        for (Map<String, String> p: parameters) {
            Set<String> keys = p.keySet();
            for (String key: keys) {
                String value = p.get(key);
                command = command + "&" + key + "=" + value;
            }
        }
        return command;
    }

    /*
        Restart Wi-Fi:
            category value: wifi
        Restart board:
            category value: board
        Restart stream:
            category value: Stream
     */
    public static String restart(String category){
        String command = "/restart.cgi?group=" + category;
        return command;
    }
}
