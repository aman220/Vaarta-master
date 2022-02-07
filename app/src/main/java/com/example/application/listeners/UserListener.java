package com.example.application.listeners;

import com.example.application.models.User;

public interface UserListener {
    void onUserClicked(User user);
    void initiateVideoMeeting(User user);
    void initiateAudioMeeting(User user);
}
