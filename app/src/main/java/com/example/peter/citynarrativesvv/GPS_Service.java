package com.example.peter.citynarrativesvv;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import com.google.android.gms.location.LocationListener;

import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GPS_Service extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener

{
    private LocationListener listener;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private FusedLocationProviderApi locationProviderApi =
            LocationServices.FusedLocationApi;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    } //Not Bound to one activity to it can be accessed by all

    @Override
    public void onCreate() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(30 * 1000);// User location updated every 30 seconds
        locationRequest.setFastestInterval(15*1000);// Checking location provided by other applications, this requires little extra cost
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);//Or _HIGH_ACCURACY
        googleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        //noinspection MissingPermission (The permission is Checked in the  Home Activity)
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Intent i = new Intent("location_update");
        i.putExtra("coordinates", location);
        sendBroadcast(i);
    }
}
