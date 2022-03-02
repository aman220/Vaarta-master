package com.example.application;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chivorn.smartmaterialspinner.SmartMaterialSpinner;
import com.example.application.activities.BaseActivity;
import com.example.application.adapters.ChatAdapter;
import com.example.application.databinding.ActivityChatBinding;
import com.example.application.models.ChatMessage;
import com.example.application.models.User;
import com.example.application.network.ApiClient;
import com.example.application.network.ApiService;
import com.example.application.utilities.Constants;
import com.example.application.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {
    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;
    private final Handler handler = new Handler();
    private final Bundle bundleLangSet  = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        typingListeners();
        loadReceiverDetails();
        init();
        listenerMessage();
        initSpinner();
        binding.imageProfile.setImageBitmap(getBitmapFromEncodedString(receiverUser.image));
    }
    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
        binding.textName.setOnClickListener(v -> loadRecieverUserInfo());
        binding.translateMenu.setVisibility(View.GONE);
        binding.sendImageBtn.setOnClickListener(v -> {
            Bundle chatData = new Bundle();
            chatData.putString("receiverId", receiverUser.id);
            chatData.putString("receiverName", receiverUser.name);
            chatData.putString("receiverImage", receiverUser.image);
            chatData.putString("token",receiverUser.token);
            chatData.putString("conversionId", conversionId);

            Intent intent = new Intent(getApplicationContext(), SendImage.class);
            intent.putExtras(chatData);
            startActivity(intent);
        } );
        binding.imageInfo.setOnClickListener(v -> {
            if (binding.translateMenu.getVisibility() == View.GONE) {
                binding.translateMenu.setVisibility(View.VISIBLE);
            }else {
                binding.translateMenu.setVisibility(View.GONE);
            }
        });
        binding.chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                chatAdapter.getItemCount();
                if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
//
                }
            }
        });
    }

    private void swapTranslateLanguages() {
        String tempFromLang = preferenceManager.getString(Constants.FROM_LANGUAGE);
        preferenceManager.putString(Constants.FROM_LANGUAGE,preferenceManager.getString(Constants.TO_LANGUAGE));
        preferenceManager.putString(Constants.TO_LANGUAGE,tempFromLang);
        initSpinner();
        Toast.makeText(this, "Swapped", Toast.LENGTH_SHORT).show();
    }

    private void initSpinner() {
        String[] languageArray = {"Default","English", "African", "Arabic", "Belarusian", "Bulgarian", "Bengali",
                "Catalan", "Czech", "Welsh", "Hindi", "Urdu" };
        ArrayList<String> languageArrayList = new ArrayList<>();
        Collections.addAll(languageArrayList, languageArray);
        setSpinner(binding.spinnerFromLanguage, languageArrayList, "from");
        setSpinner(binding.spinnerToLanguage, languageArrayList, "to");
    }

    private void setSpinner(SmartMaterialSpinner<String> spinnerElement, ArrayList<String> languageArrayListTemp , String type) {
        spinnerElement.setItem(languageArrayListTemp);
        if (type.equals("from")) {
            spinnerElement.setSelection(languageArrayListTemp.indexOf(preferenceManager.getString(Constants.FROM_LANGUAGE)));
        }else if (type.equals("to")) {
            spinnerElement.setSelection(languageArrayListTemp.indexOf(preferenceManager.getString(Constants.TO_LANGUAGE)));
        }
        spinnerElement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if(type.equals("from")){
                    preferenceManager.putString(Constants.FROM_LANGUAGE, adapterView.getItemAtPosition(position).toString());
                    preferenceManager.putString(Constants.FROM_LANGUAGE_CODE, String.valueOf(getLanguageCode(adapterView.getItemAtPosition(position).toString())));
                }else if (type.equals("to")){
                    preferenceManager.putString(Constants.TO_LANGUAGE, adapterView.getItemAtPosition(position).toString());
                    preferenceManager.putString(Constants.TO_LANGUAGE_CODE, String.valueOf(getLanguageCode(adapterView.getItemAtPosition(position).toString())));
                }
                bundleLangSet.putInt("from",getLanguageCode(preferenceManager.getString(Constants.FROM_LANGUAGE)));
                bundleLangSet.putInt("to",getLanguageCode(preferenceManager.getString(Constants.TO_LANGUAGE)));
                chatAdapter = new ChatAdapter(
                        chatMessages,
                        getBitmapFromEncodedString(receiverUser.image),
                        preferenceManager.getString(Constants.KEY_USER_ID),
                        bundleLangSet,new ChatAdapter.OnItemClickListener(){
                    @Override
                    public void onItemClick(ChatMessage chatMessage) {
                        Toast.makeText(getApplicationContext(), "Clicked", Toast.LENGTH_SHORT).show();
                    }
                });
                binding.chatRecyclerView.setAdapter(chatAdapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { /* TODO document why this method is empty */ }
        });
    }

    public int getLanguageCode(String language){
        int languageCode;
        switch (language){
            case "English":
                languageCode= FirebaseTranslateLanguage.EN;
                break;
            case "Africans":
                languageCode= FirebaseTranslateLanguage.AF;
                break;
            case "Arabic":
                languageCode= FirebaseTranslateLanguage.AR;
                break;
            case "Belarusian":
                languageCode= FirebaseTranslateLanguage.BE;
                break;
            case "Bengali":
                languageCode= FirebaseTranslateLanguage.BN;
                break;
            case "Catalan":
                languageCode= FirebaseTranslateLanguage.CA;
                break;
            case "Czech":
                languageCode= FirebaseTranslateLanguage.CS;
                break;
            case "Welsh":
                languageCode= FirebaseTranslateLanguage.CY;
                break;
            case "Hindi":
                languageCode= FirebaseTranslateLanguage.HI;
                break;
            case "Urdu":
                languageCode= FirebaseTranslateLanguage.UR;
                break;
            default:
                languageCode=0;
        }
        return languageCode;
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        try {
            bundleLangSet.putInt("from",getLanguageCode(preferenceManager.getString(Constants.FROM_LANGUAGE)));
            preferenceManager.putString(Constants.FROM_LANGUAGE_CODE, String.valueOf(getLanguageCode(preferenceManager.getString(Constants.FROM_LANGUAGE))));
            bundleLangSet.putInt("to",getLanguageCode(preferenceManager.getString(Constants.TO_LANGUAGE)));
            preferenceManager.putString(Constants.TO_LANGUAGE_CODE, String.valueOf(getLanguageCode(preferenceManager.getString(Constants.TO_LANGUAGE))));
        }catch (Exception e){
            e.printStackTrace();
            bundleLangSet.putInt("from",0);
            bundleLangSet.putInt("to",0);

        }
        preferenceManager.putString(Constants.FROM_LANGUAGE, "Default");
        preferenceManager.putString(Constants.TO_LANGUAGE, "Default");
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID),
                bundleLangSet,
                new ChatAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(ChatMessage chatMessage) {
                        Toast.makeText(ChatActivity.this, "clicked", Toast.LENGTH_SHORT).show();
                    }
                });
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }
    private void sendMessage() {
        if (!(binding.inputMessage.getText().toString().trim().length() > 0)) {
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            binding.inputMessage.startAnimation(shake);
            Toast.makeText(this, "Empty message cant be sent", Toast.LENGTH_SHORT).show();
            return;
        }
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString().trim());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        message.put(Constants.IMAGE_URL, "");
        message.put(Constants.MEDIA_TYPE,"text");
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversionId != null) {
            updateConversion(binding.inputMessage.getText().toString().trim());
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.IMAGE_URL, "");
            conversion.put(Constants.MEDIA_TYPE,"text");
            conversion.put(Constants.KEY_RECEIVER_TOKEN, receiverUser.token);
            conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString().trim());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        if (!isReceiverAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);
                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_PCM_TOKEN, preferenceManager.getString(Constants.KEY_PCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString().trim());
                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
                sendNotification(body.toString());
            } catch (Exception exception) {
                showToast(exception.getMessage());
            }
        }
        binding.inputMessage.setText(null);
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showToast("Notification send successfully");
                } else {
                    showToast("Error:" + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }
    private void listenAvailabilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                receiverUser.token = value.getString(Constants.KEY_PCM_TOKEN);
                if (Constants.KEY_USER_STATUS == null){
                    receiverUser.status = "error";
                }else{
                    receiverUser.status =  value.getString(Constants.KEY_USER_STATUS);
                }
                if (receiverUser.image == null) {
                    receiverUser.image = value.getString(Constants.KEY_IMAGE);
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                }
            }
            if (isReceiverAvailable) {
                binding.textAvailability.setVisibility(View.VISIBLE);
            } else {
                binding.textAvailability.setVisibility(View.GONE);
            }
        });
    }
    private void listenerMessage() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.imageUrl = documentChange.getDocument().getString(Constants.IMAGE_URL);
                    chatMessage.mediaType = documentChange.getDocument().getString(Constants.MEDIA_TYPE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                    chatAdapter.notifyDataSetChanged();
                    try {
                        bundleLangSet.putInt("from", Integer.parseInt(preferenceManager.getString(Constants.FROM_LANGUAGE_CODE)));
                        bundleLangSet.putInt("to", Integer.parseInt(preferenceManager.getString(Constants.TO_LANGUAGE_CODE)));
                    }catch (Exception e){
                        bundleLangSet.putInt("from", 11);
                        bundleLangSet.putInt("to", 11);
                    }

                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversionId == null) {
            checkForConversion();
        }
    };
    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }

    }
    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }


    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }
    private void updateConversion(String message) {
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }
    private void checkForConversion() {
        if (chatMessages.size() != 0) {
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }
    private void checkForConversionRemotely(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };


    private void loadRecieverUserInfo() {
        Intent intent = new Intent(ChatActivity.this, Activity_user_profile.class);
        Bundle extras = new Bundle();
        extras.putString("token", receiverUser.token);
        extras.putString("userType", "other");
        intent.putExtras(extras);
        startActivity(intent);
    }


    private void typingListeners(){
        binding.inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* TODO document why this method is empty */ }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(binding.inputMessage.getText().toString().trim().length()>0) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            setStatus("typing|"+receiverUser.token);
                        }
                    }, 100);
                }
            }
            public void afterTextChanged(Editable s) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        setStatus("online");
                    }
                }, 3000);
            }
        });
    }

    private void setStatus(String status) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));

        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_USER_STATUS, status);
        documentReference.update(user)
                .addOnSuccessListener(unused -> {
                    preferenceManager.putString(Constants.KEY_USER_STATUS, status);
                })
                .addOnFailureListener(exception ->{
                    Toast.makeText(getApplicationContext(),"Failed to update status ",Toast.LENGTH_SHORT).show();
                });
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String metaStatus = receiverUser.status;
            if (metaStatus.indexOf("|") != -1) {
                String[] meta = metaStatus.split("\\|");
                if (meta[0].equals("typing")&&meta[1].equals(receiverUser.token)) {
                    binding.statusText.setText(meta[0]);
                }else{
                    binding.statusText.setText("online");
                }
                if (meta[0].equals("Last Seen ")) {
                    binding.statusText.setText(meta[0] + calculateTimeDifference(meta[1]));
                }
            }else {
                binding.statusText.setText(metaStatus);
            }

            handler.postDelayed(this, 100);
        }
    };

    private String calculateTimeDifference(String time){
        LocalDateTime startDate = null, endDate = null;
        Duration duration  = null;
        String result = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                Date temp  = new SimpleDateFormat("h:mm:ss a d/M/yy", Locale.US).parse(time);
                startDate =  temp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                endDate = LocalDateTime.now();
                duration = Duration.between(startDate, endDate);
                int num_seconds = (int) duration.getSeconds();
                int days = num_seconds / (60 * 60 * 24);
                num_seconds -= days * (60 * 60 * 24);
                int hours = num_seconds / (60 * 60);
                num_seconds -= hours * (60 * 60);
                int minutes = num_seconds / 60;
                if (duration.getSeconds()<25){
                    return "Just now";
                }else if( duration.getSeconds() / (60 * 60) > 3 && duration.getSeconds() / (60 * 60) < 24){
                    return new SimpleDateFormat("h:mma").format(temp);
                }else if (duration.getSeconds()<60 && duration.getSeconds()>=25){
                    return "few seconds ago";
                }else if (days == 1){
                    return "Yesterday " + new SimpleDateFormat("h:mma").format(temp);
                }else if (days<7&&days>1){
                    return "" + new SimpleDateFormat("EEE h:mma").format(temp);
                }else if (days == 7) {
                    return "a week ago";
                }else if (days > 40){
                    return "long time ago";
                }else if (days > 7) {
                    return "on "+new SimpleDateFormat("MMM-dd-yyyy").format(temp);
                }
                String hour = hours == 1 ? " hour " : " hours ";
                String minute = minutes == 1 ? " minute " : " minutes";

                result = (hours!=0?hours + hour :"") +( minutes!=0?minutes+ minute:"")+ " ago";
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(ChatActivity.this, "error dateParse "+e.getMessage(), Toast.LENGTH_SHORT).show();
                result = time;
            }
        }else {
            binding.statusText.setText(time);
        }
        return result;
    }
    @Override
    protected void onResume() {
        super.onResume();
        setStatus("online");
        handler.postDelayed(runnable, 1000);
        listenAvailabilityOfReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}