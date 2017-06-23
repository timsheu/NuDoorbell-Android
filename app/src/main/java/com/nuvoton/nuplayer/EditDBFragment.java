package com.nuvoton.nuplayer;

import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nuvoton.socketmanager.HTTPSocketInterface;
import com.nuvoton.socketmanager.HTTPSocketManager;
import com.nuvoton.socketmanager.VolleyManager;
import com.nuvoton.utility.CustomDialogFragment;
import com.nuvoton.utility.EditDeviceDialogFragment;
import com.nuvoton.utility.NuDoorbellCommand;
import com.nuvoton.utility.NuPlayerCommand;
import com.nuvoton.utility.NuWicamCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;


/**
 * Created by cchsu20 on 15/11/2016.
 */

public class EditDBFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, CustomDialogFragment.DialogFragmentInterface, EditDeviceDialogFragment.EditDeviceDialogInterface, HTTPSocketInterface{

    public interface EditDBInterface{
        void editDevice(String serial, String name, String type, String ip);
    }
    public EditDBInterface mInterface;
    private final String TAG = "AddDBFragment";
    private static long deviceID = 0;
    private static String serial = "0";
    private static String name = "Doorbell";
    private static String ip = "192.168.100.1";
    private static String type = "NuDoorbell";
    private DeviceData deviceData = null;
    private String localSsid = "";
    private String localPassword = "";

    public static EditDBFragment newInsatnce(Bundle bundle){
        deviceID = bundle.getLong("DeviceID");
        EditDBFragment fragment = new EditDBFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        String dbName = "Doorbell " + String.valueOf(serial);
        getPreferenceManager().setSharedPreferencesName(dbName);
        addPreferencesFromResource(R.xml.edit_db_setting);
        deviceData = DeviceData.findById(DeviceData.class, deviceID);
        localSsid = deviceData.getSsid();
        localPassword = deviceData.getPassword();
        ip = deviceData.getPublicIP();
        name = deviceData.getName();
        type = deviceData.getDeviceType();
        setPreferenceDefault();
        updateVideoInParameter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(Color.rgb(255, 255, 255));
        return view;
    }

    @Override
    public void onDestroy() {
        if (deviceData != null){
            deviceData.save();
        }
        mInterface.editDevice(serial, type, name, ip);
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        CustomDialogFragment dialog = new CustomDialogFragment();
        dialog.setInterface(this);
        Log.d(TAG, "onPreferenceTreeClick: " + key);
        if (key.compareTo("aec") == 0) {
            ListPreference list = (ListPreference) preference;
            list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d(TAG, "onPreferenceChange: " + o);
                    String isAEC = (String) o;
                    list.setValue(isAEC);
                    list.setSummary(isAEC);
                    if (isAEC.compareTo("OFF") == 0){
                        deviceData.setIsAECOn(false);
                        list.setSummary("Echo cancellation is OFF");
                    }else {
                        if (AcousticEchoCanceler.isAvailable()) {
                            deviceData.setIsAECOn(true);
                            list.setSummary("Echo cancellation is ON");
                        }else {
                            list.setSummary("Device does not support AEC!!");
                            list.setValue("OFF");
                            Toast.makeText(getActivity(), "Device does not support AEC!!", Toast.LENGTH_SHORT).show();
                            deviceData.setIsAECOn(false);
                        }
                    }
                    deviceData.save();
                    return true;
                }
            });
        }else if (key.compareTo("flicker") == 0){
            ListPreference list = (ListPreference) preference;
            list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d(TAG, "onPreferenceChange: " + o);
                    String isPAL = (String) o, hzValue = "50";
                    list.setValue(isPAL);
                    list.setSummary(isPAL);
                    if (isPAL.compareTo("PAL") == 0){
                        deviceData.setIsPAL(true);
                        hzValue = "50";
                    }else {
                        deviceData.setIsPAL(false);
                        hzValue = "60";
                    }
                    deviceData.save();
                    setDeviceSetting("flicker", hzValue);
                    Toast.makeText(getActivity().getApplicationContext(), "Requires device to restart", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }else if (key.compareTo("serial") == 0){
            if (isAdded()) {
                Toast.makeText(getActivity(), "Serial is auto generated, cannot be modified.", Toast.LENGTH_SHORT).show();
            }
        }else if (key.compareTo("name") == 0){
            EditTextPreference pref = (EditTextPreference) preference;
            pref.getEditText().setText(deviceData.getName());
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d(TAG, "onPreferenceChange: name");
                    name = (String) o;
                    pref.getEditText().setText(name);
                    pref.setSummary(name);
                    deviceData.setName(name);
                    deviceData.save();
                    return false;
                }
            });

        }else if (key.compareTo("ip") == 0){
            EditTextPreference pref = (EditTextPreference) preference;
            pref.getEditText().setText(deviceData.getPublicIP());
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    ip = (String) o;
                    pref.getEditText().setText(ip);
                    pref.setSummary(ip);
                    deviceData.setPublicIP(ip);
                    deviceData.setPrivateIP(ip);
                    deviceData.save();
                    return false;
                }
            });
        }else if (key.compareTo("type") == 0) {
            ListPreference list = (ListPreference) preference;
            String deviceDataTempString = deviceData.getDeviceType();
            list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d(TAG, "onPreferenceChange: " + o);
                    type = (String) o;
                    list.setValue(type);
                    list.setSummary(type);
                    deviceData.setDeviceType(type);
                    deviceData.save();
                    return true;
                }
            });
            // type is selected.
        }else if(key.compareTo("resolution") == 0){
            ListPreference list = (ListPreference) preference;
            String resolutionString = deviceData.getResolution();
            list.setValue(resolutionString);
            list.setSummary(resolutionString);
            list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d(TAG, "onPreferenceChange: " + o);
                    String value = (String) o;
                    list.setSummary(value);
                    list.setValue(value);
                    deviceData.setResolution(value);
                    deviceData.save();
                    setDeviceSetting(key, deviceData.getResolution());
                    return true;
                }
            });
        }else if(key.compareTo("bit_rate") == 0){
            ListPreference list = (ListPreference) preference;
            int deviceDataTempInt = deviceData.getBitRate();
            int listIndex = 0;
            if (deviceDataTempInt > 1024 && deviceDataTempInt <= 2048){
                listIndex = 1;
            }else if (deviceDataTempInt > 2048 && deviceDataTempInt <= 3072){
                listIndex = 2;
            }else if (deviceDataTempInt > 3072 && deviceDataTempInt <= 4096){
                listIndex = 3;
            }
            list.setValueIndex(listIndex);
            list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d(TAG, "onPreferenceChange: " + o);
                    int bitRate = Integer.valueOf((String) o);
                    deviceData.setBitRate(bitRate);
                    deviceData.save();
                    setDeviceSetting(key, String.valueOf(bitRate));
                    list.setValue((String) o);
                    return false;
                }
            });
        }else if(key.compareTo("voice_upload") == 0){
            ListPreference list = (ListPreference) preference;
            boolean isHttp = deviceData.getVoiceUploadHttp();
            String option = "Socket";
            if (isHttp){
                option = "HTTP";
            }
            list.setValue(option);
            list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d(TAG, "onPreferenceChange: " + o);
                    String httpString = (String) o;
                    if (httpString.compareTo("Socket") == 0){
                        deviceData.setVoiceUploadHttp(false);
                    }else {
                        deviceData.setVoiceUploadHttp(true);
                    }
                    list.setSummary(httpString);
                    list.setValue(httpString);
                    deviceData.save();
                    return false;
                }
            });
        }else if(key.compareTo("ssid") == 0){
            EditTextPreference pref = (EditTextPreference) preference;
            pref.getEditText().setText(deviceData.getSsid());
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    String ssid = pref.getText();
                    pref.setSummary((String) o);
                    deviceData.setSsid(ssid);
                    deviceData.save();
                    setDeviceSetting(key, ssid);
                    return false;
                }
            });
        }else if(key.compareTo("password") == 0){
            EditTextPreference pref = (EditTextPreference) preference;
            pref.getEditText().setText(deviceData.getPassword());
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    String password = pref.getText();
                    deviceData.setPassword(password);
                    deviceData.save();
                    setDeviceSetting(key, password);
                    return false;
                }
            });
        }else if(key.compareTo("show_password") == 0){
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    EditTextPreference pref = (EditTextPreference) findPreference("password");
                    pref.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL | InputType.TYPE_CLASS_TEXT);
                    Toast.makeText(getActivity().getApplicationContext(), "Password is shown", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }else if(key.compareTo("restart") == 0){
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (type.compareTo("NuWicam") == 0){
                        EditDeviceDialogFragment fragment = new EditDeviceDialogFragment();
                        String restart = "Restart";
                        List<String> list = new ArrayList<>();
                        fragment.setInterface(EditDBFragment.this);
                        fragment.setLabel(restart);
                        list.add("wifi");
                        list.add("board");
                        list.add("Stream");
                        fragment.setSpinnerData(list);
                        fragment.setType(restart);
                        fragment.setContent("Choose target to restart");
                        fragment.show(getFragmentManager(), restart);
                    }else if (type.compareTo("NuDoorbell") == 0){
                        setDeviceSetting("restart", "");
                    }else if (type.compareTo("SkyEye") == 0){

                    }
                    return false;
                }
            });
        }else if (key.compareTo("save") == 0){
            deviceData.save();
            mInterface.editDevice(serial, type, name, ip);
            FragmentTransaction trans = getFragmentManager().beginTransaction();
            trans.setCustomAnimations(android.R.animator.fade_in, R.animator.slide_out, R.animator.slide_in, R.animator.slide_out);
            getFragmentManager().popBackStackImmediate();
        }
        deviceData.save();
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void sendOkay(String category) {

    }

    @Override
    public void chooseHistory(CustomDialogFragment fragment, int index) {

    }

    public void setInterface(EditDBInterface mInterface){
        this.mInterface = mInterface;
    }

    //MARK: Utility
    public void setDeviceSetting(String category, String value){
        String deviceType = deviceData.deviceType;
        String tagString = "";
        VolleyManager.HTTPSocketTags tag = VolleyManager.HTTPSocketTags.DEFAULT;
        String command = "http://" + ip + ":" + deviceData.getHttpPort();
        if (deviceType.compareTo("NuDoorbell") == 0){
            if (category.compareTo("resolution") == 0){
                command += NuDoorbellCommand.setResolution(value);
                tag = VolleyManager.HTTPSocketTags.UPDATE_VIDEO_RESOLUTION;
                tagString = String.valueOf(VolleyManager.HTTPSocketTags.UPDATE_VIDEO_RESOLUTION.getValue());
            }else if (category.compareTo("bit_rate") == 0){
                command += NuDoorbellCommand.setEncodeBitrate(value);
                tag = VolleyManager.HTTPSocketTags.UPDATE_VIDEO_BITRATE;
                tagString = String.valueOf(VolleyManager.HTTPSocketTags.UPDATE_VIDEO_BITRATE.getValue());
            }else if (category.compareTo("ssid") == 0){
                localSsid = value;
                command += NuDoorbellCommand.wifiSetup(localSsid, localPassword);
                tag = VolleyManager.HTTPSocketTags.UPDATE_WIFI_SSID;
                tagString = String.valueOf(VolleyManager.HTTPSocketTags.UPDATE_WIFI_SSID.getValue());
            }else if (category.compareTo("password") == 0){
                localPassword = value;
                command += NuDoorbellCommand.wifiSetup(localSsid, localPassword);
                tag = VolleyManager.HTTPSocketTags.UPDATE_WIFI_PASSWORD;
                tagString = String.valueOf(VolleyManager.HTTPSocketTags.UPDATE_WIFI_PASSWORD.getValue());
            }else if (category.compareTo("flicker") == 0){
                command += NuDoorbellCommand.updateFlicker(value);
                tag = VolleyManager.HTTPSocketTags.UPDATE_VIDEO_FLICKER;
                tagString = String.valueOf(VolleyManager.HTTPSocketTags.UPDATE_VIDEO_FLICKER.getValue());
            }else if (category.compareTo("restart") == 0){
                command += NuDoorbellCommand.restart();
                tag = VolleyManager.HTTPSocketTags.RESTART;
                tagString = String.valueOf(VolleyManager.HTTPSocketTags.RESTART.getString());
                Toast.makeText(getActivity().getApplicationContext(), "Device restarted", Toast.LENGTH_SHORT).show();
            }
        }else if (deviceType.compareTo("SkyEye") == 0){
            if (category.compareTo("resolution") == 0){
                int number = 0;
                if (value.compareTo("VGA") == 0){
                    number = 1;
                }else {
                    number = 2;
                }
                command += NuPlayerCommand.setResolution("h264", number);
            }else if (category.compareTo("bit_rate") == 0){
                command += NuPlayerCommand.setEncodeBitrate("h264", Integer.valueOf(value));
            }else if (category.compareTo("ssid") == 0){
                localSsid = value;
                command += NuPlayerCommand.updateWifiParameters(localSsid, localPassword);
            }else if (category.compareTo("password") == 0){
                localPassword = value;
                command += NuPlayerCommand.updateWifiParameters(localSsid, localPassword);
            }
        }else if (deviceType.compareTo("NuWicam") == 0){
            Map<String, String> parameters = new HashMap<>();
            if (category.compareTo("resolution") == 0){
                String height = "240", width = "320";
                if (value.compareTo("1") == 0){
                    height = "480";
                    width = "640";
                }else{
                    height = "360";
                    width = "640";
                }
                parameters.put("VINHEIGHT", height);
                parameters.put("JPEGENCHEIGHT", height);
                parameters.put("VINWIDTH", width);
                parameters.put("JPEGENCWIDTH", width);
                command += NuWicamCommand.updateStreamParameters(parameters);
            }else if (category.compareTo("bit_rate") == 0){
                parameters.put("BITRATE", value);
                command += NuWicamCommand.updateStreamParameters(parameters);
            }else if (category.compareTo("ssid") == 0){
                localSsid = value;
                command += NuWicamCommand.updateWifiParameters(localSsid, localPassword);
            }else if (category.compareTo("password") == 0){
                localPassword = value;
                command += NuWicamCommand.updateWifiParameters(localSsid, localPassword);
            }
        }
        VolleyManager.getShared(getActivity().getApplicationContext()).setHttpSocketInterface(this);
        VolleyManager.getShared(getActivity().getApplicationContext()).sendCommand(command, tag);
    }

    private void setPreferenceDefault(){
        Preference pref = findPreference("name");
        pref.setSummary(deviceData.getName());
        pref = findPreference("type");
        ListPreference list = (ListPreference) pref;
        pref.setSummary(deviceData.getDeviceType());
        pref = findPreference("ip");
        pref.setSummary(deviceData.getPublicIP());
        EditTextPreference edit = (EditTextPreference) findPreference("ssid");
        edit.setSummary(deviceData.getSsid());
        edit.getEditText().setText(deviceData.getSsid());
        edit = (EditTextPreference) findPreference("password");
        edit.setSummary(deviceData.getPassword());
        edit.getEditText().setText(deviceData.getPassword());
        pref = findPreference("voice_upload");
        list = (ListPreference) pref;
        if (deviceData.getVoiceUploadHttp()){
            list.setSummary("HTTP");
            list.setValue("HTTP");
        }else {
            list.setSummary("Socket");
            list.setValue("Socket");
        }
        pref = findPreference("aec");
        list = (ListPreference) pref;
        if (deviceData.getIsAECOn()){
            list.setSummary("ON");
            list.setValue("ON");
        }else {
            list.setSummary("OFF");
            list.setValue("OFF");
        }
    }

    private void sendSetting(String command, int tag){
        String temp = "http://" + ip + ":" + deviceData.getHttpPort();
        temp += command;
        VolleyManager.getShared(getActivity().getApplicationContext()).setHttpSocketInterface(this);
        VolleyManager.getShared(getActivity().getApplicationContext()).sendCommand(temp, VolleyManager.HTTPSocketTags.fromInt(tag));
    }

    private void  updateVideoInParameter(){
        String command = NuDoorbellCommand.listVideoInputParameters();
        sendSetting(command, HTTPSocketManager.HTTPSocketTags.PLUGIN_LIST_VIDEO_IN_PARAM.getValue());
    }

    private void updateNetworkParameter(){
        String command = NuDoorbellCommand.listNetworkParameters();
        sendSetting(command, HTTPSocketManager.HTTPSocketTags.PLUGIN_LIST_NETWORK_PARAM.getValue());
    }

    //MARK: EditDeviceDialogInterface
    @Override
    public void removeDevice(String category) {

    }

    @Override
    public void spinnerChosen(EditDeviceDialogFragment fragment, int index) {

    }

    @Override
    public void enterEditPage(String category) {

    }

    @Override
    public void restartChosen(int index, String type) {

    }

    //MARK: onSharedPrefereceChanged
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    //MARK: HTTPSocketInterface
    @Override
    public void httpSocketResponse(Map<String, Object> responseMap) {
        VolleyManager.HTTPSocketTags tag = (VolleyManager.HTTPSocketTags) responseMap.get("socketTag");
        String value = (String) responseMap.get("value");
        if (tag.getValue() == HTTPSocketManager.HTTPSocketTags.PLUGIN_LIST_VIDEO_IN_PARAM.getValue()){
            String port0_flicker = (String) responseMap.get("port0_flicker");
            if (port0_flicker.compareTo("50") == 0){
                deviceData.isPAL = true;
            }else if (port0_flicker.compareTo("60") == 0){
                deviceData.isPAL = false;
            }

            String width = (String) responseMap.get("port0_planar_width");
            if (width.compareTo("1920") == 0){
                deviceData.resolution = "1080p";
            }else if (width.compareTo("1280") == 0){
                deviceData.resolution = "720p";
            }else if (width.compareTo("640") == 0){
                deviceData.resolution = "VGA";
            }else if (width.compareTo("320") == 0){
                deviceData.resolution = "QVGA";
            }

            Toast.makeText(getActivity().getApplicationContext(), "Video parameters are synchronized", Toast.LENGTH_SHORT).show();
            updateNetworkParameter();
        }else if (tag.getValue() == HTTPSocketManager.HTTPSocketTags.PLUGIN_LIST_NETWORK_PARAM.getValue()){
            String ssid = (String) responseMap.get("wifi_ap_ssid");
            deviceData.ssid = ssid;
            String password = (String) responseMap.get("wifi_ap_psk");
            deviceData.password = password;

            Toast.makeText(getActivity().getApplicationContext(), "Wi-Fi parameters are synchronized", Toast.LENGTH_SHORT).show();
        }else {
            if (value.compareTo("0") == 0){
                Toast.makeText(getActivity().getApplicationContext(), "Value sent successfully.", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getActivity().getApplicationContext(), "Value sent failed.", Toast.LENGTH_SHORT).show();
            }
        }
        deviceData.save();
        setPreferenceDefault();
    }

    @Override
    public void voiceConnectionOpened() {

    }

    @Override
    public void didDisconnected() {

    }
}