package com.zkcd.coolweather.util;

import android.text.TextUtils;

import com.zkcd.coolweather.db.CoolWeatherDB;
import com.zkcd.coolweather.model.City;
import com.zkcd.coolweather.model.County;
import com.zkcd.coolweather.model.Province;

public class Utility {
    /**
     * 解析处理服务器返回的省级数据
     * 
     * @param responce
     *            服务器返回的数据
     * @param coolWeatherDB
     *            数据库对象
     * @return
     */
    public synchronized static boolean handlerProvinceResponce(CoolWeatherDB coolWeatherDB, String responce) {
        if (!TextUtils.isEmpty(responce)) {
            String[] allProvince = responce.split(",");
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

    public synchronized static boolean handlerCitiesResponce(CoolWeatherDB coolWeatherDB, String responce, int provinceId) {
        if (!TextUtils.isEmpty(responce)) {
            String[] allCities = responce.split(",");
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
    
    public synchronized static boolean handlerCountiesResponce(CoolWeatherDB coolWeatherDB, String responce, int cityId) {
        if (!TextUtils.isEmpty(responce)) {
            String[] allCounties = responce.split(",");
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
}
