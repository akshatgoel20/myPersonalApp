package com.myapplock.interfaces;

import com.myapplock.models.BlockAppItem;

public interface ActivityStartingListener
{
    // public void onActivityStarting(String packageName, String activityName);
    void onActivityStarting(BlockAppItem appItem);
    void setLastPackageToEmplty();
}
