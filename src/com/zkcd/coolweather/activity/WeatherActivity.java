package com.zkcd.coolweather.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zkcd.coolweather.R;
import com.zkcd.coolweather.model.Const;
import com.zkcd.coolweather.service.AutoUpdateService;
import com.zkcd.coolweather.util.HttpCallBackListener;
import com.zkcd.coolweather.util.HttpUtils;
import com.zkcd.coolweather.util.Utility;
import com.zkcd.coolweather.util.ViewUtils;

public class WeatherActivity extends Activity implements OnClickListener {

    private static final String COUNTY_CODE = "countyCode";
    private static final String WEATHER_CODE = "weatherCode";
    private WeatherActivity activity;
    private LinearLayout weatherInfoLayout;
    private TextView cityNameText;
    private TextView publishText;
    private TextView currentDataText;
    private TextView tempRangeText;
    private TextView weatherDespText;
    private Button switchCityBtn;
    private Button refreshWeatherBtn;
    private SharedPreferences prefs;
    
    public static void startAction(Context context, String countyCode){
        Intent intent = new Intent(context, WeatherActivity.class);
        intent.putExtra(COUNTY_CODE, countyCode);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        activity = this;
        initView();

        String countyCode = getIntent().getStringExtra(COUNTY_CODE);
        Log.d("huxiyang22222", "onCreate  countyCode "+countyCode);
        if (!TextUtils.isEmpty(countyCode)) {
            publishText.setText("同步中。。。");
            cityNameText.setVisibility(View.INVISIBLE);
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        }
    }

    private void initView() {
        weatherInfoLayout = ViewUtils.findViewById(activity, R.id.weather_info_layout);
        cityNameText = ViewUtils.findViewById(activity, R.id.city_name);
        publishText = ViewUtils.findViewById(activity, R.id.publish_text);
        tempRangeText = ViewUtils.findViewById(activity, R.id.temp_range);
        currentDataText = ViewUtils.findViewById(activity, R.id.current_data);
        weatherDespText = ViewUtils.findViewById(activity, R.id.weather_desp);
        switchCityBtn = ViewUtils.findViewById(activity, R.id.switch_city);
        refreshWeatherBtn = ViewUtils.findViewById(activity, R.id.refresh_weather);
        switchCityBtn.setOnClickListener(this);
        refreshWeatherBtn.setOnClickListener(this);
    }
//接口有问题    只有辽宁--锦州（里面的个别可以请求导数据）
    private void queryWeatherCode(String countyCode) {
        String address = Const.LOCAL_ADDRESS+countyCode+Const.LOCAL_ADDRESS_SUFFIX;
        queryFormServer(address, COUNTY_CODE);
        
    }

    private void queryFormServer(String address, final String type) {
        HttpUtils.sendHttprequest(address, new HttpCallBackListener() {
            
            @Override
            public void onFinish(String response) {
                Log.d("huxiyang22222", " queryFormServer  response "+response);
                if (COUNTY_CODE.equals(type)) {
                    String[] split = response.split("\\|");
                    if (split!=null&&split.length==2) {
                        Log.d("huxiyang22222", " queryFormServer  split[1] "+split[1]);
                        queryWeatherInfo(split[1]);
                    }
                }else if (WEATHER_CODE.equals(type)) {
                    Utility.handleWeatherResponse(activity, response);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            showWeatherInfo();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                
                runOnUiThread(new Runnable() {
                    public void run() {
                        publishText.setText("同步数据失败。。");
                    }
                });
            }
        });
    }

    private void queryWeatherInfo(String weatherInfoCode) {
        String weatherAddress = Const.WEATHER_ADDRESS+weatherInfoCode+Const.WEATHER_ADDRESS_SUFFIX;
        Log.d("huxiyang22222", "weatherAddress "+weatherAddress);
        queryFormServer(weatherAddress, WEATHER_CODE);
//        HttpUtils.sendOkHttpUtils(weatherAddress, new HttpCallBackListener() {
//            
//            @Override
//            public void onFinish(String response) {
//                Log.d("huxiyang22222", " sendOkHttpUtils  onFinish response "+response);
//                Utility.handleWeatherResponse(activity, response);
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        showWeatherInfo();
//                    }
//                });
//            }
//            
//            @Override
//            public void onError(Exception e) {
//                // TODO Auto-generated method stub
//                
//            }
//        });
    }

    private void showWeatherInfo() {
        Log.d("huxiyang22222", "showWeatherInfo ");
        
        cityNameText.setText(prefs.getString(Const.PREF_CITY_NAME, ""));
        publishText.setText("今天 "+prefs.getString(Const.PREF_PUBLISH_TIME, "")+" 发布");
        currentDataText.setText(prefs.getString(Const.PREF_CURRENT_DATE, ""));
        weatherDespText.setText(prefs.getString(Const.PREF_WEATHER_DESP, ""));
        tempRangeText.setText(prefs.getString(Const.PREF_TEMP_1, "")+" ~ "+prefs.getString(Const.PREF_TEMP_2, ""));

        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        Intent intent = new Intent(activity, AutoUpdateService.class);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.switch_city:
            Intent intent = new Intent(activity,ChooseAreaActivity.class);
            intent.putExtra(Const.FROM_WEATHER_ACTIVITY, true);
            startActivity(intent);
            finish();
            break;
        case R.id.refresh_weather:
            String weatherCode = prefs.getString(Const.PREF_WEATHER_CODE, "");
            if (!TextUtils.isEmpty(weatherCode)) {
                publishText.setText("同步中。。。");
                queryWeatherInfo(weatherCode); 
            }
            break;
        default:
            break;
        }
    }
}
