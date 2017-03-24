package com.myapplock.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.haibison.android.lockpattern.LockPatternActivity;
import com.haibison.android.lockpattern.util.Settings;
import com.myapplock.R;
import com.myapplock.utils.CommonUtils;
import com.myapplock.utils.MyAppLockConstansts;
import com.myapplock.utils.MyAppLockPreferences;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingFragment extends Fragment implements OnClickListener
{
    private View mView;

    private EditText mPassword_edt, mConfirmPassword_edt, mPasswordHint_edt, mSecurity_Ques, mSecurity_Ans,
        mSecurity_Email;

    private CheckBox mVibrateEnable, mPatternVisible;

    private RadioButton mPatternLockEnable, mPasswordLockEnable;

    private static final String PASSWORD_PATTERN = "[ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789]*";

    private static final String PASSWORD_PATTERN1 = "^[a-zA-Z0-9]*$";

    public static final int REQ_CREATE_PATTERN = 1;

    private Pattern pattern;

    private Matcher matcher;

    private LinearLayout mPatternLock_Setting_main, mPatternLock_Setting_content, mPasswordLock_Setting_main,
        mPasswordLock_Setting_content, mSecurityLock_Setting_main, mSecurityLock_Setting_content, mRelock_Setting_main,
        mRelock_Setting_content, mAdvance_Setting_main, mAdvance_Setting_content;

    public SettingFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_general_setting, null);
        initViews();
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initViews()
    {
        MyAppLockConstansts.cuurentFragment="SettingFragment";

        mPatternLock_Setting_main = (LinearLayout) mView.findViewById(R.id.rl_pattern_lock_mode_main);
        mPatternLock_Setting_content = (LinearLayout) mView.findViewById(R.id.rl_pattern_lock_mode);

        mPasswordLock_Setting_main = (LinearLayout) mView.findViewById(R.id.rl_password_lock_mode_main);
        mPasswordLock_Setting_content = (LinearLayout) mView.findViewById(R.id.rl_password_lock_mode);
        mSecurityLock_Setting_main = (LinearLayout) mView.findViewById(R.id.rl_security_setting_main);
        mSecurityLock_Setting_content = (LinearLayout) mView.findViewById(R.id.rl_security_setting);
        mRelock_Setting_main = (LinearLayout) mView.findViewById(R.id.relock_set_main_layout);
        mRelock_Setting_content = (LinearLayout) mView.findViewById(R.id.relock_set_content_layout);
        mAdvance_Setting_main = (LinearLayout) mView.findViewById(R.id.anvance_set_main_layout);
        mAdvance_Setting_content = (LinearLayout) mView.findViewById(R.id.anvance_set_content_layout);

        mPassword_edt = (EditText) mView.findViewById(R.id.edt_password);
        mConfirmPassword_edt = (EditText) mView.findViewById(R.id.edt_confirm_password);
        mPasswordHint_edt = (EditText) mView.findViewById(R.id.edt_hint);
        mSecurity_Ques = (EditText) mView.findViewById(R.id.edt_security_question);
        mSecurity_Ans = (EditText) mView.findViewById(R.id.edt_security_answer);
        mSecurity_Email = (EditText) mView.findViewById(R.id.edt_security_email);

        mVibrateEnable = (CheckBox) mView.findViewById(R.id.cb_vibrate_check);
        mPatternVisible = (CheckBox) mView.findViewById(R.id.cb_visible_check);

        mPatternLockEnable = (RadioButton) mView.findViewById(R.id.rb_pattern);
        mPasswordLockEnable = (RadioButton) mView.findViewById(R.id.rb_password);

        mView.findViewById(R.id.ll_pattern_setting).setOnClickListener(this);
        mView.findViewById(R.id.ll_password_setting).setOnClickListener(this);
        mView.findViewById(R.id.ll_security_setting).setOnClickListener(this);

        mPatternLockEnable.setOnClickListener(this);
        mPasswordLockEnable.setOnClickListener(this);
        mVibrateEnable.setOnClickListener(this);
        mPatternVisible.setOnClickListener(this);

        mPatternLock_Setting_main.setOnClickListener(this);
        mPatternLock_Setting_content.setOnClickListener(this);

        mPasswordLock_Setting_main.setOnClickListener(this);
        mPasswordLock_Setting_content.setOnClickListener(this);

        mSecurityLock_Setting_main.setOnClickListener(this);
        mSecurityLock_Setting_content.setOnClickListener(this);

        mRelock_Setting_main.setOnClickListener(this);
        mRelock_Setting_content.setOnClickListener(this);

        mAdvance_Setting_main.setOnClickListener(this);
        mAdvance_Setting_content.setOnClickListener(this);

        mVibrateEnable.setChecked(MyAppLockPreferences.getBoolFromPref(getActivity(),
                MyAppLockConstansts.PREF_VIBRATE, false));
        mPatternVisible.setChecked(MyAppLockPreferences.getBoolFromPref(getActivity(),
                MyAppLockConstansts.PREF_LOCKPATTERN_VISIBLE, false));

        if (MyAppLockPreferences.getBoolFromPref(getActivity(), MyAppLockConstansts.PREF_CURRENT_LOCK_MODE,
                false)) {
            mPatternLockEnable.setChecked(true);
            mPasswordLockEnable.setChecked(false);
        } else {
            mPasswordLockEnable.setChecked(true);
            mPatternLockEnable.setChecked(false);
        }

        if (mPatternVisible.isChecked()) {
            Settings.Display.setStealthMode(getActivity(), false);
        } else {
            Settings.Display.setStealthMode(getActivity(), true);
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.rb_pattern:
                mPatternLockEnable.setChecked(true);
                mPasswordLockEnable.setChecked(false);
                MyAppLockPreferences.saveBoolToPref(getActivity(), MyAppLockConstansts.PREF_CURRENT_LOCK_MODE,true);
                savePatternSetting("Pattern");

                break;
            case R.id.rb_password:
                mPasswordLockEnable.setChecked(true);
                mPatternLockEnable.setChecked(false);
                MyAppLockPreferences.saveBoolToPref(getActivity(), MyAppLockConstansts.PREF_CURRENT_LOCK_MODE, false);
                savePatternSetting("Passowrd");
                break;
            case R.id.cb_vibrate_check:
                saveSetting(0, mVibrateEnable.isChecked());
                break;
            case R.id.cb_visible_check:
                if (mPatternVisible.isChecked()) {
                    Settings.Display.setStealthMode(getActivity(), false);
                } else {
                    Settings.Display.setStealthMode(getActivity(), true);
                }
                saveSetting(1, mPatternVisible.isChecked());
                break;
            case R.id.ll_pattern_setting:
                createPattern();
                break;
            case R.id.ll_password_setting:
                savePasswordSetting();
                break;
            case R.id.ll_security_setting:
                saveSecuritySetting();
                break;
            case R.id.rl_pattern_lock_mode_main:
                mPatternLock_Setting_main.setVisibility(View.GONE);
                mPatternLock_Setting_content.setVisibility(View.VISIBLE);
                break;
            case R.id.rl_pattern_lock_mode:
                mPatternLock_Setting_main.setVisibility(View.VISIBLE);
                mPatternLock_Setting_content.setVisibility(View.GONE);
                break;
            case R.id.rl_password_lock_mode_main:
                mPasswordLock_Setting_main.setVisibility(View.GONE);
                mPasswordLock_Setting_content.setVisibility(View.VISIBLE);
                break;
            case R.id.rl_password_lock_mode:
                mPasswordLock_Setting_main.setVisibility(View.VISIBLE);
                mPasswordLock_Setting_content.setVisibility(View.GONE);
                break;
            case R.id.relock_set_main_layout:
                mRelock_Setting_main.setVisibility(View.GONE);
                mRelock_Setting_content.setVisibility(View.VISIBLE);
                break;
            case R.id.relock_set_content_layout:
                mRelock_Setting_main.setVisibility(View.VISIBLE);
                mRelock_Setting_content.setVisibility(View.GONE);
                break;
            case R.id.rl_security_setting_main:
                mSecurityLock_Setting_main.setVisibility(View.GONE);
                mSecurityLock_Setting_content.setVisibility(View.VISIBLE);
                break;
            case R.id.rl_security_setting:
                mSecurityLock_Setting_main.setVisibility(View.VISIBLE);
                mSecurityLock_Setting_content.setVisibility(View.GONE);
                break;

            case R.id.anvance_set_main_layout:
                mAdvance_Setting_main.setVisibility(View.GONE);
                mAdvance_Setting_content.setVisibility(View.VISIBLE);
                break;
            case R.id.anvance_set_content_layout:
                mAdvance_Setting_main.setVisibility(View.VISIBLE);
                mAdvance_Setting_content.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private void createPattern()
    {
        MyAppLockConstansts.cuurentFragment = "Setting";
        Intent intent =
            new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null, getActivity(), LockPatternActivity.class);
        startActivityForResult(intent, REQ_CREATE_PATTERN);
    }

    private void savePatternSetting(String lockMode)
    {
        MyAppLockPreferences.saveStrToPref(getActivity(), MyAppLockConstansts.PREF_PATTREN_LOCKMODE, lockMode);
    }

    private void saveSetting(int itemNo, boolean itemValue)
    {

        switch (itemNo) {
            case 0:
                if (itemValue) {
                    android.provider.Settings.System.putInt(getActivity().getContentResolver(),
                        android.provider.Settings.System.HAPTIC_FEEDBACK_ENABLED, 1);
                } else {
                    android.provider.Settings.System.putInt(getActivity().getContentResolver(),
                        android.provider.Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
                }

                break;
            case 1:
                MyAppLockPreferences.saveBoolToPref(getActivity(),
                        MyAppLockConstansts.PREF_LOCKPATTERN_VISIBLE, itemValue);
                break;

            default:
                break;
        }
    }

    private void savePasswordSetting()
    {
        if (mPassword_edt.length() <= 0) {
            CommonUtils.showAlert(getActivity(), "Please enter Password");
        } else if (mConfirmPassword_edt.length() <= 0) {
            CommonUtils.showAlert(getActivity(), "Please enter Confirm Password");
        } else if (!CommonUtils.checkForSamePassword(mPassword_edt.getText().toString(), mConfirmPassword_edt.getText()
            .toString())) {
            CommonUtils.showAlert(getActivity(), "Password and Confirm Password shoulb be same");
        } else {
            if (mPasswordHint_edt.getText().length() > 0) {
                MyAppLockPreferences.saveStrToPref(getActivity(), MyAppLockConstansts.PREF_PASSWORD_HINT,
                        mPasswordHint_edt.getText().toString());
            }
            MyAppLockPreferences.saveStrToPref(getActivity(), MyAppLockConstansts.PREF_PASSWORD, mPassword_edt
                .getText().toString());
        }
    }

    private void saveSecuritySetting()
    {
        if (mSecurity_Ques.length() <= 0) {
            CommonUtils.showAlert(getActivity(), "Please enter Password");
        } else if (mSecurity_Ans.length() <= 0) {
            CommonUtils.showAlert(getActivity(), "Please enter Confirm Password");
        } else {
            if (mSecurity_Email.getText().length() > 0) {
                MyAppLockPreferences.saveStrToPref(getActivity(), MyAppLockConstansts.PREF_SECURITY_EMAIL,
                        mSecurity_Email.getText().toString());
            }
            MyAppLockPreferences.saveStrToPref(getActivity(), MyAppLockConstansts.PREF_SECURITY_QUES,
                    mSecurity_Ques.getText().toString());
            MyAppLockPreferences.saveStrToPref(getActivity(), MyAppLockConstansts.PREF_SECURITY_ANS,
                    mSecurity_Ans.getText().toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // in fragment class callback
        switch (requestCode) {
            case REQ_CREATE_PATTERN: {
                if (resultCode == getActivity().RESULT_OK) {
                    char[] pattern = data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN);
                    MyAppLockPreferences.saveStrToPref(getActivity(), MyAppLockConstansts.PREF_PATTERN,
                            new String(pattern));
                }
                break;
            }// REQ_CREATE_PATTERN
        }
    }

}
