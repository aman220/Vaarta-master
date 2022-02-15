package com.example.application.adapters;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.application.databinding.ItemContainerReceivedImageBinding;
import com.example.application.databinding.ItemContainerReceviedMessageBinding;
import com.example.application.databinding.ItemContainerSendImageBinding;
import com.example.application.databinding.ItemContainerSendMessageBinding;
import com.example.application.models.ChatMessage;
import com.example.application.models.ImageMessage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private final List<ChatMessage> chatMessages;
    private final List<ImageMessage> imageMessages;
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


    public ChatAdapter(List<ChatMessage> chatMessages,List<ImageMessage> imageMessages, Bitmap receiverProfileImage, String senderId, Bundle languageset) {
        this.chatMessages = chatMessages;
        this.imageMessages = imageMessages;
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
            ((ReceiverMessageViewHolder)holder).setData(chatMessages.get(position),receiverProfileImage,bundle);
//
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
            binding.textMessage.setText(chatMessage.message);
            Log.d("translatefrom",String.valueOf(fromLanguage));
            Log.d("translateto",String.valueOf(toLanguage));
             translateText(fromLanguage,toLanguage,chatMessage.message, binding.textMessage);
//            String parseTime = chatMessage.dateTime.substring(chatMessage.dateTime.length()-7);
            binding.textDateTime.setText(parseFormatDate("hh:mm a",chatMessage.dateTime));
        }
    }

    class ReceiverMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceviedMessageBinding binding;

        ReceiverMessageViewHolder(ItemContainerReceviedMessageBinding itemContainerReceviedMessageBinding){
            super(itemContainerReceviedMessageBinding.getRoot());
            binding=itemContainerReceviedMessageBinding;
        }

        void setData(ChatMessage chatMessage,Bitmap receiverProfileImage, Bundle languageset){
            binding.textMessage.setText(chatMessage.message);
            Bundle bundle=languageset;
            binding.textMessage.setText(chatMessage.message);
            Log.d("translatefrom",String.valueOf(fromLanguage));
            Log.d("translateto",String.valueOf(toLanguage));
            translateText(toLanguage,fromLanguage,chatMessage.message, binding.textMessage);
            int fromLanguage=bundle.getInt("from");
            int toLanguage=bundle.getInt("to");
            String parseTime = chatMessage.dateTime.substring(chatMessage.dateTime.length()-7);
            binding.textDateTime.setText(parseTime);
            if (receiverProfileImage!=null){
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }

    static class SentImageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerSendImageBinding binding;
        SentImageViewHolder(ItemContainerSendImageBinding itemContainerSendImageBinding){
            super(itemContainerSendImageBinding.getRoot());
            binding=itemContainerSendImageBinding;
        }
    }

    static class ReceivedImageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerReceivedImageBinding binding;

        ReceivedImageViewHolder(ItemContainerReceivedImageBinding itemContainerReceivedImageBinding){
            super(itemContainerReceivedImageBinding.getRoot());
            binding=itemContainerReceivedImageBinding;
        }
        void setData(ChatMessage chatMessage,Bitmap receiverProfileImage){
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
//            Log.d("ChatAdapter", "setData: "+e.getMessage());
            parsedTime = dateTime.substring(dateTime.length()-7);
        }
        return parsedTime;
    }
    private static void translateText(int fromLanguageCode, int toLanguageCode, String source, TextView textMessage) {
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        textMessage.setText(s);
//                        result = String.valueOf(s);

                        Log.d("ChatAdapter", "onSuccess: " + s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ChatAdapter", "onFailure: " + e.getMessage());
//                        Toast.makeText(MainActivity.this,"Fails to Translate "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ChatAdapter", "Translation Failure: " + e.getMessage());
//                Toast.makeText(MainActivity.this,"Fails to Download Lang Model "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
