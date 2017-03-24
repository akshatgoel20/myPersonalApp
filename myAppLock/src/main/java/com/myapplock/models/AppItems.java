package com.myapplock.models;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.Comparator;

public class AppItems implements Serializable,Cloneable
{

    private int mId;

    private String mAppName;

    private Drawable mAppIcon;

    private String mAppPackageName;

    private boolean mAppLocked;

    private boolean mLayoutOpen;

    public String getAppName()
    {
        return mAppName;
    }

    public void setAppName(String mAppName)
    {
        this.mAppName = mAppName;
    }

    public String getAppPackageName()
    {
        return mAppPackageName;
    }

    public void setAppPackageName(String mAppPackageName)
    {
        this.mAppPackageName = mAppPackageName;
    }

    public boolean isAppLocked()
    {
        return mAppLocked;
    }

    public void setAppLocked(boolean mSelected)
    {
        this.mAppLocked = mSelected;
    }

    public Drawable getAppIcon()
    {
        return mAppIcon;
    }

    public void setAppIcon(Drawable mAppIcon)
    {
        this.mAppIcon = mAppIcon;
    }

    public int getId()
    {
        return mId;
    }

    public void setId(int id)
    {
        this.mId = id;
    }

    public static Comparator<AppItems> getComparator(SortParameter... sortParameters)
    {
        return new PersonComparator(sortParameters);
    }

    public enum SortParameter
    {
        ID_ASCENDING,
        ID_DESCENDING,
        NAME_ASCENDING,
        NAME_DESCENDING,
        SELECTED_ASCENDING,
        UNSELECTED_DESCENDING
    }

    public static Comparator<AppItems> getSimpleComparator()
    {
        return new SortingByName();
    }
    private static class SortingByName implements  Comparator<AppItems>
    {
        public SortingByName(){

        }
        @Override
        public int compare(AppItems o1, AppItems o2) {
            return o1.getAppName().compareTo(o2.getAppName());
        }
    }

    private static class PersonComparator implements Comparator<AppItems>
    {
        private SortParameter[] parameters;

        private PersonComparator(SortParameter[] parameters)
        {
            this.parameters = parameters;
        }

        public int compare(AppItems o1, AppItems o2)
        {
            int finalanser = 0;
            int comparison;
            for (SortParameter parameter : parameters) {
                switch (parameter) {
                    case ID_ASCENDING:
                        comparison = o1.getId() - o2.getId();
                        if (comparison != 0)
                            return comparison;
                        break;
                    case NAME_ASCENDING:
                        comparison = o1.getAppName().compareTo(o2.getAppName());
                        if (comparison != 0)
                            return comparison;
                        break;
                    case SELECTED_ASCENDING:
//                        if ((o1.isImportant() && !o2.isImportant()))
//                            return -1;
//                        if (!o1.isImportant() && o2.isImportant())
//                            return 1;
                        finalanser = o1.getAppName().compareTo(o2.getAppName());
                        break;

                }
            }
            return finalanser;
        }
    }


    public boolean isLayoutOpen() {
        return mLayoutOpen;
    }

    public void setLayoutOpen(boolean mLayoutOpen) {
        this.mLayoutOpen = mLayoutOpen;
    }

    public AppItems clone() {
        try {
            return (AppItems) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

}
