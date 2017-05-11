package com.nuvoton.utility;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AutomaticGainControl;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.nuvoton.nuplayer.DeviceData;
import com.nuvoton.socketmanager.HTTPSocketInterface;
import com.nuvoton.socketmanager.HTTPSocketManager;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by timsheu on 7/21/16.
 */
public class TwoWayTalking implements HTTPSocketInterface{

    @Override
    public void httpSocketResponse(Map<String, Object> responseMap) {

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

    public void setDeviceID(long deviceID) {
        this.deviceID = deviceID;
    }

    private long deviceID = 0;
    private static Context context;
    private final String lineEnd = "\r\n";
    private URLConnection conn;
    private HttpURLConnection httpConn;

    public void setHTTPMode(boolean HTTPMode) {
        isHTTPMode = HTTPMode;
    }

    private boolean isHTTPMode = true;
    private String localURL;
    private HTTPSocketManager socketManager;
    private TwoWayTalkingInterface mInterface;
//    private ServerSocket mServerSocket = null;
    private DatagramSocket mServerSocket = null;
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
    private String ip;
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
        DeviceData deviceData = DeviceData.findById(DeviceData.class, deviceID);
        ip = deviceData.getPublicIP();
        if (socketManager == null){
            socketManager = new HTTPSocketManager();
        }
        isRecording = true;
        if (deviceData.getIsAECOn()){
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, frequency, inputChannelConfiguration, audioEncoding, recBufSize);
        }else {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, inputChannelConfiguration, audioEncoding, recBufSize);
        }

        audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, frequency, outputChannelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM, audioRecord.getAudioSessionId());
        if (isHTTPMode){
            Log.d(TAG, "startRecording: HTTPRecordThread");
            new HTTPRecordThread().start();
        }else{
            Log.d(TAG, "startRecording: RecordThread");
            new RecordThread().start();
//            new UDPThread().start();
        }
        mInterface.showToast("Two-way talking started.");
    }

    public void stopRecording(){
        isRecording = false;
        try {
            mServerSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    class UDPThread extends Thread{
        public void run(){
            try {
                int mServerPort = 8080;
                DatagramSocket localSocket = new DatagramSocket(4869);
                localSocket.connect(InetAddress.getLocalHost(), mServerPort);
                byte[] data = new byte[recBufSize];
                localSocket.send(new DatagramPacket(data, data.length));
                audioTrack.play();
                while (isRecording) {
                    byte[] temp = new byte[recBufSize];
                    DatagramPacket packet = new DatagramPacket(temp, temp.length);
                    localSocket.receive(packet);
                    audioTrack.write(packet.getData(), 0, packet.getLength());
                    Log.d(TAG, "UDPThread, length: " + packet.getLength());
                }

                localSocket.close();
            }catch (Throwable t){
                Log.e(TAG, "Record Thread run: " + t.getMessage() );
            }
        }
    }

    class RecordThread extends Thread{
        public void run(){
            try {
                int mServerPort = 8080;
                mServerSocket = new DatagramSocket(mServerPort);
                byte[] buffer = new byte[recBufSize];
                DatagramPacket dp = new DatagramPacket(buffer, recBufSize);
                mInterface.showToast("RecordThread: before receive");
                mServerSocket.receive(dp);
                mInterface.showToast("RecordThread: after receive");
                InetAddress address = dp.getAddress();
                int port = dp.getPort();
//                mServerSocket = new ServerSocket(mServerPort);
//                byte[] buffer = new byte[recBufSize];
//                Socket socketClient = mServerSocket.accept();
//                OutputStream os = socketClient.getOutputStream();
//                DataOutputStream dos = new DataOutputStream(os);
//                while(true){
//                    Log.d(TAG, "run: sleep");
//                    sleep(500);
//                    if (mServerSocket.isConnected() || mServerSocket.isClosed()) break;
//                }
                mInterface.showToast("RecordThread: start record");
                audioRecord.startRecording();
                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0, recBufSize);
                    byte[] tmpBuf = new byte[bufferReadResult];
//                    Log.d(TAG, "run: " + String.valueOf(bufferReadResult));
                    System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                    DatagramPacket packet = new DatagramPacket(tmpBuf, tmpBuf.length, address, port);
                    mServerSocket.send(packet);
//                    audioTrack.write(tmpBuf, 0, tmpBuf.length);
                }
                mInterface.showToast("RecordThread: stop record");


////                audioTrack.play();
//                while (isRecording) {
//                    int bufferReadResult = audioRecord.read(buffer, 0, recBufSize);
//                    byte[] tmpBuf = new byte[bufferReadResult];
////                    Log.d(TAG, "run: " + String.valueOf(bufferReadResult));
//                    System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
////                    audioTrack.write(tmpBuf, 0, tmpBuf.length);
//                    dos.write(tmpBuf, 0, tmpBuf.length);
//                    dos.flush();
//                }

                mServerSocket.close();
                audioRecord.stop();
                audioRecord = null;
//                dos.close();
//                os.close();
//                socketClient.close();
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
//                audioTrack.play();
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
                byte[] zero = new byte[0];
                dos.write(zero);
                dos.flush();
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
        String[] strings = URL.split("/");
        localURL = "http://" + strings[2] + "/";
    }

    public void pokeClient(String URL, String protocol, Boolean open){
        String [] strings = URL.split("/");
        String url = strings[2];
        Log.d(TAG, "pokeClient: ");
        if (open){
            if (protocol.compareTo("tcp") == 0){
                String command = "http://" + url + "/audio.input?protocol=tcp&samplerate=8000&channel=1&volume=100&port=8080";
                localURL = url;
                HTTPSocketManager socketManager = new HTTPSocketManager();
                socketManager.setSocketInterface(this);
                socketManager.executeSendGetTask(command, HTTPSocketManager.HTTPSocketTags.UPLOAD_AUDIO_STREAM.getString());
            }
        }else {
            String command = "http://" + url + "/audio.stop";
            localURL = url;
            HTTPSocketManager socketManager = new HTTPSocketManager();
            socketManager.setSocketInterface(this);
            socketManager.executeSendGetTask(command, HTTPSocketManager.HTTPSocketTags.UPLOAD_AUDIO_STREAM.getString());
        }

    }

    public void setInterface(TwoWayTalkingInterface mInterface){
        twoWayTalking.mInterface = mInterface;
    }

    @Override
    public void didDisconnected() {

    }
}
