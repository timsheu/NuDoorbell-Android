package com.nuvoton.nuplayer;

import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.nuvoton.utility.CustomDialogFragment;
import com.nuvoton.utility.EditDeviceDialogFragment;
import com.nuvoton.utility.NuDoorbellCommand;
import com.nuvoton.utility.NuPlayerCommand;
import com.nuvoton.utility.NuWicamCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        if (key.compareTo("flicker") == 0){
            ListPreference list = (ListPreference) preference;
            list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d(TAG, "onPreferenceChange: " + o);
                    String isPAL = (String) o;
                    list.setValue(isPAL);
                    list.setSummary(isPAL);
                    if (isPAL.compareTo("PAL") == 0){
                        deviceData.setIsPAL(true);
                    }else {
                        deviceData.setIsPAL(false);
                    }
                    deviceData.save();
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
                    EditDeviceDialogFragment fragment = new EditDeviceDialogFragment();
                    String restart = "Restart";
                    fragment.setInterface(EditDBFragment.this);
                    fragment.setLabel(restart);
                    List<String> list = new ArrayList<>();
                    if (type.compareTo("NuDoorbell") == 0){
                        list.add("restart");
                    } else if (type.compareTo("SkyEye") == 0){
                        list.add("reboot");
                    } else if (type.compareTo("NuWicam") == 0){
                        list.add("wifi");
                        list.add("board");
                        list.add("Stream");
                    }
                    fragment.setSpinnerData(list);
                    fragment.setType(restart);
                    fragment.setContent("Choose target to restart");
                    fragment.show(getFragmentManager(), restart);
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
        HTTPSocketManager httpSocketManager = new HTTPSocketManager();
        httpSocketManager.setSocketInterface(this);
        String command = "http://" + ip + ":" + deviceData.getHttpPort();
        if (deviceType.compareTo("NuDoorbell") == 0){
            if (category.compareTo("resolution") == 0){
                command += NuDoorbellCommand.setResolution(value);
            }else if (category.compareTo("bit_rate") == 0){
                command += NuDoorbellCommand.setEncodeBitrate(value);
            }else if (category.compareTo("ssid") == 0){
                localSsid = value;
                command += NuDoorbellCommand.wifiSetup(localSsid, localPassword);
            }else if (category.compareTo("password") == 0){
                localPassword = value;
                command += NuDoorbellCommand.wifiSetup(localSsid, localPassword);
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
        httpSocketManager.setSocketInterface(this);
        httpSocketManager.executeSendGetTask(command);
    }

    private void setPreferenceDefault(){
        Preference pref = findPreference("name");
        pref.setSummary(deviceData.getName());
        pref = findPreference("type");
        pref.setSummary(deviceData.getDeviceType());
        pref = findPreference("ip");
        pref.setSummary(deviceData.getPublicIP());
        pref = findPreference("ssid");
        pref.setSummary(deviceData.getSsid());
        pref = findPreference("voice_upload");
        if (deviceData.getVoiceUploadHttp()){
            pref.setSummary("HTTP");
        }else {
            pref.setSummary("Socket");
        }
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
        String value = (String) responseMap.get("value");
        if (value.compareTo("0") == 0){
            Toast.makeText(getActivity().getApplicationContext(), "Value sent successfully.", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getActivity().getApplicationContext(), "Value sent failed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void voiceConnectionOpened() {

    }
}