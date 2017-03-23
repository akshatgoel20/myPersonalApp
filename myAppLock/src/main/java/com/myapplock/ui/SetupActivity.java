package com.myapplock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.haibison.android.lockpattern.LockPatternActivity;
import com.myapplock.R;
import com.myapplock.callbacks.Response;
import com.myapplock.models.AppItems;
import com.myapplock.service.LaunchDetectorService;
import com.myapplock.utils.CommonUtils;
import com.myapplock.utils.MyAppLockConstansts;
import com.myapplock.utils.MyAppLockPreferences;

import static com.myapplock.application.MyAppLock.getAppContext;
import static com.myapplock.ui.SettingFragment.REQ_CREATE_PATTERN;
import static com.myapplock.utils.MyAppLockConstansts.PREF_DEFAULT_PATTERN_SET;
import static com.myapplock.utils.MyAppLockPreferences.getBoolFromPref;

public class SetupActivity extends AppCompatActivity  implements Response {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        checkFirstInstall();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQ_CREATE_PATTERN && resultCode == RESULT_OK) {
            char[] pattern = data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN);
            savePattern(pattern);
        }
    }

    private void checkFirstInstall() {
        boolean isRunning = CommonUtils.isMyServiceRunning(this, LaunchDetectorService.class);
        if (!isRunning) {
            startService(new Intent(this, LaunchDetectorService.class));
        }
        if (getBoolFromPref(this, MyAppLockConstansts.PREF_FIRST_INSTALL_COMPLETE,false)){
            launchHomeActivity();
        }else{
            createPassword();
        }
    }

    private void createPassword(){
        AppItems appItems=new AppItems();
        appItems.setAppName(getString(R.string.set_default_pwd));
        appItems.setAppPackageName(getPackageName());
        appItems.setOpen(false);
        appItems.setStatus(false);

        SetNewPasswordFragment fragment = SetNewPasswordFragment.newInstance();
        fragment.setCallback(this);

        Bundle bundle=new Bundle();
        bundle.putSerializable("App", appItems);
        bundle.putBoolean("FromStart",true);
        fragment.setArguments(bundle);
        fragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDialog);
        fragment.show(getSupportFragmentManager(), "New Password");
    }

    private void setDefaultPattern() {
        Intent intent =
                new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null,this, LockPatternActivity.class);
        startActivityForResult(intent, REQ_CREATE_PATTERN);
    }

    private void savePattern(char[] pattern){
        MyAppLockPreferences.saveBoolToPref(getAppContext(), PREF_DEFAULT_PATTERN_SET,true);
        MyAppLockPreferences.saveStrToPref(this, MyAppLockConstansts.PREF_PATTERN, new String(pattern));
        setDefaultPasswordMode();
    }

    @Override
    public void passwordSaved() {
        setDefaultPattern();
    }

    @Override
    public void saveLockMode() {

        boolean isPasswordSaved=MyAppLockPreferences.getBoolFromPref(this,MyAppLockConstansts.PREF_DEFAULT_PASSWORD_SET,false);
        boolean isPatternSaved= MyAppLockPreferences.getBoolFromPref(this,MyAppLockConstansts.PREF_DEFAULT_PATTERN_SET,false);
        String isLockModeSet= MyAppLockPreferences.getStringFromPreferences(this,MyAppLockConstansts.PREF_PATTREN_LOCKMODE,"");

        if(isPasswordSaved && isPatternSaved && !TextUtils.isEmpty(isLockModeSet)) {
            setDefaultAppValues();
            launchHomeActivity();
        }else if(!isPasswordSaved){
            createPassword();
        }else if(!isPatternSaved){
             setDefaultPattern();
        }
    }

    private void setDefaultPasswordMode() {
        SaveDefaultLockModeFragment fragment = SaveDefaultLockModeFragment.newInstance();
        fragment.setCallback(this);
        fragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDialog);
        fragment.show(getSupportFragmentManager(), "New Password");
    }


    private void setDefaultAppValues(){
        MyAppLockPreferences.saveBoolToPref(this, MyAppLockConstansts.PREF_FIRST_INSTALL_COMPLETE, true);
        MyAppLockPreferences.saveBoolToPref(this, MyAppLockConstansts.PREF_CURRENT_LOCK_MODE, false);
        MyAppLockPreferences.saveBoolToPref(this, MyAppLockConstansts.PREF_AUTO_START, true);
        MyAppLockPreferences.saveBoolToPref(this, MyAppLockConstansts.PREF_VIBRATE, false);
        MyAppLockPreferences.saveBoolToPref(this, MyAppLockConstansts.PREF_LOCKPATTERN_VISIBLE, true);
        MyAppLockPreferences.saveBoolToPref(this, MyAppLockConstansts.PREF_RELOCKPOLICY_ENABLE, false);
        MyAppLockPreferences.saveBoolToPref(this, MyAppLockConstansts.PREF_RELOCKPOLICY_ONSCREEN_ON, false);
        MyAppLockPreferences.saveBoolToPref(this, MyAppLockConstansts.PREF_HIDE_APPLOCK_FROM_HOME, false);
        MyAppLockPreferences.saveBoolToPref(this, MyAppLockConstansts.PREF_LOCK_NEW_APP, false);
        MyAppLockPreferences.saveBoolToPref(this, MyAppLockConstansts.PREF_HIDE_IMAGES_FROM_GALLERY, true);
        MyAppLockPreferences.saveBoolToPref(this, MyAppLockConstansts.PREF_SHOW_NOTIFICATION_BAR, false);
    }

    private void launchHomeActivity() {
        startActivity(new Intent(this,HomeActivity.class));
        finish();
        overridePendingTransition(R.anim.activity_open_scale,R.anim.activity_close_translate);
    }



}
