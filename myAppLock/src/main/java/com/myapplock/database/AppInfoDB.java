package com.myapplock.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.myapplock.models.AppItems;
import com.myapplock.utils.CommonUtils;

import java.util.ArrayList;

public class AppInfoDB extends SQLiteOpenHelper
{
	public static final String	DBName							= "appinfo";
	public static final String	DB_APP_DETAILS_TABLE			= "app_details";
	public static final String	DB_APP_LOCK_KEY_DETAILS_TABLE	= "app_key_lock_details";
	public static final int		DBVersion						= 1;

	public static final String	APP_PACKAGE_NAME				= "AppPackageName";
	public static final String	APP_NAME						= "AppName";
	public static final String	APP_ICON						= "AppIcon";
	public static final String	APP_STATUS						= "AppStatus";


	public static final String  APP_PASSWORD_TYPE 				= "AppPasswordType";
	public static final String	APP_PASSWORD					= "AppPassword";
	public static final String	APP_PASSWORD_HINT				= "AppPasswordHint";



	public static final String	APP_DETAILS	= "CREATE TABLE " + DB_APP_DETAILS_TABLE + "(" + APP_PACKAGE_NAME + " TEXT PRIMARY KEY NOT NULL , "
			+ APP_NAME + " TEXT , "
			+ APP_ICON + " BLOB , "
			+APP_STATUS + " INTEGER );";

	public static final String	APP_LOKE_DETAILS	= "CREATE TABLE " + DB_APP_LOCK_KEY_DETAILS_TABLE + "(" + APP_PACKAGE_NAME + " TEXT PRIMARY KEY NOT NULL , "
			+ APP_PASSWORD_TYPE + " INTEGER , "
			+ APP_PASSWORD_HINT + " TEXT , "
			+ APP_PASSWORD + " TEXT );";



	public AppInfoDB(Context context)
	{
		super(context, DBName, null, DBVersion);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{

		db.execSQL(APP_DETAILS);
		db.execSQL(APP_LOKE_DETAILS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS " + DB_APP_DETAILS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + DB_APP_LOCK_KEY_DETAILS_TABLE);
		onCreate(db);
	}



	public int getDBCount() {
		SQLiteDatabase database=this.getWritableDatabase();
		Cursor cursor=null;
		try {

			cursor=database.query(DB_APP_DETAILS_TABLE, null, null, null, null, null, null);
			if(cursor!=null){
				cursor.moveToFirst();
				return cursor.getCount();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			try {
				database.close();
				if(cursor!=null){
					cursor.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	public void insertAppInfo(ContentValues contentValues) {

		SQLiteDatabase database= this.getWritableDatabase();
		long i=database.insert(DB_APP_DETAILS_TABLE, null, contentValues);
		database.close();
	}

	public void updateAppInfo(String where, ContentValues contentValues) {
		SQLiteDatabase database=this.getWritableDatabase();
		int i = database.update(DB_APP_DETAILS_TABLE, contentValues, where, null);
		database.close();
	}
	public ArrayList<AppItems> getLockedAppList() {
		Cursor cursor=null;
		SQLiteDatabase database=this.getWritableDatabase();
		ArrayList<AppItems> lockedAppList=new ArrayList<AppItems>();
		try {

			cursor=database.query(DB_APP_DETAILS_TABLE, null, AppInfoDB.APP_STATUS + " = '" + CommonUtils.AppStatus.Locked.ordinal() + "'", null, null, null, null);

			if (cursor !=null && cursor.moveToFirst()) {
				do {

					AppItems appItems=new AppItems();
					Boolean status = (cursor.getInt(cursor.getColumnIndex(APP_STATUS)) == 1) ? true : false;
					appItems.setAppName(cursor.getString(cursor.getColumnIndex(APP_NAME)));
					appItems.setAppPackageName(cursor.getString(cursor.getColumnIndex(APP_PACKAGE_NAME)));
					appItems.setStatus(status);

					byte[] appIcon=cursor.getBlob(cursor.getColumnIndex(APP_ICON));
					appItems.setmAppIcon(CommonUtils.getDrwableFromByte(appIcon));
					lockedAppList.add(appItems);

				} while (cursor.moveToNext());
			}



		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			try {
				database.close();
				if(cursor!=null){
					cursor.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return lockedAppList;
	}
	public ArrayList<AppItems> getUnlockedAppList() {
		Cursor cursor=null;
		SQLiteDatabase database=this.getWritableDatabase();
		ArrayList<AppItems> unlockedAppListAppItems=new ArrayList<AppItems>();
		try {
			cursor=database.query(DB_APP_DETAILS_TABLE, null, AppInfoDB.APP_STATUS + " = '" + CommonUtils.AppStatus.UnLocked.ordinal() + "'", null, null, null, null);

			if (cursor !=null) {
				cursor.moveToFirst();
				do {
					AppItems appItems=new AppItems();
					Boolean status = (cursor.getInt(cursor.getColumnIndex(APP_STATUS)) == 1) ? true : false;
					appItems.setAppName(cursor.getString(cursor.getColumnIndex(APP_NAME)));
					appItems.setAppPackageName(cursor.getString(cursor.getColumnIndex(APP_PACKAGE_NAME)));
					appItems.setStatus(status);

					byte[] appIcon=cursor.getBlob(cursor.getColumnIndex(APP_ICON));
					appItems.setmAppIcon(CommonUtils.getDrwableFromByte(appIcon));
					unlockedAppListAppItems.add(appItems);

				} while (cursor.moveToNext());
			}



		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			try {
				database.close();
				if(cursor!=null){
					cursor.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return unlockedAppListAppItems;
	}


}
