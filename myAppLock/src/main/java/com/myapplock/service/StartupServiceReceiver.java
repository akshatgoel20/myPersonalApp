package com.myapplock.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.myapplock.ui.LaunchingHiddenAppActivity;
import com.myapplock.utils.MyAppLockConstansts;
import com.myapplock.utils.MyAppLockPreferences;

public class StartupServiceReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (MyAppLockPreferences.getBoolFromPref(context, MyAppLockConstansts.PREF_AUTO_START, false)) {
                context.startService(new Intent(context, LaunchDetectorService.class));
            }
            return;
        }
        if (intent.getAction().equals("android.provider.Telephony.SECRET_CODE")) {
            Uri phoneNumber1 = intent.getData();
            if (phoneNumber1 != null) {
                String phoneNumber = phoneNumber1.toString();
                if (phoneNumber != null && MyAppLockConstansts.APP_UNLOCK_CODE.equalsIgnoreCase(phoneNumber)) {
                    Log.e("", "Successs");
                    context.startActivity(new Intent(context, LaunchingHiddenAppActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }

        }
    }
}
