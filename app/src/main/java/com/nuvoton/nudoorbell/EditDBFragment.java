package com.nuvoton.nudoorbell;

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
import android.widget.EditText;
import android.widget.Toast;

import com.nuvoton.utility.CustomDialogFragment;
import com.nuvoton.utility.EditDeviceDialogFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by cchsu20 on 15/11/2016.
 */

public class EditDBFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, CustomDialogFragment.DialogFragmentInterface, EditDeviceDialogFragment.EditDeviceDialogInterface{

    public interface EditDBInterface{
        void editDevice(String serial, String name, String type, String url);
    }
    public EditDBInterface mInterface;
    private final String TAG = "AddDBFragment";
    private static long deviceID = 0;
    private static String serial = "0";
    private static String name = "Doorbell";
    private static String url = "192.168.100.1";
    private static String type = "NuDoorbell";

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
        setPreferenceDefaultValue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(Color.rgb(255, 255, 255));
        return view;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        DeviceData deviceData = DeviceData.findById(DeviceData.class, deviceID);
        String key = preference.getKey();
        CustomDialogFragment dialog = new CustomDialogFragment();
        dialog.setInterface(this);
        Log.d(TAG, "onPreferenceTreeClick: " + key);
        if (key.compareTo("serial") == 0){
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
                    deviceData.setName(name);
                    deviceData.save();
                    return false;
                }
            });

        }else if (key.compareTo("url") == 0){
            EditTextPreference pref = (EditTextPreference) preference;
            pref.getEditText().setText(deviceData.getPublicIP());
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    url = (String) o;
                    pref.getEditText().setText(url);
                    deviceData.setPublicIP(url);
                    deviceData.setPrivateIP(url);
                    deviceData.save();
                    return false;
                }
            });
        }else if (key.compareTo("type") == 0) {
            ListPreference list = (ListPreference) preference;
            list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d(TAG, "onPreferenceChange: " + o);
                    type = (String) o;
                    deviceData.setDeviceType(type);
                    return true;
                }
            });
            // type is selected.
        }else if(key.compareTo("resolution") == 0){
            ListPreference list = (ListPreference) preference;
            list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d(TAG, "onPreferenceChange: " + o);
                    deviceData.setResolution((String) o);
                    return true;
                }
            });
        }else if(key.compareTo("bit_rate") == 0){
            ListPreference list = (ListPreference) preference;
            list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d(TAG, "onPreferenceChange: " + o);
                    deviceData.setBitRate((int) o);
                    return false;
                }
            });
        }else if(key.compareTo("voice_upload") == 0){
            ListPreference list = (ListPreference) preference;
            list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d(TAG, "onPreferenceChange: " + o);
                    boolean isHttp = true;
                    String httpString = (String) o;
                    if (httpString.compareTo("socket") == 0){
                        isHttp = false;
                    }
                    deviceData.setVoiceUploadHttp(isHttp);
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
                    deviceData.setSsid(ssid);
                    deviceData.save();
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
                    return false;
                }
            });
        }else if(key.compareTo("show_password") == 0){
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    EditTextPreference pref = (EditTextPreference) findPreference("show_password");
                    pref.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL | InputType.TYPE_CLASS_TEXT);
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
                    List<String> list = new ArrayList<String>();
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
            mInterface.editDevice(serial, type, name, url);
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
    public void setPreferenceDefaultValue(){
        DeviceData deviceData = DeviceData.findById(DeviceData.class, deviceID);
        ListPreference listPreference;
        EditTextPreference editTextPreference;
        Preference preference;

        //serial
        preference = findPreference("serial");
        String temp = "Device serial is " + deviceData.getId();
        preference.setSummary(temp);

        editTextPreference = (EditTextPreference) findPreference("name");
        editTextPreference.getEditText().setText(deviceData.getName());

        //type
        listPreference = (ListPreference) findPreference("type");
        String deviceDataTempString = deviceData.getDeviceType();
        int listIndex = 0;
        if (deviceDataTempString.compareTo("SkyEye") == 0){
            listIndex = 1;
        }else if(deviceDataTempString.compareTo("NuWicam") == 0){
            listIndex = 2;
        }
        listPreference.setValueIndex(listIndex);

        //url
        editTextPreference = (EditTextPreference) findPreference("url");
        editTextPreference.getEditText().setText(deviceData.getPublicIP());

        //resolution
        listPreference = (ListPreference) findPreference("resolution");
        deviceDataTempString = deviceData.getResolution();
        listIndex = 0;
        if (deviceDataTempString.compareTo("VGA") == 0){
            listIndex = 1;
        }else if (deviceDataTempString.compareTo("360") == 0){
            listIndex = 2;
        }
        listPreference.setValueIndex(listIndex);

        //bitrate
        listPreference = (ListPreference) findPreference("bit_rate");
        int deviceDataTempInt = deviceData.getBitRate();
        listIndex = 0;
        if (deviceDataTempInt > 1024 && deviceDataTempInt <= 2048){
            listIndex = 1;
        }else if (deviceDataTempInt > 2048 && deviceDataTempInt <= 3072){
            listIndex = 2;
        }else if (deviceDataTempInt > 3072 && deviceDataTempInt <= 4096){
            listIndex = 3;
        }
        listPreference.setValueIndex(listIndex);

        //voice upload
        listPreference = (ListPreference) findPreference("voice_upload");
        boolean deviceDataTempBoolean = deviceData.getVoiceUploadHttp();
        listIndex = 0;
        if (deviceDataTempBoolean){
            listIndex = 1;
        }
        listPreference.setValueIndex(listIndex);

        //ssid
        editTextPreference = (EditTextPreference) findPreference("ssid");
        editTextPreference.getEditText().setText(deviceData.getSsid());

        //password
        editTextPreference = (EditTextPreference) findPreference("password");
        editTextPreference.getEditText().setText(deviceData.getPassword());

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

}