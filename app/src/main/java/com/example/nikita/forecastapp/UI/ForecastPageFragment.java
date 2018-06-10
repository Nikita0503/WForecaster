package com.example.nikita.forecastapp.UI;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikita.forecastapp.R;
import com.example.nikita.forecastapp.model.PageData;
import com.example.nikita.forecastapp.model.data.ForecastInfo;

import org.w3c.dom.Text;

import butterknife.ButterKnife;

/**
 * Created by Nikita on 07.06.2018.
 */

public class ForecastPageFragment extends Fragment {
    public static final String ARGUMENT_IMAGE_ID = "arg_image_id";
    public static final String ARGUMENT_DAY_TEMP = "arg_day_temp";
    public static final String ARGUMENT_NIGHT_TEMP = "arg_night_temp";
    public static final String ARGUMENT_DATE = "arg_date";
    public static final String ARGUMENT_CITY = "arg_city";
    public static final String ARGUMENT_DESCRIPTION = "arg_description";

    private int mImageId;
    private String mDayTemp;
    private String mNightTemp;
    private String mDate;
    private String mCity;
    private String mDescription;

    static public ForecastPageFragment newInstance(int dayNumber, PageData pageData) {
        ForecastPageFragment pageFragment = new ForecastPageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_IMAGE_ID, pageData.getImageId());
        arguments.putString(ARGUMENT_DAY_TEMP, pageData.getDayTemperature());
        arguments.putString(ARGUMENT_NIGHT_TEMP, pageData.getNightTemperature());
        arguments.putString(ARGUMENT_DATE, pageData.getDate());
        arguments.putString(ARGUMENT_CITY, pageData.getCity());
        arguments.putString(ARGUMENT_DESCRIPTION, pageData.getDescription());
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageId = getArguments().getInt(ARGUMENT_IMAGE_ID);
        mDayTemp = getArguments().getString(ARGUMENT_DAY_TEMP);
        mNightTemp = getArguments().getString(ARGUMENT_NIGHT_TEMP);
        mDate = getArguments().getString(ARGUMENT_DATE);
        mCity = getArguments().getString(ARGUMENT_CITY);
        mDescription = getArguments().getString(ARGUMENT_DESCRIPTION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_fragment, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageResource(mImageId);
        TextView textViewDayTemp = (TextView) view.findViewById(R.id.textViewDayTemp);
        textViewDayTemp.setText(mDayTemp);
        TextView textViewNightTemp = (TextView) view.findViewById(R.id.textViewNightTemp);
        textViewNightTemp.setText(mNightTemp);
        TextView textViewDate = (TextView) view.findViewById(R.id.textViewDate);
        textViewDate.setText(mDate);
        TextView textViewCity = (TextView) view.findViewById(R.id.textViewCity);
        textViewCity.setText(mCity);
        TextView textViewDescription = (TextView) view.findViewById(R.id.textViewDescription);
        textViewDescription.setText(mDescription);
        return view;
    }
}
