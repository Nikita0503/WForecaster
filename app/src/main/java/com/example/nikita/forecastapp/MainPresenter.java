package com.example.nikita.forecastapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.example.nikita.forecastapp.UI.MainActivity;
import com.example.nikita.forecastapp.model.APIUtils.ForecastAPIUtils;
import com.example.nikita.forecastapp.model.APIUtils.GooglePlacesAPIUtils;
import com.example.nikita.forecastapp.model.data.CitiesTable;
import com.example.nikita.forecastapp.model.data.CityData;
import com.example.nikita.forecastapp.model.data.DBHelper;
import com.example.nikita.forecastapp.model.data.GooglePlaces.GooglePlace;
import com.example.nikita.forecastapp.model.data.OpenWeatherMap.ForecastInfo;
import com.google.android.gms.common.ConnectionResult;
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
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Nikita on 06.06.2018.
 */

public class MainPresenter implements BaseContract.BasePresenter, LoaderManager.LoaderCallbacks<Cursor>, GoogleApiClient.OnConnectionFailedListener {
    public static final String TOAST_TYPE_SUCCESS = "success";
    public static final String TOAST_TYPE_ERROR = "error";
    public static final String TOAST_TYPE_INFO = "info";
    public static final String TOAST_TYPE_WARNING = "warning";
    private boolean mSuccessOpenWeatherMapAPIRequest;
    private boolean mSuccessGoogleAPIRequest;
    private String mType;
    private CityData mCityData;
    private MainActivity mActivity;
    private CompositeDisposable mDisposables;
    private ForecastAPIUtils mForecastAPIUtils;
    private GooglePlacesAPIUtils mGooglePlacesAPIUtils;
    private GoogleApiClient mGoogleApiClient;
    private CitiesTable mCitiesTable;
    private SimpleCursorAdapter scAdapter;

    public MainPresenter(MainActivity mainActivity){
        mActivity = mainActivity;
        mForecastAPIUtils = new ForecastAPIUtils();
        mGooglePlacesAPIUtils = new GooglePlacesAPIUtils();
        mGoogleApiClient = new GoogleApiClient
                .Builder(mActivity)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(mActivity, this)
                .build();
    }

    @Override
    public void onStart() {
        mDisposables = new CompositeDisposable();
        mCitiesTable = new CitiesTable(mActivity.getApplicationContext(), this);
        mCitiesTable.open();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
        return new CityCursorLoader(mActivity.getApplicationContext(), mCitiesTable);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        scAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        sendMessage(mActivity.getResources().getString(R.string.connection_error), TOAST_TYPE_ERROR);
    }

    public SimpleCursorAdapter getAdapter(){
        String[] from = new String[] { DBHelper.COLUMN_CITY };
        int[] to = new int[] { R.id.itemTextViewCity };
        scAdapter = new SimpleCursorAdapter(mActivity.getApplicationContext(), R.layout.list_item, null, from, to, 0);
        return scAdapter;
    }

    public PlaceSelectionListener getPlaceSelectionListener() {
        return new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mActivity.startRotateLoading();
                mSuccessOpenWeatherMapAPIRequest = false;
                mSuccessGoogleAPIRequest = false;
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
                    mSuccessOpenWeatherMapAPIRequest = false;
                    mSuccessGoogleAPIRequest = false;
                    fetchDataByCoordinates(likelyPlaces.get(0).getPlace().getLatLng());
                    fetchLocationByPlaceId(likelyPlaces.get(0).getPlace().getId());
                    likelyPlaces.release();
                }catch (Exception e){
                    e.printStackTrace();
                    sendMessage(mActivity.getResources().getString(R.string.connection_error), TOAST_TYPE_ERROR);
                    mActivity.stopRotateLoading();
                }
            }
        });
    }

    public void fetchDataByMapData(Intent mapData){
        Place place = PlacePicker.getPlace(mapData, mActivity.getApplicationContext());
        mSuccessOpenWeatherMapAPIRequest = false;
        mSuccessGoogleAPIRequest = false;
        fetchDataByCoordinates(place.getLatLng());
        fetchLocationByPlaceId(place.getId());
    }

    public void fetchDataByCityName(final String city){
        Disposable weatherInfo = mCitiesTable.getCityDataSet
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<CityData>>() {
                    @Override
                    public void onSuccess(ArrayList<CityData> cityDataSet) {
                        for(CityData cityData : cityDataSet) {
                            if(cityData.name.equals(city)) {
                                fetchDataByCoordinates(cityData.latLng);
                                Log.d("KeY", cityData.placeId);
                                fetchLocationByPlaceId(cityData.placeId);
                                break;
                            }
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        sendMessage(mActivity.getResources().getString(R.string.connection_error), TOAST_TYPE_ERROR);
                        mActivity.stopRotateLoading();
                    }

                });
        mDisposables.add(weatherInfo);
    }

    public void fetchDataByCoordinates(LatLng coordinates){
        Disposable weatherInfo = mForecastAPIUtils.getDataByCoordinates(coordinates.latitude, coordinates.longitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ForecastInfo>() {
                    @Override
                    public void onSuccess(ForecastInfo forecastInfo) {
                        makePagerAdapter(forecastInfo);
                        mSuccessOpenWeatherMapAPIRequest = true;
                        checkRequestsSuccess();
                    }
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        sendMessage(mActivity.getResources().getString(R.string.connection_error), TOAST_TYPE_ERROR);
                        mActivity.stopRotateLoading();
                    }

                });
        mDisposables.add(weatherInfo);
    }

    public void fetchLocationByPlaceId(String placeId){
        Log.d("KEY", placeId);
        Disposable placeInfo = mGooglePlacesAPIUtils.getDataByPlaceId(placeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<GooglePlace>() {
                    @Override
                    public void onSuccess(GooglePlace googlePlace) {
                        try {
                            boolean founded = false;
                            String place = "chosen place";
                            for (int i = 0; i < googlePlace.getResult().getAddressComponents().size(); i++) {
                                List<String> types = googlePlace.getResult().getAddressComponents().get(i).getTypes();
                                if (types.get(0).equals("locality") && types.get(1).equals("political")) {
                                    mType = "locality";
                                    place = googlePlace.getResult().getAddressComponents().get(i).getShortName();
                                    founded = true;
                                    Log.d("KEy", googlePlace.getResult().getId());

                                    break;
                                }
                            }
                            if(!founded) {
                                for (int i = 0; i < googlePlace.getResult().getAddressComponents().size(); i++) {
                                    List<String> types = googlePlace.getResult().getAddressComponents().get(i).getTypes();
                                    if (types.get(0).equals("administrative_area_level_1") && types.get(1).equals("political")) {
                                        mType = "administrative_area_level_1";
                                        place = googlePlace.getResult().getAddressComponents().get(i).getShortName();
                                        founded = true;
                                        break;
                                    }
                                }
                            }
                            if(!founded) {
                                for (int i = 0; i < googlePlace.getResult().getAddressComponents().size(); i++) {
                                    List<String> types = googlePlace.getResult().getAddressComponents().get(i).getTypes();
                                    if (types.get(0).equals("country") && types.get(1).equals("political")) {
                                        mType = "country";
                                        place = googlePlace.getResult().getAddressComponents().get(i).getLongName();
                                        break;
                                    }
                                }
                            }
                            mCityData = new CityData(place, new LatLng(googlePlace.getResult().getGeometry().getLocation().getLat(),
                                    googlePlace.getResult().getGeometry().getLocation().getLng()), googlePlace.getResult().getPlaceId());
                            mActivity.setCity(place);
                            mSuccessGoogleAPIRequest = true;
                            checkRequestsSuccess();
                        }catch (Exception c){
                            mActivity.setCity(mActivity.getResources().getString(R.string.chosen_place));
                            sendMessage(mActivity.getResources().getString(R.string.place_not_found), TOAST_TYPE_ERROR);
                            mActivity.stopRotateLoading();
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        sendMessage(mActivity.getResources().getString(R.string.place_not_found), TOAST_TYPE_ERROR);
                        mActivity.stopRotateLoading();
                    }

                });
        mDisposables.add(placeInfo);
    }

    public void addCityToFavourite(){
        if(mCityData!=null) {
            if (mType.equals("locality")) {
                Observable<CityData> observable = Observable.create(new ObservableOnSubscribe<CityData>() {
                    @Override
                    public void subscribe(ObservableEmitter<CityData> e) throws Exception {
                        e.onNext(mCityData);
                        e.onComplete();
                    }
                });
                mDisposables.add(observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(mCitiesTable.addCity()));

            } else {
                sendMessage(mActivity.getResources().getString(R.string.inappropriate_place), TOAST_TYPE_INFO);
            }
        }
        else{
            sendMessage(mActivity.getResources().getString(R.string.choose_place_please), TOAST_TYPE_WARNING);
        }
    }

    public void removeCityFromFavourite(final String city){
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext(city);
                e.onComplete();
            }
        });
        mDisposables.add(observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(mCitiesTable.deleteCity()));
    }

    public void updateFavouriteList(){
        mActivity.getSupportLoaderManager().getLoader(0).forceLoad();
    }

    private void checkRequestsSuccess(){
        if(mSuccessOpenWeatherMapAPIRequest && mSuccessGoogleAPIRequest){
            mActivity.stopRotateLoading();
        }
    }

    public void makePagerAdapter(ForecastInfo forecastInfo){
        PagerAdapter pagerAdapter = new ForecastFragmentPagerAdapter(mActivity.getSupportFragmentManager(), forecastInfo);
        mActivity.setPagerAdapter(pagerAdapter);
    }

    public void sendMessage(String message, String type){
        MDToast toast = null;
        if(type.equals(TOAST_TYPE_SUCCESS)){
            toast = MDToast.makeText(mActivity.getApplicationContext(), message, MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS);
        }
        if(type.equals(TOAST_TYPE_ERROR)){
            toast = MDToast.makeText(mActivity.getApplicationContext(), message, MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR);
        }
        if(type.equals(TOAST_TYPE_INFO)){
            toast = MDToast.makeText(mActivity.getApplicationContext(), message, MDToast.LENGTH_SHORT, MDToast.TYPE_INFO);
        }
        if(type.equals(TOAST_TYPE_WARNING)){
            toast = MDToast.makeText(mActivity.getApplicationContext(), message, MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING);
        }
        mActivity.showMessage(toast);
    }

    @Override
    public void onStop() {
        mDisposables.clear();
        mCitiesTable.close();
    }

    static class CityCursorLoader extends CursorLoader {
        CitiesTable db;

        public CityCursorLoader(Context context, CitiesTable db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = db.getAllCities();
            return cursor;
        }

    }
}
