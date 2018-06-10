package com.example.nikita.forecastapp.model.APIUtils;

import com.example.nikita.forecastapp.ForecastService;
import com.example.nikita.forecastapp.model.data.ForecastInfo;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import io.reactivex.Single;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Nikita on 06.06.2018.
 */

public class ForecastAPIUtils implements ForecastService {

    public static final String BASE_URL = "http://api.openweathermap.org/";

    @Override
    public Single<ForecastInfo> getDataByCoordinates(double lat, double lon) {
        Retrofit retrofit = getClient(BASE_URL);
        ForecastService forecastService = retrofit.create(ForecastService.class);
        return forecastService.getDataByCoordinates(lat, lon);
    }

    public static Retrofit getClient(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit;
    }


}
