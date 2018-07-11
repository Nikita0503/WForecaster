package com.example.nikita.forecastapp.model;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikita.forecastapp.MainPresenter;
import com.example.nikita.forecastapp.R;
import com.example.nikita.forecastapp.UI.MainActivity;
import com.simplealertdialog.SimpleAlertDialogFragment;
import com.valdesekamdem.library.mdtoast.MDToast;

/**
 * Created by Nikita on 17.06.2018.
 */

public class DrawerItemClickListener implements ListView.OnItemClickListener, ListView.OnItemLongClickListener {
    public static final int REQUEST_CODE_REMOVE_CITY = 1;
    private MainActivity mActivity;
    private MainPresenter mPresenter;

    public DrawerItemClickListener(MainActivity mActivity, MainPresenter mPresenter) {
        this.mActivity = mActivity;
        this.mPresenter = mPresenter;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position>0) {
            TextView textViewCity = view.findViewById(R.id.itemTextViewCity);
            String city = textViewCity.getText().toString();
            mActivity.startRotateLoading();
            mPresenter.fetchDataByCityName(city);
        }else{
            mPresenter.addCityToFavourite();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
        if (position > 0) {
            TextView textViewCity = view.findViewById(R.id.itemTextViewCity);
            String city = textViewCity.getText().toString();
            mActivity.setChosenCityForDelete(city);
            new SimpleAlertDialogFragment.Builder()
                    .setMessage(mActivity.getResources().getString(R.string.do_you_want_delete))
                    .setPositiveButton(mActivity.getResources().getString(R.string.yes))
                    .setNegativeButton(mActivity.getResources().getString(R.string.no))
                    .setRequestCode(REQUEST_CODE_REMOVE_CITY)
                    .create().show(mActivity.getFragmentManager(), "dialog");

        }
        return true;
    }

}
