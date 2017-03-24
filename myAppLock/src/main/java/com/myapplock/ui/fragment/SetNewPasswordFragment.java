package com.myapplock.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myapplock.R;
import com.myapplock.database.UpdateDB;
import com.myapplock.framework.api.Response;
import com.myapplock.models.AppItems;
import com.myapplock.models.LockedAppDetails;
import com.myapplock.utils.CommonUtils;
import com.myapplock.utils.MyAppLockConstansts;
import com.myapplock.utils.MyAppLockPreferences;

import static com.myapplock.application.MyAppLock.getAppContext;
import static com.myapplock.utils.MyAppLockConstansts.PREF_DEFAULT_PASSWORD_SET;

public class SetNewPasswordFragment extends DialogFragment
{
    private EditText mPassword_edt, mConfirmPassword_edt, mPasswordHint_edt;
    private LinearLayout mDialogView;
    private AppItems appItems;
    private UpdateDB appInfoDB;
    private Response mResponse;

    public SetNewPasswordFragment(){

    }

    public static SetNewPasswordFragment newInstance()
    {
        return new SetNewPasswordFragment();
    }

    public void setCallback(Response mPasswordCalback){
        mResponse=mPasswordCalback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.layout_custom_alert_dialog, container, false);
        initView(rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getArguments()!=null && getArguments().getBoolean("FromStart")){
            setDialogPos();
        }
    }

    private void setDialogPos(){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1.0f;
        params.gravity = Gravity.CENTER_VERTICAL;

        mDialogView.setLayoutParams(params);
    }

    private void setDialogPosition() {
        Window window = getDialog().getWindow();

        // set "origin" to top left corner, so to speak
        window.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);

        // after that, setting values for x and y works "naturally"
        WindowManager.LayoutParams params = window.getAttributes();
        params.y = dpToPx(60);
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(new ColorDrawable(0));
        window.setAttributes(params);
    }



    public int dpToPx(float valueInDp) {
        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
    private void initView(View mView)
    {
        appItems =(AppItems)getArguments().getSerializable("App");
        mPassword_edt = (EditText) mView.findViewById(R.id.edt_password);
        mConfirmPassword_edt = (EditText) mView.findViewById(R.id.edt_confirm_password);
        mPasswordHint_edt = (EditText) mView.findViewById(R.id.edt_hint);
        mDialogView= (LinearLayout) mView.findViewById(R.id.ll_custum_dialog);

        mView.findViewById(R.id.ll_password_setting).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                validateViews();
            }
        });

        ImageView appIcon=(ImageView)mView.findViewById(R.id.app_icon_img);

        TextView mAppName=(TextView)mView.findViewById(R.id.app_name);
        mAppName.setText(appItems.getAppName());
        if(null!=appItems.getAppIcon()){
            appIcon.setImageDrawable(appItems.getAppIcon());
        }
    }


    private void validateViews(){
        if(mPassword_edt.length()<=0){
            CommonUtils.showAlert(getActivity(),"Please enter password");
        }else if(mConfirmPassword_edt.length()<=0){
            CommonUtils.showAlert(getActivity(),"Please enter confirm password");
        }else if(!mPassword_edt.getText().toString().equalsIgnoreCase(mConfirmPassword_edt.getText().toString())){
            CommonUtils.showAlert(getActivity(),"password and confirm password should be same");
        }else{
            setPassword();

        }
    }

    private void setPassword()
    {
        LockedAppDetails lockedAppDetails=new LockedAppDetails();
        lockedAppDetails.setAppName(appItems.getAppPackageName());
        lockedAppDetails.setAppLockKey(mPassword_edt.getText().toString());
        lockedAppDetails.setAppLockKeyHint(mPasswordHint_edt.getText().toString());
        lockedAppDetails.setAppLockType(CommonUtils.LockType.Password.ordinal());
        if(appItems.getAppName().equalsIgnoreCase(getActivity().getString(R.string.set_default_pwd))){
            MyAppLockPreferences.saveBoolToPref(getAppContext(), PREF_DEFAULT_PASSWORD_SET,true);
            MyAppLockPreferences.saveStrToPref(getAppContext(), MyAppLockConstansts.PREF_PASSWORD, mPassword_edt.getText().toString());
            showSetPasswordAlert("Your default password has been saved. now setup default pattren for the apps",true);
        }else{
            getDBInstance().insertAppKeyDetailIntoDB(lockedAppDetails);
            saveToPreference(appItems);
        }

    }

    private void saveToPreference(AppItems appItems) {
        appItems.setLayoutOpen(false);
        appItems.setAppLocked(true);

        getDBInstance().UpdateAppIntoDB(appItems);
        showSetPasswordAlert("Your password has been saved",true);

    }
    public void dismissDialog() {
        getActivity().startActivityForResult(getActivity().getIntent(), 10);
        dismiss();
    }

    private UpdateDB getDBInstance() {

        if (appInfoDB == null) {
            appInfoDB = new UpdateDB(getActivity());
        }
        return appInfoDB;
    }

   private void showSetPasswordAlert(String msg,final boolean isdefault){
      new AlertDialog.Builder(getActivity() ).setMessage(msg).setPositiveButton("OK", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               if(isdefault){
                   mResponse.onPasswordSaved();
               }
               dialog.dismiss();
               dismiss();
           }
       }).create().show();
    }
}
