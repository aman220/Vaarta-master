package com.example.application.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.example.application.Activity_user_profile;
import com.example.application.AddStoryActivity;
import com.example.application.ChatActivity;
import com.example.application.R;
import com.example.application.adapters.RecentConversationAdapter;
import com.example.application.adapters.StoryAdapter;
import com.example.application.camera;
import com.example.application.databinding.ActivityMainBinding;
import com.example.application.listeners.ConversionListener;
import com.example.application.listeners.UserListener;
import com.example.application.models.ChatMessage;
import com.example.application.models.User;
import com.example.application.utilities.Constants;
import com.example.application.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends BaseActivity implements ConversionListener , UserListener {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private int REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1;
    private StoryAdapter StoryAdapter;
    private List<ChatMessage> conversations;
    private RecentConversationAdapter conversationsAdapter;
    private FirebaseFirestore database;
    BottomNavigationView bottomNavigationView;

    //hear is the bottom navigation bar of this activity

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
        getUsersSearched("");
        listenConversation();
        checkForBatteryOptimization();


        bottomNavigationView = findViewById(R.id.bottomNavigation);

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

    private void init() {
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationAdapter(
                conversations, this);
        binding.conversationRecycleView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
        conversationsAdapter.notifyDataSetChanged();
    }


    public void goProfile() {
        Intent intent = new Intent(MainActivity.this, Activity_user_profile.class);
        Bundle extras = new Bundle();
        extras.putString("token", preferenceManager.getString(Constants.KEY_PCM_TOKEN));
        extras.putString("userType", "self");
        intent.putExtras(extras);
        startActivity(intent);
    }

    private void setListerners() {
        binding.imageSignOut.setOnClickListener(v -> signout());
        binding.imageProfile.setOnClickListener(v -> goProfile());

    }


    private void loadUserDetails() {
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String meassage) {

        Toast.makeText(getApplicationContext(), meassage, Toast.LENGTH_SHORT).show();
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        preferenceManager.putString(Constants.KEY_PCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_PCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("unable to update token"));
    }

    private void signout() {
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_PCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), login.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Unable to sign out"));
    }

    @Override
    public void onConversionClicked(User user) {
        Toast.makeText(this, "conversationid", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

    public void onConversionLongClicked(int position) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Waring")
                .setMessage("Are you Sure You want to Delete This  Chat")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseFirestore database = FirebaseFirestore.getInstance();
                        DocumentReference documentReference =
                                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document();
                        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    conversations.remove(position);
                                    conversationsAdapter.notifyItemRemoved(position);
                                    dialog.dismiss();
                                } else
                                    Toast.makeText(MainActivity.this, "Unable To Delete", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).setNegativeButton("Cancel", null)
                .show();
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
                    conversationsAdapter.notifyDataSetChanged();
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
                builder.setTitle("warning");
                builder.setMessage("Battery optimization is enable It can interrupt running Background service ");
                builder.setPositiveButton("disable", (dialog, which) -> {
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
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.menu_home);
    }



    private void getUsersSearched(String s) {
        Loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    Loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }else if(queryDocumentSnapshot.getString(Constants.KEY_NAME).toLowerCase().contains(s.toLowerCase())) {
                                User user = new User();
                                user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                user.setImage(queryDocumentSnapshot.getString(Constants.KEY_IMAGE));
                                user.token = queryDocumentSnapshot.getString(Constants.KEY_PCM_TOKEN);
                                user.id = queryDocumentSnapshot.getId();
                                users.add(user);
                            }else{
                            }
                        }
                        if (users.size() > 0 ) {
                            binding.textErrorMessage.setVisibility(View.INVISIBLE);
                            StoryAdapter storyAdapter = new StoryAdapter(users , this);
                            binding.usersRecycleView.setAdapter(storyAdapter);
                            binding.usersRecycleView.setVisibility(View.VISIBLE);
                        }
                        else {
                            StoryAdapter storyAdapter = new StoryAdapter(users , this);
                            binding.usersRecycleView.setAdapter(storyAdapter);
                            binding.textErrorMessage.setText(String.format( "%s","No search result found"));
                            binding.textErrorMessage.setVisibility(View.VISIBLE);
                        }
                    }else{
                        binding.textErrorMessage.setText(String.format( "%s","No User available"));
                        binding.textErrorMessage.setVisibility(View.VISIBLE);
                    }
                });
    }


    private void Loading(Boolean isloading) {
        if (isloading) {
            binding.progressBar2.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar2.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent= new Intent(getApplicationContext(), AddStoryActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }

    public void onUserClicked2(User user) {
        Intent intent= new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }

    @Override
    public void initiateVideoMeeting(User user) {

    }

    @Override
    public void initiateAudioMeeting(User user) {

    }

}




