package com.nuvoton.utility;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.nuvoton.nuplayer.DeviceData;
import com.nuvoton.nuplayer.R;
import com.nuvoton.socketmanager.NewLoginInterface;

import org.angmarch.views.NiceSpinner;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by timsheu on 8/4/16.
 */
public class FirebaseCloudDialogFragment extends DialogFragment {
    public void setFirebaseCloudDialogInterface(NewLoginInterface mInterface) {
        this.mInterface = mInterface;
    }

    private NewLoginInterface mInterface;
    private static final String TAG = "FirebaseCloudDialogFragment";
    private String label = "Received Login Data";
    private String content = "Received login data from cloud, insert it to database?";
    private String publicIP, privateIP, httpPort, rtspPort;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(label).setMessage(content).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<DeviceData> list = DeviceData.find(DeviceData.class, "PUBLIC_IP = ?", publicIP);
                if (list.size() > 0){
                    Toast.makeText(getActivity().getApplicationContext(), "Device is already in database, will do nothing.", Toast.LENGTH_LONG).show();
                }else {
                    DeviceData deviceData = new DeviceData();
                    deviceData.setPublicIP(publicIP);
                    deviceData.setPrivateIP(privateIP);
                    int httpPortNumber = Integer.valueOf(httpPort);
                    deviceData.setHttpPort(httpPortNumber);
                    int rtspPortNumber = Integer.valueOf(rtspPort);
                    deviceData.setRtspPort(rtspPortNumber);
                    deviceData.save();
                    mInterface.refreshTable(deviceData);
                }
            }
        }).setNegativeButton("Cancel", null);

        return builder.create();
    }

    public void setContent(String content){
        this.content = content;
    }

    public void setLoginData(String publicIP, String privateIP, String httpPort, String rtspPort) {
        this.publicIP = publicIP;
        this.privateIP = privateIP;
        this.httpPort = httpPort;
        this.rtspPort = rtspPort;
    }
}
