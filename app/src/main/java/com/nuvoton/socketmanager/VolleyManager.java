package com.nuvoton.socketmanager;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by cchsu20 on 20/06/2017.
 */

public class VolleyManager {
    private final String TAG = "VolleyManager";
    private final int SOCKET_TIME_OUT_MS = 2500;
    private final int SOCKET_RETRY_TIMES = 2;
    public enum HTTPSocketTags {
        PLUGIN_LIST_VIDEO_IN_PARAM(10),
        PLUGIN_LIST_NETWORK_PARAM(11),
        UPDATE_VIDEO_RESOLUTION(20),
        UPDATE_VIDEO_FLICKER(21),
        UPDATE_VIDEO_BITRATE(22),
        UPDATE_VIDEO_AE_WINDOW(23),
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

        public static HTTPSocketTags fromInt(int i){
            for (HTTPSocketTags tags: HTTPSocketTags.values()){
                if (tags.getValue() == i){
                    return tags;
                }
            }
            return null;
        }
    }

    public RequestQueue mQueue;
    private HTTPSocketInterface httpSocketInterface = null;
    private static VolleyManager shared = new VolleyManager();
    private VolleyManager (){}

    public static VolleyManager getShared(Context context){
        if (shared.mQueue == null){
            shared.mQueue = Volley.newRequestQueue(context);
        }
        return shared;
    }

    public void sendCommand(String command, HTTPSocketTags tag){
        Log.d(TAG, "sendCommand: " + command);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(command, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (httpSocketInterface != null){
                    try {
                        Map<String, Object> map = toMap(response);
                        map.put("socketTag", tag);
                        if (httpSocketInterface != null){
                            httpSocketInterface.httpSocketResponse(map);
                            httpSocketInterface.didDisconnected();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.getMessage());
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        shared.mQueue.add(jsonObjectRequest);
    }

    private static Map<String, Object> toMap(JSONObject jsonObject) throws JSONException {
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

    public void setHttpSocketInterface(HTTPSocketInterface httpSocketInterface) {
        this.httpSocketInterface = httpSocketInterface;
    }
}
