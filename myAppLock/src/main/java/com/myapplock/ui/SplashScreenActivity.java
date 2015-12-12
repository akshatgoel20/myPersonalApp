package com.myapplock.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.haibison.android.lockpattern.LockPatternActivity;
import com.haibison.android.lockpattern.util.Settings;

public class SplashScreenActivity extends Activity
{
    public static final String ACTION_APPLICATION_PASSED = "com.gueei.applocker.applicationpassedtest";

    private static final int REQ_ENTER_PATTERN = 2;

    public static final String BlockedPackageName = "locked package name";

    public static final String BlockedActivityName = "locked activity name";

    public static final String EXTRA_PACKAGE_NAME = "com.gueei.applocker.extra.package.name";

    public static boolean isClose = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        char[] savedPattern = Settings.Security.getPattern(this);
        if (savedPattern != null) {
            Log.e("", "Pattern" + savedPattern.toString());
        }
        isClose = false;
        Intent intent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null, this, LockPatternActivity.class);
        intent.putExtra(LockPatternActivity.EXTRA_PATTERN, savedPattern);
        intent.putExtra(EXTRA_PACKAGE_NAME, getIntent().getStringExtra(BlockedPackageName));

        if (getIntent().getBooleanExtra("ShowForgot", false)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(LockPatternActivity.EXTRA_PENDING_INTENT_FORGOT_PATTERN, new Intent(this,
                HomeActivity.class));
        }
        startActivityForResult(intent, REQ_ENTER_PATTERN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_ENTER_PATTERN: {

                switch (resultCode) {
                    case RESULT_OK:
                        test_passed();
                        break;
                    case RESULT_CANCELED:
                        finish();
                        break;
                    case LockPatternActivity.RESULT_FAILED:
                        finish();
                        break;
                    case LockPatternActivity.RESULT_FORGOT_PATTERN:
                        Log.e("", "RESULT_FORGOT_PATTERN");
                        finish();
                        break;
                    case 10:
                        Log.e("", "App Killed");
                        closeApp();
                        break;
                }
                break;
            }
        }
    }

    private void closeApp()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void test_passed()
    {
        sendBroadcast(new Intent().setAction(ACTION_APPLICATION_PASSED).putExtra(EXTRA_PACKAGE_NAME,
            getIntent().getStringExtra(BlockedPackageName)));
        finish();
    }

}
