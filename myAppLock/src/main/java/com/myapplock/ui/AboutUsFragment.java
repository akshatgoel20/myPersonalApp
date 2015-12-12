package com.myapplock.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myapplock.R;

public class AboutUsFragment extends Fragment
{
    public static final String ACTION_APPLICATION_PASSED = "com.gueei.applocker.applicationpassedtest";

    private static final int REQ_ENTER_PATTERN = 2;

    public static final String BlockedPackageName = "locked package name";

    public static final String BlockedActivityName = "locked activity name";

    public static final String EXTRA_PACKAGE_NAME = "com.gueei.applocker.extra.package.name";

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        mView = inflater.inflate(R.layout.splash_home, null);
        return mView;
    }

}
