package com.example.application;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.application.activities.BaseActivity;
import com.example.application.adapters.ChatAdapter;
import com.example.application.databinding.ActivityTranslateCallBinding;
import com.example.application.models.ChatMessage;
import com.example.application.models.User;
import com.example.application.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TranslateCall extends BaseActivity {

    private String txtSpeechInput;
    private ImageButton btnSpeak;
    private User receiverUser;
    private FirebaseFirestore database;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private ActivityTranslateCallBinding binding;
    TextToSpeech textToSpeech;
    private int  toLanguage;
    private String conversionId = null;

    private Integer  lang_code;
    private PreferenceManager preferenceManager;
    private final int REQ_CODE_SPEECH_INPUT = 100;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTranslateCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadReceiverDetails();
        setSpinner();
        binding.btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
        binding.btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translateText(1,lang_code ,binding.txtSpeechInput.getText().toString() ,binding.translatedOutput);
                textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {

                        // if No error is found then only it will run
                        if(i!= TextToSpeech.ERROR){
                            // To Choose language of speech
                            textToSpeech.setLanguage(Locale.UK);
                            textToSpeech.speak(binding.translatedOutput.getText().toString(), TextToSpeech.QUEUE_FLUSH, null,null);

                        }

                    }
                });

            }
        });

        binding.imageRejectInvitation.setOnClickListener(v ->
        {
            Toast.makeText(this, "Call Cut", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, receiverUser.id, Toast.LENGTH_SHORT).show();
        });

    }


    private void setSpinner(){
        String[] languageArray = {"Default","English", "African", "Arabic", "Belarusian", "Bulgarian", "Bengali",
                "Catalan", "Czech", "Welsh", "Hindi", "Urdu", "Spanish",
                "French",
                "Italian",
                "Japanese",
                "Russian",
                "Tamil",
                "Chinese",
                "Marathi" };
        ArrayList<String> languageArrayList = new ArrayList<>();
        Collections.addAll(languageArrayList, languageArray);
        binding.spinnerToLanguage.setSelection(languageArrayList.indexOf(0));
        binding.spinnerToLanguage.setItem(languageArrayList);

        binding.spinnerToLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String native_lang=adapterView.getItemAtPosition(position).toString();
                lang_code=getLanguageCode(native_lang);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(TranslateCall.this, "Select target Language ", Toast.LENGTH_SHORT).show();

            }
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
            case "German":
                languageCode= FirebaseTranslateLanguage.DE;
                break;
            case "Spanish":
                languageCode= FirebaseTranslateLanguage.ES;
                break;
            case "French":
                languageCode= FirebaseTranslateLanguage.FR;
                break;
            case "Italian":
                languageCode= FirebaseTranslateLanguage.IT;
                break;
            case "Japanese":
                languageCode= FirebaseTranslateLanguage.JA;
                break;
            case "Russian":
                languageCode= FirebaseTranslateLanguage.RU;
                break;
            case "Tamil":
                languageCode= FirebaseTranslateLanguage.TA;
                break;
            case "Chinese":
                languageCode= FirebaseTranslateLanguage.ZH;
                break;
            case "Marathi":
                languageCode= FirebaseTranslateLanguage.MR;
                break;
            default:
                languageCode=0;
        }
        return languageCode;
    }

//    public int getSpeekLanguageCode(String language){
//        int languageCode;
//        switch (language){
//            case "English":
//                languageCode= textToSpeech.setLanguage(Locale.UK);
//                break;
//            case "Africans":
//                languageCode= FirebaseTranslateLanguage.AF;
//                break;
//
//            default:
//                languageCode=0;
//        }
//        return languageCode;
//    }

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


    private void loadReceiverDetails() {
        User user = (User) getIntent().getSerializableExtra("user");
        if (user != null) {
           binding.imageProfile.setImageBitmap(getBitmapFromEncodedString(user.image));
           binding.textUsername.setText(String.format("%s", user.name));
        }
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }





    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        binding.translatedOutput.setText("");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    binding.txtSpeechInput.setText(result.get(0));
                    txtSpeechInput = result.get(0);
                    if (txtSpeechInput != null) {
                        Toast.makeText(this, result.get(0), Toast.LENGTH_SHORT).show();

                    }
                    else
                    {
                        Toast.makeText(TranslateCall.this, "Empty", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }

        }
        translateText(1,lang_code ,binding.txtSpeechInput.getText().toString() ,binding.translatedOutput);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {


                if(i!= TextToSpeech.ERROR){
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    textToSpeech.speak(binding.translatedOutput.getText().toString(), TextToSpeech.QUEUE_FLUSH, null,null);

                }

            }
        });
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}