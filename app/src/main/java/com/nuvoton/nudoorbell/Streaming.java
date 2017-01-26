package com.nuvoton.nudoorbell;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.ffmpeg.FFmpegDisplay;
import com.appunite.ffmpeg.FFmpegError;
import com.appunite.ffmpeg.FFmpegListener;
import com.appunite.ffmpeg.FFmpegPlayer;
import com.appunite.ffmpeg.FFmpegStreamInfo;
import com.appunite.ffmpeg.FFmpegSurfaceView;
import com.appunite.ffmpeg.NotPlayingException;
import com.nuvoton.socketmanager.HTTPSocketInterface;
import com.nuvoton.socketmanager.HTTPSocketManager;
import com.nuvoton.utility.NuDoorbellCommand;
import com.nuvoton.utility.NuPlayerCommand;
import com.nuvoton.utility.NuWicamCommand;
import com.nuvoton.utility.TwoWayTalking;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Streaming extends AppCompatActivity implements FFmpegListener, TwoWayTalking.TwoWayTalkingInterface, HTTPSocketInterface{
    private static String TAG = "Streaming";
    private boolean isConnected = false;
    private TwoWayTalking mTwoWayTalking;
    private boolean isDuplex = true;
    private int retry = 0;
    private boolean isPlaying = false, isTracking = false;
    private int mCurrentTimeS;
    private View thisView;
    private FFmpegPlayer mMpegPlayer;
    private FFmpegSurfaceView mVideoView;
    private int mAudioStreamNo = FFmpegPlayer.UNKNOWN_STREAM;
    private int mSubtitleStreamNo = FFmpegPlayer.NO_STREAM;
    private Timer delayConnectTimer;
    private boolean isGranted = false;
    private boolean isHide = false;
    private String localURL;
    private long localId;
    private DeviceData deviceData;
    OnHideBottomBarListener onHideBottomBarListener;

    //Tow way talking interface implementation
    @Override
    public void showToast(String message) {

    }

    @Override
    public void didOpenVoiceUpload() {

    }

    //MARK: http socket interface
    @Override
    public void httpSocketResponse(Map<String, Object> responseMap) {
        Log.d(TAG, "httpSocketResponse: " + responseMap);
    }

    public interface OnHideBottomBarListener{
        public void onHideBottomBar(boolean isHide);
        public void onEnableClick(boolean isEnable);
    }

    //Bind view with ButterKnife
    @BindView(R.id.videoView)       FFmpegSurfaceView videoView;
    @BindView(R.id.seekBar)         SeekBar seekBar;
    @BindView(R.id.playButton)      ImageButton playButton;
    @BindView(R.id.expandButton)    ImageButton expandButton;
    @BindView(R.id.snapshotButton)  ImageButton snapshotButton;
    @BindView(R.id.redDot)          ImageView redDot;
    @BindView(R.id.onlineText)      TextView onlineText;
    @BindView(R.id.progressBar)     ProgressBar progressBar;
    @BindView(R.id.phone_hang)      ImageButton phoneHang;
    @BindView(R.id.phone_ans)       ImageButton phoneAns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        if (intent != null){
            Log.d(TAG, "onCreate, intent extras: " + intent.getExtras().keySet());
            localId = (long) intent.getExtras().get("ID");
        }else if (savedInstanceState != null){
            Log.d(TAG, "onCreate: " + savedInstanceState);
        }
        deviceData = DeviceData.findById(DeviceData.class, localId);
        localURL = "rtsp://" + deviceData.getPublicIP() + "/cam1/h264";
        if (deviceData.deviceType.compareTo("NuWicam") == 0){
            snapshotButton.setEnabled(false);
        }
        setupFFMPEGSurface();
        setDataSource();
    }

    @OnClick({R.id.playButton, R.id.expandButton, R.id.snapshotButton, R.id.redDot, R.id.phone_hang, R.id.phone_ans})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.snapshotButton:
                String snapshotCommand = NuPlayerCommand.snapshot();
                if (deviceData.deviceType.compareTo("NuDoorbell") == 0){
                    snapshotCommand = NuDoorbellCommand.snapshot();
                }
                HTTPSocketManager httpSocketManager = new HTTPSocketManager();
                httpSocketManager.setHttpSocketInterface(this);
                httpSocketManager.executeSendGetTask(snapshotCommand);
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
                if (isGranted){
                    if (isDuplex) {
                        mTwoWayTalking = TwoWayTalking.getInstance(getApplicationContext());
                        mTwoWayTalking.setInterface(Streaming.this);
                        if (!mTwoWayTalking.isRecording) {
                            Toast.makeText(Streaming.this, "Audio upload started.", Toast.LENGTH_SHORT).show();
                            boolean isHttpVoice = true;
                            isHttpVoice = deviceData.isVoiceUploadHttp;
                            if (isHttpVoice) {
                                mTwoWayTalking.updateURL(localURL);
                            } else {
                                mTwoWayTalking.pokeClient(localURL, "tcp");
                            }
                            mTwoWayTalking.startRecording();
                            phoneAns.setEnabled(false);
                            phoneHang.setEnabled(true);
                        }
                    }
                }else{
                    Toast.makeText(Streaming.this, "The permission is not granted, please kill the app and authorize it in the setting!", Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.phone_hang:
                Log.d(TAG, "onClick: phone_hang");
                if (isDuplex){
                    mTwoWayTalking = TwoWayTalking.getInstance(getApplicationContext());
                    mTwoWayTalking.setInterface(Streaming.this);
                    if (mTwoWayTalking.isRecording){
                        Toast.makeText(Streaming.this, "Stop audio upload.", Toast.LENGTH_SHORT).show();
                        mTwoWayTalking.stopRecording();
                        phoneAns.setEnabled(true);
                        phoneHang.setEnabled(false);
                    }
                }
                break;
            default:
                break;
        }
    }

    //UI interactions
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

    //Utility

    //FFMPEG
    public void setupFFMPEGSurface(){
            mVideoView = (FFmpegSurfaceView) findViewById(R.id.videoView);
            mVideoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: ");
                    isHide = !isHide;
                    onHideBottomBarListener.onHideBottomBar(isHide);
                    return false;
                }
            });
            mMpegPlayer = new FFmpegPlayer((FFmpegDisplay) mVideoView, this);
    }

    private void setDataSource() {
        Log.d(TAG, "setDataSource: ");

        String resolution = "0";
//        mVideoView.setResolution(resolution);

        progressBar.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        HashMap<String, String> params = new HashMap<>();
        // set font for ass
        params.put("probesize", "5120");
        params.put("max_delay", "0");
        params.put("fflags", "nobuffer");
        params.put("flush_packets", "1");
        boolean isTCP = deviceData.getTCPTransmission();
        isTCP = deviceData.isTCPTransmission;
        if (isTCP){
            params.put("rtsp_transport", "tcp");
        }
        mMpegPlayer.setMpegListener(this);
        mMpegPlayer.setDataSource(localURL, params, FFmpegPlayer.UNKNOWN_STREAM, mAudioStreamNo,
                mSubtitleStreamNo, resolution);
//        mMpegPlayer.setDataSource("rtsp://192.168.8.14/cam1/h264", params, FFmpegPlayer.UNKNOWN_STREAM, mAudioStreamNo,
//                mSubtitleStreamNo, resolution);
    }

    //MARK: FFMPEG interface implementation

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
        if ( !isTracking){
            mCurrentTimeS = (int)(currentTimeUs / 1000000);
            int videoDurationS = (int)(videoDurationUs / 1000000);
            seekBar.setMax(videoDurationS);
            seekBar.setProgress(mCurrentTimeS);
        }
        if (isFinished){
            playButton.setImageResource(R.drawable.play);
            isPlaying = false;
        }
    }

    public void onFFSeeked(NotPlayingException result){
        Log.d(TAG, "onFFSeeked: ");
    }
}
