package com.example.nikita.forecastapp.model.data;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Nikita on 17.06.2018.
 */

public class CityData {
    public String name;
    public LatLng latLng;
    public String placeId;

    public CityData(String name, LatLng latLng, String placeId) {
        this.name = name;
        this.latLng = latLng;
        this.placeId = placeId;
    }
}
