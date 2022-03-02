package com.example.application.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.application.R;
import com.example.application.databinding.ItemContainerUserBinding;
import com.example.application.listeners.UserListener;
import com.example.application.models.User;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Timer;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{

    private List<User> user;
    private final UserListener userListener;
    private Timer timer;
    private List<User> usersSource;


    public UsersAdapter(List<User> users ,UserListener userListener) {
        this.user = users;
        this.userListener = userListener;
        usersSource = user;
    }

    @NotNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
               LayoutInflater.from(parent.getContext()),
               parent,
               false
       );
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.UserViewHolder holder, int position) {
        holder.setUserData(user.get(position));
    }

    @Override
    public int getItemCount() {
        return user.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserBinding binding;
        ImageView imageAudioMeeting , imageVideoMeeting;


        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
            imageAudioMeeting=itemView.findViewById(R.id.imageAudioMeeting);
            imageVideoMeeting=itemView.findViewById(R.id.imageVideoMeeting);
        }

        void setUserData(User user){
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);

            binding.imageProfile.setImageBitmap(getUserImage(user.getImage()));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
            imageVideoMeeting.setOnClickListener(v -> userListener.initiateVideoMeeting(user));
            imageAudioMeeting.setOnClickListener(v -> userListener.initiateAudioMeeting(user));
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}

