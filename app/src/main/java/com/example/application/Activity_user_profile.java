package com.example.application;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.application.databinding.ActivityUserProfileBinding;
import com.example.application.models.User;
import com.example.application.utilities.Constants;
import com.example.application.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Activity_user_profile extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private ActivityUserProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        binding.progressBar.setVisibility(View.VISIBLE);
        Bundle extras = getIntent().getExtras();
        String user = extras.getString("userType");
//        Toast.makeText(this, user, Toast.LENGTH_SHORT).show();
        loadingCurtain();
        if(user.equals("self")){
            getUsersSearched(extras.getString("token"));
        }else if(user.equals("other")){
            binding.editprofile.setVisibility(View.GONE);
            getUsersSearched(extras.getString("token"));
        }
    }

    private void loadingCurtain(){
        binding.overlayCurtain.setVisibility(View.VISIBLE);
        Loading(true);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (binding.name.getText().equals("")){
                    loadingCurtain();
                }else {
                    Loading(false);
                    binding.overlayCurtain.setVisibility(View.GONE);
                }
            }
        }, 100);
    }

    private void goEdit() {
        Intent i = new Intent (this, UserUpdate.class);
        startActivity(i);
    }

    private void getUsersSearched(String s) {
        Loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        if (s.equals(preferenceManager.getString(Constants.KEY_PCM_TOKEN))) {
                            binding.name.setText(preferenceManager.getString(Constants.KEY_NAME));
                            binding.Email.setText(preferenceManager.getString(Constants.KEY_EMAIL));
                            binding.textphone.setText(preferenceManager.getString(Constants.KEY_PHONE));
                            binding.bio.setText(preferenceManager.getString(Constants.KEY_BIO));
                            byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            binding.imageProfile.setImageBitmap(bitmap);
                        }
                        else{
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                if (s.equals(queryDocumentSnapshot.getString(Constants.KEY_PCM_TOKEN))) {
                                    binding.name.setText(queryDocumentSnapshot.getString(Constants.KEY_NAME));
                                    binding.Email.setText(queryDocumentSnapshot.getString(Constants.KEY_EMAIL));
                                    binding.textphone.setText(queryDocumentSnapshot.getString(Constants.KEY_PHONE));
                                    binding.bio.setText(queryDocumentSnapshot.getString(Constants.KEY_BIO));
                                    byte[] bytes = Base64.decode(queryDocumentSnapshot.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    binding.imageProfile.setImageBitmap(bitmap);
                                }
                            }
                        }
                    }else{
                        Toast.makeText(this, "Error Loading Profile ", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void Loading(Boolean isloading) {
        if (isloading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

}