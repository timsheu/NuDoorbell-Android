package com.nuvoton.nudoorbell;

import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.firebase.iid.FirebaseInstanceId;
import com.karumi.dexter.Dexter;
import com.nuvoton.socketmanager.BGBCReceiverService;
import com.nuvoton.utility.EventMessageClass;
import com.nuvoton.socketmanager.FCMExecutive;
import com.nuvoton.utility.ReadConfigure;
import com.nuvoton.socketmanager.ShmadiaConnectManager;

import android.app.FragmentManager;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class LivePage extends AppCompatActivity implements LiveFragment.OnHideBottomBarListener, SettingFragment.OnHideBottomBarListener, ShmadiaConnectManager.ShmadiaConnectInterface, BGBCReceiverService.BGBCInterface{
    // live view callbacks
    public void onHideBottomBar(boolean isHide){
        if (isHide){
            bottomNavigation.hideBottomNavigation(true);
        } else {
            bottomNavigation.restoreBottomNavigation(true);
        }
    }

    public void onEnableClick(boolean isEnable){
        bottomNavigation.setEnabled(isEnable);
        Log.d(TAG, "onEnableClick: " + String.valueOf(isEnable));
    }

    @Override
    public void restartStream() {
        Log.d(TAG, "restartStream: ");
        if (liveFragment != null){
            liveFragment.stopStreamFromSetting();
        }
    }

    Intent mServiceIntent;
    private BGBCReceiverService mBGBCReceiverService;
    private int index=0;
    private String platform = "NuDoorbell";
    private String cameraSerial = "5";
    private boolean clicked = false;
    private boolean isLandscape = false;
    private static final String TAG = "SkyEye", FRAGMENT_TAG = "CURRENT_FRAGMENT_INDEX";
    private AHBottomNavigation bottomNavigation;
    private ArrayList<AHBottomNavigationItem> bottomNavigationItems = new ArrayList<>();
    private FragmentManager fragmentManager = getFragmentManager();
    private LiveFragment liveFragment = null;
    private SettingFragment settingFragment = null;
    private String serviceURL;
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        View decorView = getWindow().getDecorView();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            if (index != 1){
                bottomNavigation.hideBottomNavigation(true);
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                isLandscape = true;
            }
        } else {
            bottomNavigation.restoreBottomNavigation(true);
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            isLandscape = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isFCMTokenExist();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dexter.initialize(getApplication());
        int orientation = getWindowManager().getDefaultDisplay().getRotation();
        View decorView = getWindow().getDecorView();
        setContentView(R.layout.activity_live_page);
        Log.d(TAG, "onCreate:" + platform + ", " + cameraSerial);
        initUI();
        ReadConfigure configure = ReadConfigure.getInstance(this, false);
        String cameraName = "Setup Camera " + cameraSerial;
        SharedPreferences preference = getApplicationContext().getSharedPreferences(cameraName, Context.MODE_PRIVATE);
        String version = preference.getString("Version", "0");
        if (version.compareTo(String.valueOf(R.string.version)) != 0){
            configure.initSharedPreference(Integer.valueOf(cameraSerial), true);
        }
        switch (orientation){
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (index != 1){
                    bottomNavigation.hideBottomNavigation(true);
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                    isLandscape = true;
                }
                break;
        }
//        Log.d(TAG, "onCreate: before get token");
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null){
            Log.d(TAG, "onCreate: token" + token);
            preference.edit().putString("FCM Token", token).apply();
        }
        mBGBCReceiverService = new BGBCReceiverService();
        mBGBCReceiverService.setBgbcInterface(this);
        mServiceIntent = new Intent(this, mBGBCReceiverService.getClass());
        if (!isMyServiceRunning(mBGBCReceiverService.getClass())) {
            mServiceIntent.putExtra("StopService", true);
            stopService(mServiceIntent);
        }
        String result = getIntent().getStringExtra("URL");
        if (result != null){
            serviceURL = result;
        }
        Log.d(TAG, "onCreate: " + serviceURL);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }



    @Override
    protected void onSaveInstanceState(Bundle outState){
        outState.putInt(FRAGMENT_TAG, index);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState: " + String.valueOf(savedInstanceState.getInt(FRAGMENT_TAG)));
        changeFragment(savedInstanceState.getInt(FRAGMENT_TAG));
    }

    private void initUI(){
        final Bundle bundle = new Bundle();
        bundle.putString("Platform", platform);
        bundle.putString("CameraSerial", cameraSerial);
        if (liveFragment == null){
            liveFragment = LiveFragment.newInstance(bundle);
        }
        if (settingFragment == null){
            settingFragment = SettingFragment.newInstance(bundle);
        }
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        AHBottomNavigationItem liveItem = new AHBottomNavigationItem("Live", R.drawable.livetab);
        final AHBottomNavigationItem settingItem = new AHBottomNavigationItem("Setting", R.drawable.geartab);

        bottomNavigationItems.add(liveItem);
        bottomNavigationItems.add(settingItem);

        bottomNavigation.addItems(bottomNavigationItems);
        bottomNavigation.setAccentColor(Color.parseColor("#007DFF"));

        bottomNavigation.setNotification(0, 0);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected){
                if (index == position) return true;
                FragmentTransaction trans = fragmentManager.beginTransaction();
                if (liveFragment.isAdded()) trans.hide(liveFragment);
                if (settingFragment.isAdded()) trans.hide(settingFragment);

                if (position == 0){
                    if (!liveFragment.isAdded()){
                        trans.add(R.id.fragment_content, liveFragment);
                    }else{
                        trans.show(liveFragment);
                    }
                }else{
                    if (!settingFragment.isAdded()){
                        trans.add(R.id.fragment_content, settingFragment);
                    }else{
                        trans.show(settingFragment);
                    }
                }
                bottomNavigation.setNotification(0, position);
                index = position;
                trans.commit();
                return true;
            }
        });
        FragmentTransaction trans = fragmentManager.beginTransaction();
        if (!liveFragment.isAdded()){
            trans.add(R.id.fragment_content, liveFragment).commit();
        }else{
            trans.hide(settingFragment).show(liveFragment).commit();
        }
    }

    private void changeFragment(int savedIndex){
        Bundle bundle = new Bundle();
        bundle.putString("Platform", platform);
        bundle.putString("CameraSerial", cameraSerial);
        bundle.putString("URL", serviceURL);
        index = savedIndex;
        if (index == 0){
            FragmentTransaction trans = fragmentManager.beginTransaction();
            if (!liveFragment.isAdded()){
                trans.hide(settingFragment).add(R.id.fragment_content, liveFragment).commit();
            }else{
                trans.hide(settingFragment).show(liveFragment).commit();
            }
        }else{
            FragmentTransaction trans = fragmentManager.beginTransaction();
            if (!settingFragment.isAdded()){
                trans.hide(liveFragment).add(R.id.fragment_content, settingFragment).commit();
            }else{
                trans.hide(liveFragment).show(settingFragment).commit();
            }
        }
        bottomNavigation.setNotification(0, index);
        bottomNavigation.setCurrentItem(index);
    }

    public void showBottomBar(boolean option){
        if (option == false){
            bottomNavigation.hideBottomNavigation(true);
        }else {
            bottomNavigation.restoreBottomNavigation(true);
        }
    }

    private boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit){
            super.onBackPressed();
            return;
        }else {
            Toast.makeText(this, "Press Back again to Exit !", Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3000);

        }
    }

    @Override
    protected void onDestroy() {
        mServiceIntent.putExtra("StopService", false);
        stopService(mServiceIntent);
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        ShmadiaConnectManager.getInstance(this).closeSocket();
    }

    Runnable isFCMTokenExistRunnable = new Runnable() {
        @Override
        public void run() {
            isFCMTokenExist();
        }
    };

    public void isFCMTokenExist(){
        ReadConfigure configure = ReadConfigure.getInstance(getApplicationContext(), false);
        String token = configure.getTargetValue("FCM Token");
        Log.d(TAG, "isFCMTokenExist: " + token);
        if (token.compareTo("-1") != 0){
            FCMExecutive.getInstance(this).setToken(token);
            ShmadiaConnectManager manager = ShmadiaConnectManager.getInstance(this);
            manager.shmadiaConnectInterface = this;
            manager.openSocket();
        }else{
            new Handler().postDelayed(isFCMTokenExistRunnable, 5000);
        }
    }

    @Override
    public void announceIsConnected() {
        EventMessageClass messageClass = new EventMessageClass();
        char[] uuidArray = messageClass.TEST_UUID.toCharArray();
        messageClass.request.szUUID = uuidArray;
        messageClass.request.eRole = EventMessageClass.E_EVENTMSG_ROLE.eEVENTMSG_ROLE_USER.getRole();
        char[] tokenArray = FCMExecutive.getInstance(this).getToken().toCharArray();
        messageClass.request.szCloudRegID = tokenArray;
        ShmadiaConnectManager.getInstance(this).writeMessageToShmadia(messageClass);
        Log.d(TAG, "sendRegistrationToServer: " + messageClass.toString());
    }

    @Override
    public void updateURLToLive(String URL) {
        liveFragment.updateStreamingURL(URL);
    }
}
