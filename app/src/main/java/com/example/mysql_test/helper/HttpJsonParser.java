package com.example.mysql_test.helper;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

public class HttpJsonParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    HttpURLConnection urlConnection = null;

    // function get json from url
    // by making HTTP POST or GET method
    public JSONObject makeHttpRequest(String url, String method,
                                      Map<String, String> params) {
        Log.w("myApp", "inside makeHttpRequest eee");
        Log.w("myApp", "inside makeHttpRequest"+url+" eee");
        try {
            Uri.Builder builder = new Uri.Builder();
            URL urlObj;
            String encodedParams = "";
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    builder.appendQueryParameter(entry.getKey(), entry.getValue());
                }
            }
            if (builder.build().getEncodedQuery() != null) {
                encodedParams =  builder.build().getEncodedQuery();

            }
            if ("GET".equals(method)) {
                Log.w("myApp", url);
                url = url + "?" + encodedParams;
                Log.w("encodedParams", encodedParams);
                Log.w("url", url);
                urlObj = new URL(url);
                urlConnection = (HttpURLConnection) urlObj.openConnection();
                urlConnection.setRequestMethod(method);
                Log.w("myApp", "inside if GET");


            } else {
                urlObj = new URL(url);
                Log.w("urlObj", "inside else "+urlObj);
                urlConnection = (HttpURLConnection) urlObj.openConnection();
                urlConnection.setRequestMethod(method);
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("Content-Length", String.valueOf(encodedParams.getBytes().length));
                urlConnection.getOutputStream().write(encodedParams.getBytes());
            }

            Log.w("myApp", "OUT OF if GET");
            urlConnection.connect();
            Log.w("myApp12", ""+params);
            is = urlConnection.getInputStream();
            Log.w("myApp111212", ""+is);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            Log.w("myApp", "AFTER READER"+reader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
                Log.w("myApp", line);
            }
            is.close();
            json = sb.toString();
            jObj = new JSONObject(json);


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        } catch (Exception e) {
            Log.e("Exception", "Error parsing data " + e.toString());
        }

        // return JSON String
        Log.w("myApp", "finishing makeHttpRequest");
        Log.w("myApp", json);
        Log.w("myApp111", jObj.toString());
        return jObj;

    }
}