package com.example.application;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.application.databinding.ActivityChatBinding;
import com.example.application.databinding.ActivitySendImageBinding;
import com.example.application.models.User;
import com.example.application.utilities.Constants;
import com.example.application.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

public class SendImage extends AppCompatActivity {



    private ActivitySendImageBinding binding;
    private PreferenceManager preferenceManager;
    private Uri filePath;
    private User receiver;
    private ActivityChatBinding chat;
    FirebaseStorage storage;
    StorageReference storageReference;

    private final int PICK_IMAGE_REQUEST = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendImageBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        binding.btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });

        binding.sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadImage();
            }
        });

    }

    private void SelectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,
                resultCode,
                data);

        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                binding.imgView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void UploadImage() {
        Toast.makeText(this, "haa pahuch rha hai", Toast.LENGTH_SHORT).show();
        if (filePath != null) {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference documentReference =
                    database.collection(Constants.KEY_COLLECTION_USERS).document(
                            preferenceManager.getString(Constants.KEY_USER_ID)
                    );
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            Bundle data = getIntent().getExtras();
            String receiverId = data.getString("receiverId");
            String receiverName = data.getString("receiverName");
            String receiverImage = data.getString("receiverImage");
            String token = data.getString("token");
            String conversionId = data.getString("conversionId");
            StorageReference ref = storageReference.child("images/" + documentReference.toString());
            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {

                        public void onComplete(@NonNull Task<Uri> task) {
                            String profileImageUrl=task.getResult().toString();
                            PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
                            preferenceManager.putString(Constants.IMAGE_URL,profileImageUrl);
                            Toast.makeText(SendImage.this,profileImageUrl ,Toast.LENGTH_LONG).show();
                            HashMap<String, Object> message = new HashMap<>();
                            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                            message.put(Constants.KEY_RECEIVER_ID,receiverId);
                            message.put(Constants.KEY_MESSAGE, "");
                            message.put(Constants.IMAGE_URL, profileImageUrl);
                            message.put(Constants.MEDIA_TYPE,"image");
                            message.put(Constants.KEY_TIMESTAMP, new Date());
                            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
                            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                            startActivity(intent);
                        }
                    });
                    Toast.makeText(SendImage.this, "Uploaded", Toast.LENGTH_SHORT).show();
                }

            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(SendImage.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }



}