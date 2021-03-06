package com.zkcd.coolweather.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.zkcd.coolweather.db.CoolWeatherDB;
import com.zkcd.coolweather.model.Const;
import com.zkcd.coolweather.model.City;
import com.zkcd.coolweather.model.County;
import com.zkcd.coolweather.model.Province;

public class Utility {

    private static final String TAG = "Utility";
    /**
     * 解析处理服务器返回的省级数据
     * 
     * @param responce
     *            服务器返回的数据
     * @param coolWeatherDB
     *            数据库对象
     * @return
     */
    public synchronized static boolean handlerProvinceResponce(CoolWeatherDB coolWeatherDB, String response) {
        if (!TextUtils.isEmpty(response)) {
            String[] allProvince = response.split(",");
            if (allProvince != null && allProvince.length > 0) {
                for (String prov : allProvince) {
                    String[] signalProv = prov.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(signalProv[0]);
                    province.setProvinceName(signalProv[1]);
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    public synchronized static boolean handlerCitiesResponce(CoolWeatherDB coolWeatherDB, String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length > 0) {
                for (String cit : allCities) {
                    String[] signalCity = cit.split("\\|");
                    City city = new City();
                    city.setCityCode(signalCity[0]);
                    city.setCityName(signalCity[1]);
                    city.setProvinceId(provinceId);
                    coolWeatherDB.saveCities(city);
                }
                return true;
            }
        }
        return false;
    }

    public synchronized static boolean handlerCountiesResponce(CoolWeatherDB coolWeatherDB, String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCounties = response.split(",");
            if (allCounties != null && allCounties.length > 0) {
                for (String coun : allCounties) {
                    String[] signalCounty = coun.split("\\|");
                    County county = new County();
                    county.setCountyCode(signalCounty[0]);
                    county.setCountyName(signalCounty[1]);
                    county.setCityId(cityId);
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 将服务器返回的JSON数据解析，并存储到本地
     * @param context
     * @param response
     */
    public static void handleWeatherResponse(Context context, String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
            String cityName = weatherInfo.getString("city");
            String weatherCode = weatherInfo.getString("cityid");
            String temp1 = weatherInfo.getString("temp1");
            String temp2 = weatherInfo.getString("temp2");
            String weatherDesp = weatherInfo.getString("weather");
            String publishTime = weatherInfo.getString("ptime");
            saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 将服务器返回的所有天气信息存储到Share的Preferences文件中
     * @param context
     * @param cityName
     * @param weatherCode
     * @param temp1
     * @param temp2
     * @param weatherDesp
     * @param publishTime
     */
    private static void saveWeatherInfo(Context context, String cityName,
            String weatherCode, String temp1, String temp2, String weatherDesp,
            String publishTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putBoolean(Const.PREF_CITY_SELECTED, true);
        edit.putString(Const.PREF_CITY_NAME, cityName);
        edit.putString(Const.PREF_WEATHER_CODE, weatherCode);
        edit.putString(Const.PREF_TEMP_1, temp1);
        edit.putString(Const.PREF_TEMP_2, temp2);
        edit.putString(Const.PREF_WEATHER_DESP, weatherDesp);
        edit.putString(Const.PREF_PUBLISH_TIME, publishTime);
        edit.putString(Const.PREF_CURRENT_DATE, dateFormat.format(new Date()));
        edit.commit();
    }
}
