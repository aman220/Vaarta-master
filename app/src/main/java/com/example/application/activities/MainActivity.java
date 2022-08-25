package com.example.application.activities;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.application.Activity_profile;
import com.example.application.Activity_send_video;
import com.example.application.ChatActivity;
import com.example.application.R;
import com.example.application.adapters.RecentConversationAdapter;
import com.example.application.camera;
import com.example.application.databinding.ActivityMainBinding;
import com.example.application.listeners.ConversionListener;
import com.example.application.models.ChatMessage;
import com.example.application.models.User;
import com.example.application.utilities.Constants;
import com.example.application.utilities.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends BaseActivity implements ConversionListener
{
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private int REQUEST_CODE_BATTERY_OPTIMIZATIONS=1;


    private List<ChatMessage> conversations;
    private RecentConversationAdapter conversationsAdapter;
    private FirebaseFirestore database;

    //hear is the bottom navigation bar of this activity
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();
        getToken();
        init();
        setListerners();
        listenConversation();
        checkForBatteryOptimization();

        bottomNavigationView = findViewById(R.id.bottomNavigation);

//        bottomNavigationView.setSelectedItemId(R.id.menu_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_search:
                        startActivity(new Intent(getApplicationContext()
                                , UsersActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.menu_account:
                        startActivity(new Intent(getApplicationContext()
                                , Activity_profile.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.menu_music:
                        startActivity(new Intent(getApplicationContext()
                                , camera.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.menu_home:
                        return true;
                }
                return false;
            }
        });
    }

    private  void init(){
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationAdapter(conversations,this);
        binding.conversationRecycleView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }


    public void goProfile(){
        Intent intent=new Intent(MainActivity.this, Activity_send_video.class);
//        Bundle extras = new Bundle();
//        extras.putString("token",preferenceManager.getString(Constants.KEY_PCM_TOKEN));
//        extras.putString("userType","self");
//        intent.putExtras(extras);
        startActivity(intent);
    }

    private void setListerners(){
        binding.imageSignOut.setOnClickListener(v -> signout());
        binding.imageProfile.setOnClickListener(v -> goProfile());
        binding.textName.setOnClickListener(v -> goProfile());
    }


    private void loadUserDetails() {
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String meassage) {
        Toast.makeText(getApplicationContext(),meassage,Toast.LENGTH_SHORT).show();
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_PCM_TOKEN,token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference=
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_PCM_TOKEN , token)
//                .addOnSuccessListener(unused -> showToast("Token Update successfully"))
                .addOnFailureListener(e ->showToast("unable to update token"));
    }

    private void signout(){
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String,Object> updates = new HashMap<>();
        updates.put(Constants.KEY_PCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused->{
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), login.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Unable to sign out, Closing forcefully");
                    try{
                        ((ActivityManager)getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
                    }catch (Exception e1){
                        showToast("Unable to clear data");
                    }
                });
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent=new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }

    @Override
    public void onConversionLongClicked(int position, User user) {

    }

    private void listenConversation(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }






    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null) {
            return;
        }
        if(value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    } else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                }else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)) {
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations,(obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationRecycleView.smoothScrollToPosition(0);
            binding.conversationRecycleView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };


    private void checkForBatteryOptimization(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Recommendation!");
                builder.setMessage("Battery optimization is enable It can interrupt running Background service, please consider disabling it.");
                builder.setPositiveButton("Disable Now", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivityForResult(intent , REQUEST_CODE_BATTERY_OPTIMIZATIONS);
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.create().show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_BATTERY_OPTIMIZATIONS);
        checkForBatteryOptimization();
    }
    @Override
    public void onResume(){
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.menu_home);
    }
}