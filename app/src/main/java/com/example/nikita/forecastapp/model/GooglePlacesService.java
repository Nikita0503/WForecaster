package com.example.nikita.forecastapp.model;

import com.example.nikita.forecastapp.model.data.GooglePlaces.GooglePlace;
import com.example.nikita.forecastapp.model.data.OpenWeatherMap.ForecastInfo;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Nikita on 16.06.2018.
 */

public interface GooglePlacesService {
    @GET("/maps/api/place/details/json?key=AIzaSyBG4Kysi0h_ZsRVOtRDqituNJfoAZA-de8&language=en")
    Single<GooglePlace> getDataByPlaceId(@Query("placeid") String placeId);
}
