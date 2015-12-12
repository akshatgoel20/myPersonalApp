package com.myapplock.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.haibison.android.lockpattern.LockPatternActivity;
import com.myapplock.R;
import com.myapplock.database.UpdateDB;
import com.myapplock.models.LockedAppDetails;
import com.myapplock.utils.CommonUtils;
import com.myapplock.utils.MyAppLockConstansts;
import com.myapplock.utils.MyAppLockPreferences;

public class LockScreenActivity extends Activity implements OnClickListener, TextWatcher
{

    private static final int REQ_ENTER_PATTERN = 2;

    private TextView mAppName;

    private ImageView mAppIcon;

    private EditText mPassword;

    private StringBuilder mPasswordBuffer = new StringBuilder();

    private UpdateDB mUpdateDB;
    private LockedAppDetails lockedAppDetails;

    @Override
    protected void onCreate(Bundle arg0)
    {
        super.onCreate(arg0);
        checkPatternType();

    }

    private void checkPatternType()
    {
        lockedAppDetails = getDBInstance().getLockedAppKeyList().get(getIntent().getStringExtra(MyAppLockConstansts.BlockedPackageName));

        if (lockedAppDetails.getAppLockType()== CommonUtils.LockType.Password.ordinal()) {
            setContentView(R.layout.lockscreen);
            initView();
        } else {
            Intent intent =
                    new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null, this, LockPatternActivity.class);
            intent.putExtra(LockPatternActivity.EXTRA_PATTERN, lockedAppDetails.getAppLockKey().toCharArray());
            startActivityForResult(intent, REQ_ENTER_PATTERN);
        }
    }

    @SuppressLint("NewApi")
    private void initView()
    {
        try {
            // appItem=(BlockAppItem) getIntent().getSerializableExtra(LockScreenActivity.BlockApp);

            mAppName = (TextView) findViewById(R.id.appName);
            mAppIcon = (ImageView) findViewById(R.id.appIcon);

            findViewById(R.id.btn_zero).setOnClickListener(this);
            findViewById(R.id.btn_one).setOnClickListener(this);
            findViewById(R.id.btn_two).setOnClickListener(this);
            findViewById(R.id.btn_three).setOnClickListener(this);
            findViewById(R.id.btn_four).setOnClickListener(this);
            findViewById(R.id.btn_five).setOnClickListener(this);
            findViewById(R.id.btn_six).setOnClickListener(this);
            findViewById(R.id.btn_seven).setOnClickListener(this);
            findViewById(R.id.btn_eight).setOnClickListener(this);
            findViewById(R.id.btn_nine).setOnClickListener(this);
            findViewById(R.id.btn_back).setOnClickListener(this);
            findViewById(R.id.btn_cancel).setOnClickListener(this);
            mPassword = (EditText) findViewById(R.id.edt_password);
            mPassword.setFocusable(false);
            mPassword.addTextChangedListener(this);

            mAppName.setText(getIntent().getStringExtra(MyAppLockConstansts.BlockedAppName));
            Bundle extras = getIntent().getExtras();
            byte[] b = extras.getByteArray(MyAppLockConstansts.BlockedAppIcon);
            Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
            mAppIcon.setImageBitmap(bmp);

            mPassword.setHint(lockedAppDetails.getAppLockKeyHint());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.btn_zero:
                enterPassword("0");
                break;
            case R.id.btn_one:
                enterPassword("1");
                break;
            case R.id.btn_two:
                enterPassword("2");
                break;
            case R.id.btn_three:
                enterPassword("3");
                break;
            case R.id.btn_four:
                enterPassword("4");
                break;
            case R.id.btn_five:
                enterPassword("5");
                break;
            case R.id.btn_six:
                enterPassword("6");
                break;
            case R.id.btn_seven:
                enterPassword("7");
                break;
            case R.id.btn_eight:
                enterPassword("8");
                break;
            case R.id.btn_nine:
                enterPassword("9");
                break;
            case R.id.btn_back:
                closeApp();
                break;
            case R.id.btn_cancel:
                clearText();
                break;

            default:
                break;
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        closeApp();
    }

    @Override
    public void afterTextChanged(Editable arg0)
    {
        if (verifyPassword()) {
            test_passed();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
    {
    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
    {
    }

    private void enterPassword(String passcode)
    {
        mPasswordBuffer.append(passcode);
        mPassword.setText(mPasswordBuffer);
    }

    private void clearText()
    {
        if (mPasswordBuffer.length() > 0) {
            mPasswordBuffer = mPasswordBuffer.replace(mPasswordBuffer.length() - 1, mPasswordBuffer.length(), "");
            mPassword.setText(mPasswordBuffer);
        }
    }

    private void closeApp()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public boolean verifyPassword()
    {
        if (mPasswordBuffer == null)
            return false;
        return mPasswordBuffer.toString().equals(lockedAppDetails.getAppLockKey());
    }

    private void test_passed()
    {
        this.sendBroadcast(new Intent().setAction(MyAppLockConstansts.ACTION_APPLICATION_PASSED).putExtra(
                MyAppLockConstansts.EXTRA_PACKAGE_NAME, getIntent().getStringExtra(MyAppLockConstansts.BlockedPackageName)));

        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode) {
            case REQ_ENTER_PATTERN: {
                /*
                 * NOTE that there are 4 possible result codes!!!
                 */
                switch (resultCode) {
                    case RESULT_OK:
                        // The user passed
                        test_passed();
                        break;
                    case RESULT_CANCELED:
                        // The user cancelled the task
                        closeApp();
                        break;
                    case LockPatternActivity.RESULT_FAILED:
                        // The user failed to enter the pattern
                        closeApp(); // TODO: change it latter
                        break;
                    case LockPatternActivity.RESULT_FORGOT_PATTERN:
                        // The user forgot the pattern and invoked your recovery Activity.
                        break;
                }

                /*
                 * In any case, there's always a key EXTRA_RETRY_COUNT, which holds the number of tries that the user
                 * did.
                 */
                int retryCount = data.getIntExtra(LockPatternActivity.EXTRA_RETRY_COUNT, 0);

                break;
            }// REQ_ENTER_PATTERN
        }
    }
    private UpdateDB getDBInstance(){
        if(mUpdateDB==null){
            mUpdateDB=new UpdateDB(this);
        }
        return mUpdateDB;
    }
}
