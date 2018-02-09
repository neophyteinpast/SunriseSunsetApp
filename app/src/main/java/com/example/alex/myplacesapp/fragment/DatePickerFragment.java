package com.example.alex.myplacesapp.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.alex.myplacesapp.R;
import com.example.alex.myplacesapp.service.DateService;

import java.util.Calendar;

/**
 * Created by Alex on 07.02.2018.
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    public static final String TAG = DatePickerFragment.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog():");

        // Use the current date as the default date in the picker
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Log.d(TAG, "onDateSet(): year = " + year + ", month = " + (month + 1) + ", day = " + dayOfMonth);
        // Do something with the date chosen by the user
        ((TextView)getActivity().findViewById(R.id.tvDate))
                .setText(DateService.getDate(year, month + 1, dayOfMonth));
    }
}
