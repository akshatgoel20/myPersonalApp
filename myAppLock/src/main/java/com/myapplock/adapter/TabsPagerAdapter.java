package com.myapplock.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class TabsPagerAdapter extends FragmentPagerAdapter
{
	private String[] mTitle;
	private List<Fragment>	fragments;
	
	public TabsPagerAdapter(FragmentManager fm,List<Fragment> fragments,String[] titles)
	{
		super(fm);
		this.fragments = fragments;
        mTitle=titles;
	}

	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}

	@Override
	public Fragment getItem(int position)
	{
		return fragments.get(position);
		
	}

    @Override
    public CharSequence getPageTitle(int position)
    {
        return mTitle[position];
    }
	@Override
	public int getCount()
	{
		 return 2;
	
	}
	
}
