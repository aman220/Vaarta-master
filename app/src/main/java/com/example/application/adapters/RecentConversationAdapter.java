package com.example.application.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.application.databinding.ItemContainerRecentConversionBinding;
import com.example.application.listeners.ConversionListener;
import com.example.application.models.ChatMessage;
import com.example.application.models.User;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversionViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;


    public RecentConversationAdapter(List<ChatMessage> chatMessages,ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener=conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }


    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentConversionBinding binding;

        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding){
            super(itemContainerRecentConversionBinding.getRoot());
            binding =itemContainerRecentConversionBinding;



        }
        void setData(ChatMessage chatMessage){

            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.statusText.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(chatMessage.dateObject));
//            binding.statusText.setText(parseFormatDate("hh:mm a",chatMessage.dateTime));


            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id= chatMessage.conversionId;
                user.name=chatMessage.conversionName;
                user.setImage(chatMessage.conversionImage);
                conversionListener.onConversionClicked(user);

            });
            binding.getRoot().setOnLongClickListener(v ->{
                User user = new User();
                user.id= chatMessage.conversionId;
                conversionListener.onConversionLongClicked(getAdapterPosition(),user);
                return false;
            });
        }
    }

//    private static String parseFormatDate(String pattern,String dateTime){
//        String parsedTime = null;
//        try {
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//                Date fullTime = new SimpleDateFormat(" hh:mm a", Locale.US).parse(dateTime);
//                parsedTime = new SimpleDateFormat(pattern).format(fullTime);
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//            parsedTime = dateTime.substring(dateTime.length()-7);
//        }
//        return parsedTime;
//    }


    private Bitmap getConversionImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }





}

