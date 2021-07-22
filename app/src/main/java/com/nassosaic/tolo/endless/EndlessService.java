package com.nassosaic.tolo.endless;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.widget.Toast;

public final class EndlessService extends Service {

    private PowerManager.WakeLock wakeLock;
    private boolean isServiceStarted;

    public static boolean isRunning = false;
    public static boolean autoConnect = false;
    public Handler autoConnectHandler;
    public static int count = 12;

    public IBinder onBind(Intent intent) {
        Utils.log("Some component want to bind with the service");
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.log("onStartCommand executed with startId: " + startId);
        if (intent != null) {
            String action = intent.getAction();
            Utils.log("using an intent with action " + action);
            Actions getAction = Actions.valueOf(action);
            switch (getAction) {
                case START:
                    this.startService();
                    break;
                case STOP:
                    this.stopService();
                    break;
                default:
                    Utils.log("This should never happen. No action in the received intent");
                    break;
            }
        } else {
            Utils.log("with a null intent. It has been probably restarted by the system.");
        }

        return START_STICKY;
    }

    public void onCreate() {
        super.onCreate();
        Utils.log("The service has been created".toUpperCase());
        Notification notification = this.createNotification();
        startForeground(1, notification);
    }

    public void onDestroy() {
        super.onDestroy();
        Utils.log("The service has been destroyed".toUpperCase());
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show();
    }

    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), EndlessService.class);
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Object object = getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        AlarmManager alarmService = (AlarmManager) object;
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + (long) 1000, restartServicePendingIntent);
    }

    private final void startService() {
        if (!isServiceStarted) {
            Utils.log("Starting the foreground service task");
            Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show();
            isServiceStarted = true;
            ServiceTracker.setServiceState(this, ServiceState.STARTED);
            Object object = this.getSystemService(Context.POWER_SERVICE);

            PowerManager powermanager = (PowerManager)object;
            PowerManager.WakeLock wakelock = powermanager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock");
            wakelock.acquire();
            wakeLock = wakelock;

            // we're starting a loop in a looper
            autoConnectHandler = new Handler(Looper.getMainLooper());
            autoConnectHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                        while (isServiceStarted) {
                            // TODO PUT CODE
                        }
                        Utils.log("End of the loop for the service");
                }
            }, 1 * 60 * 1000);
        }
    }

    private final void stopService() {
        Utils.log("Stopping the foreground service");
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show();

        try {
            if (wakeLock != null) {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }

            stopForeground(true);
            stopSelf();
        } catch (Exception exception) {
            Utils.log("Service stopped without being started: " + exception.getMessage());
        }

        isServiceStarted = false;
        ServiceTracker.setServiceState(this, ServiceState.STOPPED);
    }

    private final Notification createNotification() {
        String notificationChannelId = "ENDLESS SERVICE CHANNEL";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Object object = this.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationManager notificationManager = (NotificationManager) object;
            NotificationChannel notificationchannel = new NotificationChannel(
                    notificationChannelId,
                    "Endless Service notifications channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationchannel.setDescription("Endless Service channel");
            notificationchannel.enableLights(true);
            notificationchannel.setLightColor(Color.RED);
            notificationchannel.enableVibration(true);
            notificationchannel.setVibrationPattern(new long[]{100L, 200L, 300L, 400L, 500L, 400L, 300L, 200L, 400L});
            notificationManager.createNotificationChannel(notificationchannel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingintent = PendingIntent.getActivity(this, 0, intent, 0);
        PendingIntent pendingIntent = pendingintent;

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? new Notification.Builder(
                this,
                notificationChannelId
        ) : new Notification.Builder(this);
        Notification notification = builder
                .setContentTitle("Endless Service")
                .setContentText("This is your favorite endless service working")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Ticker text")
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

        return notification;
    }

}
