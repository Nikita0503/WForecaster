package com.example.nikita.forecastapp;

/**
 * Created by Nikita on 06.06.2018.
 */

public interface BaseContract {
    interface BaseView{
        void showMessage(String message);
    }
    interface BasePresenter{
        void onStart();
        void onStop();
    }
}
