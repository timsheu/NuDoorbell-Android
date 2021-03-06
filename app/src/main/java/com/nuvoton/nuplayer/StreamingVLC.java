package com.nuvoton.nuplayer;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.nuvoton.socketmanager.HTTPSocketInterface;
import com.nuvoton.socketmanager.VolleyManager;
import com.nuvoton.utility.NuDoorbellCommand;
import com.nuvoton.utility.NuPlayerCommand;
import com.nuvoton.utility.TwoWayTalking;

import org.json.JSONArray;
import org.json.JSONObject;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RunnableFuture;
import java.util.stream.Stream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StreamingVLC extends AppCompatActivity implements TwoWayTalking.TwoWayTalkingInterface, HTTPSocketInterface, IVLCVout.OnNewVideoLayoutListener{
    private RequestQueue volleyQueue;
    private static String TAG = "StreamingVLC";
    private final Handler mHandler = new Handler();
    private View.OnLayoutChangeListener mOnLayoutChangeListener = null;
    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoHeight = 0;
    private int mVideoWidth = 0;
    private int mVideoVisibleHeight = 0;
    private int mVideoVisibleWidth = 0;
    private int mVideoSarNum = 0;
    private int mVideoSarDen = 0;
    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_SCREEN = 1;
    private static final int SURFACE_FILL = 2;
    private static final int SURFACE_16_9 = 3;
    private static final int SURFACE_4_3 = 4;
    private static final int SURFACE_ORIGINAL = 5;
    private static int CURRENT_SIZE = SURFACE_BEST_FIT;
    private boolean isConnected = false;
    private TwoWayTalking mTwoWayTalking;
    private boolean isDuplex = true;
    private int retry = 0;
    private boolean isPlaying = false, isTracking = false;
    private int mCurrentTimeS;
    private View thisView;
    private Timer delayConnectTimer;
    private boolean isGranted = false;
    private String localURL;
    private long localId;
    private DeviceData deviceData;
    private boolean isAEWindow = false;

    //Bind view with ButterKnife
    @BindView(R.id.video_surface_frame) FrameLayout mVideoSurfaceFrame;
    @BindView(R.id.video_surface)   SurfaceView mVideoSurface;
    @BindView(R.id.seekBar)         SeekBar seekBar;
    @BindView(R.id.playButton)      ImageButton playButton;
    @BindView(R.id.expandButton)    ImageButton expandButton;
    @BindView(R.id.snapshotButton)  ImageButton snapshotButton;
    @BindView(R.id.redDot)          ImageView redDot;
    @BindView(R.id.onlineText)      TextView onlineText;
    @BindView(R.id.progressBar)     ProgressBar progressBar;
    @BindView(R.id.phone_hang)      ImageButton phoneHang;
    @BindView(R.id.phone_ans)       ImageButton phoneAns;

    //Tow way talking interface implementation
    @Override
    public void showToast(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StreamingVLC.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void didOpenVoiceUpload() {

    }

    //MARK: http socket interface
    @Override
    public void httpSocketResponse(Map<String, Object> responseMap) {
        Log.d(TAG, "httpSocketResponse: " + responseMap);
        VolleyManager.HTTPSocketTags socketTag = (VolleyManager.HTTPSocketTags) responseMap.get("socketTag");
        String returnValue = (String) responseMap.get("value");
        if (returnValue.compareTo("0") == 0){
            if (socketTag == VolleyManager.HTTPSocketTags.UPDATE_VIDEO_AE_WINDOW){
                Toast.makeText(getApplicationContext(), "Send AE command success", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void voiceConnectionOpened() {

    }

    @Override
    public void didDisconnected() {

    }

    //    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mVideoSarNum = sarNum;
        mVideoSarDen = sarDen;
        updateVideoSurfaces();
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming_vlc);
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

        Dexter.withActivity(this).
                withPermission(android.Manifest.permission.RECORD_AUDIO)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Log.d(TAG, "onPermissionGranted: permitted");
                        isGranted = true;
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Log.d(TAG, "onPermissionDenied: not permitted");
                        isGranted = false;
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
        final ArrayList<String> args = new ArrayList<>();
        args.add("--rtsp-tcp");
//        args.add("--audio-time-stretch"); // time stretching
        args.add("--swscale-mode=0");
        args.add("--file-caching=1000");
        args.add("--network-caching=1000");
        args.add("--avcodec-skip-frame");
        args.add("--avcodec-hw=any");

//        args.add("-vvv");
//        args.add("--file-caching=500");
//        args.add("--network-caching=500");
        args.add("--clock-jitter=10");
        args.add("--clock-synchro=10");
//        args.add("--rtp-max-dropout=50");
//        args.add("--rtp-max-misorder=50");
//        args.add("--rtsp-frame-buffer-size=128");
//        args.add("--avcodec-hw=no");
        mLibVLC = new LibVLC(this, args);
        mMediaPlayer = new MediaPlayer(mLibVLC);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mLibVLC.release();
    }

    @Override
    protected void onStart() {
        super.onStart();

        final IVLCVout vlcVout = mMediaPlayer.getVLCVout();
        vlcVout.setVideoView(mVideoSurface);
        vlcVout.attachViews(this);

        Media media = new Media(mLibVLC, Uri.parse(localURL));
        media.setHWDecoderEnabled(true, true);
        media.addOption(":network-caching=1000");
        media.addOption(":clock-jitter=10");
        media.addOption(":clock-synchro=10");
        mMediaPlayer.setMedia(media);
//        media.release();
        mMediaPlayer.play();

        if (mOnLayoutChangeListener == null) {
            mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
                private final Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        updateVideoSurfaces();
                    }
                };
                @Override
                public void onLayoutChange(View v, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                        mHandler.removeCallbacks(mRunnable);
                        mHandler.post(mRunnable);
                    }
                }
            };
        }
        mVideoSurfaceFrame.addOnLayoutChangeListener(mOnLayoutChangeListener);
//        mVideoSurfaceFrame.setOnTouchListener(AEWindowOnTouchListener);
    }

    protected void onStop() {
        super.onStop();

        if (mOnLayoutChangeListener != null) {
            mVideoSurfaceFrame.removeOnLayoutChangeListener(mOnLayoutChangeListener);
            mOnLayoutChangeListener = null;
        }

        mMediaPlayer.stop();

        mMediaPlayer.getVLCVout().detachViews();
    }

    @OnClick({R.id.playButton, R.id.expandButton, R.id.snapshotButton, R.id.redDot, R.id.phone_hang, R.id.phone_ans})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.snapshotButton:
                String snapshotCommand = NuPlayerCommand.snapshot();
                if (deviceData.deviceType.compareTo("NuDoorbell") == 0){
                    snapshotCommand = NuDoorbellCommand.snapshot();
                }
                VolleyManager.getShared(getApplicationContext()).setHttpSocketInterface(this);
                VolleyManager.getShared(getApplicationContext()).sendCommand(snapshotCommand, VolleyManager.HTTPSocketTags.DEFAULT);
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
//                    mMpegPlayer.pause();
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
                        mTwoWayTalking.setInterface(StreamingVLC.this);
                        mTwoWayTalking.setHTTPMode(deviceData.getVoiceUploadHttp());
                        mTwoWayTalking.setDeviceID(deviceData.getId());
                        if (!mTwoWayTalking.isRecording) {
                            Toast.makeText(StreamingVLC.this, "Audio upload started.", Toast.LENGTH_SHORT).show();
                            boolean isHttpVoice = true;
                            isHttpVoice = deviceData.isVoiceUploadHttp;
                            if (isHttpVoice) {
                                mTwoWayTalking.updateURL(localURL);
                            } else {
                                mTwoWayTalking.pokeClient(localURL, "tcp", true);
                            }
                            mTwoWayTalking.startRecording();
                            phoneAns.setEnabled(false);
                            phoneHang.setEnabled(true);
                        }
                    }
                }else{
                    Toast.makeText(StreamingVLC.this, "The permission is not granted, please kill the app and authorize it in the setting!", Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.phone_hang:
                Log.d(TAG, "onClick: phone_hang");
                if (isDuplex){
                    mTwoWayTalking = TwoWayTalking.getInstance(getApplicationContext());
                    mTwoWayTalking.setInterface(StreamingVLC.this);
                    if (mTwoWayTalking.isRecording){
                        Toast.makeText(StreamingVLC.this, "Stop audio upload.", Toast.LENGTH_SHORT).show();
                        mTwoWayTalking.stopRecording();
                        phoneAns.setEnabled(true);
                        phoneHang.setEnabled(false);
                        mTwoWayTalking.pokeClient(localURL, "tcp", false);
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
    private void updateVideoSurfaces() {
        int sw = getWindow().getDecorView().getWidth();
        int sh = getWindow().getDecorView().getHeight();

        // sanity check
        if (sw * sh == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }

        mMediaPlayer.getVLCVout().setWindowSize(sw, sh);

        ViewGroup.LayoutParams lp = mVideoSurface.getLayoutParams();
        if (mVideoWidth * mVideoHeight == 0) {
            /* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API */
            lp.width  = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoSurface.setLayoutParams(lp);
            mVideoSurface.setOnTouchListener(AEWindowOnTouchListener);
            lp = mVideoSurfaceFrame.getLayoutParams();
            lp.width  = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoSurfaceFrame.setLayoutParams(lp);
            changeMediaPlayerLayout(sw, sh);
            return;
        }

        if (lp.width == lp.height && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            /* We handle the placement of the video using Android View LayoutParams */
            mMediaPlayer.setAspectRatio(null);
            mMediaPlayer.setScale(0);
        }

        double dw = sw, dh = sh;
        final boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mVideoSarDen == mVideoSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double)mVideoVisibleWidth / (double)mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double)mVideoSarNum / mVideoSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_SCREEN:
                if (dar >= ar)
                    dh = dw / ar; /* horizontal */
                else
                    dw = dh * ar; /* vertical */
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        // set display size
        lp.width  = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        mVideoSurface.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = mVideoSurfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        mVideoSurfaceFrame.setLayoutParams(lp);

        mVideoSurface.invalidate();
    }

    private void changeMediaPlayerLayout(int displayW, int displayH) {
        /* Change the video placement using the MediaPlayer API */
        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                mMediaPlayer.setAspectRatio(null);
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_FIT_SCREEN:
            case SURFACE_FILL: {
                Media.VideoTrack vtrack = mMediaPlayer.getCurrentVideoTrack();
                if (vtrack == null)
                    return;
                final boolean videoSwapped = vtrack.orientation == Media.VideoTrack.Orientation.LeftBottom
                        || vtrack.orientation == Media.VideoTrack.Orientation.RightTop;
                if (CURRENT_SIZE == SURFACE_FIT_SCREEN) {
                    int videoW = vtrack.width;
                    int videoH = vtrack.height;

                    if (videoSwapped) {
                        int swap = videoW;
                        videoW = videoH;
                        videoH = swap;
                    }
                    if (vtrack.sarNum != vtrack.sarDen)
                        videoW = videoW * vtrack.sarNum / vtrack.sarDen;

                    float ar = videoW / (float) videoH;
                    float dar = displayW / (float) displayH;

                    float scale;
                    if (dar >= ar)
                        scale = displayW / (float) videoW; /* horizontal */
                    else
                        scale = displayH / (float) videoH; /* vertical */
                    mMediaPlayer.setScale(scale);
                    mMediaPlayer.setAspectRatio(null);
                } else {
                    mMediaPlayer.setScale(0);
                    mMediaPlayer.setAspectRatio(!videoSwapped ? ""+displayW+":"+displayH
                            : ""+displayH+":"+displayW);
                }
                break;
            }
            case SURFACE_16_9:
                mMediaPlayer.setAspectRatio("16:9");
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_4_3:
                mMediaPlayer.setAspectRatio("4:3");
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_ORIGINAL:
                mMediaPlayer.setAspectRatio(null);
                mMediaPlayer.setScale(1);
                break;
        }
    }

    View.OnTouchListener AEWindowOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (isAEWindow == true){
                return true;
            }
            isAEWindow = true;
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                Log.d(TAG, "onTouch:Touch coordinates : " +
                        String.valueOf(event.getX()) + "x" + String.valueOf(event.getY()) +
                "\nview size " + String.valueOf(mVideoSurface.getWidth()) + "x" + String.valueOf(mVideoSurface.getHeight()));
            }
            float xPositionRatio = event.getX() / mVideoSurface.getWidth(),
                    yPositionRatio = event.getY() / mVideoSurface.getHeight();
            if (xPositionRatio < 0.25F){
                xPositionRatio = 0.25F;
            }else if (xPositionRatio > 0.75F){
                xPositionRatio = 0.75F;
            }

            if (yPositionRatio < 0.25F){
                yPositionRatio = 0.25F;
            }else if (yPositionRatio > 0.75F){
                yPositionRatio = 0.75F;
            }
            int startX, endX, startY, endY;
            String resolution = deviceData.getResolution();
            if (resolution.compareTo("QVGA") == 0){
                startX = (int)((xPositionRatio - 0.25F) * 320);
                endX = (int)((xPositionRatio + 0.25F) * 320);
                startY = (int)((yPositionRatio - 0.25F) * 240);
                endY = (int)((yPositionRatio + 0.25F) * 240);
            }else if (resolution.compareTo("VGA") == 0){
                startX = (int)((xPositionRatio - 0.25F) * 640);
                endX = (int)((xPositionRatio + 0.25F) * 640);
                startY = (int)((yPositionRatio - 0.25F) * 480);
                endY = (int)((yPositionRatio + 0.25F) * 480);
            }else if (resolution.compareTo("720p") == 0){
                startX = (int)((xPositionRatio - 0.25F) * 1280);
                endX = (int)((xPositionRatio + 0.25F) * 1280);
                startY = (int)((yPositionRatio - 0.25F) * 720);
                endY = (int)((yPositionRatio + 0.25F) * 720);
            }else if (resolution.compareTo("1080p") == 0){ //1080p
                startX = (int)((xPositionRatio - 0.25F) * 1920);
                endX = (int)((xPositionRatio + 0.25F) * 1920);
                startY = (int)((yPositionRatio - 0.25F) * 1080);
                endY = (int)((yPositionRatio + 0.25F) * 1080);
            }else {
                startX = (int)((xPositionRatio - 0.25F) * 640);
                endX = (int)((xPositionRatio + 0.25F) * 640);
                startY = (int)((yPositionRatio - 0.25F) * 368);
                endY = (int)((yPositionRatio + 0.25F) * 368);
            }
            Log.d(TAG, "onTouch: " + startX + ", " + endX + ", " + startY + ", " + endY);
            String command = "http://" + deviceData.getPrivateIP() + ":" + deviceData.getHttpPort();
            command += NuDoorbellCommand.setAEWindow(startX, endX, startY, endY);
            if (volleyQueue == null){
                volleyQueue = VolleyManager.getShared(getApplicationContext()).mQueue;
            }
            VolleyManager.getShared(getApplicationContext()).setHttpSocketInterface(StreamingVLC.this);
            VolleyManager.getShared(getApplicationContext()).sendCommand(command, VolleyManager.HTTPSocketTags.UPDATE_VIDEO_AE_WINDOW);
            Toast.makeText(StreamingVLC.this, "Set AE Window, delay 5 seconds", Toast.LENGTH_SHORT).show();
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    setAEWindow(false);
                }
            }, 5000);
            return true;
        }
    };

    public void setAEWindow(boolean AEWindow) {
        isAEWindow = AEWindow;
    }
}
