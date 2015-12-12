package com.myapplock.interfaces;

import com.myapplock.models.BlockAppItem;

public interface ActivityStartingListener
{
    // public void onActivityStarting(String packageName, String activityName);
    public void onActivityStarting(BlockAppItem appItem);
}
