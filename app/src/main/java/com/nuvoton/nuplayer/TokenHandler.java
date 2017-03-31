package com.nuvoton.nuplayer;

import android.util.Log;

import java.util.List;

/**
 * Created by cchsu20 on 16/03/2017.
 */

public class TokenHandler {
    static String TAG = "TokenHandler";
    static public void setToken(String token){
        TokenHandler.initialTokenDatabase();
        try{
            TokenRecord.deleteAll(TokenRecord.class);
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "setToken: Table is not ready exception");
        }

        TokenRecord record = new TokenRecord(token, "FCMToken");
        Long recordID = record.save();
        Log.d(TAG, "setToken: ID: " + recordID);
    }

    static private void initialTokenDatabase(){
        List<TokenRecord> list;
        try{
            list = TokenRecord.listAll(TokenRecord.class);
            Log.d(TAG, "initialTokenDatabase: initialized, with data count: " + list.size());
        }catch (Exception e){
            Log.d(TAG, "initialTokenDatabase: failed, insert first data");
            TokenRecord tokenRecord = new TokenRecord("-1", "FCMToken");
            tokenRecord.save();
        }
    }

    static public String getToken(){
        TokenHandler.initialTokenDatabase();
        TokenRecord record = new TokenRecord();
        List<TokenRecord> list = TokenRecord.find(TokenRecord.class, "name = ?", "FCMToken");
        if (list.size() > 0){
            record = list.get(0);
        }else {
            record = new TokenRecord("-1", "FCMToken");
            record.save();
        }
        return record.token;
    }
}
