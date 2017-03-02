package com.nuvoton.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.nuvoton.nudoorbell.R;

import org.angmarch.views.NiceSpinner;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by timsheu on 8/4/16.
 */
public class RestartDialogFragment extends DialogFragment {
    private EditDeviceDialogInterface editDeviceDialogInterface;
    public interface EditDeviceDialogInterface {
        void removeDevice(String category);
        void spinnerChosen(RestartDialogFragment fragment, int index);
        void enterEditPage(String category);
    }

    private static final String TAG = "RestartDialogFragment";
    private String label = "Dialog Label";
    private String content = "Dialog Content";
    private String type = "Dialog";
    private NiceSpinner spinner;
    private List<String> localSpinnerData;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (type.compareTo("Dialog ") == 0){
            builder.setTitle(label).setMessage(content).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    editDeviceDialogInterface.removeDevice(label);
                }
            }).setNegativeButton("Cancel", null);
        }else if (type.compareTo("Spinner") == 0){
            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_confirm, null);
            spinner = (NiceSpinner) view.findViewById(R.id.historySpinner);
            spinner.attachDataSource(localSpinnerData);
            spinner.setTextColor(Color.BLACK);
            Log.d(TAG, "onCreateDialog: " + localSpinnerData);
            final RestartDialogFragment fragment = this;
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "onItemSelected: " + String.valueOf(position));
                    editDeviceDialogInterface.spinnerChosen(fragment, position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.d(TAG, "onNothingSelected: ");
                }
            });
            builder.setView(view).setTitle(label).setNegativeButton("Cancel", null);
        }else if (type.compareTo("Selection") == 0){
            builder.setTitle(label).setMessage(content).setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    editDeviceDialogInterface.removeDevice(label);
                }
            }).setNegativeButton("Setup", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    editDeviceDialogInterface.enterEditPage(label);
                }
            });
        }

        return builder.create();
    }

    public void setLabel(String label){
        this.label = label;
    }

    public void setContent(String content){
        this.content = content;
    }

    public void setInterface(EditDeviceDialogInterface dialogFragmentInterface){
        this.editDeviceDialogInterface = dialogFragmentInterface;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setSpinnerData(List<String> spinnerData){
        localSpinnerData = new LinkedList<>(spinnerData);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        spinner.dismissDropDown();
    }
}
