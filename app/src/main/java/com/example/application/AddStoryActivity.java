package com.example.application;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.application.databinding.ActivityAddStoryBinding;

public class AddStoryActivity extends AppCompatActivity {
    private ActivityAddStoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}