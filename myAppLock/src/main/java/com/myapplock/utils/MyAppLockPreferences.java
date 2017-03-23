package com.myapplock.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.myapplock.application.MyAppLock;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class MyAppLockPreferences
{

	public static void saveStrToPref(Context mContext, String key, String value)
	{
		putStringInPreferences(mContext, key, value);
	}

	private static void putStringInPreferences(Context mContext, String key, String nick)
	{
		SharedPreferences.Editor editor = getPrefs(mContext).edit();
		editor.putString(key, nick);
		editor.commit();
	}

	public static void saveBoolToPref(Context mContext, String key, boolean value)
	{
		SharedPreferences.Editor editor = getPrefs(mContext).edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	public static void saveIntToPreference(Context mContext, String key, int value)
	{
		SharedPreferences.Editor editor = getPrefs(mContext).edit();
		editor.putLong(key, value);
		editor.commit();
	}
	public static Long getIntFromPreferences(Context mContext, String key, long defaultValue)
	{
		long temp = getPrefs(mContext).getLong(key, defaultValue);
		return temp;
	}
	public static void saveArrayToPref(Context mContext, String key, String[] applicationList)
	{
		String combined = "";
		for (int i=0; i<applicationList.length; i++){
			combined = combined + applicationList[i] + ";";
		}
		SharedPreferences.Editor editor = getPrefs(mContext).edit();
		editor.putString(key, combined);
		editor.commit();
	}
	public static String[] getArrayFromPreferences(Context mContext, String key, String[] defaultValue)
	{
		String[] mApplicationList = getPrefs(mContext).getString(key, "").split(";");
		return mApplicationList;
	}

	public static String getStringFromPreferences(Context mContext, String key, String defaultValue)
	{
		String temp = getPrefs(mContext).getString(key, defaultValue);
		return temp;
	}


	public static Boolean getBoolFromPref(Context mContext, String key, boolean defaultValue)
	{
		boolean temp = getPrefs(mContext).getBoolean(key, defaultValue);
		return temp;
	}

	public static void removeStringFromPreferences(Context mContext, String key)
	{
		SharedPreferences sharedPreferences = getPrefs(mContext);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.remove(key);
		editor.commit();
	}

	public static SharedPreferences getPrefs(Context context)
	{
		return MyAppLock.getAppContext().getSharedPreferences(MyAppLockConstansts.PREF_NAME, context.MODE_PRIVATE);

	}
	public static void saveMap(Context context,Map<String,Boolean> inputMap){
		SharedPreferences pSharedPref = getPrefs(context);
		if (pSharedPref != null){
			JSONObject jsonObject = new JSONObject(inputMap);
			String jsonString = jsonObject.toString();
			Editor editor = pSharedPref.edit();
			editor.remove("My_map").commit();
			editor.putString("My_map", jsonString);
			editor.commit();
		}
	}

	public static Map<String,Boolean> loadMap(Context context){
		Map<String,Boolean> outputMap = new HashMap<String,Boolean>();
		SharedPreferences pSharedPref = getPrefs(context);
		try{
			if (pSharedPref != null){       
				String jsonString = pSharedPref.getString("My_map", (new JSONObject()).toString());
				JSONObject jsonObject = new JSONObject(jsonString);
				Iterator<String> keysItr = jsonObject.keys();
				while(keysItr.hasNext()) {
					String key = keysItr.next();
					Boolean value = (Boolean) jsonObject.get(key);
					outputMap.put(key, value);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return outputMap;
	}

}
