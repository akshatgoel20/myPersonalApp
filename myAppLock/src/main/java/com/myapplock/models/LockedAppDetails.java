package com.myapplock.models;

import java.io.Serializable;

public class LockedAppDetails implements Serializable
{
    private String appName;
    private int appLockType;
    private String appLockKey;
    private String appLockKeyHint;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getAppLockType() {
        return appLockType;
    }

    public void setAppLockType(int appLockType) {
        this.appLockType = appLockType;
    }

    public String getAppLockKeyHint() {
        return appLockKeyHint;
    }

    public void setAppLockKeyHint(String appLockKeyHint) {
        this.appLockKeyHint = appLockKeyHint;
    }

    public String getAppLockKey() {
        return appLockKey;
    }

    public void setAppLockKey(String appLockKey) {
        this.appLockKey = appLockKey;
    }
}
