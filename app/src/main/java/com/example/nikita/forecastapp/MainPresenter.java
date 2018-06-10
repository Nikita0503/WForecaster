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
import com.example.nikita.forecastapp.model.data.ForecastInfo;
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
    private GoogleApiClient mGoogleApiClient;

    public MainPresenter(MainActivity mainActivity){
        mActivity = mainActivity;
        mForecastAPIUtils = new ForecastAPIUtils();
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
                fetchDataByCoordinates(place.getLatLng());
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
                        Toast.makeText(mActivity.getApplicationContext(), "ERROR PRESENTER", Toast.LENGTH_SHORT).show();
                        mActivity.stopRotateLoading();
                    }

                });
        mDisposables.add(sunInfo);
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
                    likelyPlaces.release();
                }catch (Exception c){
                    mActivity.stopRotateLoading();
                }
            }
        });
    }

    public void fetchDataByMapData(Intent mapData){
        Place place = PlacePicker.getPlace(mapData, mActivity.getApplicationContext());
        String toastMsg = String.format("Place: %s", place.getName());
        Toast.makeText(mActivity.getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
        fetchDataByCoordinates(place.getLatLng());
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
