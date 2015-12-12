package com.myapplock.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.myapplock.models.AppItems;
import com.myapplock.models.LockedAppDetails;
import com.myapplock.utils.CommonUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by amjaiswal on 7/9/2015.
 */
public class UpdateDB {
    AppInfoDB appInfoDB;
    Context mContext;

    public UpdateDB(Context context) {
        mContext = context;
    }

    public void insertAppIntoDB(AppItems appItems) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AppInfoDB.APP_PACKAGE_NAME, appItems.getAppPackageName());
        contentValues.put(AppInfoDB.APP_NAME, appItems.getAppName());
        contentValues.put(AppInfoDB.APP_ICON, CommonUtils.getByteFromDrawable(appItems.getmAppIcon()));
        contentValues.put(AppInfoDB.APP_STATUS, CommonUtils.AppStatus.UnLocked.ordinal());
        insertAppInfo(contentValues);
    }

    public void UpdateAppIntoDB(AppItems appItems) {
        String where = AppInfoDB.APP_PACKAGE_NAME + " = '" + appItems.getAppPackageName() + "'";
        ContentValues contentValues = new ContentValues();
        contentValues.put(AppInfoDB.APP_PACKAGE_NAME, appItems.getAppPackageName());
        contentValues.put(AppInfoDB.APP_NAME, appItems.getAppName());
        contentValues.put(AppInfoDB.APP_ICON,  CommonUtils.getByteFromDrawable(appItems.getmAppIcon()));
        contentValues.put(AppInfoDB.APP_STATUS, CommonUtils.AppStatus.Locked.ordinal());
        updateAppInfo(where, contentValues);
    }

    public void insertAppKeyDetailIntoDB(LockedAppDetails appItems) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(AppInfoDB.APP_PACKAGE_NAME, appItems.getAppName());
        contentValues.put(AppInfoDB.APP_PASSWORD_TYPE, appItems.getAppLockType());
        contentValues.put(AppInfoDB.APP_PASSWORD_HINT, appItems.getAppLockKeyHint());
        contentValues.put(AppInfoDB.APP_PASSWORD, appItems.getAppLockKey());
        insertAppKeyInfo(contentValues);
    }

    public void updateAppKeyDetailIntoDB(String appItemsPackageName) {
        String where = AppInfoDB.APP_PACKAGE_NAME + " = '" + appItemsPackageName + "'";

        updateAppKeyInfo(where);
    }


    public void insertAppInfo(ContentValues contentValues) {

        SQLiteDatabase database = getDBInstance().getWritableDatabase();
        long i = database.insert(AppInfoDB.DB_APP_DETAILS_TABLE, null, contentValues);
        database.close();
    }

    public void updateAppInfo(String where, ContentValues contentValues) {
        SQLiteDatabase database = getDBInstance().getWritableDatabase();
        int i = database.update(AppInfoDB.DB_APP_DETAILS_TABLE, contentValues, where, null);
        database.close();
    }

    public void insertAppKeyInfo(ContentValues contentValues) {

        SQLiteDatabase database = getDBInstance().getWritableDatabase();
        long i = database.insert(AppInfoDB.DB_APP_LOCK_KEY_DETAILS_TABLE, null, contentValues);
        database.close();
    }

    public void updateAppKeyInfo(String where) {
        SQLiteDatabase database = getDBInstance().getWritableDatabase();
        int i = database.delete(AppInfoDB.DB_APP_LOCK_KEY_DETAILS_TABLE, where, null);
        database.close();
    }


    public int getDBCount(String TableName) {
        SQLiteDatabase database = getDBInstance().getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = database.query(AppInfoDB.DB_APP_DETAILS_TABLE, null, null, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                return cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                database.close();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }


    public ArrayList<AppItems> getLockedAppList() {
        Cursor cursor = null;
        SQLiteDatabase database = getDBInstance().getWritableDatabase();
        ArrayList<AppItems> lockedAppList = new ArrayList<AppItems>();
        try {

            cursor = database.query(AppInfoDB.DB_APP_DETAILS_TABLE, null, AppInfoDB.APP_STATUS + " = '" + CommonUtils.AppStatus.Locked.ordinal() + "'", null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {

                    AppItems appItems = new AppItems();
                    Boolean status = (cursor.getInt(cursor.getColumnIndex(AppInfoDB.APP_STATUS)) == 1) ? true : false;
                    appItems.setAppName(cursor.getString(cursor.getColumnIndex(AppInfoDB.APP_NAME)));
                    appItems.setAppPackageName(cursor.getString(cursor.getColumnIndex(AppInfoDB.APP_PACKAGE_NAME)));
                    appItems.setStatus(status);

                    byte[] appIcon = cursor.getBlob(cursor.getColumnIndex(AppInfoDB.APP_ICON));
                    appItems.setmAppIcon(CommonUtils.getDrwableFromByte(appIcon));
                    lockedAppList.add(appItems);

                } while (cursor.moveToNext());
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                database.close();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return lockedAppList;
    }

    public ArrayList<AppItems> getUnlockedAppList() {
        Cursor cursor = null;
        SQLiteDatabase database = getDBInstance().getWritableDatabase();
        ArrayList<AppItems> unlockedAppListAppItems = new ArrayList<AppItems>();
        try {
            cursor = database.query(AppInfoDB.DB_APP_DETAILS_TABLE, null, AppInfoDB.APP_STATUS + " = '" + CommonUtils.AppStatus.UnLocked.ordinal() + "'", null, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    AppItems appItems = new AppItems();
                    Boolean status = (cursor.getInt(cursor.getColumnIndex(AppInfoDB.APP_STATUS)) == 1) ? true : false;
                    appItems.setAppName(cursor.getString(cursor.getColumnIndex(AppInfoDB.APP_NAME)));
                    appItems.setAppPackageName(cursor.getString(cursor.getColumnIndex(AppInfoDB.APP_PACKAGE_NAME)));
                    appItems.setStatus(status);

                    byte[] appIcon = cursor.getBlob(cursor.getColumnIndex(AppInfoDB.APP_ICON));

                    appItems.setmAppIcon(CommonUtils.getDrwableFromByte(appIcon));
                    unlockedAppListAppItems.add(appItems);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                database.close();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return unlockedAppListAppItems;
    }

    public HashMap<String, LockedAppDetails> getLockedAppKeyList() {
        Cursor cursor = null;
        SQLiteDatabase database = getDBInstance().getWritableDatabase();
        HashMap<String, LockedAppDetails> lockedAppList = new HashMap<String, LockedAppDetails>();
        try {
            cursor = database.query(AppInfoDB.DB_APP_LOCK_KEY_DETAILS_TABLE, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    LockedAppDetails appItems = new LockedAppDetails();
                    String appPackageName = cursor.getString(cursor.getColumnIndex(AppInfoDB.APP_PACKAGE_NAME));
                    appItems.setAppName(appPackageName);
                    appItems.setAppLockKey(cursor.getString(cursor.getColumnIndex(AppInfoDB.APP_PASSWORD)));
                    appItems.setAppLockType(cursor.getInt(cursor.getColumnIndex(AppInfoDB.APP_PASSWORD_TYPE)));
                    appItems.setAppLockKeyHint(cursor.getString(cursor.getColumnIndex(AppInfoDB.APP_PASSWORD_HINT)));
                    lockedAppList.put(appPackageName, appItems);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                database.close();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return lockedAppList;
    }


    private AppInfoDB getDBInstance() {

        if (appInfoDB == null) {
            appInfoDB = new AppInfoDB(mContext);
        }
        return appInfoDB;
    }
}
