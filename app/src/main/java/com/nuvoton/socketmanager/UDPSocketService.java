package com.nuvoton.socketmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.nuvoton.nuplayer.DeviceData;
import com.nuvoton.nuplayer.R;
import com.nuvoton.nuplayer.Streaming;

public class UDPSocketService extends Service implements BroadcastReceiver.BroadcastInterface {
    public interface BGBCInterface {
        void updateURLToLive(DeviceData deviceData);
    }
    public BGBCInterface bgbcInterface;
    private int mID = 100;
    private final String TAG = "UDPSocketService";
    private BroadcastReceiver bcrReceiver;

    public UDPSocketService() {

    }

    public MyBinder myBinder = new MyBinder();
    public class MyBinder extends Binder{
        public UDPSocketService getService(){
            return UDPSocketService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: " + intent.getBooleanExtra("StopService", false));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
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
    public void signalDataHandled(DeviceData deviceData) {
        Log.d(TAG, "signalDataHandled: " + deviceData.getPublicIP());
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Receive broadcast ring!")
                .setContentText("Click to open NuDoorbell")
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                ;

        Intent resultIntent = new Intent(this, Streaming.class);
        resultIntent.putExtra("ID", deviceData.getId());

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(Streaming.class);
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
            bgbcInterface.updateURLToLive(deviceData);
        }catch (Exception e){
            e.printStackTrace();
        }
//        openWaiting();
//        this.stopSelf();
    }

    private void openWaiting(){
        Log.d(TAG, "openWaiting: ");
        bcrReceiver = BroadcastReceiver.getInstance(this);
        bcrReceiver.openUDPSocket();
        bcrReceiver.setBroadcastInterface(this);
    }

    public void setBgbcInterface(BGBCInterface bgbcInterface) {
        Log.d(TAG, "setBgbcInterface: ");
        this.bgbcInterface = bgbcInterface;
    }
}
