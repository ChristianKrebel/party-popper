package com.partypopper.app.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import androidx.core.content.ContextCompat;
import lombok.Getter;

/**
 * LocationService to easily handle the user's location.
 * Given by evandrix (https://stackoverflow.com/users/177728/evandrix)
 * and Ashton Engberg, sroes
 * https://stackoverflow.com/questions/29657948/get-the-current-location-fast-and-once-in-android)
 * and edited by the Party Popper team.
 */

public class LocationService implements LocationListener {

    //Permission
    public static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    //The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    //The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    private final static boolean forceNetwork = false;

    private static LocationService instance = null;

    private LocationManager locationManager;
    public Location location;
    @Getter
    private double latitude, longitude;
    private boolean isGPSEnabled, locationServiceAvailable;
    private boolean isNetworkEnabled;

    private LocationCallback callback;

    public static interface LocationCallback {
        public void onNewLocationAvailable(GPSCoordinates location);
    }


    /**
     * Singleton implementation
     * @return
     */
    public static LocationService getLocationManager(Context context, LocationCallback callback)     {
        if (instance == null) {
            instance = new LocationService(context, callback);
        }
        return instance;
    }

    /**
     * Local constructor
     */
    private LocationService( Context context, LocationCallback callback )     {
        this.callback = callback;
        initLocationService(context);
    }



    /**
     * Sets up location service after permissions is granted
     */
    @TargetApi(23)
    private void initLocationService(Context context) {


        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        try   {
            this.longitude = 0.0;
            this.latitude = 0.0;
            this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (forceNetwork) isGPSEnabled = false;

            if (!isNetworkEnabled && !isGPSEnabled)    {
                // cannot get location
                this.locationServiceAvailable = false;
            }
            //else
            {
                this.locationServiceAvailable = true;

                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null)   {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        updateCoordinates();
                    }
                }//end if

                if (isGPSEnabled)  {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null)  {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        updateCoordinates();
                    }
                }
            }
        } catch (Exception ex)  {
            Log.e(ex.getMessage(), ex.getMessage());
        }
    }

    private void updateCoordinates() {
        callback.onNewLocationAvailable(new GPSCoordinates(location.getLatitude(), location.getLongitude()));
    }


    @Override
    public void onLocationChanged(Location location)     {
        updateCoordinates();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    /**
     * Class to replace Location class for easier initialization
     */
    public static class GPSCoordinates {
        public float longitude = -1;
        public float latitude = -1;

        public GPSCoordinates(float theLatitude, float theLongitude) {
            longitude = theLongitude;
            latitude = theLatitude;
        }

        public GPSCoordinates(double theLatitude, double theLongitude) {
            longitude = (float) theLongitude;
            latitude = (float) theLatitude;
        }
    }
}
