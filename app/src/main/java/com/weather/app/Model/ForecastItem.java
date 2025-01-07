package com.weather.app.Model;

public class ForecastItem {

    String dayName;
    String tempMax;
    String tempMin;
    String icon;

    public ForecastItem(String dayName, String tempMax, String tempMin, String icon) {
        this.dayName = dayName;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.icon = icon;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
