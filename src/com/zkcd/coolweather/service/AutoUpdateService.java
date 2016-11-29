package com.zkcd.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.zkcd.coolweather.model.Const;
import com.zkcd.coolweather.receiver.AutoUpdateReceiver;
import com.zkcd.coolweather.util.HttpCallBackListener;
import com.zkcd.coolweather.util.HttpUtils;
import com.zkcd.coolweather.util.Utility;

public class AutoUpdateService extends Service {
    private static final String TAG = "AutoUpdateService";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.d(TAG, "onCreate start");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                updateWeather();
            }
        }).start();

        //定时任务
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8*60*60*1000; //这是8小时的毫秒数,即8小时更新一次
        long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i, 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }
    

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode = prefs.getString(Const.PREF_WEATHER_CODE, "");
        String address = Const.WEATHER_ADDRESS+weatherCode+Const.WEATHER_ADDRESS_SUFFIX;
        HttpUtils.sendHttprequest(address, new HttpCallBackListener() {

            @Override
            public void onFinish(String response) {
                Utility.handleWeatherResponse(AutoUpdateService.this, response);
            }
            
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
}
