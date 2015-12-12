package com.myapplock.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabWidget;

import com.myapplock.R;
import com.myapplock.adapter.TabsPagerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class LaunchingFragment extends Fragment  implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener
{
    private View mView;

    private ViewPager viewPager;

    private TabsPagerAdapter mAdapter;

    static Context mContext;

    private TabHost mTabHost;

    private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, LaunchingFragment.TabInfo>();
    String Titles[]={"Uplocked Apps","Locked Apps"};
    public LaunchingFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_settings, null);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        this.initialiseTabHost(savedInstanceState);

        this.intialiseViewPager();
    }

    private void intialiseViewPager()
    {

        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(getActivity(), AllAppsLandingFragment.class.getName()));
        fragments.add(Fragment.instantiate(getActivity(), LockedAppsLandingFragment.class.getName()));

        viewPager = (ViewPager) mView.findViewById(R.id.viewpager);

        mAdapter = new TabsPagerAdapter(super.getChildFragmentManager(), fragments,Titles);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(mAdapter);
        viewPager.setOnPageChangeListener(this);

    }

    private void initialiseTabHost(Bundle args)
    {

        mTabHost = (TabHost) mView.findViewById(android.R.id.tabhost);
        mTabHost.setup();
        TabInfo tabInfo = null;

        LaunchingFragment.AddTab(LaunchingFragment.this, this.mTabHost,
                this.mTabHost.newTabSpec("All Apps").setIndicator("All Apps"), (tabInfo =
                        new TabInfo("All Apps", AllAppsLandingFragment .class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);

        LaunchingFragment.AddTab(LaunchingFragment.this, this.mTabHost,
                this.mTabHost.newTabSpec("Locked Apps").setIndicator("Locked Apps"), (tabInfo =
                        new TabInfo("Locked Apps", LockedAppsLandingFragment.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);

        TabWidget tabWidget = mTabHost.getTabWidget();
        tabWidget.setStripEnabled(false);
        mTabHost.setOnTabChangedListener(this);
    }

    private static void AddTab(LaunchingFragment activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo)
    {
        tabSpec.setContent(activity.new TabFactory(activity));
        tabHost.addTab(tabSpec);
    }

    private class TabInfo
    {
        private String tag;

        TabInfo(String tag, Class<?> clazz, Bundle args)
        {
            this.tag = tag;
        }

    }

    class TabFactory implements TabContentFactory
    {

        public TabFactory(LaunchingFragment activity)
        {

        }

        public View createTabContent(String tag)
        {
            View v = new View(getActivity());
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageSelected(int position)
    {
        this.mTabHost.setCurrentTab(position);

    }

    @Override
    public void onTabChanged(String arg0)
    {
        int pos = this.mTabHost.getCurrentTab();
        this.viewPager.setCurrentItem(pos);

    }
}
