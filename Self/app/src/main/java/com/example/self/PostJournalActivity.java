package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.self.model.Journal;
import com.example.self.ui.JournalApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GALLERY_CODE = 1;
    //private static final String TAG = "PostJournalActivity";
    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView addPhotoButton;
    private EditText titleEditText;
    private EditText thoughtEditText;
    private TextView currentUserTextView;
    private TextView currentDateTextView;
    private ImageView postImage;
    private Uri imageUri;

    private String currentUserId;
    private String currentUsername;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //Connection to FireStore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private CollectionReference collectionReference = db.collection("Journal");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        currentDateTextView = findViewById(R.id.post_date_textView);
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.post_progressBar);
        titleEditText = findViewById(R.id.post_title_editText);
        thoughtEditText = findViewById(R.id.post_description_editText);
        currentUserTextView = findViewById(R.id.post_username_textView);
        saveButton = findViewById(R.id.post_save_journal_button);
        addPhotoButton = findViewById(R.id.postCameraButton);
        postImage = findViewById(R.id.post_imageView);

        saveButton.setOnClickListener(this);
        addPhotoButton.setOnClickListener(this);

        progressBar.setVisibility(View.INVISIBLE);

        if (JournalApi.getInstance() != null) {
            currentUserId = JournalApi.getInstance().getUserId();
            currentUsername = JournalApi.getInstance().getUsername();

            currentUserTextView.setText(currentUsername);
            currentDateTextView.setText((new Timestamp(System.currentTimeMillis())).toString());

        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();

                if(user != null){

                }else {

                }
            }
        };



    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.post_save_journal_button:
                //save journal
                saveJournal();
                break;
            case R.id.postCameraButton:
                //get image from gallery

                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                break;
        }

    }

    private void saveJournal() {

        final String title = titleEditText.getText().toString().trim();
        final String thought = thoughtEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        if(!TextUtils.isEmpty(title)
        &&!TextUtils.isEmpty(thought)
        &&imageUri != null) {

            final Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            //String test = timestamp.toString();
            //Log.d(TAG, test);

            final StorageReference filePath = storageReference
                    .child("journal_image")
                    .child("image"+ timestamp);


            filePath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();

                                    Journal journal = new Journal();
                                    journal.setTitle(title);
                                    journal.setThought(thought);
                                    journal.setImageUrl(imageUrl);
                                    journal.setTimeAdded(timestamp);
                                    journal.setUsername(currentUsername);
                                    journal.setUserId(currentUserId);

                                    //Invoke collection reference
                                    collectionReference.add(journal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressBar.setVisibility(View.INVISIBLE);

                                                    startActivity(new Intent(PostJournalActivity.this,
                                                            JournalListActivity.class));
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    //Log.d(TAG, "onFailure: " + e.getMessage());
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(PostJournalActivity.this,
                                    "Something went wrong.\nPlease try after sometime",
                                    Toast.LENGTH_LONG).show();
                            //Log.d(TAG, "onFailure: " + e.getMessage());
                        }
                    });
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(PostJournalActivity.this,
                    "Please fill all fields",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();//get the path of image
                postImage.setImageURI(imageUri);//set image
            }
        }
    }
}
