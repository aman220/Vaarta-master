package com.example.application.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.application.databinding.ItemContainerStoryBinding;
import com.example.application.listeners.UserListener;
import com.example.application.models.User;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder>{

    private List<User> user;
    private final UserListener userListener;
    private List<User> usersSource;

    public StoryAdapter(List<User> user, UserListener userListener) {
        this.user = user;
        this.userListener = userListener;
        usersSource = user;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerStoryBinding itemContainerStoryBinding = ItemContainerStoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new StoryAdapter.StoryViewHolder(itemContainerStoryBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        holder.setUserData(user.get(position));
    }

    @Override
    public int getItemCount() {
        return user.size();
    }

    class StoryViewHolder extends RecyclerView.ViewHolder{

        ItemContainerStoryBinding binding;

        StoryViewHolder(ItemContainerStoryBinding itemContainerStoryBinding) {
            super(itemContainerStoryBinding.getRoot());
            binding = itemContainerStoryBinding;
        }

        void setUserData(User user){
            binding.statusText.setText(user.name);
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
            binding.imageview.setImageBitmap(getUserImage(user.getImage()));
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


}
