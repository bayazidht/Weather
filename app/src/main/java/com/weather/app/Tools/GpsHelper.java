package com.weather.app.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.weather.app.R;

public class GpsHelper {

    private final Context mContext;

    public GpsHelper(Context context) {
        this.mContext = context;
    }

    public boolean isGpsEnabled() {
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!gps_enabled && !network_enabled) {
            new MaterialAlertDialogBuilder(mContext)
                    .setIcon(R.drawable.ic_location)
                    .setTitle("Enable GPS")
                    .setMessage("To get weather update in your address, you need to enable GPS.")
                    .setCancelable(false)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Enable", (paramDialogInterface, paramInt) -> mContext.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .show();
        } else {
            return true;
        }
        return false;
    }

    public void showLocationPermissionDialog() {
        new MaterialAlertDialogBuilder(mContext)
                .setIcon(R.drawable.ic_location)
                .setTitle("Allow Location")
                .setMessage("To get weather update in your address, you need to allow location.")
                .setCancelable(false)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Allow", (paramDialogInterface, paramInt) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
                    intent.setData(uri);
                    mContext.startActivity(intent);
                })
                .show();
    }

    public void showLocationRequestFailedMessage() {
        Snackbar snackbar = Snackbar.make(((Activity)mContext).findViewById(R.id.main), "Location request failed!", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Refresh", view -> ((Activity) mContext).recreate());
        snackbar.show();
    }
}
