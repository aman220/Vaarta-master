package com.example.application.adapters;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.application.ChatActivity;
import com.example.application.PopupMenuCustomLayout;
import com.example.application.R;
import com.example.application.databinding.ItemContainerReceviedMessageBinding;
import com.example.application.databinding.ItemContainerSendMessageBinding;
import com.example.application.models.ChatMessage;
import com.example.application.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.jsibbold.zoomage.ZoomageView;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import me.mutasem.simplevoiceplayer.PlayerView;

public class ChatAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder > {


    private final ChatActivity chatActivity;
    private final List<ChatMessage> chatMessages;
    private Bitmap receiverProfileImage;
    private final String senderId;
    private View.OnClickListener onItemClickListener;
    private final int fromLanguage;
    private final int toLanguage;
    private final Bundle bundle;
    private static final int VIEW_TYPE_SENT =1;
    private static final int VIEW_TYPE_RECEIVED=2;
    private ViewGroup parentView;
    PlayerView playerView;
    VideoView videoView;

    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage=bitmap;
    }


    public ChatAdapter(ChatActivity chatActivity,List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId, Bundle languageset, OnItemClickListener onItemClickListener) {
        this.chatMessages = chatMessages;
        this.chatActivity = chatActivity;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        bundle=languageset;
        fromLanguage=bundle.getInt("from");
        toLanguage=bundle.getInt("to");
    }

    public interface OnItemClickListener {
        void onItemClick(ChatMessage chatMessage);
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
            binding.getRoot().setTag(this);
            binding.getRoot().setOnClickListener(onItemClickListener);
        }


        void setData(ChatMessage chatMessage){
            if (chatMessage.mediaType==null){
                if (chatMessage.imageUrl.equals("")){
                    chatMessage.mediaType = "text";
                }else{
                    chatMessage.mediaType = "image";
                }
                if (chatMessage.mediaType != "text" && chatMessage.mediaType != "image"){
                    chatMessage.mediaType ="audio";
                }else {
                    chatMessage.mediaType="video";
                }
            }

            if (chatMessage.mediaType.equals("text")){
                binding.textMessage.setText(chatMessage.message);
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.imageMessage.setVisibility(View.GONE);
                binding.audioMessage.setVisibility(View.GONE);
            }else if(chatMessage.mediaType.equals("image")){
                Glide.with(binding.getRoot().getContext())
                        .load(chatMessage.imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_noun_broken_image)
                        .into(binding.imageMessage);
                binding.imageMessage.setVisibility(View.VISIBLE);
                binding.textMessage.setVisibility(View.GONE);
                binding.imageMessage.setScaleType(ImageView.ScaleType.FIT_XY);
                binding.imageMessage.setOnClickListener(v -> {
                    fullScreenImagePopup(binding.getRoot().getContext(),chatMessage.imageUrl);
                });
            }else if (chatMessage.mediaType.equals("audio")){
                playerView=binding.audioMessage;
                String fileUrl = chatMessage.audioUrl;
                playerView.setAudio(Uri.parse(fileUrl));
//                Toast.makeText(chatActivity, chatMessage.audioUrl, Toast.LENGTH_SHORT).show();
                binding.audioMessage.setVisibility(View.VISIBLE);
                binding.imageMessage.setVisibility(View.GONE);
                binding.textMessage.setVisibility(View.GONE);
            }else if (chatMessage.mediaType.equals("video")){

                String videoUrl = "https://firebasestorage.googleapis.com/v0/b/chatting-application-375a1.appspot.com/o/Media%2FVideo%2Fvideos1660464047954.mp4?alt=media&token=f42bbabf-0200-44e8-9d09-c919220ac2cf";
                Uri uri = Uri.parse(videoUrl);
                binding.videoMessage.setVideoURI(uri);
                videoView=binding.videoMessage;
                videoView.start();
                videoView.setVisibility(View.VISIBLE);
                binding.videoMessage.setVisibility(View.VISIBLE);

                binding.audioMessage.setVisibility(View.GONE);
                binding.imageMessage.setVisibility(View.GONE);
                binding.textMessage.setVisibility(View.GONE);
            }


            binding.getRoot().setOnClickListener(v -> {
            });

            binding.getRoot().setOnLongClickListener(v -> {
                return chatPopupMenu(binding.getRoot(), "send",chatMessage);
            });
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
            if (chatMessage.mediaType==null){
                if (chatMessage.imageUrl.equals("")){
                    chatMessage.mediaType = "text";
                }else{
                    chatMessage.mediaType = "image";
                }
                if (chatMessage.mediaType!="text" && chatMessage.mediaType!="image"){
                    chatMessage.mediaType ="audio";
                }
            }
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

                binding.imageMessage.setOnClickListener(v -> {
                    fullScreenImagePopup(binding.getRoot().getContext(),chatMessage.imageUrl);
                });
            }else if (chatMessage.mediaType.equals("audio")){
                playerView=binding.audioMessage;
                String fileUrl = chatMessage.audioUrl;
                playerView.setAudio(Uri.parse(fileUrl));
//                Toast.makeText(chatActivity, chatMessage.audioUrl, Toast.LENGTH_SHORT).show();
                binding.audioMessage.setVisibility(View.VISIBLE);
                binding.imageMessage.setVisibility(View.GONE);
                binding.textMessage.setVisibility(View.GONE);
            }



            binding.getRoot().setOnLongClickListener(v -> {
                return chatPopupMenu(binding.getRoot(), "receive",chatMessage);
            });
            binding.textDateTime.setText(parseFormatDate("hh:mm a",chatMessage.dateTime));
            if (receiverProfileImage!=null){
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }


        static private void fullScreenImagePopup(Context thisContext, String imageUrl){
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
        Glide.with(thisContext)
                .load(imageUrl)
                .placeholder(R.drawable.ic_noun_loading_image)
                .into(imageView);
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
    }




    private Boolean chatPopupMenu(View view, String messageType,ChatMessage chatMessage) {
        PopupMenuCustomLayout popupMenu = new PopupMenuCustomLayout(
                view.getContext(), R.layout.message_popup_menu,
                new PopupMenuCustomLayout.PopupMenuCustomOnClickListener() {
                    @Override
                    public void onClick(int itemId) {
                        switch (itemId) {
                            case R.id.popup_menu_custom_item_copy:
                                copyToClipboard(view.getContext(),chatMessage);
                                break;
                            case R.id.popup_menu_custom_item_delete:
                                deleteMessage(view,chatMessage);
                                break;
                            case R.id.popup_menu_custom_item_edit:
                                editMessage(view,chatMessage);
                                break;
                        }
                    }
                });

        if (messageType.equals("receive")){
            popupMenu.setMenuItemVisibility(1,false);
            popupMenu.setPadding(0,30,30,30,30);
        }
        View childMessage = ((ViewGroup) view).getChildAt(1);
        popupMenu.show( childMessage, Gravity.START, 0, 130);
        return false;
    }


    private void copyToClipboard(Context context, ChatMessage chatMessage) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("message", chatMessage.message);
        Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show();
        clipboard.setPrimaryClip(clip);
    }


    private void deleteMessage(View view, ChatMessage chatMessage) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CHAT).document(
                        chatMessage.messageId
                );
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    view.setVisibility(View.GONE);
//                    ChatAdapter.notifyDataSetChanged();
//                    chatActivity.chatAdapter.notifyDataSetChanged();
//                    chatActivity.binding.chatRecyclerView.scrollToPosition(chatActivity.chatAdapter.getItemCount()-1);
                }else{
                    Toast.makeText(view.getContext(), "unable to delete ", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void editMessage(View view, ChatMessage chatMessage) {
        chatActivity.binding.editMessageSnack.setVisibility(View.VISIBLE);
        String originalMessage = chatMessage.message;
        chatActivity.binding.inputMessage.setText(originalMessage);
        chatActivity.binding.inputMessage.setSelection(originalMessage.length());
        chatActivity.binding.layoutEdit.setVisibility(View.VISIBLE);
//        chatActivity.binding.layoutSend.setVisibility(View.GONE);
        chatActivity.binding.inputMessage.requestFocus();
        chatActivity.binding.removeEditSnack.setOnClickListener(v -> {
            chatActivity.binding.editMessageSnack.setVisibility(View.GONE);
            chatActivity.binding.layoutEdit.setVisibility(View.GONE);
            chatActivity.binding.layoutSend.setVisibility(View.VISIBLE);
            chatActivity.binding.inputMessage.setText(null);
            chatActivity.binding.inputMessage.clearFocus();
        });
        chatActivity.binding.layoutEdit.setOnClickListener(v -> {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CHAT).document(chatMessage.messageId);
            HashMap<String, Object> chat = new HashMap<>();
            chat.put(Constants.KEY_SENDER_ID, chatMessage.senderId);
            chat.put(Constants.KEY_RECEIVER_ID, chatMessage.receiverId);
            chat.put(Constants.KEY_MESSAGE, chatActivity.binding.inputMessage.getText().toString().trim());
            chat.put(Constants.KEY_TIMESTAMP, new Date());
            chat.put(Constants.IMAGE_URL, "");
            chat.put(Constants.MEDIA_TYPE,"text");
            documentReference.update(chat)
                    .addOnSuccessListener(unused -> {
                        chatActivity.binding.editMessageSnack.setVisibility(View.GONE);
                        chatActivity.binding.layoutEdit.setVisibility(View.GONE);
                        chatActivity.binding.layoutSend.setVisibility(View.VISIBLE);
                        chatActivity.binding.inputMessage.setText(null);
                        chatActivity.binding.inputMessage.clearFocus();
                    })
                    .addOnFailureListener(exception ->{
                        Toast.makeText(view.getContext(),"Failed to update Profile "+ exception.getMessage(),Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private static String parseFormatDate(String pattern,String dateTime){
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