package com.nuvoton.socketmanager;

import com.nuvoton.utility.FileContent;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by timsheu on 6/13/16.
 */
public interface SocketInterface {
    void showToastMessage(String message, int duration);
    void updateFileList(ArrayList<FileContent> fileList);
    void deviceIsAlive();
    void updateSettingContent(String category, String value);
    void updateSettingContent(String category, JSONObject jsonObject);
    void voiceConnectionOpened();
}
