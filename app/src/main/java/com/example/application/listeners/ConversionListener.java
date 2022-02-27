package com.example.application.listeners;

import com.example.application.models.User;

public interface ConversionListener {
    void onConversionClicked(User user);
    void onConversionLongClicked(int position);

}
