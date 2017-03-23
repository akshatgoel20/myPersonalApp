package com.myapplock.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.myapplock.R;
import com.myapplock.callbacks.Response;
import com.myapplock.utils.CommonUtils;
import com.myapplock.utils.MyAppLockConstansts;
import com.myapplock.utils.MyAppLockPreferences;

public class SaveDefaultLockModeFragment extends DialogFragment
{
    private RadioButton mPasswordMode, mPatternMode;
    private Response mResponse;

    public SaveDefaultLockModeFragment(){

    }

    public static SaveDefaultLockModeFragment newInstance()
    {
        return new SaveDefaultLockModeFragment();
    }

    public void setCallback(Response mPasswordCalback){
        mResponse=mPasswordCalback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.layout_custom_save_mode_dialog, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View mView)
    {
        mPasswordMode = (RadioButton) mView.findViewById(R.id.rb_password_mode);
        mPatternMode = (RadioButton) mView.findViewById(R.id.rb_pattern_mode);

        mPasswordMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                setDefaultLockMode("Passowrd",isChecked);
            }
        });

        mPatternMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setDefaultLockMode("Pattern",isChecked);
            }
        });

        mView.findViewById(R.id.ll_password_setting).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(mPasswordMode.isChecked() || mPatternMode.isChecked() ){
                    showSetPasswordAlert("your default lock mode saved. please press ok proceed");
                }else{
                    CommonUtils.showAlert(getActivity(),"Please select the lock mode.");
                }
            }
        });

    }

    private void setDefaultLockMode(String password, boolean isChecked) {
        if(password.equalsIgnoreCase("Passowrd")){
            MyAppLockPreferences.saveBoolToPref(getActivity(), MyAppLockConstansts.PREF_CURRENT_LOCK_MODE,false);
            MyAppLockPreferences.saveStrToPref(getActivity(), MyAppLockConstansts.PREF_PATTREN_LOCKMODE, password);
            mPasswordMode.setChecked(isChecked);
            mPatternMode.setChecked(!isChecked);
        }else{
            MyAppLockPreferences.saveBoolToPref(getActivity(), MyAppLockConstansts.PREF_CURRENT_LOCK_MODE, true);
            MyAppLockPreferences.saveStrToPref(getActivity(), MyAppLockConstansts.PREF_PATTREN_LOCKMODE, password);
            mPasswordMode.setChecked(!isChecked);
            mPatternMode.setChecked(isChecked);
        }
    }

   private void showSetPasswordAlert(String msg){
      new AlertDialog.Builder(getActivity() ).setMessage(msg).setPositiveButton("OK", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               mResponse.saveLockMode();
               dialog.dismiss();
               dismiss();
           }
       }).create().show();
    }
}
