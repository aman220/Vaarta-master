package com.example.application;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.HashMap;

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
        setListerners();
        loadReceiverDetails();
//        getUser();
    }


    private void loadReceiverDetails(){
        receiverUser=(User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.bio.setText(preferenceManager.getString(Constants.KEY_BIO));
        binding.inputphone.setText(preferenceManager.getString(Constants.KEY_PHONE));
        binding.textEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        binding.inputPassword.setText(preferenceManager.getString(Constants.KEY_PASSWORD));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        encodedImage = encodeImage(bitmap);
        binding.imageProfile.setImageBitmap(bitmap);
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
//                            binding.textAdd.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private String encodeImage (Bitmap bitmap){
//		int previewWidth = bitmap.getWidth() < 500 ? 600 : bitmap.getWidth();
        int size = 800;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap previewBitmap = getResizedBitmap(bitmap, size);
        do {
            previewBitmap = getResizedBitmap(bitmap, size);
            int quality = 0;
            do {
                previewBitmap.compress(Bitmap.CompressFormat.PNG, quality, byteArrayOutputStream);
                quality += 10;
                Log.d("UserUpdate", "Quality: " + quality);
                Log.d("UserUpdate", "Size: " + byteArrayOutputStream.toByteArray().length);
            } while (byteArrayOutputStream.size() > 1040000 && quality < 100);
            size -= 50;
        }while (byteArrayOutputStream.size() > 1040000);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
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
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));

        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME,binding.textName.getText().toString());
        user.put(Constants.KEY_EMAIL,binding.textEmail.getText().toString());
        user.put(Constants.KEY_PHONE,binding.inputphone.getText().toString());
        user.put(Constants.KEY_BIO,binding.bio.getText().toString());
        user.put(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE,encodedImage);
        documentReference.update(user)
                .addOnSuccessListener(unused -> {
                    preferenceManager.putString(Constants.KEY_NAME, binding.textName.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL, binding.textEmail.getText().toString());
                    preferenceManager.putString(Constants.KEY_PHONE, binding.inputphone.getText().toString());
                    preferenceManager.putString(Constants.KEY_BIO, binding.bio.getText().toString());
                    preferenceManager.putString(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), Activity_profile.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    loading(false);
                })
                .addOnFailureListener(exception ->{
                    loading(false);
                    Toast.makeText(getApplicationContext(),"Failed to update Profile ",Toast.LENGTH_SHORT).show();
                });
    }


    private void loading (Boolean isLoading){
        if(isLoading) {
            binding.buttonupdate.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonupdate.setVisibility(View.VISIBLE);
        }
    }


//    private void UserUpdate (){
//            FirebaseFirestore database = FirebaseFirestore.getInstance();
//            DocumentReference documentReference=
//                database.collection(Constants.KEY_COLLECTION_USERS).document(
//                        preferenceManager.getString(Constants.KEY_USER_ID)
//                );
//        documentReference.update(Constants.KEY_NAME, binding.textName.getText().toString());
//        documentReference.update(Constants.KEY_PHONE, binding.inputphone.getText().toString());
//        documentReference.update(Constants.KEY_EMAIL, binding.textEmail.getText().toString());
//        documentReference.update(Constants.KEY_BIO, binding.bio.getText().toString());
//        documentReference.update(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
//        documentReference.update(Constants.KEY_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
//
//        preferenceManager.putString(Constants.KEY_NAME, binding.textName.getText().toString());
//        preferenceManager.putString(Constants.KEY_PHONE, binding.inputphone.getText().toString());
//        preferenceManager.putString(Constants.KEY_BIO, binding.bio.getText().toString());
//        preferenceManager.putString(Constants.KEY_EMAIL, binding.textEmail.getText().toString());
//        preferenceManager.putString(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
//        preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
//        Toast.makeText(getApplicationContext(), "Profile Update Successfully", Toast.LENGTH_SHORT).show();
//    }

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
//        bottomNavigationView.setSelectedItemId(R.id.menu_account);
    }

}









