package com.example.nikita.forecastapp.UI;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikita.forecastapp.BaseContract;
import com.example.nikita.forecastapp.MainPresenter;
import com.example.nikita.forecastapp.R;
import com.example.nikita.forecastapp.UI.ForecastPageFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.ocnyang.pagetransformerhelp.transformer.CubeOutTransformer;
import com.ocnyang.pagetransformerhelp.transformer.ScaleInOutTransformer;
import com.victor.loading.rotate.RotateLoading;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements BaseContract.BaseView, GoogleApiClient.OnConnectionFailedListener {
    int PLACE_PICKER_REQUEST = 1;
    private MainPresenter mPresenter;
    private PlaceAutocompleteFragment mAutocompleteFragment;
    @BindView(R.id.viewPager)
    ViewPager mPager;
    @BindView(R.id.rotateLoading)
    RotateLoading mRotateLoading;
    @BindView(R.id.textViewCity)
    TextView mTextViewCity;
    @OnClick(R.id.imageViewMarker)
    public void onClickMarker(){
        startRotateLoading();
        mPresenter.fetchCurrentLocation();
    }
    @OnClick(R.id.imageViewMap)
    public void onClickMap(){
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startRotateLoading();
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPresenter = new MainPresenter(this);
        mPresenter.onStart();
        mAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        mAutocompleteFragment.setFilter(mPresenter.getPlaceFilter());
        mAutocompleteFragment.setOnPlaceSelectedListener(mPresenter.getPlaceSelectionListener());
        mPager.setPageTransformer(true, new ScaleInOutTransformer());
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                mPresenter.fetchDataByMapData(data);
            }
        }
    }

    public void setPagerAdapter(PagerAdapter pagerAdapter){
        mPager.setAdapter(pagerAdapter);
    }

    public void setCity(String city){
        mTextViewCity.setText(city);
    }

    public void startRotateLoading(){
        mRotateLoading.start();
    }

    public void stopRotateLoading(){
        mRotateLoading.stop();
    }

    @Override
    public void onStop(){
        super.onStop();
        mPresenter.onStop();
    }



}
