package com.example.application;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.application.activities.MainActivity;
import com.example.application.activities.UsersActivity;
import com.example.application.activities.login;
import com.example.application.databinding.ActivityUserUpdateBinding;
import com.example.application.models.User;
import com.example.application.utilities.Constants;
import com.example.application.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class UserUpdate extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private User receiverUser;
    private String encodedImage;
    private ActivityUserUpdateBinding binding;
    private FirebaseFirestore db;
    private Button updatebutton;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserUpdateBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        setListerners();
//        getUser();


        bottomNavigationView  = findViewById(R.id.bottomNavigation);
//        BottomNavigationView.setSelectedItemId(R.id.menu_account);
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

    }

    private void loadReceiverDetails(){
//        Log.d("Mesaggeeeee----", "loadReceiverDetails: succez till now");
//        String msg = (String) preferenceManager.getString(Constants.KEY_EMAIL) ;
//        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        receiverUser=(User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.bio.setText(preferenceManager.getString(Constants.KEY_BIO));
        binding.inputphone.setText(preferenceManager.getString(Constants.KEY_PHONE));
        binding.textEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        binding.inputPassword.setText(preferenceManager.getString(Constants.KEY_PASSWORD));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    public void goprofile(View view){
        Intent i = new Intent(this, OtpSendActivity.class);
        startActivity(i);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result ->{
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAdd.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private String encodeImage (Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight= bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void setListerners() {

        binding.buttonupdate.setOnClickListener(v -> UserUpdate());
        binding.imagedelete.setOnClickListener(v -> setDialog());
// this is for add image click and redirect to the image folder

        binding.textAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }


    //update user details in firebase


    private void UserUpdate (){
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference documentReference=
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_NAME, binding.textName.getText().toString());
        documentReference.update(Constants.KEY_PHONE, binding.inputphone.getText().toString());
        documentReference.update(Constants.KEY_EMAIL, binding.textEmail.getText().toString());
        documentReference.update(Constants.KEY_BIO, binding.bio.getText().toString());
        documentReference.update(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        documentReference.update(Constants.KEY_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));

        preferenceManager.putString(Constants.KEY_NAME, binding.textName.getText().toString());
        preferenceManager.putString(Constants.KEY_PHONE, binding.inputphone.getText().toString());
        preferenceManager.putString(Constants.KEY_BIO, binding.bio.getText().toString());
        preferenceManager.putString(Constants.KEY_EMAIL, binding.textEmail.getText().toString());
        preferenceManager.putString(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
        Toast.makeText(getApplicationContext(), "Profile Update Successfully", Toast.LENGTH_SHORT).show();
    }

    public void deletedata(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Profile Removed Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), login.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "unable to delete ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Waring")
                .setMessage("Are you Sure You want to Delete Your Profile")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletedata();
                        dialog.dismiss();
                    }
                }).setNegativeButton("cancel", null)
                .show();
    }

    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.menu_account);
    }

}









