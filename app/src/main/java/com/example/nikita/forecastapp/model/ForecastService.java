package com.example.nikita.forecastapp.model;

import com.example.nikita.forecastapp.model.data.OpenWeatherMap.ForecastInfo;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Nikita on 06.06.2018.
 */

public interface ForecastService {
    @GET("/data/2.5/forecast/daily?cnt=7&appid=a7566f90e4ed0120ac27665a49f3bc9a")
    Single<ForecastInfo> getDataByCoordinates(@Query("lat") double lat, @Query("lon") double lon);
}
