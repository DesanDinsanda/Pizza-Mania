package com.example.pizza_mania;

import android.app.Application;

import com.google.android.gms.maps.model.LatLng;

public class GlobalApp extends Application {
    private LatLng currentLocation;

    public LatLng getCurrentLocation(){
        return this.currentLocation;
    }

    public void setCurrentLocation(LatLng loc){
        this.currentLocation = loc;
    }
}
