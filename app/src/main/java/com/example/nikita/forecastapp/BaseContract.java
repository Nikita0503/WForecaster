package com.example.nikita.forecastapp;

import com.valdesekamdem.library.mdtoast.MDToast;

/**
 * Created by Nikita on 06.06.2018.
 */

public interface BaseContract {
    interface BaseView{
        void showMessage(MDToast message);
    }
    interface BasePresenter{
        void onStart();
        void onStop();
    }
}
