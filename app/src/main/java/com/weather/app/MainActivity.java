package com.weather.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.snackbar.Snackbar;
import com.weather.app.Adapter.ForecastRecyclerAdapter;
import com.weather.app.Model.ForecastItem;
import com.weather.app.Tools.GpsHelper;
import com.weather.app.Tools.Helper;
import com.weather.app.Tools.NetworkHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final String unit = "C";

    private double lat, lon;
    private String city;

    private SearchBar searchBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView tv_sky, tv_temp, tv_feels_like, tv_temp_max_min, tv_wind_speed,
            tv_wind_direction, tv_humidity, tv_visibility, tv_sunrise, tv_sunset;

    private ImageView iv_weather;

    private ForecastRecyclerAdapter recyclerAdapter;
    private ArrayList<ForecastItem> forecastItems;

    private SharedPreferences sharedPref;

    @Override
    public void onResume(){
        super.onResume();
        if (!new NetworkHelper(this).isNetworkAvailable()) {
            new NetworkHelper(this).showMessage();
            loadOffline();
        } else {
            requestLocationPermissions();
        }
    }

    private void loadOffline() {
        String weather_data = sharedPref.getString(Config.SHAREDPREF_KEY_WEATHER, null);
        String forecast_data = sharedPref.getString(Config.SHAREDPREF_KEY_FORECAST, null);

        if (weather_data != null) setWeather(weather_data);
        if (forecast_data != null) setForecast(forecast_data);
    }

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

        iv_weather = findViewById(R.id.iv_weather);

        RecyclerView mRecyclerView = findViewById(R.id.forecast_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        forecastItems = new ArrayList<>();
        recyclerAdapter = new ForecastRecyclerAdapter(this, forecastItems);
        mRecyclerView.setAdapter(recyclerAdapter);

        setSearch();
        setSwipeRefresh();

        sharedPref = getSharedPreferences("OFFLINE_DATA", MODE_PRIVATE);
    }

    private void setSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setEnabled(new NetworkHelper(this).isNetworkAvailable());
        swipeRefreshLayout.setOnRefreshListener(() -> requestWeather(lat, lon, city));
    }

    private void setSearch() {
        searchBar = findViewById(R.id.search_bar);
        SearchView searchView = findViewById(R.id.search_view);

        searchView.getEditText().setOnEditorActionListener((textView, i, keyEvent) -> {
            String text = textView.getText().toString();
            if (new NetworkHelper(this).isNetworkAvailable()) {
                searchView.hide();
                searchBar.setText(text);
                requestWeather(0, 0, text);
            } else {
                new NetworkHelper(this).showMessage();
            }
            return false;
        });
    }

    private void requestWeather(double latitude, double longitude, String city) {
        this.lat = latitude;
        this.lon = longitude;
        this.city = city;

        swipeRefreshLayout.setRefreshing(true);
        findViewById(R.id.weather_details).setVisibility(View.GONE);

        String url;
        if (city == null) url = Config.WEATHER_API_URL+"lat="+latitude+"&lon="+longitude+"&appid="+Config.API_KEY;
        else url = Config.WEATHER_API_URL+"q="+city+"&appid="+Config.API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {

            setWeather(response);
            requestForecast(latitude, longitude, city);
        }, error -> {
            swipeRefreshLayout.setRefreshing(false);

            Snackbar snackbar = Snackbar.make(findViewById(R.id.main), "Invalid address!", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Clear", view -> recreate());
            snackbar.show();
        });
        queue.add(stringRequest);
    }

    private void setWeather(String data) {
        try {
            JSONObject obj = new JSONObject(data);

            new Helper().saveData(sharedPref, Config.SHAREDPREF_KEY_WEATHER, data);

            JSONObject weather = obj.getJSONArray("weather").getJSONObject(0);
            JSONObject main = obj.getJSONObject("main");
            JSONObject wind = obj.getJSONObject("wind");
            JSONObject sys = obj.getJSONObject("sys");
            JSONObject coord = obj.getJSONObject("coord");

            double temp = main.getDouble("temp");
            double feels_like = main.getDouble("feels_like");
            double temp_max = main.getDouble("temp_max");
            double temp_min = main.getDouble("temp_min");

            setAddress(coord.getDouble("lat"), coord.getDouble("lon"));

            Glide.with(this).load(new Helper().getWeatherIcon(weather.getString("icon"))).into(iv_weather);

            tv_sky.setText(String.format("%s", weather.getString("main")));
            tv_temp.setText(String.format("%s°", new Helper().getTemp(temp, unit)));
            tv_feels_like.setText(String.format("Feels like %s°", new Helper().getTemp(feels_like, unit)));
            tv_temp_max_min.setText(String.format("High %s° · Low %s°", new Helper().getTemp(temp_max, unit), new Helper().getTemp(temp_min, unit)));

            tv_wind_speed.setText(String.format("%s km/h", new Helper().getWindSpeed(wind.getDouble("speed"))));
            tv_wind_direction.setText(String.format("From %s", new Helper().getWindDirection(wind.getDouble("deg"))));

            tv_humidity.setText(String.format("%s%%", main.getString("humidity")));

            tv_visibility.setText(String.format("%s km", new Helper().getVisibility(obj.getDouble("visibility"))));

            tv_sunrise.setText(String.format("%s", new Helper().getTime(sys.getLong("sunrise"))));
            tv_sunset.setText(String.format("%s", new Helper().getTime(sys.getLong("sunset"))));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void requestForecast(double latitude, double longitude, String city) {
        String url;
        if (city == null) url = Config.FORECAST_API_URL+"lat="+latitude+"&lon="+longitude+"&appid="+Config.API_KEY;
        else url = Config.FORECAST_API_URL+"q="+city+"&appid="+Config.API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, this::setForecast, error -> {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "Forecast request failed!", Toast.LENGTH_SHORT).show();
        });
        queue.add(stringRequest);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setForecast(String data) {
        forecastItems.clear();
        recyclerAdapter.notifyDataSetChanged();
        try {
            JSONObject obj = new JSONObject(data);

            new Helper().saveData(sharedPref, Config.SHAREDPREF_KEY_FORECAST, data);

            JSONArray list = obj.getJSONArray("list");

            for(int i=0; i<list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                JSONObject main = item.getJSONObject("main");
                JSONObject weatherObj = item.getJSONArray("weather").getJSONObject(0);

                long time = item.getLong("dt");

                forecastItems.add(new ForecastItem(
                        new Helper().getDayName(time),
                        new Helper().getTemp(main.getDouble("temp_max"), unit),
                        new Helper().getTemp(main.getDouble("temp_min"), unit),
                        weatherObj.getString("icon")
                ));
            }
            recyclerAdapter.notifyDataSetChanged();

            findViewById(R.id.weather_details).setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
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
                .addOnFailureListener(e ->  new GpsHelper(this).showLocationRequestFailedMessage());
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest
                .Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMaxUpdates(1)
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
            requestWeather(location.getLatitude(), location.getLongitude(), null);
        } else {
            new GpsHelper(this).showLocationRequestFailedMessage();
        }
    }

    private void setAddress(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                String country = addresses.get(0).getCountryName();
                String state = addresses.get(0).getAdminArea();
                String city = addresses.get(0).getLocality();
                searchBar.setText(String.format("%s, %s, %s", city, state, country));
            }
        } catch (IOException e) {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }
}