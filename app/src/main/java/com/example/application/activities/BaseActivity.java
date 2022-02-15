package com.example.application.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.application.utilities.Constants;
import com.example.application.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BaseActivity extends AppCompatActivity {

    private DocumentReference documentReference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        documentReference=database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Constants.KEY_AVAILABILITY,0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mma d/M/yy");
        documentReference.update(Constants.KEY_USER_STATUS,"Last Seen |"+ new SimpleDateFormat("h:mm:ss a d/M/yy").format(Calendar.getInstance().getTime()));


    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Constants.KEY_AVAILABILITY,1);
        documentReference.update(Constants.KEY_USER_STATUS,"online");
    }
}
