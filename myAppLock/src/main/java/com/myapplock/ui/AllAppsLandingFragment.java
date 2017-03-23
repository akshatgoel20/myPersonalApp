package com.myapplock.ui;

import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.haibison.android.lockpattern.LockPatternActivity;
import com.myapplock.R;
import com.myapplock.adapter.AppAdapter;
import com.myapplock.adapter.AppAdapter.OnItemClickListener;
import com.myapplock.application.MyAppLock;
import com.myapplock.database.AppInfoDB;
import com.myapplock.database.UpdateDB;
import com.myapplock.interfaces.UpdateListContent;
import com.myapplock.models.AppItems;
import com.myapplock.models.LockedAppDetails;
import com.myapplock.utils.CommonUtils;
import com.myapplock.utils.MyAppLockConstansts;
import com.myapplock.utils.MyAppLockPreferences;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AllAppsLandingFragment extends Fragment implements OnClickListener,UpdateListContent {


    public static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 101;
    public static final int DEFAULT = 103;
    public static final int PASSWORD = 104;
    public static final int PATTERN = 105;
    private View mView;

    private RecyclerView mAppListView;

    private ProgressDialog loading;

    private AppAdapter adapter;

    private static final int REQ_CREATE_PATTERN = 1;

    private int tempPos = -1;

    private boolean isUpdateCalled;
    private MyAppLock myAppLock;

    private boolean isPasswodDialogOpen;

    private UpdateDB mUpdateDB;

    private AppItems mCurrentApp;
    public AllAppsLandingFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        setRetainInstance(true);
        mView = inflater.inflate(R.layout.fragment_landing, null);
        initView();
        // forImageView();
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        new LoadApplicationTask().execute();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.btn_selectAll:
                selectAllApp();
                break;
            case R.id.btn_deSelectAll:
                deSelectAllApp();
                break;
            case R.id.btn_refresh:
                resfreshApp();
                break;

            default:
                break;
        }
    }

    private void initView()
    {
        MyAppLockConstansts.cuurentFragment="AllAppsLandingFragment";
        mAppListView = (RecyclerView) mView.findViewById(R.id.recycler_view);
    }

    private void resfreshApp()
    {
        Comparator<AppItems> cp =
                AppItems.getComparator(AppItems.SortParameter.NAME_DESCENDING, AppItems.SortParameter.SELECTED_ASCENDING);
        Comparator<AppItems> cp1 =AppItems.getSimpleComparator();
        Collections.sort(getAppContext().getUnlockedAppList(), cp1);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            adapter = new AppAdapter(getActivity(), getAppContext().getUnlockedAppList(), CommonUtils.AppStatus.UnLocked.ordinal());
            mAppListView.setAdapter(adapter);
        }

    }

    private void deSelectAllApp()
    {
        for (AppItems app : getAppContext().getUnlockedAppList()) {
            app.setStatus(false);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            adapter = new AppAdapter(getActivity(), getAppContext().getUnlockedAppList(),CommonUtils.AppStatus.UnLocked.ordinal());
            mAppListView.setAdapter(adapter);
        }
    }

    private void selectAllApp()
    {

        for (AppItems app : getAppContext().getUnlockedAppList()) {
            app.setStatus(true);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            adapter = new AppAdapter(getActivity(), getAppContext().getUnlockedAppList(),CommonUtils.AppStatus.UnLocked.ordinal());
            mAppListView.setAdapter(adapter);
        }
    }




    @Override
    public void onResume() {
        super.onResume();
        if(isPasswodDialogOpen){
            isPasswodDialogOpen=false;
            updateList();
        }

    }

    @Override
    public void updateList() {
        isUpdateCalled=true;
        Comparator<AppItems> cp =
                AppItems.getComparator(AppItems.SortParameter.NAME_DESCENDING,
                        AppItems.SortParameter.SELECTED_ASCENDING);
        Comparator<AppItems> cp1 =AppItems.getSimpleComparator();
        Collections.sort(getAppContext().getUnlockedAppList(), cp1);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            adapter = new AppAdapter(getActivity(), getAppContext().getUnlockedAppList(),CommonUtils.AppStatus.UnLocked.ordinal());
            mAppListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    private class LoadApplicationTask extends AsyncTask<Integer, Integer, Integer>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            loading = ProgressDialog.show(getActivity(), "Please wait", "Gathering application... ");
        }

        @Override
        protected Integer doInBackground(Integer... params)
        {
            getAppContext().getUnlockedAppList().clear();
            if(getAppContext() !=null && !getAppContext().getUnlockedAppList().isEmpty()){
                getAppContext().getUnlockedAppList().addAll(getAppContext().getUnlockedAppList());
            }
            else if (getDBInstance().getDBCount(AppInfoDB.DB_APP_DETAILS_TABLE) > 0) {
                getAppContext().getUnlockedAppList().addAll(getDBInstance().getUnlockedAppList());
            }else{

                Drawable image=null;
                AppItems appInfo;
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

                List<ResolveInfo> mApps = getActivity().getPackageManager().queryIntentActivities(mainIntent, 0);

                int length = mApps.size();
                for (int i = 0; i < length; i++) {
                    ResolveInfo info = mApps.get(i);
                    image = info.loadIcon(getActivity().getPackageManager());

                    appInfo = new AppItems();
                    appInfo.setAppName(info.activityInfo.loadLabel(getActivity().getPackageManager()).toString());
                    appInfo.setAppPackageName(info.activityInfo.packageName);
                    appInfo.setmAppIcon(image);
                    appInfo.setStatus(false);
                    appInfo.setOpen(false);

                    if (!info.activityInfo.packageName.equalsIgnoreCase(getActivity().getPackageName())) {
                        getDBInstance().insertAppIntoDB(appInfo);
                    }
                }

                // Add default components
                ApplicationInfo info;
                try {

                    info = getActivity().getPackageManager().getApplicationInfo("com.android.packageinstaller", 0);
                    image=info.loadIcon(getActivity().getPackageManager());
                    appInfo = new AppItems();
                    appInfo.setAppName(info.loadLabel(getActivity().getPackageManager()).toString());
                    appInfo.setAppPackageName(info.packageName);
                    appInfo.setmAppIcon(image);
                    appInfo.setStatus(false);
                    appInfo.setOpen(false);

                    getDBInstance().insertAppIntoDB(appInfo);

                    getAppContext().getUnlockedAppList().addAll(getDBInstance().getUnlockedAppList());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            super.onPostExecute(result);
            loading.dismiss();
            Comparator<AppItems> cp1 =AppItems.getSimpleComparator();
            Collections.sort(getAppContext().getUnlockedAppList(), cp1);

            adapter = new AppAdapter(getActivity(), getAppContext().getUnlockedAppList(),CommonUtils.AppStatus.UnLocked.ordinal());
            LinearLayoutManager layoutManager =
                    new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            mAppListView.setLayoutManager(layoutManager);
            mAppListView.setAdapter(adapter);
            mAppListView.setItemAnimator(new DefaultItemAnimator());

            adapter.SetOnItemClickListener(new OnItemClickListener()
            {

                @Override
                public void onItemClick(View view, int position)
                {
                    AppItems model = getAppContext().getUnlockedAppList().get(position);
                    if (view instanceof Button) {
                        switch (view.getId()) {
                            case R.id.left:

//                                saveToPreference(model);
                                CheckAndAccept(DEFAULT,model);
                                Toast.makeText(getActivity(), "Letf: " + position, Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.middel:
//                                createPassword(model);
                                CheckAndAccept(PASSWORD,model);
                                Toast.makeText(getActivity(), "Middel: " + position, Toast.LENGTH_SHORT).show();
                                break;

                            case R.id.right:
//                                createPattern(model);
                                CheckAndAccept(PATTERN,model);
                                Toast.makeText(getActivity(), "Right: " + position, Toast.LENGTH_SHORT).show();
                                break;

                            default:
                                break;
                        }
                    } else {

                        if (tempPos != -1) {
                            getAppContext().getUnlockedAppList().get(tempPos).setOpen(false);
                        }
                        if (tempPos == position) {
                            tempPos = -1;
                            model.setOpen(false);
                        } else {
                            model.setOpen(true);
                            tempPos = position;
                        }
                        getListClose(position);
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void getListClose(int currPos){
        if(isUpdateCalled){
            isUpdateCalled=false;
            for(int i=0;i<getAppContext().getUnlockedAppList().size();i++){
                if(currPos!=i){
                    getAppContext().getUnlockedAppList().get(i).setOpen(false);
                }
            }
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // in fragment class callback
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS:
                if (hasPermission()){
                    //getStats();
                }
                break;
            case REQ_CREATE_PATTERN: {
                if (resultCode == getActivity().RESULT_OK) {
                    char[] pattern = data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN);
                    setKeyLock(mCurrentApp,new String(pattern), CommonUtils.LockType.Pattern.ordinal(),true);
                }
                break;
            }// REQ_CREATE_PATTERN
        }
    }

    private void createPassword(AppItems appItems){
        isPasswodDialogOpen=true;
        SetNewPasswordFragment fragment = SetNewPasswordFragment.newInstance();

        Bundle bundle=new Bundle();
        bundle.putSerializable("App", appItems);
        fragment.setArguments(bundle);
        fragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDialog);
        fragment.show(getFragmentManager(), "New Password");
    }




    private void createPattern(AppItems pAppItem)
    {
        mCurrentApp=pAppItem;

        Intent intent =
                new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null, getActivity(), LockPatternActivity.class);
        startActivityForResult(intent, REQ_CREATE_PATTERN);
    }


    private void setKeyLock(AppItems mCurrentApp,String pLockKey, int pLockType,boolean isNew)
    {
        LockedAppDetails lockedAppDetails=new LockedAppDetails();
        lockedAppDetails.setAppName(mCurrentApp.getAppPackageName());
        lockedAppDetails.setAppLockKey(pLockKey);
        lockedAppDetails.setAppLockKeyHint("");
        lockedAppDetails.setAppLockType(pLockType);
        getDBInstance().insertAppKeyDetailIntoDB(lockedAppDetails);
        if(isNew){
            saveToPreference(mCurrentApp);
            updateList();
        }

    }

    private void saveToPreference(AppItems appItems) {

        appItems.setOpen(false);
        appItems.setStatus(true);
        getAppContext().getLockedAppList().add(appItems);
        getAppContext().getUnlockedAppList().remove(appItems);
        getDBInstance().UpdateAppIntoDB(appItems);

        Boolean KeyType=MyAppLockPreferences.getBoolFromPref(getActivity(),MyAppLockConstansts.PREF_CURRENT_LOCK_MODE,false);
        if(KeyType){
            String lock=MyAppLockPreferences.getStringFromPreferences(getActivity(),MyAppLockConstansts.PREF_PATTERN,"");
            setKeyLock(appItems,lock, CommonUtils.LockType.Pattern.ordinal(),false);
        }else{
            String password=MyAppLockPreferences.getStringFromPreferences(getActivity(),MyAppLockConstansts.PREF_PASSWORD,"");
            setKeyLock(appItems, password, CommonUtils.LockType.Password.ordinal(), false);
        }
    }
    private UpdateDB getDBInstance() {

        if (mUpdateDB == null) {
            mUpdateDB = new UpdateDB(getActivity());
        }
        return mUpdateDB;
    }

    private MyAppLock  getAppContext(){
        if(myAppLock==null){
            myAppLock=(MyAppLock)getActivity().getApplicationContext();
        }
        return myAppLock;
    }



    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager) getActivity().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getActivity().getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void  requestPermission(){
        startActivityForResult(
                new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
    }

    private void CheckAndAccept(int passwordType,AppItems appItems) {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP_MR1 && hasPermission()) {
            if(passwordType==DEFAULT){
                saveToPreference(appItems);
            }else if(passwordType==PASSWORD){
                createPassword(appItems);
            }else if(passwordType==PATTERN){
                createPattern(appItems);
            }
        } else {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
            builder.setMessage("Ask User to take permission");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermission();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.create().show();
        }
    }
}
