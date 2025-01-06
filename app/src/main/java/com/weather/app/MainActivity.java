package com.weather.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    TextView tv_sky, tv_temp, tv_feels_like, tv_temp_max_min, tv_wind_speed, tv_wind_direction, tv_humidity, tv_visibility, tv_sunrise, tv_sunset;

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

        requestWeather(22.936901, 91.307137);
    }

    private void requestWeather(double latitude, double longitude) {
        String url = Config.API_URL+"lat="+latitude+"&lon="+longitude+"&appid="+Config.API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                JSONObject weather = obj.getJSONArray("weather").getJSONObject(0);
                JSONObject main = obj.getJSONObject("main");
                JSONObject wind = obj.getJSONObject("wind");
                JSONObject sys = obj.getJSONObject("sys");
                JSONObject clouds = obj.getJSONObject("clouds");

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
        }, error -> {
            findViewById(R.id.progress_circular).setVisibility(View.GONE);
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        });
        queue.add(stringRequest);
    }

    private String getTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        return sdf.format(time);
    }
}