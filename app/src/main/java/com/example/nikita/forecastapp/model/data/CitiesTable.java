package com.example.nikita.forecastapp.model.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.example.nikita.forecastapp.MainPresenter;
import com.example.nikita.forecastapp.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by Nikita on 17.06.2018.
 */

public class CitiesTable {
    private static final int DB_VERSION = 1;
    public static final String DB_TABLE = "cities";
    private final Context mCtx;
    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;
    private MainPresenter mMainPresenter;

    public CitiesTable(Context ctx, MainPresenter mainPresenter) {
        mCtx = ctx;
        mMainPresenter = mainPresenter;
    }

    public void open() {
        mDBHelper = new DBHelper(mCtx, DBHelper.DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    public Cursor getAllCities() {
        return mDB.query(DB_TABLE, null, null, null, null, null, null);
    }

    public Single<ArrayList<CityData>> getCityDataSet = Single.create(new SingleOnSubscribe<ArrayList<CityData>>() {
        @Override
        public void subscribe(SingleEmitter<ArrayList<CityData>> e) throws Exception {
            ArrayList<CityData> cityDataSet = new ArrayList<CityData>();
            Cursor c = mDB.query(DB_TABLE, null, null, null, null, null, null);
            if (c.moveToFirst()) {
                int cityNameColIndex = c.getColumnIndex(DBHelper.COLUMN_CITY);
                int lotColIndex = c.getColumnIndex(DBHelper.COLUMN_LATITUDE);
                int lngColIndex = c.getColumnIndex(DBHelper.COLUMN_LONGITUDE);
                int placeIdColIndex = c.getColumnIndex(DBHelper.COLUMN_PLACE_ID);
                do {
                    cityDataSet.add(new CityData(c.getString(cityNameColIndex), new LatLng(c.getDouble(lotColIndex), c.getDouble(lngColIndex)), c.getString(placeIdColIndex)));
                } while (c.moveToNext());
            }
            e.onSuccess(cityDataSet);
        }
    });

    public DisposableObserver<CityData> addCity(){
        return new DisposableObserver<CityData>() {
            @Override
            public void onNext(CityData cityData) {
                Log.d("TABLE", cityData.placeId);
                ContentValues cv = new ContentValues();
                cv.put(DBHelper.COLUMN_CITY, cityData.name);
                cv.put(DBHelper.COLUMN_LATITUDE, cityData.latLng.latitude);
                cv.put(DBHelper.COLUMN_LONGITUDE, cityData.latLng.longitude);
                cv.put(DBHelper.COLUMN_PLACE_ID, cityData.placeId);
                long d = mDB.insert(DB_TABLE, null, cv);
                if(d == -1){
                    mMainPresenter.sendMessage(mCtx.getResources().getString(R.string.city_already_exist), MainPresenter.TOAST_TYPE_ERROR);
                }else {
                    mMainPresenter.sendMessage(mCtx.getResources().getString(R.string.city_was_successfully_added), MainPresenter.TOAST_TYPE_SUCCESS);
                }
            }

            @Override
            public void onError(Throwable e) {
                mMainPresenter.sendMessage(mCtx.getResources().getString(R.string.error), MainPresenter.TOAST_TYPE_ERROR);
            }

            @Override
            public void onComplete() {
                mMainPresenter.updateFavouriteList();
            }
        };
    }

    public DisposableObserver<String> deleteCity(){
        return new DisposableObserver<String>() {
            @Override
            public void onNext(String city) {
                mDB.delete(DB_TABLE, DBHelper.COLUMN_CITY + " = '" + city + "'", null);
            }

            @Override
            public void onError(Throwable e) {
                mMainPresenter.sendMessage(mCtx.getResources().getString(R.string.error), MainPresenter.TOAST_TYPE_ERROR);
            }

            @Override
            public void onComplete() {
                mMainPresenter.updateFavouriteList();
            }
        };
    }

}
