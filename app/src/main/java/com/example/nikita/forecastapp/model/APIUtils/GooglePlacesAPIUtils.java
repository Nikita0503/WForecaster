package com.example.nikita.forecastapp.model.APIUtils;

import com.example.nikita.forecastapp.model.data.GooglePlaces.GooglePlace;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import io.reactivex.Single;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Nikita on 16.06.2018.
 */

public class GooglePlacesAPIUtils implements GooglePlacesService{

    public static final String BASE_URL = "https://maps.googleapis.com/";

    @Override
    public Single<GooglePlace> getDataByPlaceId(String placeId) {
        Retrofit retrofit = getClient(BASE_URL);
        GooglePlacesService googlePlacesService = retrofit.create(GooglePlacesService.class);
        return googlePlacesService.getDataByPlaceId(placeId);
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
