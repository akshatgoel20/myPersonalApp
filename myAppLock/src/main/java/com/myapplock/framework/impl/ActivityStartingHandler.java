package com.myapplock.framework.impl;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.myapplock.database.AppInfoDB;
import com.myapplock.database.UpdateDB;
import com.myapplock.framework.api.ActivityStartingListener;
import com.myapplock.models.BlockAppItem;
import com.myapplock.models.LockedAppDetails;
import com.myapplock.ui.activity.LockScreenActivity;
import com.myapplock.utils.MyAppLockConstansts;
import com.myapplock.utils.MyAppLockPreferences;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ActivityStartingHandler implements ActivityStartingListener
{
    private Context mContext;

    private ActivityManager mAm;

    private String lastRunningPackage;

    private Hashtable<String, Runnable> tempAllowedPackages = new Hashtable<String, Runnable>();

    private Handler handler;

    private String lockScreenActivityName = "";

    private HashMap<String , LockedAppDetails> lockedAppList;

    private UpdateDB appInfoDB;

    public ActivityStartingHandler(Context context)
    {
        try {
            mContext = context;
            handler = new Handler();
            mAm = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            lastRunningPackage = getTopPackageName();
            context.registerReceiver(new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    String packagename = intent.getStringExtra(MyAppLockConstansts.EXTRA_PACKAGE_NAME);
                    long relock_time =
                            MyAppLockPreferences.getIntFromPreferences(mContext, MyAppLockConstansts.PREF_RELOCKTIMEOUT, 0);

                    boolean relock_policy_enable =
                            MyAppLockPreferences.getBoolFromPref(mContext,
                                    MyAppLockConstansts.PREF_RELOCKPOLICY_ENABLE, false);
                    if (relock_policy_enable && relock_time > 0) {
                        if (!tempAllowedPackages.isEmpty() && tempAllowedPackages.containsKey(packagename)) {
                            // Extend the time
                            Log.d("Detector", "Extending timeout for: " + packagename);
                            handler.removeCallbacks(tempAllowedPackages.get(packagename));
                        }
                        long relock_time1 =
                                MyAppLockPreferences.getIntFromPreferences(mContext,
                                        MyAppLockConstansts.PREF_RELOCKTIMEOUT, 0);
                        Runnable runnable = new RemoveFromTempRunnable(packagename);
                        tempAllowedPackages.put(packagename, runnable);
                        handler.postDelayed(runnable, relock_time1 * 1000 * 60);
                        log();
                    }
                    lastRunningPackage = packagename;
                }
            }, new IntentFilter(MyAppLockConstansts.ACTION_APPLICATION_PASSED));

            boolean isPattern_Lock =
                    MyAppLockPreferences.getBoolFromPref(mContext, MyAppLockConstansts.PREF_CURRENT_LOCK_MODE,
                            false);
            lockScreenActivityName = ".LockScreenActivity";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void log()
    {
        String output = "temp allowed: ";
        for (String p : tempAllowedPackages.keySet()) {
            output += p + ", ";
        }
        Log.d("Detector", output);
    }

    private class RemoveFromTempRunnable implements Runnable
    {
        private String mPackageName;

        public RemoveFromTempRunnable(String pname)
        {
            mPackageName = pname;
        }

        public void run()
        {
            Log.d("Detector", "Lock timeout Expires: " + mPackageName);
            tempAllowedPackages.remove(mPackageName);
        }
    }

    private RunningAppProcessInfo getRunningProcessInfo(ActivityManager mActivityManager)
    {
        final List<ActivityManager.RunningAppProcessInfo> pis = mActivityManager.getRunningAppProcesses();

        return pis.get(0);
    }

    private String getTopPackageName()
    {
        BlockAppItem  blockAppItem=printForegroundTask();
        if(null!=blockAppItem){
           return blockAppItem.getAppPackageName();
        }
        return "";
    }
    private BlockAppItem printForegroundTask() {
        String currentApp = "NULL";
        BlockAppItem appItem=null;
        PackageManager pm = mContext.getApplicationContext().getPackageManager();
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                UsageStatsManager usm = (UsageStatsManager)mContext. getSystemService(Context.USAGE_STATS_SERVICE);
                long time = System.currentTimeMillis();
                List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
                if (appList != null && appList.size() > 0) {
                    SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                    for (UsageStats usageStats : appList) {
                        mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    }
                    if (mySortedMap != null && !mySortedMap.isEmpty()) {
                        currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                        if (!TextUtils.isEmpty(currentApp) && currentApp.equalsIgnoreCase("com.google.android.googlequicksearchbox")) {
                            currentApp = "";
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
                ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
                currentApp = tasks.get(0).processName;
                if (!TextUtils.isEmpty(currentApp)) {

                    ApplicationInfo ai;
                    try {
                        ai = pm.getApplicationInfo( mContext.getPackageName(), 0);
                        String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
                        Drawable icon = pm.getApplicationIcon(currentApp);
                        appItem=new BlockAppItem();
                        appItem.setAppName(applicationName);
                        appItem.setAppPackageName(currentApp);
                        appItem.setAppIcon(icon);

                    } catch (final PackageManager.NameNotFoundException e) {
                        ai = null;
                    }
                }
            }
        }catch (Exception e){

        }
        Log.e("adapter", "Current App in foreground is: " + currentApp);
        return appItem;
    }


    public void onActivityStarting(BlockAppItem appItem)
    {
        if(null==appItem){
            return;
        }
        // debug: //debug: log.i("Detector","onActivityStarting");
        String packageName = appItem.getAppPackageName();
        String appname = appItem.getAppName();
        String activityName = appItem.getAppActivityname();
        Drawable appIcone = appItem.getAppIcon();
        synchronized (this) {

            try {

                // Putting the lastRunningPackage up makes applocker's preferences activties
                // not getting locked all the time!
                if (packageName.equals(lastRunningPackage))
                    return;

                if (packageName.equals(mContext.getPackageName())) {

                    // Of course cannot block lock screen
                    // debug: //debug: log.i("Detector",activityName);
                    // debug: //debug: log.i("Detector",lockScreenActivityName);
                    if (activityName!=null && activityName.equals(lockScreenActivityName))
                        return;
                    // But we need to block preferences
                    blockActivity(packageName, activityName, appname, appIcone);
                    return;
                }

                if (getDBInstance().getDBCount(AppInfoDB.DB_APP_LOCK_KEY_DETAILS_TABLE) > 0) {
                    getLockedAppKeyList().clear();
                    getLockedAppKeyList().putAll(getDBInstance().getLockedAppKeyList());
                }
                long relock_time =
                        MyAppLockPreferences.getIntFromPreferences(mContext, MyAppLockConstansts.PREF_RELOCKTIMEOUT, 0);
                if ((relock_time > 0))
                    if (tempAllowedPackages.containsKey(packageName))
                        return;

                if (!getLockedAppKeyList().isEmpty()) {
                    Iterator it = getLockedAppKeyList().entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        if (pair.getKey().equals(packageName)) {
                            blockActivity(packageName, activityName, appname, appIcone);
                            return;
                        }
                    }
                }
                lastRunningPackage = packageName;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setLastPackageToEmplty() {
        lastRunningPackage="";
    }


    private void blockActivity(String packageName, String activityName, String appname, Drawable appIcone)
    {
        lastRunningPackage = packageName;
        Bitmap bitmap = drawableToBitmap(appIcone);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();

        Intent lockIntent = new Intent(mContext, LockScreenActivity.class);
        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        lockIntent.putExtra(MyAppLockConstansts.BlockedActivityName, activityName)
                .putExtra(MyAppLockConstansts.BlockedPackageName, packageName)
                .putExtra(MyAppLockConstansts.BlockedAppName, appname).putExtra(MyAppLockConstansts.BlockedAppIcon, b);

        mContext.startActivity(lockIntent);


    }

    public static Bitmap drawableToBitmap(Drawable drawable)
    {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap =
                Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public HashMap<String,LockedAppDetails> getLockedAppKeyList()
    {

        if (lockedAppList == null) {
            lockedAppList = new HashMap<String,LockedAppDetails>();
        }
        return lockedAppList;
    }

    private UpdateDB getDBInstance() {


        if (appInfoDB == null) {
            appInfoDB = new UpdateDB(mContext);
        }
        return appInfoDB;
    }
}
