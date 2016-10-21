package com.zkcd.coolweather.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.R.string;
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
                try {
                    URL url = new URL(address);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(8000);
                    if (conn.getResponseCode() == 200) {
                        InputStream inputStream = conn.getInputStream();
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
                    Log.e(TAG, "URL or address Exception ");
                    e.printStackTrace();
                } catch (IOException e) {
                    if (listener!=null) {
                        listener.onError(e);
                    }
                    Log.e(TAG, "HttpURLConnection  Exception ");
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
                    if (conn!=null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }
}
