package com.myapplock.ui.fragment;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.haibison.android.lockpattern.LockPatternActivity;
import com.myapplock.R;
import com.myapplock.adapter.LockedAppAdapter;
import com.myapplock.database.AppInfoDB;
import com.myapplock.database.UpdateDB;
import com.myapplock.framework.api.UpdateListContent;
import com.myapplock.models.AppItems;
import com.myapplock.models.LockedAppDetails;
import com.myapplock.utils.CommonUtils;
import com.myapplock.utils.MyAppLockConstansts;
import com.myapplock.utils.MyAppLockPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllAppsLandingFragment extends Fragment implements OnClickListener, UpdateListContent {


    public static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 101;
    public static final int DEFAULT = 103;
    public static final int PASSWORD = 104;
    public static final int PATTERN = 105;
    private View mView;

    private RecyclerView mAppListView;

    private ProgressDialog loading;

    private LockedAppAdapter adapter;

    private static final int REQ_CREATE_PATTERN = 1;

    private int tempPos = -1;

    private boolean isUpdateCalled;

    private boolean isPasswordDialogOpen;

    private UpdateDB mUpdateDB;

    private AppItems mCurrentApp;

    private int mClickedPos = -1;

    public List<AppItems> getAllAppsList() {
        if (null == mAllAppsList) {
            mAllAppsList = new ArrayList<AppItems>();
        }
        return mAllAppsList;
    }

    private List<AppItems> mAllAppsList;


    public AllAppsLandingFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        mView = inflater.inflate(R.layout.fragment_landing, null);
        initView();
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new LoadApplicationTask().execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_selectAll:
                selectAllApp();
                break;
            case R.id.btn_deSelectAll:
                deSelectAllApp();
                break;
            case R.id.btn_refresh:
                refreshApp();
                break;
            default:
                break;
        }
    }

    private void initView() {
        MyAppLockConstansts.cuurentFragment = "AllAppsLandingFragment";
        mAppListView = (RecyclerView) mView.findViewById(R.id.recycler_view);
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            actionBarHeight = actionBarHeight + actionBarHeight;

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//            layoutParams.setMargins(0, actionBarHeight, 0, 0);
            mAppListView.setLayoutParams(layoutParams);
        }
    }

    private void refreshApp() {
        Collections.sort(getAllAppsList(), AppItems.getSimpleComparator());

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            adapter = new LockedAppAdapter(getAllAppsList(), mAppListView);
            mAppListView.setAdapter(adapter);
        }
    }

    private void deSelectAllApp() {
        for (AppItems app : getAllAppsList()) {
            app.setAppLocked(false);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            adapter = new LockedAppAdapter(getAllAppsList(), mAppListView);
            mAppListView.setAdapter(adapter);
        }
    }

    private void selectAllApp() {

        for (AppItems app : getAllAppsList()) {
            app.setAppLocked(true);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            adapter = new LockedAppAdapter(getAllAppsList(), mAppListView);
            mAppListView.setAdapter(adapter);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (isPasswordDialogOpen) {
            isPasswordDialogOpen = false;
            updateList();
        }

    }

    @Override
    public void updateList() {
        isUpdateCalled = true;
        Collections.sort(getAllAppsList(), AppItems.getSimpleComparator());

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            adapter = new LockedAppAdapter(getAllAppsList(), mAppListView);
            mAppListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    private class LoadApplicationTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(getActivity(), "Please wait", "Gathering application... ");
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            getAllAppsList().clear();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<PackageInfo> packList = getActivity().getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
            List<ResolveInfo> mApps = getActivity().getPackageManager().queryIntentActivities(mainIntent, 0);
            int totalAppInDB = getDBInstance().getDBCount(AppInfoDB.DB_APP_DETAILS_TABLE);

            if (totalAppInDB == (mApps.size() - 1)) {
                getAllAppsList().addAll(getDBInstance().getAllAppList());
            } else {
                Drawable image;
                AppItems appInfo;

                int length = mApps.size();
                for (int i = 0; i < length; i++) {
                    ResolveInfo info = mApps.get(i);
                    image = info.loadIcon(getActivity().getPackageManager());

                    appInfo = new AppItems();
                    appInfo.setAppName(info.activityInfo.loadLabel(getActivity().getPackageManager()).toString());
                    appInfo.setAppPackageName(info.activityInfo.packageName);
                    appInfo.setAppIcon(image);
                    appInfo.setAppLocked(false);
                    appInfo.setLayoutOpen(false);

                    if (!info.activityInfo.packageName.equalsIgnoreCase(getActivity().getPackageName())) {
                        getDBInstance().insertAppIntoDB(appInfo);
                    }
                }

                // Add default components
                ApplicationInfo info;
                try {

                    info = getActivity().getPackageManager().getApplicationInfo("com.google.android.packageinstaller", 0);
                    image = info.loadIcon(getActivity().getPackageManager());
                    appInfo = new AppItems();
                    appInfo.setAppName(info.loadLabel(getActivity().getPackageManager()).toString());
                    appInfo.setAppPackageName(info.packageName);
                    appInfo.setAppIcon(image);
                    appInfo.setAppLocked(false);
                    appInfo.setLayoutOpen(false);

                    getDBInstance().insertAppIntoDB(appInfo);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                getAllAppsList().addAll(getDBInstance().getAllAppList());
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            loading.dismiss();
            Collections.sort(getAllAppsList(), AppItems.getSimpleComparator());

            adapter = new LockedAppAdapter(getAllAppsList(), mAppListView);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            mAppListView.setLayoutManager(mLayoutManager);
            mAppListView.setItemAnimator(new DefaultItemAnimator());
            mAppListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            adapter.SetOnItemClickListener(new LockedAppAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    mClickedPos = position;
                    AppItems app = getAllAppsList().get(position);
                    if (view instanceof Button || view instanceof ImageView) {
                        switch (view.getId()) {
                            case R.id.btn_default_pass:
                                CheckAndAccept(DEFAULT, app, position);
                                break;
                            case R.id.btn_pin_pass:
                                CheckAndAccept(PASSWORD, app, position);
                                break;
                            case R.id.btn_pattern_pass:
                                CheckAndAccept(PATTERN, app, position);
                                break;
                            case R.id.btn_unlock_app:
                                saveToPreference(app, false, position);
                                break;
                            case R.id.iv_app_selected_unselected_icon:
                                if (app.isAppLocked()) {
                                    saveToPreference(app, false, position);
                                }else{
                                    showHideLockLayout(app,position);
                                }
                                break;
                            default:
                                break;
                        }
                        adapter.notifyItemChanged(position);
                    } else {
                        showHideLockLayout(app,position);
                    }

                }
            });
        }
    }

    private void showHideLockLayout(AppItems model,int position){
        if (tempPos != -1) {
            getAllAppsList().get(tempPos).setLayoutOpen(false);
        }
        if (tempPos == position) {
            tempPos = -1;
            model.setLayoutOpen(false);
        } else {
            model.setLayoutOpen(true);
            tempPos = position;
        }
        getListClose(position);
        adapter.notifyDataSetChanged();
    }

    private void getListClose(int currPos) {
        if (isUpdateCalled) {
            isUpdateCalled = false;
            for (int i = 0; i < getAllAppsList().size(); i++) {
                if (currPos != i) {
                    getAllAppsList().get(i).setLayoutOpen(false);
                }
            }
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // in fragment class callback
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS:
                if (hasPermission()) {
                    //getStats();
                }
                break;
            case REQ_CREATE_PATTERN: {
                if (resultCode == getActivity().RESULT_OK) {
                    char[] pattern = data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN);
                    setKeyLock(mCurrentApp, new String(pattern), CommonUtils.LockType.Pattern.ordinal(), true, mClickedPos);
                }
                break;
            }// REQ_CREATE_PATTERN
        }
    }

    private void createPassword(AppItems appItems) {
        isPasswordDialogOpen = true;
        SetNewPasswordFragment fragment = SetNewPasswordFragment.newInstance();

        Bundle bundle = new Bundle();
        bundle.putSerializable("App", appItems);
        fragment.setArguments(bundle);
        fragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDialog);
        fragment.show(getFragmentManager(), "New Password");
    }


    private void createPattern(AppItems pAppItem) {
        mCurrentApp = pAppItem;

        Intent intent =
                new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null, getActivity(), LockPatternActivity.class);
        startActivityForResult(intent, REQ_CREATE_PATTERN);
    }


    private void setKeyLock(AppItems mCurrentApp, String pLockKey, int pLockType, boolean isNew, int pos) {
        LockedAppDetails lockedAppDetails = new LockedAppDetails();
        lockedAppDetails.setAppName(mCurrentApp.getAppPackageName());
        lockedAppDetails.setAppLockKey(pLockKey);
        lockedAppDetails.setAppLockKeyHint("");
        lockedAppDetails.setAppLockType(pLockType);
        getDBInstance().insertAppKeyDetailIntoDB(lockedAppDetails);
        if (isNew) {
            saveToPreference(mCurrentApp, true, pos);
            updateList();
        }

    }

    private void saveToPreference(AppItems appItems, boolean isLock, int pos) {

        appItems.setLayoutOpen(false);
        appItems.setAppLocked(isLock);
        getDBInstance().UpdateAppIntoDB(appItems);
        Boolean KeyType = MyAppLockPreferences.getBoolFromPref(getActivity(), MyAppLockConstansts.PREF_CURRENT_LOCK_MODE, false);
        if (KeyType) {
            String lock = MyAppLockPreferences.getStringFromPreferences(getActivity(), MyAppLockConstansts.PREF_PATTERN, "");
            setKeyLock(appItems, lock, CommonUtils.LockType.Pattern.ordinal(), false, pos);
        } else {
            String password = MyAppLockPreferences.getStringFromPreferences(getActivity(), MyAppLockConstansts.PREF_PASSWORD, "");
            setKeyLock(appItems, password, CommonUtils.LockType.Password.ordinal(), false, pos);
        }
        if (null != adapter) {
            adapter.notifyItemChanged(pos);
        }
    }

    private UpdateDB getDBInstance() {

        if (mUpdateDB == null) {
            mUpdateDB = new UpdateDB(getActivity());
        }
        return mUpdateDB;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager) getActivity().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getActivity().getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestPermission() {
        startActivityForResult(
                new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
    }

    private void CheckAndAccept(int passwordType, AppItems appItems, int pos) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (passwordType == DEFAULT) {
                saveToPreference(appItems, true, pos);
            } else if (passwordType == PASSWORD) {
                createPassword(appItems);
            } else if (passwordType == PATTERN) {
                createPattern(appItems);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && hasPermission()) {
            if (passwordType == DEFAULT) {
                saveToPreference(appItems, true, pos);
            } else if (passwordType == PASSWORD) {
                createPassword(appItems);
            } else if (passwordType == PATTERN) {
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
