//References used:
//http://stackoverflow.com/questions/2166961/determining-the-current-foreground-application-from-a-background-task-or-service
//http://stackoverflow.com/questions/10630737/how-to-stop-a-thread-created-by-implementing-runnable-interface
//android permission used:
//android.permission.GET_TASKS

package com.myapplock.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.myapplock.R;
import com.myapplock.interfaces.ActivityStartingListener;
import com.myapplock.models.BlockAppItem;
import com.myapplock.ui.ActivityStartingHandler;
import com.myapplock.ui.HomeActivity;
import com.myapplock.utils.MyAppLockConstansts;
import com.myapplock.utils.MyAppLockPreferences;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class LaunchDetectorService extends Service
{

    private Thread mThread;
    private UsageStatsManager lUsageStatsManager;
    private PackageManager pm;
    private ActivityManager am;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    @Override
    public void onCreate()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            lUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        }
         pm = getApplicationContext().getPackageManager();
         am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
         checkNotificationBarState();
        MyAppLockPreferences.saveBoolToPref(this, MyAppLockConstansts.PREF_SERVICE_ENABLED, true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        handleCommand();
        return Service.START_STICKY;
    }

    private void handleCommand()
    {
        startServiceForeground(R.string.service_running);
        startMonitorThread();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void startServiceForeground(int serviceRunning)
    {
        final Intent i = new Intent();
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification note = new Notification();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                note =
                        new Notification.Builder(this).setContentTitle("MyAppLock")
                                .setSmallIcon(android.R.drawable.ic_dialog_info).setContentText("MyAppLock is active!")
                                .setOngoing(true).setContentIntent(pi).build();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                note =
                        new Notification.Builder(this).setContentTitle("MyAppLock")
                                .setSmallIcon(android.R.drawable.ic_dialog_info).setContentText("MyAppLock is active!")
                                .setOngoing(true).setContentIntent(pi).getNotification();
            }
        } catch (final Throwable e) {
            e.printStackTrace();
        }

        note.tickerText = "MyAppLock is running.";
        note.when = System.currentTimeMillis();
        note.flags |= Notification.FLAG_NO_CLEAR;
        this.startForeground(serviceRunning, note);
    }

    private void checkNotificationBarState() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.statusbar_icon)
                        .setContentTitle("Notifications Example")
                        .setContentText("This is a test notification");

        Intent notificationIntent = new Intent(this, HomeActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(MyAppLockConstansts.PREF_NOTE_ID, builder.build());
    }


    private void startMonitorThread()
    {
        if (mThread != null)
            mThread.interrupt();

        mThread = new MonitorLogThread(new ActivityStartingHandler(this));
        mThread.start();
    }

    private class MonitorLogThread extends Thread
    {
        ActivityStartingListener mListener;

        MonitorLogThread(ActivityStartingListener listener)
        {
            mListener = listener;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void run()
        {

            while (!this.isInterrupted()) {

                try {
                    Thread.sleep(200);
                    BlockAppItem blockAppItem= printForegroundTask();
                    if (mListener != null && null!=blockAppItem) {
                        mListener.onActivityStarting(blockAppItem);
                    }else{
                        if(null!=mListener){
                            mListener.setLastPackageToEmplty();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private BlockAppItem printForegroundTask() {
        String currentApp = "NULL";
        BlockAppItem appItem=null;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                long time = System.currentTimeMillis();
                List<UsageStats> appList = lUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
                if (appList != null && appList.size() > 0) {
                    SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                    for (UsageStats usageStats : appList) {
                        mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    }
                    if (!mySortedMap.isEmpty()) {
                        currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                        if (!TextUtils.isEmpty(currentApp) && currentApp.equalsIgnoreCase("com.google.android.googlequicksearchbox")) {
                            return null;
                        }
                        appItem=new BlockAppItem();
                        String mActivityName = (String) pm.getApplicationLabel(pm.getApplicationInfo(currentApp, PackageManager.GET_META_DATA));
                        appItem.setAppName(mActivityName);
                        appItem.setAppPackageName(currentApp);
                        appItem.setAppIcon(pm.getApplicationIcon(pm.getApplicationInfo(currentApp,PackageManager.GET_META_DATA)));
                    }
                }
            } else {

                List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
                currentApp = tasks.get(0).processName;
                if (!TextUtils.isEmpty(currentApp)) {

                    ApplicationInfo ai;
                    try {
                        ai = pm.getApplicationInfo( this.getPackageName(), 0);
                        String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
                        Drawable icon = pm.getApplicationIcon(currentApp);
                        appItem=new BlockAppItem();
                        appItem.setAppName(applicationName);
                        appItem.setAppPackageName(currentApp);
                        appItem.setAppIcon(icon);

                    } catch (final NameNotFoundException e) {
                        Log.e("TAG","Error: "+e);
                    }
                }
            }
        }catch (Exception e){
             Log.e("TAG","Error: "+e);
        }
        Log.e("adapter", "Current App in foreground is: " + currentApp);
        return appItem;
    }



    @Override
    public void onDestroy()
    {
        mThread.interrupt();
        this.stopForeground(true);

    }
}
