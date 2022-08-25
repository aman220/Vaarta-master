package com.example.application;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.application.databinding.ActivitySendImageBinding;
import com.example.application.utilities.Constants;
import com.example.application.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;

public class SendImage extends AppCompatActivity {


    private ActivitySendImageBinding binding;
    private PreferenceManager preferenceManager;
    private Uri filePath;
    private String fileName;
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

        binding.btnChoose.setOnClickListener(v -> SelectImage());

        binding.ProgressCircle.setOnClickListener(v -> UploadImage());

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
            fileName = data.getData().getLastPathSegment();
            try {
                Glide.with(binding.getRoot().getContext())
                        .load(filePath)
                        .into(binding.imgView);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                TextRecognizer txtRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray items = txtRecognizer.detect(frame);
                StringBuilder strBuilder = new StringBuilder();
                for (int i = 0; i < items.size(); i++) {
                    TextBlock item = (TextBlock) items.valueAt(i);
                    strBuilder.append(item.getValue());
                    binding.textShow.setText(strBuilder);
                }
            } catch (Exception e) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    binding.imagView.setImageBitmap(bitmap);

                }catch (Exception e1) {
                    e1.printStackTrace();

                }
            }

        }

    }


    private void UploadImage(){

        if(filePath != null)
        {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference documentReference=
                    database.collection(Constants.KEY_COLLECTION_USERS).document(
                            preferenceManager.getString(Constants.KEY_USER_ID)
                    );
            final ProgressDialog progressDialog = new ProgressDialog(this);
            Snackbar snack = Snackbar.make(binding.getRoot(), "Uploading...", Snackbar.LENGTH_INDEFINITE);
            snack.show();
            binding.ProgressCircle.show();
            Bundle data = getIntent().getExtras();
            String receiverId = data.getString("receiverId");
            StorageReference ref = storageReference.child("chats/media/images/"+ fileName);
            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            String profileImageUrl=task.getResult().toString();
                            PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
                            preferenceManager.putString(Constants.IMAGE_URL,profileImageUrl);
                            Toast.makeText(SendImage.this,profileImageUrl ,Toast.LENGTH_LONG).show();
                            HashMap<String, Object> message = new HashMap<>();
                            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                            message.put(Constants.KEY_RECEIVER_ID, receiverId);
                            message.put(Constants.KEY_MESSAGE, "");
                            message.put(Constants.IMAGE_URL, profileImageUrl);
                            message.put(Constants.MEDIA_TYPE,"image");
                            message.put(Constants.KEY_TIMESTAMP, new Date());
                            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
                        }
                    });
                    binding.ProgressCircle.beginFinalAnimation();
                    binding.ProgressCircle.hide();
                    snack.dismiss();
                    Snackbar.make(binding.ProgressCircle, "Cloud_upload_complete", Snackbar.LENGTH_LONG)
                            .setAction("Action", null)
                            .show();
                    finish();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            binding.ProgressCircle.beginFinalAnimation();
                            binding.ProgressCircle.hide();
                            snack.dismiss();
                            Toast.makeText(SendImage.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            snack.setText("Uploading "+getSize(taskSnapshot.getBytesTransferred())
                                    +"/"+ getSize(taskSnapshot.getTotalByteCount())
                                    +"\t"+progress+"%");
                        }
                    });
        }

    }
    public static String getSize(long size) {
        long kilo = 1024;
        long mega = kilo * kilo;
        long giga = mega * kilo;
        long tera = giga * kilo;
        String s = "";
        double kb = (double)size / kilo;
        double mb = kb / kilo;
        double gb = mb / kilo;
        double tb = gb / kilo;
        if(size < kilo) {
            s = size + " Bytes";
        } else if(size >= kilo && size < mega) {
            s =  String.format("%.2f", kb) + " KB";
        } else if(size >= mega && size < giga) {
            s = String.format("%.2f", mb) + " MB";
        } else if(size >= giga && size < tera) {
            s = String.format("%.2f", gb) + " GB";
        } else if(size >= tera) {
            s = String.format("%.2f", tb) + " TB";
        }
        return s;
    }
    private void setListerners(){

//        binding.imageSend.setOnClickListener(v -> sendMessage());
    }



}