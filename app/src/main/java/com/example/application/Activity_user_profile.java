package com.example.application;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.application.databinding.ActivityUserProfileBinding;
import com.example.application.models.User;
import com.example.application.utilities.Constants;
import com.example.application.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jsibbold.zoomage.ZoomageView;

import java.util.ArrayList;
import java.util.List;

public class Activity_user_profile extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private User receiverUser;
    private String encodedImage;
    private ActivityUserProfileBinding binding;
    private FirebaseFirestore db;
    private Button updatebutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        setListerners();
        binding.progressBar.setVisibility(View.VISIBLE);
        Bundle extras = getIntent().getExtras();
        String user = extras.getString("userType");
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

    private void setListerners(){

        binding.editprofile.setOnClickListener(v -> goEdit());
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
                            binding.imageProfile.setOnClickListener(v -> fullScreenImagePopup(this, bitmap));
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
                                    binding.imageProfile.setOnClickListener(v -> fullScreenImagePopup(this, bitmap));

                                }
                            }
                        }
                    }else{
//
                    }
                });
    }


    private void fullScreenImagePopup(Context thisContext, Bitmap imageBitmap){
        Dialog builder = new Dialog(thisContext);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
            }
        });
        ZoomageView imageView = new ZoomageView(thisContext);
        imageView.setImageBitmap(imageBitmap);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setCropToPadding(true);
        builder.show();
    }
    private void Loading(Boolean isloading) {
        if (isloading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

}