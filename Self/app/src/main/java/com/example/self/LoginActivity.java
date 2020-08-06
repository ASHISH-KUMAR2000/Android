package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.self.ui.JournalApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //private static final String TAG = "LoginActivity";

    //Login Activity Buttons
    private Button signInButton, signUpButton;
    private ProgressBar progressBar;

    //Login Activity Text Views
    private AutoCompleteTextView emailIdTextView;
    private EditText passwordTextView;

    //Firestore Connection
    private FirebaseAuth firebaseAuth;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        signInButton = findViewById(R.id.login_sign_in_button);
        signUpButton = findViewById(R.id.login_sign_up_button);

        emailIdTextView = findViewById(R.id.login_email_id_text);
        passwordTextView = findViewById(R.id.login_email_password_text);
        progressBar = findViewById(R.id.login_progressBar);

        firebaseAuth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(this);
        signInButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_sign_in_button:
                String username = emailIdTextView.getText().toString().trim();
                String password = passwordTextView.getText().toString().trim();
                loginEmailPassword(username, password);
                break;
            case R.id.login_sign_up_button:
                //Start Create Account Activity
                startActivity(new Intent(LoginActivity.this,
                        CreateAccountActivity.class));
                break;

        }
    }

    private void loginEmailPassword(final String username, final String password) {

        if(!TextUtils.isEmpty(username)
        &&!TextUtils.isEmpty(password)) {

            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                String currentUserId = user.getUid();

                                collectionReference
                                        .whereEqualTo("userId", currentUserId)
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                                @Nullable FirebaseFirestoreException e) {
                                                if (e != null) {
                                                    //Log.d(TAG, e.getMessage());
                                                } else {
                                                    //assert queryDocumentSnapshots != null;
                                                    if (!queryDocumentSnapshots.isEmpty()) {

                                                        //Log.d(TAG, username+" "+password);

                                                        for (QueryDocumentSnapshot snapshots : queryDocumentSnapshots) {
                                                            JournalApi journalApi = JournalApi.getInstance();
                                                            journalApi.setUsername(snapshots.getString("username"));
                                                            journalApi.setUserId(snapshots.getString("userId"));

                                                            progressBar.setVisibility(View.INVISIBLE);

                                                            //Go to PostJournalActivity
                                                            startActivity(new Intent(LoginActivity.this,
                                                                    PostJournalActivity.class));
                                                            finish();

                                                        }
                                                    } else {
                                                        //Log.d(TAG, "snapshot is empty");
                                                    }
                                                }
                                            }
                                        });
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(LoginActivity.this,
                                        "Please enter a registered email.\nOr Check your internet connection.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);

                            Toast.makeText(LoginActivity.this,
                                    "Enter  a valid email and password.\nOr try after sometime",
                                    Toast.LENGTH_LONG);

                            //Log.d(TAG, e.getMessage());
                        }
                    });

        } else {
            Toast.makeText(LoginActivity.this,
                    "Enter  a valid email and password",
                    Toast.LENGTH_LONG).show();
        }
    }
}
