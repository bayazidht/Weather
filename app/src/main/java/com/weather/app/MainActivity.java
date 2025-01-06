package com.weather.app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    TextView tv_sky, tv_temp, tv_feels_like, tv_temp_max_min;

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


            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }, error -> {});
        queue.add(stringRequest);

    }
}