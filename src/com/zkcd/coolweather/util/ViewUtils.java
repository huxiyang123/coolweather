package com.zkcd.coolweather.util;


import android.app.Activity;
import android.view.View;

public class ViewUtils {

    @SuppressWarnings("unchecked")
    public static <T extends View> T findViewById(View rootView,int resId)
    {
        return (T)rootView.findViewById(resId);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T findViewById(Activity activity,int resId)
    {
        return (T)activity.findViewById(resId);
    }
}
