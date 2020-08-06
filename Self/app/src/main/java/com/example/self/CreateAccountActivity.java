package com.example.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.self.ui.JournalApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {

    //private static final String TAG = "CreateAccountActivity";
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //FireStore Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");
    //Create Account Fields
    private EditText emailEditText;
    private EditText usernameEditText, passwordEditText;
    private Button createAcoountButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_acoount);

        emailEditText = findViewById(R.id.sign_up_email_id);
        usernameEditText = findViewById(R.id.sign_up_username);
        passwordEditText = findViewById(R.id.sign_up_email_password);
        createAcoountButton = findViewById(R.id.sign_up_create_button);
        progressBar = findViewById(R.id.sign_up_loading);

        firebaseAuth = FirebaseAuth.getInstance();

        createAcoountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();

                if(!TextUtils.isEmpty(email)
                &&!TextUtils.isEmpty(password)
                &&!TextUtils.isEmpty(username)) {
                    createUserEmailAccount(email, password, username);
                } else {
                    Toast.makeText(CreateAccountActivity.this,
                            "Empty Fields Not Allowed." ,
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                currentUser = firebaseAuth.getCurrentUser();

                if(currentUser != null) {
                    //user is already login
                }else {
                    //no user yet
                }
            }
        };

    }

    private  void createUserEmailAccount(final String email, final String password, final String username) {
        if (!TextUtils.isEmpty(email)
        &&!TextUtils.isEmpty(password)
        &&!TextUtils.isEmpty(username)){

            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        currentUser = firebaseAuth.getCurrentUser();
                        assert currentUser != null;
                        final String currentUserId = currentUser.getUid();

                        //Create a user Map so we can create a user in the User collection

                        Map<String, Object> userObj = new HashMap<>();

                        userObj.put("userId", currentUserId);
                        userObj.put("username", username);

                        //Save to fire store
                        collectionReference.add(userObj)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        documentReference.get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (Objects.requireNonNull(task.getResult().exists())) {
                                                            String name = task.getResult()
                                                                    .getString("username");

                                                            JournalApi journalApi = JournalApi.getInstance();//Global Api
                                                            journalApi.setUserId(currentUserId);
                                                            journalApi.setUsername(name);

                                                            Intent intent = new Intent(CreateAccountActivity.this,
                                                                    PostJournalActivity.class);
                                                            //intent.putExtra("username", name);
                                                            startActivity(intent);
                                                        } else {
                                                            //Log.d(TAG, "PostJournal Intent failed");
                                                        }
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //Log.d(TAG, "collectionReference.add(userObj) failed");
                                }
                        });



                    }else {
                        progressBar.setVisibility(View.INVISIBLE);
                        //Something went wrong
                        //Log.d(TAG, "onFailure: " +task);
                        Toast.makeText(CreateAccountActivity.this,
                                "Something went wrong !\nPlease try after sometime.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Log.d(TAG, "firebaseAuth.createUserWithEmailAndPassword failed");
                        }
                    });
        } else {
            Toast.makeText(CreateAccountActivity.this,
                    "Empty Fields Not Allowed",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
    }

}
