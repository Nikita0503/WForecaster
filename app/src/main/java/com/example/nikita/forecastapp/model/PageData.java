package com.example.nikita.forecastapp.model;

import com.example.nikita.forecastapp.R;
import com.example.nikita.forecastapp.model.data.OpenWeatherMap.ForecastInfo;

import java.util.Date;

/**
 * Created by Nikita on 08.06.2018.
 */

public class PageData {
    private static final int ID_CLEARSKY = 800;
    private static final int ID_SUNNYCLOUD = 8;
    private static final int ID_CLOUDS = 7;
    private static final int ID_SNOW = 6;
    private static final int ID_RAIN = 5;
    private static final int ID_DRIZZLE = 3;
    private static final int ID_THUNDER = 2;
    private static final double CONST_FOR_TRANSLATION_TEMPERATURE_1 = 1.8;
    private static final double CONST_FOR_TRANSLATION_TEMPERATURE_2 = 459.67;
    private static final int CONST_FOR_TRANSLATION_TEMPERATURE_3 = 32;
    private static final double CONST_FOR_TRANSLATION_TEMPERATURE_4 = 0.55555555556;
    private static final int BEGINNING_OF_DAY_NAME = 4;
    private static final int END_OF_DAY_NAME = 11;
    private static final int BEGINNING_OF_DAY_NAME_FROM_API = 0;
    private static final int END_OF_DAY_NAME_FROM_API = 3;
    private static final String MONDAY = "Mon";
    private static final String TUESDAY = "Tue";
    private static final String WEDNESDAY = "Wed";
    private static final String THURSDAY = "Thu";
    private static final String FRIDAY = "Fri";
    private static final String SATURDAY = "Sat";
    private static final String SUNDAY = "Sun";

    private int mDayNumber;
    private ForecastInfo mForecastInfo;
    public PageData(int dayNumber, ForecastInfo forecastInfo){
        mDayNumber = dayNumber;
        mForecastInfo = forecastInfo;
    }

    public int getImageId() {
        int id = mForecastInfo.getList().get(mDayNumber).getWeather().get(0).getId();
        String condition = mForecastInfo.getList().get(mDayNumber).getWeather().get(0).getDescription();
        int imageId = R.drawable.img_sun;
        if (id == ID_CLEARSKY) {
            imageId = R.drawable.img_sun;
        } else {
            switch (id / 100) {
                case ID_SUNNYCLOUD:;
                    imageId = R.drawable.img_sunnycloud;
                    break;
                case ID_CLOUDS:
                    imageId = R.drawable.img_clouds;
                    break;
                case ID_SNOW:
                    imageId = R.drawable.img_snow;
                    break;
                case ID_RAIN:
                    if(condition.equals("light rain")){
                        imageId = R.drawable.img_light_rain;
                    }
                    if(condition.equals("moderate rain")){
                        imageId = R.drawable.img_rain;
                    }
                    if(condition.equals("heavy intensity rain")){
                        imageId = R.drawable.img_heavy_rain;
                    }
                    break;
                case ID_DRIZZLE:
                    imageId = R.drawable.img_drizzle;
                    break;
                case ID_THUNDER:
                    imageId = R.drawable.img_thunder;
                    break;
            }
        }
        return imageId;
    }

    public String getDayTemperature() {
        double tempDouble = mForecastInfo.getList().get(mDayNumber).getTemp().getDay();
        int temp = (int) ((((tempDouble * CONST_FOR_TRANSLATION_TEMPERATURE_1 - CONST_FOR_TRANSLATION_TEMPERATURE_2))
                - CONST_FOR_TRANSLATION_TEMPERATURE_3) * CONST_FOR_TRANSLATION_TEMPERATURE_4);
        if (temp > 0) {
            return String.valueOf("+" + temp + "°C");
        }
        else{
            return String.valueOf(String.valueOf(temp + "°C"));
        }
    }

    public String getNightTemperature() {
        double tempDouble = mForecastInfo.getList().get(mDayNumber).getTemp().getNight();
        int temp = (int) ((((tempDouble * CONST_FOR_TRANSLATION_TEMPERATURE_1 - CONST_FOR_TRANSLATION_TEMPERATURE_2))
                - CONST_FOR_TRANSLATION_TEMPERATURE_3) * CONST_FOR_TRANSLATION_TEMPERATURE_4);
        if (temp > 0) {
            return String.valueOf("+" + temp + "°C");
        }
        else {
            return String.valueOf(temp + "°C");
        }
    }

    public String getDate() {
        long dateLong = mForecastInfo.getList().get(mDayNumber).getDt();
        Date date = convertUnixTimestampToDate(dateLong);
        String dateStr = date.toString();
        String dayOfWeek = "";
        switch (dateStr.substring(BEGINNING_OF_DAY_NAME_FROM_API, END_OF_DAY_NAME_FROM_API)) {
            case MONDAY:
                dayOfWeek = "Monday";
                break;
            case TUESDAY:
                dayOfWeek = "Tuesday";
                break;
            case WEDNESDAY:
                dayOfWeek = "Wednesday";
                break;
            case THURSDAY:
                dayOfWeek = "Thursday";
                break;
            case FRIDAY:
                dayOfWeek = "Friday";
                break;
            case SATURDAY:
                dayOfWeek = "Saturday";
                break;
            case SUNDAY:
                dayOfWeek = "Sunday";
                break;
        }
        return dateStr.substring(BEGINNING_OF_DAY_NAME, END_OF_DAY_NAME) + " " + dayOfWeek;
    }

    public String getDescription() {
        String condition = mForecastInfo.getList().get(mDayNumber).getWeather().get(0).getDescription();
        return condition;
    }

    public static Date convertUnixTimestampToDate(long timestamp) {
        Date date = new Date(timestamp * 1000L);
        return date;
    }
}
