package com.example.application.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permissions {

    public boolean isRecordingOk(Context context){
        return ContextCompat.checkSelfPermission(context , Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED;
    }

    public void requestRecordings(Activity activity){
        ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.RECORD_AUDIO} , Constants.RECORDING_REQUEST_CODE);
    }
}
