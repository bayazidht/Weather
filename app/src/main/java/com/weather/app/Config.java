package com.weather.app;

public class Config {
    public final static String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?";
    public final static String FORECAST_API_URL = "https://api.openweathermap.org/data/2.5/forecast?";

    public final static String API_KEY = BuildConfig.API_KEY;

    public final static String SHAREDPREF_KEY_WEATHER = "weather";
    public final static String SHAREDPREF_KEY_FORECAST = "forecast";
}
