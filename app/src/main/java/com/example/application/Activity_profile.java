package com.example.application;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.application.activities.MainActivity;
import com.example.application.activities.UsersActivity;
import com.example.application.databinding.ActivityProfileBinding;
import com.example.application.models.User;
import com.example.application.utilities.Constants;
import com.example.application.utilities.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class Activity_profile extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private User receiverUser;
    private String encodedImage;
    private ActivityProfileBinding binding;
    private FirebaseFirestore db;
    private Button updatebutton;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        setListeners();


        bottomNavigationView  = findViewById(R.id.bottomNavigation);
//        BottomNavigationView.setSelectedItemId(R.id.menu_account);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_search:
                        startActivity(new Intent(getApplicationContext()
                                , UsersActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.menu_account:
                        return true;
                    case R.id.menu_music:
                        startActivity(new Intent(getApplicationContext()
                                , camera.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.menu_home:
                        startActivity(new Intent(getApplicationContext()
                                , MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
    }
    private void setListeners() {
        binding.icBack.setOnClickListener(v -> onBackPressed());
    }



    public void goprofile(View view){
        Intent i = new Intent(this, UserUpdate.class);
        startActivity(i);
    }

    private void loadReceiverDetails(){
        receiverUser=(User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textUsername.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.textEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        binding.Email.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        binding.textphone.setText(preferenceManager.getString(Constants.KEY_PHONE));
        binding.bio.setText(preferenceManager.getString(Constants.KEY_BIO));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.menu_account);
    }

}