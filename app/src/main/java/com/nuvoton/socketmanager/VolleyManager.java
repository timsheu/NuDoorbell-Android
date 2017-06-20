package com.nuvoton.socketmanager;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by cchsu20 on 20/06/2017.
 */

public class VolleyManager {
    public RequestQueue mQueue;

    private static VolleyManager shared = new VolleyManager();
    private VolleyManager (){}

    public static VolleyManager getShared(Context context){
        if (shared.mQueue == null){
            shared.mQueue = Volley.newRequestQueue(context);
        }
        return shared;
    }
}
