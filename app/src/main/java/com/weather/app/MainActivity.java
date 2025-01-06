package com.weather.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String unit = "F";

    TextView tv_sky, tv_temp, tv_feels_like, tv_temp_max_min, tv_wind_speed,
            tv_wind_direction, tv_humidity, tv_visibility, tv_sunrise, tv_sunset;

    private ForecastRecyclerAdapter recyclerAdapter;
    private ArrayList<ForecastItem> forecastItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_sky = findViewById(R.id.tv_sky);
        tv_temp = findViewById(R.id.tv_temp);
        tv_feels_like = findViewById(R.id.tv_feels_like);
        tv_temp_max_min = findViewById(R.id.tv_temp_max_min);

        tv_wind_speed = findViewById(R.id.tv_wind_speed);
        tv_wind_direction = findViewById(R.id.tv_wind_direction);

        tv_humidity = findViewById(R.id.tv_humidity);

        tv_visibility = findViewById(R.id.tv_visibility);

        tv_sunrise = findViewById(R.id.tv_sunrise);
        tv_sunset = findViewById(R.id.tv_sunset);

        RecyclerView mRecyclerView = findViewById(R.id.forecast_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        forecastItems = new ArrayList<>();
        recyclerAdapter = new ForecastRecyclerAdapter(this, forecastItems);
        mRecyclerView.setAdapter(recyclerAdapter);

    }

    private void requestWeather(double latitude, double longitude) {
        String url = Config.WEATHER_API_URL+"lat="+latitude+"&lon="+longitude+"&appid="+Config.API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                JSONObject weather = obj.getJSONArray("weather").getJSONObject(0);
                JSONObject main = obj.getJSONObject("main");
                JSONObject wind = obj.getJSONObject("wind");
                JSONObject sys = obj.getJSONObject("sys");

                double temp = main.getDouble("temp");
                double feels_like = main.getDouble("feels_like");
                double temp_max = main.getDouble("temp_max");
                double temp_min = main.getDouble("temp_min");

                tv_sky.setText(String.format("%s", weather.getString("main")));
                tv_temp.setText(String.format("%s°", new Helper().getTemp(temp, unit)));
                tv_feels_like.setText(String.format("Feels like %s°", new Helper().getTemp(feels_like, unit)));
                tv_temp_max_min.setText(String.format("High %s° · Low %s°", new Helper().getTemp(temp_max, unit), new Helper().getTemp(temp_min, unit)));

                tv_wind_speed.setText(String.format("%s mph", wind.getDouble("speed")));
                tv_wind_direction.setText(String.format("From %s", new Helper().getWindDirection(wind.getDouble("deg"))));

                tv_humidity.setText(String.format("%s%%", main.getString("humidity")));

                tv_visibility.setText(String.format("%s km", new Helper().getVisibility(obj.getDouble("visibility"))));

                tv_sunrise.setText(String.format("%s", new Helper().getTime(sys.getLong("sunrise"))));
                tv_sunset.setText(String.format("%s", new Helper().getTime(sys.getLong("sunset"))));

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            findViewById(R.id.progress_circular).setVisibility(View.GONE);
            findViewById(R.id.weather_details).setVisibility(View.VISIBLE);
        }, error -> {
            findViewById(R.id.progress_circular).setVisibility(View.GONE);
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        });
        queue.add(stringRequest);
    }

    private void requestForecast(double latitude, double longitude) {
        String url = Config.FORECAST_API_URL+"lat="+latitude+"&lon="+longitude+"&appid="+Config.API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                JSONArray list = obj.getJSONArray("list");

                for(int i=0; i<list.length(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    JSONObject main = item.getJSONObject("main");
                    JSONObject weatherObj = item.getJSONArray("weather").getJSONObject(0);

                    long time = item.getLong("dt");

                    forecastItems.add(new ForecastItem(
                            new Helper().getDayName(time),
                            main.getString("temp_max"),
                            main.getString("temp_min"),
                            weatherObj.getString("icon")
                    ));
                }
                recyclerAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            findViewById(R.id.progress_circular).setVisibility(View.GONE);
            findViewById(R.id.weather_details).setVisibility(View.VISIBLE);
        }, error -> {
            findViewById(R.id.progress_circular).setVisibility(View.GONE);
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        });
        queue.add(stringRequest);
    }




    private void requestLocationPermissions() {
        locationPermissionRequest.launch(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
    }
    ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        Boolean fineLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false);
        Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION,false);
        if (Boolean.TRUE.equals(fineLocationGranted) || Boolean.TRUE.equals(coarseLocationGranted)) {
            if (new GpsHelper(this).isGpsEnabled()) getLastLocation();
        } else {
           new GpsHelper(this).showLocationPermissionDialog();
        }
    });

    private FusedLocationProviderClient fusedLocationProviderClient;
    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location!=null) getWeather(location);
                    else getCurrentLocation();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest
                .Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(LocationRequest.Builder.IMPLICIT_MIN_UPDATE_INTERVAL)
                .setMaxUpdateDelayMillis(10000)
                .build();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                getWeather(location);
            }
        }, Looper.myLooper());
    }

    private void getWeather(Location location) {
        if (location != null) {
            requestWeather(location.getLatitude(), location.getLongitude());
            requestForecast(location.getLatitude(), location.getLongitude());
        } else {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        requestLocationPermissions();
    }
}