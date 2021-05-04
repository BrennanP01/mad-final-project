package edu.lewisu.cs.bprice.watertracker;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherFetcher {
    public interface OnWeatherReceivedListener{
        void onWeatherReceived(WeatherDay weatherDay);
        void onErrorResponse(VolleyError error);
    }

    private String weatherUrl = "http://dataservice.accuweather.com/forecasts/v1/daily/1day";
    private String locationKeyUrl = "http://dataservice.accuweather.com/locations/v1/cities/geoposition/search";
    private final String API_KEY = "yzHAQIPl5qXg9hsWhCpdhnqXU43W2WUf";
    private final RequestQueue mRequestQueue;
    private String latLong;

    private Location mLocation;


    public WeatherFetcher(Context context, Location location){
        mRequestQueue = Volley.newRequestQueue(context);
        this.mLocation = location;
    }

    public void fetchWeather(final OnWeatherReceivedListener listener){
        latLong = String.valueOf(mLocation.getLatitude()) + "," + String.valueOf(mLocation.getLongitude());
        String lUrl = Uri.parse(locationKeyUrl).buildUpon()
                .appendQueryParameter("apikey", API_KEY)
                .appendQueryParameter("q", latLong)
                .build().toString();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, lUrl, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                String locationCode = parseJsonLocation(response);
                weatherUrl = weatherUrl + "/" + locationCode;
                String wUrl = Uri.parse(weatherUrl).buildUpon()
                        .appendQueryParameter("apikey", API_KEY)
                        .build().toString();

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, wUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        WeatherDay weatherDay = parseJsonWeather(response);
                        listener.onWeatherReceived(weatherDay);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onErrorResponse(error);
                    }
                });
                mRequestQueue.add(request);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onErrorResponse(error);
            }
        });
        mRequestQueue.add(request);
    }

    String parseJsonLocation(JSONObject jsonObject){
        String locKey = null;
        try{
            locKey = jsonObject.getString("Key");
        }catch (Exception ex){
            Log.d("WeatherFetcher:", ex.toString());
        }
        return locKey;
    }

    WeatherDay parseJsonWeather(JSONObject jsonObject){
        boolean rain;
        String location;
        String date;
        int minTemp;
        int maxTemp;
        WeatherDay weatherDay = new WeatherDay("","",0,0,true);
        try{
            JSONArray dailyForecasts = jsonObject.getJSONArray("DailyForecasts");
            JSONObject forecast = dailyForecasts.getJSONObject(0);
            date = forecast.getString("Date");
            JSONObject temperature = forecast.getJSONObject("Temperature");
            JSONObject minTempObj = temperature.getJSONObject("Minimum");
            minTemp = minTempObj.getInt("Value");
            JSONObject maxTempObj = temperature.getJSONObject("Maximum");
            maxTemp = maxTempObj.getInt("Value");
            location = "Peotone, IL";
            JSONObject day = forecast.getJSONObject("Day");
            rain = day.getBoolean("HasPrecipitation");

            weatherDay = new WeatherDay(location, date, minTemp, maxTemp, rain);



        }catch (Exception ex){
            Log.d("WeatherFetcher:", ex.toString());
        }
        return weatherDay;
    }
}
