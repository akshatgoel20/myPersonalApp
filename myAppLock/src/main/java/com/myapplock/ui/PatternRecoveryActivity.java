package com.myapplock.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.haibison.android.lockpattern.LockPatternActivity;
import com.myapplock.R;

public class PatternRecoveryActivity extends Activity
{
    public static final String ACTION_APPLICATION_PASSED = "com.gueei.applocker.applicationpassedtest";

    private static final int REQ_ENTER_PATTERN = 2;

    public static final String BlockedPackageName = "locked package name";

    public static final String BlockedActivityName = "locked activity name";

    public static final String EXTRA_PACKAGE_NAME = "com.gueei.applocker.extra.package.name";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_home);
        // char[] savedPattern = SecurityPrefs.getPattern(this);
        // if (savedPattern != null) {
        // Log.e("", "Pattern" + savedPattern.toString());
        // }
        //
        // Intent intent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null, this,
        // LockPatternActivity.class);
        // intent.putExtra(LockPatternActivity.EXTRA_PATTERN, savedPattern);
        // intent.putExtra(LockPatternActivity.EXTRA_INTENT_ACTIVITY_FORGOT_PATTERN, new Intent(this, null));
        // startActivityForResult(intent, REQ_ENTER_PATTERN);
        // startService(new Intent(this,LaunchDetectorService.class));
        //
        // new Handler().postDelayed(new Runnable() {
        //
        // @Override
        // public void run() {
        // startActivity(new Intent(SplashScreenActivity.this,HomeActivity.class));
        // finish();
        // }
        // }, 2000);

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
                        int retryCount = data.getIntExtra(LockPatternActivity.EXTRA_RETRY_COUNT, 0);

                        break;
                    case LockPatternActivity.RESULT_FORGOT_PATTERN:
                        // The user forgot the pattern and invoked your recovery Activity.
                        break;
                }
                int retryCount = data.getIntExtra(LockPatternActivity.EXTRA_RETRY_COUNT, 0);
                Log.e("", "Failed Count " + retryCount);
                break;
            }
        }
    }

    private void test_passed()
    {
        sendBroadcast(new Intent().setAction(ACTION_APPLICATION_PASSED).putExtra(EXTRA_PACKAGE_NAME,
            getIntent().getStringExtra(BlockedPackageName)));
        finish();
    }

}
