package com.myapplock.models;

import java.io.Serializable;

import android.graphics.drawable.Drawable;

public class BlockAppItem implements Serializable{

	private String appPackageName;
	private String  appName;
	private String appActivityname;
	private Drawable appIcon;
	
	public String getAppPackageName() {
		return appPackageName;
	}
	public void setAppPackageName(String appPackageName) {
		this.appPackageName = appPackageName;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public Drawable getAppIcon() {
		return appIcon;
	}
	public void setAppIcon(Drawable appIcon) {
		this.appIcon = appIcon;
	}
	public String getAppActivityname() {
		return appActivityname;
	}
	public void setAppActivityname(String appActivityname) {
		this.appActivityname = appActivityname;
	}
}
