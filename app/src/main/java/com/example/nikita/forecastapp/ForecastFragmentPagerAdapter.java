package com.example.nikita.forecastapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.nikita.forecastapp.UI.ForecastPageFragment;
import com.example.nikita.forecastapp.model.PageData;
import com.example.nikita.forecastapp.model.data.OpenWeatherMap.ForecastInfo;

/**
 * Created by Nikita on 08.06.2018.
 */

public class ForecastFragmentPagerAdapter extends FragmentStatePagerAdapter {
    public static final int AMOUNT_OF_DAYS = 7;
    private ForecastInfo mForecastInfo;

    public ForecastFragmentPagerAdapter(FragmentManager fm, ForecastInfo forecastInfo) {
        super(fm);
        mForecastInfo = forecastInfo;
    }

    @Override
    public Fragment getItem(int position) {
        PageData pageData = new PageData(position, mForecastInfo);
        return ForecastPageFragment.newInstance(position, pageData);
    }

    @Override
    public int getCount() {
        return AMOUNT_OF_DAYS;
    }
}
