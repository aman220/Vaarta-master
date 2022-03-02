package com.example.application.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.application.Activity_profile;
import com.example.application.ChatActivity;
import com.example.application.R;
import com.example.application.adapters.UsersAdapter;
import com.example.application.camera;
import com.example.application.databinding.ActivityUsersBinding;
import com.example.application.listeners.UserListener;
import com.example.application.models.User;
import com.example.application.utilities.Constants;
import com.example.application.utilities.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        setListerners();
        getUsersSearched("");


        bottomNavigationView  = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_search:
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
                        startActivity(new Intent(getApplicationContext()
                                , MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });


        EditText search=findViewById(R.id.search);
        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                getUsersSearched(search.getText().toString());
            }
        });


    }

    private void setListerners(){

        binding.imageBack.setOnClickListener(v -> onBackPressed());
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
                                user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                                user.setImage(queryDocumentSnapshot.getString(Constants.KEY_IMAGE));
                                user.token = queryDocumentSnapshot.getString(Constants.KEY_PCM_TOKEN);
                                user.id = queryDocumentSnapshot.getId();
                                users.add(user);
                            }else{
                            }
                        }
                        if (users.size() > 0 ) {
                            binding.textErrorMessage.setVisibility(View.INVISIBLE);
                            UsersAdapter usersAdapter = new UsersAdapter(users , this);
                            binding.usersRecycleView.setAdapter(usersAdapter);
                            binding.usersRecycleView.setVisibility(View.VISIBLE);
                        }
                        else {
                            UsersAdapter usersAdapter = new UsersAdapter(users , this);
                            binding.usersRecycleView.setAdapter(usersAdapter);
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
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }



    @Override
    public void onUserClicked(User user) {
        Intent intent= new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }

    @Override
    public void onUserClicked2(User user) {

    }


    @Override
    public void initiateVideoMeeting(User user) {
        if (user.token==null || user.token.trim().isEmpty()){
            Toast.makeText(
                    this,
                    user.name+"is not available for meeting ",
                    Toast.LENGTH_SHORT
            ).show();
        }else {
            Intent intent=new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
            intent.putExtra("user",user);
            intent.putExtra("type","video");
            startActivity(intent);
        }
    }

    @Override
    public void initiateAudioMeeting(User user) {
        if (user.token==null || user.token.trim().isEmpty()){
            Toast.makeText(
                    this,
                    user.name+"is not available for meeting ",
                    Toast.LENGTH_SHORT
            ).show();
        }else {
            Intent intent=new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
            intent.putExtra("user",user);
            intent.putExtra("type","audio");
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       MenuItem item=menu.findItem(R.id.search);
        SearchView searchView=(SearchView)item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                procresssearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                procresssearch(s);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void procresssearch(String s) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.menu_search);
    }
}