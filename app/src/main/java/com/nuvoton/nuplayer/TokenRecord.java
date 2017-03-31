package com.nuvoton.nuplayer;

import com.orm.SugarRecord;

/**
 * Created by cchsu20 on 16/03/2017.
 */

public class TokenRecord extends SugarRecord {
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }



    String token;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    public TokenRecord() {}

    public TokenRecord(String token, String name){
        this.token = token;
        this.name = name;
    }
}
