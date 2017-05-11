package com.nuvoton.socketmanager;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Timer;

/**
 * Created by timsheu on 6/3/16.
 */
public class HTTPSocketManager {
    public enum HTTPSocketTags {
        PLUGIN_LIST_VIDEO_IN_PARAM(10),
        PLUGIN_LIST_NETWORK_PARAM(11),
        UPDATE_VIDEO_RESOLUTION(20),
        UPDATE_VIDEO_FLICKER(21),
        UPDATE_VIDEO_BITRATE(22),
        UPLOAD_AUDIO_STREAM(30),
        UPDATE_WIFI_SSID(40),
        UPDATE_WIFI_PASSWORD(41),
        RESTART(90),
        DEFAULT(100);

        private int value;
        private String valueString;
        HTTPSocketTags(int value){
            this.value = value;
            this.valueString = String.valueOf(value);
        }
        public int getValue() {
            return this.value;
        }

        public String getString(){
            return this.valueString;
        }

    }
    private Map<String, String> paramters;
    private byte[] dataContent;
    private ArrayList<String> commandList;

    public void setHttpSocketInterface(HTTPSocketInterface httpSocketInterface) {
        this.httpSocketInterface = httpSocketInterface;
    }

    public void setTwoWayTalking(boolean twoWayTalking) {
        isTwoWayTalking = twoWayTalking;
    }

    private boolean isTwoWayTalking = false;
    private HTTPSocketInterface httpSocketInterface = null;
    private URL url;
    private static final String TAG = "HTTPSocketManager";

    Timer timer = new Timer();

    private InputStream OpenHttpConnection(String urlString) throws IOException
    {
        InputStream in =null;
        int response = -1;

        url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if(!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");
        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setConnectTimeout(5000);
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();
            if(response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        } catch (Exception ex) {
            httpSocketInterface.didDisconnected();
            //Log.d("Networking", ex.getLocalizedMessage());
            ex.printStackTrace();
            throw new IOException("Error connceting");
        }
        return  in;
    }

    private String SendGet(String url)
    {
        try {
            Log.d(TAG, "SendGet: " + url);
            InputStream In = OpenHttpConnection(url);
            if (In == null){
                return null;
            }
            InputStreamReader isr = new InputStreamReader(In);
            int count = 0;
            int charRead;
            String result = "";
            char[] buf = new char[64];
            try{
                while((charRead = isr.read(buf)) > 0){
                    String readString = String.copyValueOf(buf,0,charRead);
                    result += readString;
                    buf = new char[64];
                }
            }catch (IOException e){
                Log.d(TAG,e.getLocalizedMessage());
            }
            Log.d(TAG, "Response Content from server: " + result);
            In.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private class SendGetTask extends AsyncTask<String,Void,String> {
        int tag = 0;
        @Override
        protected String doInBackground(String... params) {
            String result= SendGet(params[0]);
            tag = Integer.valueOf(params[1]);
            return result;
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                Map<String, Object> map = toMap(jsonObject);
                map.put("tag", tag);
                httpSocketInterface.httpSocketResponse(map);
                httpSocketInterface.didDisconnected();
                if (isTwoWayTalking) {
                    httpSocketInterface.voiceConnectionOpened();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void executeSendGetTask(String command, String tag){
        new SendGetTask().execute(command, tag);
    }


    public void executeSendGetTaskList(ArrayList<String> list, String commandType){
        new SendGetTask().execute(list.get(0), commandType);
    }

    public void setCommandList(ArrayList<String> list){
        Log.d(TAG, "setCommandList: " + list.toString());
        commandList = list;
    }

    public void setParameters(Map<String, String> parameters) {
        this.paramters = parameters;
    }


    private static Map<String, Object> toMap(JSONObject jsonObject) throws JSONException{
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()){
            String key = keys.next();
            Object value = jsonObject.get(key);

            if (value instanceof JSONArray){
                value = toList((JSONArray) value);
            }else if(value instanceof JSONObject){
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    private static List<Object> toList(JSONArray array) throws JSONException{
        List<Object> list = new ArrayList<>();
        for (int i=0; i<array.length(); i++){
            Object value = array.get(i);
            if (value instanceof JSONArray){
                value = toList((JSONArray) value);
            }else if (value instanceof JSONObject){
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    public void setSocketInterface(HTTPSocketInterface socketInterface){
        this.httpSocketInterface = socketInterface;
    }
}
