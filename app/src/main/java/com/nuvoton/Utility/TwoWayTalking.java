package com.nuvoton.utility;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import com.nuvoton.socketmanager.SocketInterface;
import com.nuvoton.socketmanager.SocketManager;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by timsheu on 7/21/16.
 */
public class TwoWayTalking implements SocketInterface{

    @Override
    public void showToastMessage(String message, int duration) {

    }

    @Override
    public void updateFileList(ArrayList<FileContent> fileList) {

    }

    @Override
    public void deviceIsAlive() {

    }

    @Override
    public void updateSettingContent(String category, String value) {

    }

    @Override
    public void updateSettingContent(String category, JSONObject jsonObject) {

    }

    @Override
    public void voiceConnectionOpened() {
        Log.d(TAG, "voiceConnectionOpened: ");
        mInterface.didOpenVoiceUpload();
    }

    public interface TwoWayTalkingInterface {
        public void showToast(String message);
        public void didOpenVoiceUpload();
    }
    private static Context context;
    private final String lineEnd = "\r\n";
    private URLConnection conn;
    private HttpURLConnection httpConn;
    private boolean isHTTPMode = true;
    private String localURL;
    private SocketManager socketManager;
    private TwoWayTalkingInterface mInterface;
    private ServerSocket mServerSocket = null;
    private URL url;
    private HttpURLConnection httpURLConnection;
    static public boolean isRecording = false;
    static final String TAG = "TwoWayTalking";
    static final int frequency = 8000;
    static final int inputChannelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    static final int outputChannelConfiguration = AudioFormat.CHANNEL_OUT_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int recBufSize, playBufSize;
    AudioRecord audioRecord;
    AudioTrack audioTrack;
    private static TwoWayTalking twoWayTalking = new TwoWayTalking();
    private TwoWayTalking(){
        recBufSize = AudioRecord.getMinBufferSize(frequency, inputChannelConfiguration, audioEncoding);
        playBufSize = AudioTrack.getMinBufferSize(frequency, outputChannelConfiguration, audioEncoding);
        Log.d(TAG, "instance initializer: " + String.valueOf(recBufSize) + " ," + String.valueOf(playBufSize));
    }
    public static TwoWayTalking getInstance(Context context){
        TwoWayTalking.context = context;
        return twoWayTalking;
    }

    public void startRecording(){
        if (socketManager == null){
            socketManager = new SocketManager();
        }

        isRecording = true;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, inputChannelConfiguration, audioEncoding, recBufSize);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, outputChannelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM, audioRecord.getAudioSessionId());
        if (isHTTPMode){
            Log.d(TAG, "startRecording: HTTPRecordThread");
            new HTTPRecordThread().start();
        }else{
            Log.d(TAG, "startRecording: RecordThread");
            new RecordThread().start();
        }
        mInterface.showToast("Two-way talking started.");
    }

    public void stopRecording(){
        isRecording = false;
    }

    class RecordThread extends Thread{
        public void run(){
            try {
                int mServerPort = 8080;
                mServerSocket = new ServerSocket(mServerPort);
                byte[] buffer = new byte[recBufSize];
                Socket socketClient = mServerSocket.accept();
                OutputStream os = socketClient.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);
                while(true){
                    sleep(500);
                    if (isRecording) break;
                }
                audioRecord.startRecording();
//                audioTrack.play();
                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0, recBufSize);
                    byte[] tmpBuf = new byte[bufferReadResult];
//                    Log.d(TAG, "run: " + String.valueOf(bufferReadResult));
                    System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
//                    audioTrack.write(tmpBuf, 0, tmpBuf.length);
                    dos.write(tmpBuf, 0, tmpBuf.length);
                    dos.flush();
                }

                mServerSocket.close();
                audioRecord.stop();
                audioRecord = null;
                dos.close();
                os.close();
                socketClient.close();
                mInterface.showToast("Two-way talking ends.");
            }catch (Throwable t){
                Log.e(TAG, "Record Thread run: " + t.getMessage() );
            }
        }
    }

    class HTTPRecordThread extends Thread{
        public void run(){
            try {
                byte[] buffer = new byte[recBufSize];
                audioRecord.startRecording();
                audioTrack.play();
                int bufferReadResult = recBufSize;
//                int bufferReadResult = audioRecord.read(buffer, 0, recBufSize);
                byte[] tmpBuf = new byte[bufferReadResult];
                System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                String command = localURL + "audio.input?samplerate=8000&channel=1&volume=100";
//                command = "http://192.168.8.5/"+ "audio.input?samplerate=8000&channel=1&volume=100";
                Log.d(TAG, "run: " + command);
                URL url = new URL(command);
                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setInstanceFollowRedirects(false);
                Log.d(TAG, "executeSendPostTask: in POST");
                httpConn.setRequestMethod("POST");
                httpConn.setRequestProperty("Content-Type", "audio/l16");
                httpConn.setRequestProperty("Content-Length", "268435455");
//                httpConn.setRequestProperty("Content-Encoding", "gzip");
                long uint = 268435455;
//                httpConn.setChunkedStreamingMode(recBufSize);
                Log.d(TAG, "run: " + String.valueOf(uint));
                httpConn.setFixedLengthStreamingMode(uint);
                Log.d(TAG, "run: " + recBufSize);
                httpConn.setDoOutput(true);
                httpConn.setDoInput(true);
                httpConn.setUseCaches(false);
//                int i=0, j=49, tmp=0;
                DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
//                AssetManager am = context.getAssets();
//                AssetFileDescriptor adf = am.openFd("8kHz.wav");
//                FileInputStream fis = adf.createInputStream();
                Log.d(TAG, "run: " + ByteOrder.nativeOrder().toString());
                while (isRecording) {
//                    i%=10;
//                    i++;
//                    tmp = i+j;
                    bufferReadResult = audioRecord.read(buffer, 0, recBufSize);
                    tmpBuf = new byte[bufferReadResult];
//                    fis.read(tmpBuf, 0, tmpBuf.length);
//                    Arrays.fill(tmpBuf, (byte)tmp);
                    System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
//                    audioTrack.write(tmpBuf, 0, tmpBuf.length);
                    if (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)){
                        for (int i=0; i<tmpBuf.length; i+=2){
                            byte temp = tmpBuf[i];
                            tmpBuf[i] = tmpBuf[i+1];
                            tmpBuf[i+1] = temp;
                        }
                    }
                    dos.write(tmpBuf, 0, tmpBuf.length);
                    dos.flush();
                }
                if (dos != null)
                    dos.close();
                if (httpConn != null)
                    httpConn.disconnect();
                audioRecord.stop();
                audioRecord = null;
                mInterface.showToast("HTTP Two-way talking ends.");
            }catch (Exception e){
                if (httpConn != null)
                    httpConn.disconnect();
                if (audioRecord != null)
                    audioRecord.stop();
                e.printStackTrace();
            }
        }
    }

    public void updateURL(String URL){
        localURL = URL;
    }

    public void pokeClient(String URL, String protocol){
        Log.d(TAG, "pokeClient: ");
        if (protocol.compareTo("tcp") == 0){
            String command = URL + "audio.input?protocol=tcp&samplerate=8000&channel=1&volume=100&port=8080";
            localURL = URL;
            SocketManager socketManager = new SocketManager();
            socketManager.setSocketInterface(this);
            socketManager.executeSendGetTask(command, SocketManager.CMDSET_TWOWAY);
        }
    }

    public void setInterface(TwoWayTalkingInterface mInterface){
        twoWayTalking.mInterface = mInterface;
    }

}
