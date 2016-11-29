package com.zkcd.coolweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zkcd.coolweather.service.AutoUpdateService;

public class AutoUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //去启动更新天气信息的service
        context.startService(new Intent(context, AutoUpdateService.class));
    }
}
