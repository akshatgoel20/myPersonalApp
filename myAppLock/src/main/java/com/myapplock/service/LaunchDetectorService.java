//References used:
//http://stackoverflow.com/questions/2166961/determining-the-current-foreground-application-from-a-background-task-or-service
//http://stackoverflow.com/questions/10630737/how-to-stop-a-thread-created-by-implementing-runnable-interface
//android permission used:
//android.permission.GET_TASKS

package com.myapplock.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.myapplock.ui.ActivityStartingHandler;
import com.myapplock.ui.HomeActivity;
import com.myapplock.R;
import com.myapplock.interfaces.ActivityStartingListener;
import com.myapplock.models.BlockAppItem;
import com.myapplock.utils.MyAppLockConstansts;
import com.myapplock.utils.MyAppLockPreferences;

import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Matcher;

public class LaunchDetectorService extends Service
{
    public static final String ACTION_DETECTOR_SERVICE = "com.gueei.detector.service";

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private static Thread mThread;

    private NotificationManager mNM;

    Matcher m;

    private static boolean constantInited = false;

    @Override
    public void onCreate()
    {
        // debug: log.i("Detector","Service.Oncreate");
        initConstant();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        checkNotificationBarState();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        handleCommand(intent);
        return Service.START_STICKY;
    }

    private void handleCommand(Intent intent)
    {
        startServiceForeground(R.string.service_running);

        startMonitorThread((ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE));
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

    @SuppressWarnings("deprecation")
    private void checkNotificationBarState()
    {
        boolean show_notificationbar =
                MyAppLockPreferences.getBoolFromPref(this, MyAppLockConstansts.PREF_SHOW_NOTIFICATION_BAR, false);
        if (show_notificationbar) {
            CharSequence text = getText(R.string.service_running);

            Notification notification = new Notification(R.drawable.statusbar_icon, text, System.currentTimeMillis());

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, HomeActivity.class), 0);

            notification.setLatestEventInfo(this, text, text, contentIntent);
            mNM.notify(MyAppLockConstansts.PREF_NOTE_ID, notification);
        }
    }

    // private void startMonitorThread(final ActivityManager am)
    // {
    // if (mThread != null)
    // mThread.interrupt();
    //
    // mThread = new MonitorlogThread(new ActivityStartingHandler(this));
    // mThread.start();
    // }

    private void initConstant()
    {
        // debug: log.i("Detector","Service.OninitConstant");
        if (constantInited)
            return;
    }

    private void startMonitorThread(final ActivityManager am)
    {
        if (mThread != null)
            mThread.interrupt();

        mThread = new MonitorlogThread(new ActivityStartingHandler(this));
        mThread.start();
    }

    private class MonitorlogThread extends Thread
    {

        ActivityStartingListener mListener;

        public MonitorlogThread(ActivityStartingListener listener)
        {
            mListener = listener;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void run()
        {

            while (!this.isInterrupted()) {

                try {
                    boolean callActivityHandler = true;
                    Thread.sleep(200);
                    // //debug: log.i("Detector","try!");
                    // This is the code I use in my service to identify the current foreground application, its really
                    // easy:

                    ActivityManager am = (ActivityManager) getBaseContext().getSystemService(ACTIVITY_SERVICE);
                    PackageManager pm = getBaseContext().getPackageManager();
                    ActivityManager.RunningAppProcessInfo runningProcessInfo = null;
                    RunningTaskInfo info = null;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        runningProcessInfo = getTopLauncherActivAppProcess(am);
                        if (runningProcessInfo == null) {
                            callActivityHandler = false;
                        }
                    } else {
                        info = am.getRunningTasks(1).get(0);
                    }
                    BlockAppItem blockAppItem = getLaunchingAppDetails(pm, info, runningProcessInfo);
                    String foregroundTaskPackageName = getTopLauncherPackageName(info, runningProcessInfo);

                    String foregroundTaskActivityName = getTopLauncherActivityName(pm, info, runningProcessInfo);

                    Log.e("", "aName: " + foregroundTaskActivityName + " pName: " + foregroundTaskPackageName);

                    if (mListener != null && callActivityHandler) {
                        // mListener.onActivityStarting(foregroundAppPackageInfo.packageName,foregroundTaskActivityName);
                        mListener.onActivityStarting(blockAppItem);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private String getTopLauncherPackageName(RunningTaskInfo runningTaskInfo,
                                             RunningAppProcessInfo runningAppProcessInfo)
    {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return runningTaskInfo.topActivity.getPackageName();
        } else {
            if (runningAppProcessInfo != null) {
                return runningAppProcessInfo.pkgList[0];
            } else {
                return null;
            }

        }
    }

    private RunningAppProcessInfo getTopLauncherActivAppProcess(ActivityManager mActivityManager)
    {
        final int PROCESS_STATE_TOP = 2;
        RunningAppProcessInfo currentInfo = null;
        Field field = null;
        try {
            field = RunningAppProcessInfo.class.getDeclaredField("processState");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        List<RunningAppProcessInfo> appList = mActivityManager.getRunningAppProcesses();
        for (RunningAppProcessInfo app : appList) {
            if (app.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && app.importanceReasonCode == 0) {
                Integer state = null;
                try {
                    state = field.getInt(app);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                if (state != null && state == PROCESS_STATE_TOP) {
                    currentInfo = app;
                    break;
                }
            }
        }
        return currentInfo;
    }

    private String getTopLauncherActivityName(PackageManager pm, RunningTaskInfo runningTaskInfo,
                                              RunningAppProcessInfo runningAppProcessInfo)
    {
        CharSequence mActivityName = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return runningTaskInfo.topActivity.getShortClassName();
        } else {
            try {
                mActivityName =
                        pm.getApplicationLabel(pm.getApplicationInfo(runningAppProcessInfo.processName,
                                PackageManager.GET_META_DATA));
                return mActivityName.toString();
            } catch (Exception e) {
            }
        }
        return "";
    }

    private BlockAppItem getLaunchingAppDetails(PackageManager pm, RunningTaskInfo runningTaskInfo,
                                                RunningAppProcessInfo runningAppProcessInfo)
    {
        BlockAppItem appItem = new BlockAppItem();
        try {
            CharSequence mActivityName = "";
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                appItem.setAppName(runningTaskInfo.topActivity.getShortClassName());
                appItem.setAppPackageName(runningTaskInfo.topActivity.getPackageName());
                appItem.setAppIcon(pm.getApplicationIcon(runningTaskInfo.topActivity.getPackageName()));
                return appItem;
            } else {
                try {
                    mActivityName =
                            pm.getApplicationLabel(pm.getApplicationInfo(runningAppProcessInfo.processName,
                                    PackageManager.GET_META_DATA));
                    appItem.setAppName(mActivityName.toString());
                    appItem.setAppPackageName(runningAppProcessInfo.pkgList[0]);
                    appItem.setAppIcon(pm.getApplicationIcon(pm.getApplicationInfo(runningAppProcessInfo.processName,
                            PackageManager.GET_META_DATA)));
                    return appItem;

                } catch (Exception e) {
                }
            }

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return appItem;
    }

    @Override
    public void onDestroy()
    {
        mThread.interrupt();
        this.stopForeground(true);

    }
}
