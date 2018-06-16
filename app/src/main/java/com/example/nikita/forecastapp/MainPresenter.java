package com.example.nikita.forecastapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.widget.Toast;

import com.example.nikita.forecastapp.UI.MainActivity;
import com.example.nikita.forecastapp.model.APIUtils.ForecastAPIUtils;
import com.example.nikita.forecastapp.model.APIUtils.GooglePlacesAPIUtils;
import com.example.nikita.forecastapp.model.data.GooglePlaces.GooglePlace;
import com.example.nikita.forecastapp.model.data.OpenWeatherMap.ForecastInfo;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Nikita on 06.06.2018.
 */

public class MainPresenter implements BaseContract.BasePresenter { // Забахать так чтобы пользователь мог указать кол-во дней (в запросе к API есть такая тема)
    private MainActivity mActivity;
    private CompositeDisposable mDisposables;
    private ForecastAPIUtils mForecastAPIUtils;
    private GooglePlacesAPIUtils mGooglePlacesAPIUtils;
    private GoogleApiClient mGoogleApiClient;

    public MainPresenter(MainActivity mainActivity){
        mActivity = mainActivity;
        mForecastAPIUtils = new ForecastAPIUtils();
        mGooglePlacesAPIUtils = new GooglePlacesAPIUtils();
        mGoogleApiClient = new GoogleApiClient
                .Builder(mActivity)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(mActivity, mActivity)
                .build();
    }

    @Override
    public void onStart() {
        mDisposables = new CompositeDisposable();
    }

    public PlaceSelectionListener getPlaceSelectionListener() {
        return new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mActivity.startRotateLoading();
                Log.d("PLACE", place.getId());
                fetchDataByCoordinates(place.getLatLng());
                fetchLocationByPlaceId(place.getId());
            }

            @Override
            public void onError(Status status) {
                mActivity.stopRotateLoading();
                Log.i("ERROR", "An error occurred: " + status);
            }
        };
    }

    public AutocompleteFilter getPlaceFilter(){
        return new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();
    }


    public void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(mActivity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                try {
                    fetchDataByCoordinates(likelyPlaces.get(0).getPlace().getLatLng());
                    fetchLocationByPlaceId(likelyPlaces.get(0).getPlace().getId());
                    likelyPlaces.release();
                }catch (Exception c){
                    mActivity.stopRotateLoading();
                }
            }
        });
    }

    public void fetchDataByMapData(Intent mapData){
        Place place = PlacePicker.getPlace(mapData, mActivity.getApplicationContext());
        fetchDataByCoordinates(place.getLatLng());
        fetchLocationByPlaceId(place.getId());
    }

    public void fetchDataByCoordinates(LatLng coordinates){
        Disposable sunInfo = mForecastAPIUtils.getDataByCoordinates(coordinates.latitude, coordinates.longitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ForecastInfo>() {
                    @Override
                    public void onSuccess(ForecastInfo forecastInfo) {
                        makePagerAdapter(forecastInfo);
                        mActivity.stopRotateLoading();
                    }
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mActivity.showMessage(mActivity.getResources().getString(R.string.connection_error));
                        mActivity.stopRotateLoading();
                    }

                });
        mDisposables.add(sunInfo);
    }

    public void fetchLocationByPlaceId(String placeId){
        Disposable placeInfo = mGooglePlacesAPIUtils.getDataByPlaceId(placeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<GooglePlace>() {
                    @Override
                    public void onSuccess(GooglePlace googlePlace) {
                        mActivity.setCity(googlePlace.getResult().getAddressComponents().get(1).getShortName());
                    }
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mActivity.setCity(mActivity.getResources().getString(R.string.place_name_not_found));
                    }

                });
        mDisposables.add(placeInfo);
    }

    public void makePagerAdapter(ForecastInfo forecastInfo){
        PagerAdapter pagerAdapter = new ForecastFragmentPagerAdapter(mActivity.getSupportFragmentManager(), forecastInfo);
        mActivity.setPagerAdapter(pagerAdapter);
    }


    @Override
    public void onStop() {
        mDisposables.clear();
    }
}
