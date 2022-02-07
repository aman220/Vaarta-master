package com.example.application.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.application.OtpSendActivity;
import com.example.application.databinding.SignupBinding;
import com.example.application.utilities.Constants;
import com.example.application.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private String encodedImage;
    private SignupBinding binding ;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListerners();
    }
// this is for Signup Button If details wass correct then Signup Button Works othervide Not

    private void setListerners() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.buttonsignUp.setOnClickListener(v -> {
            if (isValidSignUpDetails()){
                SignUp();
            }
        });

// this is for add image click and redirect to the image folder

        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    public void gologin(View view){
        Intent i = new Intent(this, login.class);
        startActivity(i);
    }

    //this used for print toast in a string

    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    //This is for the signup details send to firestore databse .
    private void SignUp(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME,binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL,binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD,binding.inputpassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user).addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(), OtpSendActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
        })
                .addOnFailureListener(exception ->{
                    loading(false);
                    showToast(exception.getMessage());
                });



    }

    // this is for image frame is equal to the given frame in signup page .

    private String encodeImage (Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight= bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    //  this is for add image in a sign up page ...?
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
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    //here is end

    // This is all About Enter details if details are correct order than signup successfull othervide show toast not succesfull
    // if else condition in every methord

    private Boolean isValidSignUpDetails(){
        if (encodedImage == null){
            showToast("Select Profile Picture");
            return false;
        }else if (binding.inputName.getText().toString().trim().isEmpty()){
            showToast("Enter Name ");
            return false;
        }else if (binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter Email");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter Valid Image");
            return false;
        }else if (binding.inputpassword.getText().toString().trim().isEmpty()) {
            showToast("Enter Password ");
            return false;
        }else if (binding.inputConfirmpassword.getText().toString().trim().isEmpty()){
            showToast("Confirm Your Password");
            return false;
        }else if (!binding.inputpassword.getText().toString().equals(binding.inputConfirmpassword.getText().toString())){
            showToast("Password & Confirm Password Must Be Same ");
            return false;
        }else {
            return true;
        }
    }

    // This is for Loading Button if we click on sigh up Loading will start

    private void loading (boolean isLoading){
        if (isLoading){
            binding.buttonsignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonsignUp.setVisibility(View.VISIBLE);
        }
    }
}


