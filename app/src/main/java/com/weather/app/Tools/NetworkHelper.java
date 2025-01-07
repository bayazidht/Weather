package com.weather.app.Tools;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;

import com.google.android.material.snackbar.Snackbar;
import com.weather.app.R;

public class NetworkHelper {

    private final Context mContext;

    public NetworkHelper(Context context) {
        this.mContext = context;
    }

    public void showMessage() {
        Snackbar snackbar = Snackbar.make(((Activity)mContext).findViewById(R.id.main), "No internet!", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Refresh", view -> ((Activity) mContext).recreate());
        snackbar.show();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = ((ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
