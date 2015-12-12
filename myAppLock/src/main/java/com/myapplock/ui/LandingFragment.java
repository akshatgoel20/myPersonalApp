package com.myapplock.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.myapplock.R;
import com.myapplock.adapter.TabsPagerAdapter;

import java.util.List;
import java.util.Vector;

public class LandingFragment extends Fragment
{
    private View mView;

    static ViewPager viewPager;

    private TabsPagerAdapter mAdapter;

    static Context mContext;

    private  Toolbar tToolbar;


    String Titles[]={"UNLOCKED APPS","LOCKED APPS"};

    public LandingFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_tabs, null);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);


        this.intialiseViewPager();
    }

    private void intialiseViewPager()
    {
//        tToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
//        ((AppCompatActivity)getActivity()).setSupportActionBar(tToolbar);
//
//        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(getActivity(), AllAppsLandingFragment.class.getName()));
        fragments.add(Fragment.instantiate(getActivity(), LockedAppsLandingFragment.class.getName()));

        viewPager = (ViewPager) getActivity().findViewById(R.id.pager);
        mAdapter = new TabsPagerAdapter(getChildFragmentManager(), fragments, Titles);

        viewPager.setAdapter(mAdapter);
        initTabs();
    }
    private void initTabs(){

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tab);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());

                switch (tab.getPosition()) {
                    case 0:
                        showToast("One");
                        break;
                    case 1:
                        showToast("Two");

                        break;
                    case 2:
                        showToast("Three");

                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
    void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
}
