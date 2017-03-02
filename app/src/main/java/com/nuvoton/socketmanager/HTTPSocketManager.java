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
import java.util.Timer;

/**
 * Created by timsheu on 6/3/16.
 */
public class HTTPSocketManager {
    public static final String CMDSET_TWOWAY="17";
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
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();
            if(response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        } catch (Exception ex) {
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
        @Override
        protected String doInBackground(String... params) {
            String result= SendGet(params[0]);
            return result;
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                Map<String, Object> map = toMap(jsonObject);
                httpSocketInterface.httpSocketResponse(map);
                if (isTwoWayTalking) {
                    httpSocketInterface.voiceConnectionOpened();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void executeSendGetTask(String command){
        new SendGetTask().execute(command);
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
