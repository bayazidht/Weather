package com.weather.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

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

                tv_sky.setText(weather.getString("main"));
                tv_temp.setText(String.format("%s°", main.getString("temp")));
                tv_feels_like.setText(String.format("Feels like %s°", main.getString("feels_like")));
                tv_temp_max_min.setText(String.format("High %s° · Low %s°", main.getString("temp_max"), main.getString("temp_min")));

                tv_wind_speed.setText(String.format("%s mph", wind.getString("speed")));
                tv_wind_direction.setText(String.format("From %s°", wind.getString("deg")));

                tv_humidity.setText(String.format("%s%%", main.getString("humidity")));

                tv_visibility.setText(String.format("%s miles", obj.getDouble("visibility")/1000));

                tv_sunrise.setText(String.format("%s", getTime(sys.getLong("sunrise"))));
                tv_sunset.setText(String.format("%s", getTime(sys.getLong("sunset"))));

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

                    forecastItems.add(new ForecastItem(getDayName(time), main.getString("temp_max"), main.getString("temp_min"), weatherObj.getString("icon")));
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
    private String getDayName(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
        return sdf.format(time);
    }


    private String getTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(time);
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
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Allow Location")
                    .setMessage("To get weather update in your address, you need to allow location.")
                    .setCancelable(false)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Allow", (paramDialogInterface, paramInt) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .show();
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