package com.myapplock.ui;

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
import com.myapplock.application.MyAppLock;
import com.myapplock.database.UpdateDB;
import com.myapplock.models.AppItems;
import com.myapplock.models.LockedAppDetails;
import com.myapplock.utils.CommonUtils;

public class SetNewPasswordFragment extends DialogFragment
{
    private EditText mPassword_edt, mConfirmPassword_edt, mPasswordHint_edt;

    private AppItems appItems;

    private UpdateDB appInfoDB;
    private MyAppLock myAppLock;
    public SetNewPasswordFragment(){

    }

    public static SetNewPasswordFragment newInstance()
    {
        SetNewPasswordFragment dialog = new SetNewPasswordFragment();
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.layout_custom_alert_dialog, container, false);
        initView(rootView);
//        setDialogPosition();
        return rootView;
    }



    private void setDialogPosition() {
        Window window = getDialog().getWindow();

        // set "origin" to top left corner, so to speak
        window.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP);

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

        mView.findViewById(R.id.ll_password_setting).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                validateViews();
            }
        });

        ImageView appIcon=(ImageView)mView.findViewById(R.id.app_icon_img);

        TextView mAppName=(TextView)mView.findViewById(R.id.app_name);
        mAppName.setText(appItems.getAppName());
        appIcon.setImageDrawable(appItems.getmAppIcon());
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
            dismissDialog();
        }
    }

    private void setPassword()
    {
        LockedAppDetails lockedAppDetails=new LockedAppDetails();
        lockedAppDetails.setAppName(appItems.getAppPackageName());
        lockedAppDetails.setAppLockKey(mPassword_edt.getText().toString());
        lockedAppDetails.setAppLockKeyHint(mPasswordHint_edt.getText().toString());
        lockedAppDetails.setAppLockType(CommonUtils.LockType.Password.ordinal());
        getDBInstance().insertAppKeyDetailIntoDB(lockedAppDetails);
        saveToPreference(appItems);
    }

    private void saveToPreference(AppItems appItems) {
        appItems.setOpen(false);
        appItems.setStatus(true);

        getAppContext().getLockedAppList().add(appItems);
        getAppContext().getUnlockedAppList().remove(appItems);
        getDBInstance().UpdateAppIntoDB(appItems);

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
    private MyAppLock  getAppContext(){
        if(myAppLock==null){
            myAppLock=(MyAppLock)getActivity().getApplicationContext();
        }
        return myAppLock;
    }
}
