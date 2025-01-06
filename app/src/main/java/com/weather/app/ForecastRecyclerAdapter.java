package com.weather.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ForecastRecyclerAdapter extends RecyclerView.Adapter<ForecastRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private final ArrayList<ForecastItem> forecastItems;

    public ForecastRecyclerAdapter(Context context, ArrayList<ForecastItem> forecastItems) {
        this.mContext = context;
        this.forecastItems = forecastItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_forecast_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ForecastItem item = forecastItems.get(position);
        holder.tvDayName.setText(item.dayName);
        holder.tvTempMax.setText(item.tempMax);
        holder.tvTempMin.setText(item.tempMin);

    }

    @Override
    public int getItemCount() {
        return forecastItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView tvDayName, tvTempMax, tvTempMin;
        private final ImageView ivWeather;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tv_day_name);
            tvTempMax = itemView.findViewById(R.id.tv_temp_max);
            tvTempMin = itemView.findViewById(R.id.tv_temp_min);
            ivWeather = itemView.findViewById(R.id.iv_weather);
        }
    }

}
