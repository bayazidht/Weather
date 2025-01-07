package com.weather.app.Tools;

import android.content.SharedPreferences;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Helper {

    public String getTemp(double temp, String unit) {
        if (unit.equals("C")) {
            return new DecimalFormat("#").format(temp-273.15);
        } else if (unit.equals("F")) {
            return new DecimalFormat("#").format((temp-273.15)*9/5+32);
        } else {
            return new DecimalFormat("#").format(temp);
        }
    }

    public String getDayName(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE\nh a", Locale.getDefault());
        return sdf.format(time*1000);
    }

    public String getTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(time*1000);
    }

    public String getWindDirection(double deg) {
        if (deg >= 270 && deg <= 315 || deg>= 225 && deg <= 270) {
            return "West";
        } else if (deg >= 180 && deg <= 225 || deg>= 135 && deg <= 180) {
            return "South";
        } else if (deg >= 90 && deg <= 135 || deg>= 45 && deg <= 90) {
            return "East";
        } else {
            return "North";
        }
    }

    public String getVisibility(double visibility) {
        return new DecimalFormat("#.#").format((visibility/1000)/1.609344);
    }

    public String getWindSpeed(double speed) {
        return new DecimalFormat("#.#").format(speed);
    }

    public String getWeatherIcon(String icon) {
        return "https://openweathermap.org/img/wn/"+icon+"@4x.png";
    }

    public void saveData(SharedPreferences sharedPref, String key, String data) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, data);
        editor.apply();
    }

}
