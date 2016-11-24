package com.zkcd.coolweather.activity;

import java.util.ArrayList;
import java.util.List;

import com.zkcd.coolweather.R;
import com.zkcd.coolweather.db.CoolWeatherDB;
import com.zkcd.coolweather.model.City;
import com.zkcd.coolweather.model.County;
import com.zkcd.coolweather.model.Province;
import com.zkcd.coolweather.util.HttpCallBackListener;
import com.zkcd.coolweather.util.HttpUtils;
import com.zkcd.coolweather.util.Utility;
import com.zkcd.coolweather.util.ViewUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {

    private static final String ADDRESS_CITY = "http://www.weather.com.cn/data/list3/city";
    private static final String ADDRESS_SUFFIX = ".xml";
    private static final String TYPE_PROVINCE = "province";
    private static final String TYPE_CITY = "city";
    private static final String TYPE_COUNTY = "county";
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;
    private int currentLevel;
    
    private ListView listView;
    private TextView titleText;
    private ChooseAreaActivity mActivity;
    private List<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<Province> provinceList;
    private ProgressDialog progressDialog;
    protected Province selectedProvince;
    private List<City> cityList;
    protected City selectedCity;
    private List<County> countyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        mActivity =this;
        listView = ViewUtils.findViewById(mActivity, R.id.list_view);
        titleText = ViewUtils.findViewById(mActivity, R.id.title_text);
        adapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        coolWeatherDB = CoolWeatherDB.getInstance(mActivity);
        //初始化province数据
        queryProvinces();
        listView.setOnItemClickListener(new OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounty();
                }
            }
        });
    }

    /**
     * 查询全国所有省，有先从数据库中查询，如果没有查询到再去服务器上下载
     */
    private void queryProvinces() {
        provinceList = coolWeatherDB.loadProvince();
        if (provinceList.size()>0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);//表示将listview列表移动到指定的Position处
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }else{
            queryFromServer(null, TYPE_PROVINCE);
        }
    }

    private void queryCities() {
        cityList = coolWeatherDB.loadCity(selectedProvince.getId());
        if (cityList.size()>0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else {
            queryFromServer(selectedProvince.getProvinceCode(), TYPE_CITY);
        }
    }

    private void queryCounty() {
        countyList = coolWeatherDB.loadCounty(selectedCity.getId());
        if (countyList.size()>0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            queryFromServer(selectedCity.getCityCode(), TYPE_COUNTY);
        }
    }
    
    private void queryFromServer(String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)) {
            address = ADDRESS_CITY+code+ADDRESS_SUFFIX;
        }else{
            address = ADDRESS_CITY+ADDRESS_SUFFIX;
        }
        showProgressDialog();
        HttpUtils.sendHttprequest(address, new HttpCallBackListener() {
            
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if (TYPE_PROVINCE.equals(type)) {
                    result = Utility.handlerProvinceResponce(coolWeatherDB, response);
                }else if (TYPE_CITY.equals(type)) {
                    result = Utility.handlerCitiesResponce(coolWeatherDB, response, selectedProvince.getId());
                }else if (TYPE_COUNTY.equals(type)) {
                    result = Utility.handlerCountiesResponce(coolWeatherDB, response,selectedCity.getId());
                }
                
                if (result) {
                    //通过runOnUiThread方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        public void run() {
                            closeProgressDialog();
                            if (TYPE_PROVINCE.equals(type)) {
                                queryProvinces();
                            }else if (TYPE_CITY.equals(type)) {
                                queryCities();
                            }else if (TYPE_COUNTY.equals(type)) {
                                queryCounty();
                            }
                        }

                    });
                }
            }
            
            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                      closeProgressDialog();
                      Toast.makeText(mActivity, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(mActivity);
            progressDialog.setMessage("正在加载。。。");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
       if (progressDialog==null) {
        return;
       }
       progressDialog.dismiss();
    }
        
    /**
     * 捕获Back键，根据当前级别确定返回市列表、省列表，还是直接退出
     */
    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        }else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        }else{
            finish();
        }
    }
}
