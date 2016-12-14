package com.nuvoton.nudoorbell;


import android.*;
import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.view.View.OnClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import com.appunite.ffmpeg.FFmpegError;
import com.appunite.ffmpeg.FFmpegPlayer;
import com.appunite.ffmpeg.FFmpegListener;
import com.appunite.ffmpeg.FFmpegDisplay;
import com.appunite.ffmpeg.FFmpegStreamInfo;
import com.appunite.ffmpeg.FFmpegSurfaceView;
import com.appunite.ffmpeg.NotPlayingException;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener;
import com.nuvoton.socketmanager.BroadcastReceiver;
import com.nuvoton.socketmanager.ReadConfigure;
import com.nuvoton.socketmanager.SocketInterface;
import com.nuvoton.socketmanager.SocketManager;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class LiveFragment extends Fragment implements OnClickListener, OnSeekBarChangeListener,
        FFmpegListener, SocketInterface, ReadConfigure.ReadConfigureInterface, TwoWayTalking.TwoWayTalkingInterface, BroadcastReceiver.BCRInterface {
    private boolean isTCP = false;
    private TwoWayTalking mTwoWayTalking;
    private boolean isDuplex = true;
    private int retry = 0;
    private boolean isRestart = false;
    private ArrayList<ImageButton> lightButtonList = new ArrayList<>();
    private Handler handler = new Handler();
    private boolean flashOn = true;
    private String localURL;
    private SocketManager socketManager;
    private ReadConfigure configure;
    private int orientation;
    private String cameraSerial;
    private ProgressBar progressBar;
    private TextView onlineText;
    private ImageView redDot;
    private boolean isPlaying = false, isTracking = false, isConnected = false;
    private int mCurrentTimeS;
    private View thisView;
    private FFmpegPlayer mMpegPlayer;
    private FFmpegSurfaceView mVideoView;
    private SeekBar seekBar;
    private ImageButton snapshotButton, playButton, expandButton, hangButton, ansButton;
    private int mAudioStreamNo = FFmpegPlayer.UNKNOWN_STREAM;
    private int mSubtitleStreamNo = FFmpegPlayer.NO_STREAM;
    private static final String TAG = "LiveFragment";
    private Timer delayConnectTimer;

    private boolean isHide = false;
    OnHideBottomBarListener onHideBottomBarListener;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.snapshotButton:
                Log.d(TAG, "onClick: snapshot");
                break;
            case R.id.playButton:
                Log.d(TAG, "onClick: play");
                playButton.setEnabled(false);
                repeatRedDot(false);
                if (!isPlaying){
                    isPlaying = true;
                }else {
                    isPlaying = false;
                    mMpegPlayer.pause();
                }
                break;
            case R.id.expandButton:
                Log.d(TAG, "onClick: expand");
                break;
            case R.id.phone_ans:
                Log.d(TAG, "onClick: phone_ans");
                if (isDuplex){
                    mTwoWayTalking = TwoWayTalking.getInstance(getActivity().getApplicationContext());
                    mTwoWayTalking.setInterface(this);
                    if (!mTwoWayTalking.isRecording){
                        if (isAdded()){
                            Toast.makeText(getActivity(), "Audio upload started.", Toast.LENGTH_SHORT).show();
                        }
                        String cameraName = "Setup Camera " + cameraSerial;
                        SharedPreferences preference = getActivity().getSharedPreferences(cameraName, Context.MODE_PRIVATE);
                        String httpModeString = preference.getString("VoiceUpload", "http");
                        if (httpModeString.compareTo("http") == 0){
                            mTwoWayTalking.updateURL(getDeviceURL());
                        }else {
                            mTwoWayTalking.pokeClient(getDeviceURL(), "tcp");
                        }
                        mTwoWayTalking.startRecording();
                        ansButton.setEnabled(false);
                        hangButton.setEnabled(true);
                    }
                }
                break;
            case R.id.phone_hang:
                Log.d(TAG, "onClick: phone_hang");
                if (isDuplex){
                    mTwoWayTalking = TwoWayTalking.getInstance(getActivity().getApplicationContext());
                    mTwoWayTalking.setInterface(this);
                    if (mTwoWayTalking.isRecording){
                        if (isAdded()){
                            Toast.makeText(getActivity(), "Stop audio upload.", Toast.LENGTH_SHORT).show();
                        }
                        mTwoWayTalking.stopRecording();
                        ansButton.setEnabled(true);
                        hangButton.setEnabled(false);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void didOpenVoiceUpload() {
        Log.d(TAG, "didOpenVoiceUpload: ");
//        mTwoWayTalking.startRecording();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        Log.d(TAG, "onProgressChanged:");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isTracking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isTracking = false;
    }

    @Override
    public void updateStreamingURL(String updatedURL) {
        if (configure == null){
            configure = ReadConfigure.getInstance(getActivity().getApplicationContext(), false);
            configure.setReadConfigureInterface(this);
        }
        localURL = updatedURL;
        Log.d(TAG, "updateStreamingURL: " + localURL);
        String cameraName = "Setup Camera " + cameraSerial;
        SharedPreferences preference = getActivity().getSharedPreferences(cameraName, Context.MODE_PRIVATE);
        preference.edit().putString("URL", localURL).commit();
        if (!isPlaying){
            setDataSource();
            mMpegPlayer.resume();
        }
    }

    @Override
    public void signalDataHandled(String URL) {
        Log.d(TAG, "signalDataHandled: ");
    }

    public interface OnHideBottomBarListener{
        public void onHideBottomBar(boolean isHide);
        public void onEnableClick(boolean isEnable);
    }


    public LiveFragment() {
        // Required empty public constructor
    }

    public static LiveFragment newInstance(Bundle b){
        LiveFragment fragment = new LiveFragment();
        fragment.setArguments(b);
        return fragment;
    }

    public void setupFFMPEGSurface(){
        if (isAdded()) {
            mVideoView = (FFmpegSurfaceView) getActivity().findViewById(R.id.videoView);
            mVideoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: ");
                    if (!isHide){
                        isHide = true;
                    }else {
                        isHide = false;
                    }
                    onHideBottomBarListener.onHideBottomBar(isHide);
                    return false;
                }
            });
            mMpegPlayer = new FFmpegPlayer((FFmpegDisplay) mVideoView, this);
        }
        if (configure == null){
            configure = ReadConfigure.getInstance(getActivity(), false);
        }
        localURL = configure.getTargetValue("URL");
    }

    public void registerUI(){

        playButton = (ImageButton) thisView.findViewById(R.id.playButton);
        playButton.setOnClickListener(this);
        playButton.setEnabled(false);

        expandButton = (ImageButton) thisView.findViewById(R.id.expandButton);
        expandButton.setOnClickListener(this);
        expandButton.setEnabled(false);

        seekBar = (SeekBar) thisView.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setEnabled(false);

        hangButton = (ImageButton) thisView.findViewById(R.id.phone_hang);
        hangButton.setOnClickListener(this);
        hangButton.setEnabled(false);

        ansButton = (ImageButton) thisView.findViewById(R.id.phone_ans);
        ansButton.setOnClickListener(this);
        ansButton.setEnabled(true);


        onlineText = (TextView) thisView.findViewById(R.id.onlineText);
        progressBar = (ProgressBar) thisView.findViewById(R.id.progressBar);
        redDot = (ImageView) thisView.findViewById(R.id.redDot);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        thisView = inflater.inflate(R.layout.fragment_live, container, false);
        registerUI();
        determineOrientation();
        if (socketManager == null){
            socketManager = new SocketManager();
        }
        socketManager.setSocketInterface(this);
        BroadcastReceiver bcr = BroadcastReceiver.getInstance(getActivity().getApplicationContext());
        bcr.openUDPSocket();
        bcr.setBcrInterface(this);
        PermissionListener dialogPermissionListener =
                DialogOnDeniedPermissionListener.Builder
                .withContext(getActivity())
                .withTitle("Mic Permission")
                .withMessage(R.string.request_mic_auth)
                .withButtonText("OK")
                .withIcon(R.drawable.microphone)
                .build();
        Dexter.checkPermission(dialogPermissionListener, Manifest.permission.RECORD_AUDIO);
        // Inflate the layout for this fragment
        return thisView;
    }

    @Override
    public void onPause() {
        super.onPause();
        repeatRedDot(false);
//        repeatTimerDelay(false);
        isConnected = false;
        if (TwoWayTalking.isRecording){
            mTwoWayTalking.stopRecording();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        repeatRedDot(false);
//        repeatTimerDelay(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setupFFMPEGSurface();
        if (isAdded()) {
            configure = ReadConfigure.getInstance(getActivity().getApplicationContext(), false);
            configure.setReadConfigureInterface(this);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged: live fragment");
        super.onConfigurationChanged(newConfig);
        if (!isAdded()) return;
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
        registerUI();
        determineOrientation();
        mVideoView = (FFmpegSurfaceView) getView().findViewById(R.id.videoView);
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isHide){
                    isHide = true;
                }else {
                    isHide = false;
                }
                onHideBottomBarListener.onHideBottomBar(isHide);
                return false;
            }
        });
        mMpegPlayer = new FFmpegPlayer((FFmpegDisplay) mVideoView, this);
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        View subview = inflater.inflate(R.layout.fragment_live, viewGroup);

        // Find your buttons in subview, set up onclicks, set up callbacks to your parent fragment or activity here.
        // You can create ViewHolder or separate method for that.
        // example of accessing views: TextView textViewExample =
        // (TextView) view.findViewById(R.id.text_view_example);
        // textViewExample.setText("example");
    }

    @Override
    public void voiceConnectionOpened() {
        Log.d(TAG, "voiceConnectionOpened: ");
        mTwoWayTalking.startRecording();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isAdded()) return;
        try{
            onHideBottomBarListener = (OnHideBottomBarListener) getActivity();
        }catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString() + " must implement onHideBottomBarListener");
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        Bundle bundle = getArguments();
        if (bundle != null){
            cameraSerial = getArguments().getString("CameraSerial");
            localURL = getArguments().getString("URL");
        }
        if (onHideBottomBarListener != null){
            onHideBottomBarListener.onEnableClick(true);
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void determineOrientation(){
        orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            isHide = true;
        }else {
            isHide = false;
        }
    }

    private class TimerDelayConnectRTSP extends TimerTask{
        public void run(){
            Log.d(TAG, "run: timer delay connect rtsp");
            setDataSource();
        }
    }

    private void repeatTimerDelay(boolean option){
        Log.d(TAG, "repeatTimerDelay: ");
        if (option){
            delayConnectTimer = new Timer();
            delayConnectTimer.schedule(new TimerDelayConnectRTSP(), 3000);
        }else{
            delayConnectTimer.cancel();
            delayConnectTimer = null;
        }
    }

    private void repeatRedDot(boolean option){
        Log.d(TAG, "repeatRedDot: " + String.valueOf(option));
        if (option){
            redDot.setImageResource(R.drawable.recordflashon);
            onlineText.setText("Online");
        }else{
            redDot.setImageResource(R.drawable.recordflashoff);
            onlineText.setText("Offline");
        }
    }

    private void setDataSource() {
        Log.d(TAG, "setDataSource: ");
        String resolution = "1";
        mVideoView.setResolution(resolution);

        progressBar.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        HashMap<String, String> params = new HashMap<>();
        // set font for ass
        File assFont = new File(Environment.getExternalStorageDirectory(),
                "DroidSansFallback.ttf");
        params.put("ass_default_font_path", assFont.getAbsolutePath());
        params.put("probesize", "5120");
        params.put("max_delay", "0");
        params.put("fflags", "nobuffer");
        params.put("flush_packets", "1");
//        if (isTCP){
            params.put("rtsp_transport", "tcp");
//        }
        mMpegPlayer.setMpegListener(this);
        mMpegPlayer.setDataSource(localURL, params, FFmpegPlayer.UNKNOWN_STREAM, mAudioStreamNo,
                mSubtitleStreamNo, resolution);
//        mMpegPlayer.setDataSource("rtsp://192.168.8.14/cam1/h264", params, FFmpegPlayer.UNKNOWN_STREAM, mAudioStreamNo,
//                mSubtitleStreamNo, resolution);
    }

    // FFMPEG interface implementation

    public void onFFDataSourceLoaded(FFmpegError err, FFmpegStreamInfo[] streams){
        if (err != null){
            String format = "Could not open stream";
            Log.d(TAG, "onFFDataSourceLoaded: " + retry + ", " +
                    "" + format + ", " + err.getMessage());
            progressBar.setVisibility(View.VISIBLE);
            onlineText.setText(R.string.offline);
            onlineText.setTextColor(0xFFFFFF);
//            if (retry < 2 && !isConnected){
//                retry++;
//                repeatTimerDelay(true);
//            }
        } else{
            isConnected = true;
            retry = 0;
            Log.d(TAG, "onFFDataSourceLoaded: loaded");
            progressBar.setVisibility(View.GONE);
            mMpegPlayer.resume();
//            repeatCheck(false);
        }
    }

    public void onFFResume(NotPlayingException result){
        isPlaying = isConnected = true;
        Log.d(TAG, "onFFResume: ");
        playButton.setImageResource(R.drawable.pause);
        playButton.setEnabled(true);
        repeatRedDot(true);
    }

    public void onFFPause(NotPlayingException err){
        isPlaying = false;
        Log.d(TAG, "onFFPause: ");
        playButton.setImageResource(R.drawable.play);
        playButton.setEnabled(true);
        repeatRedDot(false);
    }

    public void onFFStop(){
        isPlaying = false;
        Log.d(TAG, "onFFStop: ");
        playButton.setImageResource(R.drawable.play);
        playButton.setEnabled(true);
        repeatRedDot(false);
    }

    public void onFFUpdateTime(long currentTimeUs, long videoDurationUs, boolean isFinished){
//        Log.d(TAG, "onFFUpdateTime: ");
        if ( isTracking == false){
            mCurrentTimeS = (int)(currentTimeUs / 1000000);
            int videoDurationS = (int)(videoDurationUs / 1000000);
            seekBar.setMax(videoDurationS);
            seekBar.setProgress(mCurrentTimeS);
        }
        if (isFinished == true){
            playButton.setImageResource(R.drawable.play);
            isPlaying = false;
        }
    }

    public void onFFSeeked(NotPlayingException result){
        Log.d(TAG, "onFFSeeked: ");
    }

    //socket manager delegate
    @Override
    public void showToastMessage(final String message, final int duration) {
        if (isAdded()){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "showToastMessage run: " + message);
                }
            });

        }

    }

    @Override
    public void updateFileList(ArrayList<FileContent> fileList) {

    }

    @Override
    public void deviceIsAlive() {
        Log.d(TAG, "deviceIsAlive: ");
        onlineText.setText(R.string.online);
        if (!isPlaying){
            setDataSource();
        }
    }

    @Override
    public void updateSettingContent(String category, String value) {

    }

    @Override
    public void updateSettingContent(String category, JSONObject jsonObject) {

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged: " + String.valueOf(hidden) + " isRestart: " + String.valueOf(isRestart));
        if (isRestart){
            isRestart = false;
        }
    }
    public void stopStreamFromSetting(){
        Log.d(TAG, "stopStreamFromSetting: ");
        if (mMpegPlayer != null){
            mMpegPlayer.stop();
        }
        socketManager.setSocketInterface(this);
        repeatRedDot(false);
        isRestart = true;
    }
	
	public void setResolution(String resolution){
        mVideoView.setResolution(resolution);
    }

    private String getDeviceURL(){
        String cameraName = "Setup Camera " + cameraSerial;
        SharedPreferences preference = getActivity().getSharedPreferences(cameraName, Context.MODE_PRIVATE);
        String urlString = preference.getString("URL", "DEFAULT");
        isTCP = preference.getBoolean("Transmission", false);
        localURL = new String(urlString);
        String [] ipCut = urlString.split("/");
        String ip = ipCut[2];
        String port = preference.getString("Camera Port", "80");
        String url = "http://" + ip + ":" + port +"/";
        return url;
    }

    @Override
    public void showToast(String message) {
        showToastMessage(message, Toast.LENGTH_SHORT);
    }
}

