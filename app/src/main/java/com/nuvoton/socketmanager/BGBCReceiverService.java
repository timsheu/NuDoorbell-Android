package com.nuvoton.socketmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Parcel;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.nuvoton.nudoorbell.LivePage;
import com.nuvoton.nudoorbell.R;

import org.acra.log.AndroidLogDelegate;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.Charset;

public class BGBCReceiverService extends Service implements BroadcastReceiver.BCRInterface{
    public interface BGBCInterface {
        void updateURLToLive(String URL);
    }
    public BGBCInterface bgbcInterface;
    private int mID = 100;
    private final String TAG = "BGBCReceiverService";
    private BroadcastReceiver bcrReceiver;

    public BGBCReceiverService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: " + intent.getBooleanExtra("StopService", false));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        openWaiting();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BroadcastReceiver.getInstance(this).closeUDPSocket();
        Intent intent = new Intent("com.nuvoton.ActivityRecognition.RestartService");
        sendBroadcast(intent);
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void signalDataHandled(String URL) {
        Log.d(TAG, "signalDataHandled: " + URL);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Receive broadcast ring!")
                .setContentText("Click to open NuDoorbell")
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                ;

        Intent resultIntent = new Intent(this, LivePage.class);
        resultIntent.putExtra("URL", URL);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(LivePage.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarm);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mID, mBuilder.build());
        try {
            bgbcInterface.updateURLToLive(URL);
        }catch (Exception e){
            e.printStackTrace();
        }
//        openWaiting();
        this.stopSelf();
    }

    private void openWaiting(){
        Log.d(TAG, "openWaiting: ");
        bcrReceiver = BroadcastReceiver.getInstance(this);
        bcrReceiver.closeUDPSocket();
        bcrReceiver.openUDPSocket();
        bcrReceiver.setBcrInterface(this);
    }

    public void setBgbcInterface(BGBCInterface bgbcInterface) {
        Log.d(TAG, "setBgbcInterface: ");
        this.bgbcInterface = bgbcInterface;
    }
}
