package com.weather.app;

public class ForecastItem {

    String dayName;
    String tempMax;
    String tempMin;
    String weather;

    public ForecastItem(String dayName, String tempMax, String tempMin, String weather) {
        this.dayName = dayName;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.weather = weather;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public String getTempMax() {
        return tempMax;
    }

    public void setTempMax(String tempMax) {
        this.tempMax = tempMax;
    }

    public String getTempMin() {
        return tempMin;
    }

    public void setTempMin(String tempMin) {
        this.tempMin = tempMin;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }
}
