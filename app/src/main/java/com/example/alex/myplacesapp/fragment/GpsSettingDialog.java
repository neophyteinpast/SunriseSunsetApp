package com.example.alex.myplacesapp.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.alex.myplacesapp.R;

/**
 * Created by Alex on 17.02.2018.
 */

public class GpsSettingDialog extends DialogFragment {
    private static final String TAG = GpsSettingDialog.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.enable_gps_message)
                .setPositiveButton(R.string.enable_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "Enable button clicked!");
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "Cancel button clicked!");
                        Toast.makeText(getActivity().getApplicationContext(),
                                "GPS option disabled!", Toast.LENGTH_SHORT).show();
                    }
                });

        return builder.create();
    }
}
