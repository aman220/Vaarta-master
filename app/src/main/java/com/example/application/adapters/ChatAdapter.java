package com.example.application.adapters;

import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.application.R;
import com.example.application.databinding.ItemContainerReceviedMessageBinding;
import com.example.application.databinding.ItemContainerSendMessageBinding;
import com.example.application.models.ChatMessage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {



    private final List<ChatMessage> chatMessages;
    private Bitmap receiverProfileImage;
    private final String senderId;
    private final int fromLanguage;
    private final int toLanguage;
    private final Bundle bundle;
    private static final int VIEW_TYPE_SENT =1;
    private static final int VIEW_TYPE_RECEIVED=2;

    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage=bitmap;
    }


    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId, Bundle languageset) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        bundle=languageset;
        fromLanguage=bundle.getInt("from");
        toLanguage=bundle.getInt("to");
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSendMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }else {
            return new ReceiverMessageViewHolder(
                    ItemContainerReceviedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position)==VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));

        }else {
            ((ReceiverMessageViewHolder)holder).setData(chatMessages.get(position),receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }
    static int i=0;
    class SentMessageViewHolder extends RecyclerView.ViewHolder{


        private final ItemContainerSendMessageBinding binding;
        SentMessageViewHolder(ItemContainerSendMessageBinding itemContainerSendMessageBinding){
            super(itemContainerSendMessageBinding.getRoot());
            binding=itemContainerSendMessageBinding;
        }

        void setData(ChatMessage chatMessage){
            if (chatMessage.mediaType.equals("text")){
                binding.textMessage.setText(chatMessage.message);
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.imageMessage.setVisibility(View.GONE);
            }else if(chatMessage.mediaType.equals("image")){
                Glide.with(binding.getRoot().getContext())
                        .load(chatMessage.imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_noun_broken_image)
                        .into(binding.imageMessage);
                binding.imageMessage.setVisibility(View.VISIBLE);
                binding.textMessage.setVisibility(View.GONE);
                binding.imageMessage.setScaleType(ImageView.ScaleType.FIT_XY);

            }
            binding.textDateTime.setText(parseFormatDate("hh:mm a",chatMessage.dateTime));
        }
    }
    class ReceiverMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceviedMessageBinding binding;

        ReceiverMessageViewHolder(ItemContainerReceviedMessageBinding itemContainerReceviedMessageBinding){
            super(itemContainerReceviedMessageBinding.getRoot());
            binding=itemContainerReceviedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage){
            if (chatMessage.mediaType.equals("text")){
                translateText(fromLanguage,toLanguage,chatMessage.message, binding.textMessage);
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.imageMessage.setVisibility(View.GONE);
            }else if(chatMessage.mediaType.equals("image")){
                Glide.with(binding.getRoot().getContext())
                        .load(chatMessage.imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_noun_broken_image)
                        .into(binding.imageMessage);
                binding.imageMessage.setScaleType(ImageView.ScaleType.FIT_XY);
                binding.imageMessage.setVisibility(View.VISIBLE);
                binding.textMessage.setVisibility(View.GONE);
            }
            binding.textDateTime.setText(parseFormatDate("hh:mm a",chatMessage.dateTime));
            if (receiverProfileImage!=null){
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }
    static private String parseFormatDate(String pattern,String dateTime){
        String parsedTime = null;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Date fullTime = new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.US).parse(dateTime);
                parsedTime = new SimpleDateFormat(pattern).format(fullTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            parsedTime = dateTime.substring(dateTime.length()-7);
        }
        return parsedTime;
    }


    private static void translateText(int fromLanguageCode, int toLanguageCode, String source, TextView textMessage){
        FirebaseTranslatorOptions options= new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();

        FirebaseTranslator translator= FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions= new FirebaseModelDownloadConditions.Builder()
                .build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        if (s!=source){
                            textMessage.setVisibility(View.VISIBLE);
                        }
                        textMessage.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }
}