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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nuvoton.utility.CustomDialogFragment;


/**
 * Created by cchsu20 on 15/11/2016.
 */

public class AddDBFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, CustomDialogFragment.DialogFragmentInterface{
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    public interface SetDBInterface{
        void addNewDoorbell(String serial, String name, String type, String url);
    }

    public SetDBInterface mInterface;
    private final String TAG = "AddDBFragment";
    private static String serial = "0";
    private static String name = "Device";
    private static String url = "rtsp://192.168.100.1/cam1/h264";
    private static String type = "NuDoorbell";


    public static AddDBFragment newInsatnce(Bundle bundle){
        String temp = bundle.getString("Serial");
        if (temp != null){
            serial = temp;
        }
        temp = bundle.getString("name");
        if (temp != null){
            name = temp;
        }
        temp = bundle.getString("url");
        if (temp != null){
            url = temp;
        }
        AddDBFragment fragment = new AddDBFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        String dbName = "Doorbell " + String.valueOf(serial);
        getPreferenceManager().setSharedPreferencesName(dbName);
        addPreferencesFromResource(R.xml.new_db_setting);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(Color.rgb(255, 255, 255));
        return view;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
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
            pref.getEditText().setText(name);

        }else if (key.compareTo("url") == 0){
            EditTextPreference pref = (EditTextPreference) preference;
            pref.getEditText().setText(url);
        }else if (key.compareTo("type") == 0){
            ListPreference list = (ListPreference) preference;
            list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.d(TAG, "onPreferenceChange: " + newValue);
                    type = (String) newValue;
                    return false;
                }
            });
            // type is selected.
        }else if (key.compareTo("save") == 0){
            mInterface.addNewDoorbell(serial, type, name, url);
            FragmentTransaction trans = getFragmentManager().beginTransaction();
            trans.setCustomAnimations(android.R.animator.fade_in, R.animator.slide_out, R.animator.slide_in, R.animator.slide_out);
            getFragmentManager().popBackStackImmediate();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    //CustomDialogInterface

    @Override
    public void sendOkay(String category) {

    }

    @Override
    public void chooseHistory(CustomDialogFragment fragment, int index) {

    }

    public void setInterface(SetDBInterface mInterface){
        this.mInterface = mInterface;
    }


}