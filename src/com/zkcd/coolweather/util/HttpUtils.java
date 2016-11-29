package com.zkcd.coolweather.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import android.util.Log;

public class HttpUtils {

    private static final String TAG = "HttpUtils";

    public static void sendHttprequest(final String address,
            final HttpCallBackListener listener) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                HttpURLConnection conn = null;
                BufferedReader br = null;
                InputStream inputStream = null;
                try {
                    URL url = new URL(address);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(8000);
//                    conn.setRequestProperty("Content-type", "application/x-java-serialized-object");
                    conn.connect();
                    if (conn.getResponseCode() == 200) {
                        inputStream = conn.getInputStream();
                        br = new BufferedReader(new InputStreamReader(
                                inputStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }
                        if (listener!=null) {
                            listener.onFinish(response.toString());
                        }
                    }
                } catch (MalformedURLException e) {
                    if (listener!=null) {
                        listener.onError(e);
                    }
                    Log.e(TAG, "URL or address Exception "+e);
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, "HttpURLConnection  Exception "+e);
                    if (listener!=null) {
                        listener.onError(e);
                    }
                    e.printStackTrace();
                }finally{
                    if (br!=null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if (inputStream!=null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if (conn!=null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }

    public static void sendOkHttpUtils(final String address,
            final HttpCallBackListener listener) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Call call = okHttpClient.newCall(new Request.Builder().url(address).build());
        call.enqueue(new Callback() {

            @Override
            public void onResponse(Response response) throws IOException {
                String string = response.body().string();// response.body().string() 这里只能回调一次，所以想多次使用它，将其复制给一个变量，之后就可以重复使用了
                if (listener!=null) {
                    listener.onFinish(string);
                }
            }

            @Override
            public void onFailure(Request arg0, IOException arg1) {
                if (listener!=null) {
                    listener.onError(arg1);
                }
            }
        });
    }
}
