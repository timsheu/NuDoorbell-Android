package com.nuvoton.socketmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.longevitysoft.android.xml.plist.*;
import com.longevitysoft.android.xml.plist.domain.aArray;
import com.longevitysoft.android.xml.plist.domain.Dict;
import com.longevitysoft.android.xml.plist.domain.PList;
import com.longevitysoft.android.xml.plist.domain.PListObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by timsheu on 6/3/16.
 */
public class ReadConfigure {
    private static Context contextLocal;
    private static final String TAG = "ReadConfigure";
    private static ReadConfigure readConfigure = new ReadConfigure();

    private static final String configCommand    = "ConfigCommandPropertyList.plist";
    private static final String systemCommand    = "SystemCommandPropertyList.plist";
    private static final String videoCommand     = "VideoCommandPropertyList.plist";

    public static ArrayList<Map> configCommandSet, systemCommandSet, videoCommandSet;

    private ReadConfigure(){
        Log.d(TAG, "ReadConfigure: create");
    }
    public static ReadConfigure getInstance(Context context, Boolean clear){
        Log.d(TAG, "getInstance: ");
        contextLocal = context;
//        for (int i=0; i<5; i++){
        int i = 5; // hard coded for NuWicam Player
            if (isSharedpreferenceCreated(i) == false) {
                initSharedPreference(i, clear);
            }
//        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = contextLocal.getAssets().open(configCommand);
                    configCommandSet = parsePropertyList(inputStream);
                    if (inputStream != null) { inputStream.close(); }

                    inputStream = contextLocal.getAssets().open(systemCommand);
                    systemCommandSet = parsePropertyList(inputStream);
                    if (inputStream != null) { inputStream.close(); }

                    inputStream = contextLocal.getAssets().open(videoCommand);
                    videoCommandSet = parsePropertyList(inputStream);
                    if (inputStream != null) { inputStream.close(); }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).run();

        return readConfigure;
    }

    private void copyFile(InputStream inputStream, File dest) throws IOException{
        FileOutputStream outputStream = new FileOutputStream(dest);
        try {
            byte[] bytes = new byte[1024];
            int read;
            while((read = inputStream.read(bytes)) != -1){
                outputStream.write(bytes, 0, read);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (inputStream != null){
                try{
                    inputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if (outputStream != null){
                try {
                    outputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private static ArrayList<Map> parsePropertyList(InputStream input){
        PListXMLParser parser = new PListXMLParser();
        PListXMLHandler handler = new PListXMLHandler();
        parser.setHandler(handler);
        ArrayList<Map> list = new ArrayList<Map>();
        try {
            parser.parse(input);
        }catch (IOException e){
            e.printStackTrace();
        }catch (IllegalStateException e){
            e.printStackTrace();
        }
        PList plist = ((PListXMLHandler) parser.getHandler()).getPlist();
        aArray commandSetAArray = (aArray) plist.getRootElement(); // Root array
        for (int i = 0; i < commandSetAArray.size() ; i++) { // parse every NSDictionary
            Dict commandSet = (Dict) commandSetAArray.get(i); // NSDictionary at index "i"
            Map<String, PListObject> commands = commandSet.getConfigMap(); // Convert NSDictionary to Java Map
            list.add(commands);
        }
        return list;
    }

    public static void initSharedPreference(int cameraSerial, boolean clear){
        String preferenceName = "Setup Camera " + String.valueOf(cameraSerial);
        Log.d(TAG, "initSharedPreference: " + preferenceName);
        SharedPreferences preferences = contextLocal.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (clear){
            editor.clear();
        }
        editor.putString("Version", "1.1.6");
        editor.putBoolean("first created", true);
        editor.putString("Adaptive", "0");
        editor.putString("Fixed Quality", "0");
        editor.putString("Fixed Bit Rate", "0");
        editor.putBoolean("Transmission", false);
        editor.putString("Mute", "No");
        editor.putString("Available Storage", "No");
        editor.putBoolean("Recorder Status", true);
        editor.putString("Serial", String.valueOf(cameraSerial));
        editor.putString("Resolution", "0");
        editor.putString("Encode Quality", "0");
        editor.putString("Bit Rate", "4096");
        editor.putString("FPS", "30");
        editor.putBoolean("Device Mic", true);
        editor.putString("SSID", "NuWicam");
        editor.putString("Password", "12345678");
        editor.putString("Send Report", "1");
        if (cameraSerial == 0 || cameraSerial == 1){ // DVR and local IP
            editor.putString("Name", "LOCAL-IP");
            editor.putString("URL", "rtsp://192.168.100.1/cam1/h264");
            editor.putString("History 0", "rtsp://192.168.100.1/cam1/h264");
            editor.putString("History 1", "rtsp://192.168.100.1/cam1/mpeg4");
        }else if (cameraSerial == 2){ //
            editor.putString("Name", "DEMO-IP");
            editor.putString("URL", "rtsp://114.35.206.240/cam1/h264");
            editor.putString("History 0", "rtsp://114.35.206.240/cam1/h264");
            editor.putString("History 1", "rtsp://114.35.206.240/cam1/mpeg4");
        }else if (cameraSerial == 3){
            editor.putString("Name", "DEMO-NO-IP");
            editor.putString("URL", "rtsp://nuvoton.no-ip.biz/cam1/h264");
            editor.putString("History 0", "rtsp://nuvoton.no-ip.biz/cam1/h264");
            editor.putString("History 1", "rtsp://nuvoton.no-ip.biz/cam1/mpeg4");
        }else if (cameraSerial == 4){
            editor.putString("Name", "DEMO-NO-IP");
            editor.putString("URL", "rtsp://nuvoton.no-ip.biz/cam1/h264");
            editor.putString("History 0", "rtsp://nuvoton.no-ip.biz/cam1/h264");
            editor.putString("History 1", "rtsp://nuvoton.no-ip.biz/cam1/mpeg4");
        }else if (cameraSerial == 5){
            editor.putString("Name", "LOCAL-IP");
            editor.putString("URL", "rtsp://192.168.100.1/cam1/mpeg4");
            editor.putString("History 0", "rtsp://192.168.100.1/cam1/mpeg4");
        }
        editor.commit();
    }

    private static boolean isSharedpreferenceCreated(int cameraSerial){
        String preferenceName = "Setup Camera " + String.valueOf(cameraSerial);
        Log.d(TAG, "initSharedPreference: " + preferenceName);
        SharedPreferences preferences = contextLocal.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        if (preferences.getBoolean("first created", false)){
            return true;
        }else {
            return false;
        }
    }

}
