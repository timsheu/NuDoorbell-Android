package com.nuvoton.nudoorbell;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.widget.Toast;

import com.nuvoton.socketmanager.SocketInterface;
import com.nuvoton.socketmanager.SocketManager;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
    private TwoWayTalkingInterface mInterface;
    private ServerSocket mServerSocket = null;
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
    public static TwoWayTalking getInstance(){
        return twoWayTalking;
    }

    public void startRecording(){
        isRecording = true;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, inputChannelConfiguration, audioEncoding, recBufSize);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, outputChannelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM, audioRecord.getAudioSessionId());
        new RecordThread().start();
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

    public void pokeClient(String URL, String protocol){
        Log.d(TAG, "pokeClient: ");
        if (protocol.compareTo("tcp") == 0){
            String command = URL + "audio.input?protocol=tcp&samplerate=8000&channel=1&volume=100&port=8080";
            SocketManager socketManager = new SocketManager();
            socketManager.setSocketInterface(this);
            socketManager.executeSendGetTask(command, SocketManager.CMDSET_TWOWAY);
        }
    }

    public void setInterface(TwoWayTalkingInterface mInterface){
        twoWayTalking.mInterface = mInterface;
    }
}
