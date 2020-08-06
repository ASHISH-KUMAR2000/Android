package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.self.ui.JournalApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //private static final String TAG = "MainActivity";
    //Start Screen field
    private Button startButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();

                if(user != null){
                    user = firebaseAuth.getCurrentUser();
                    final String userId = user.getUid();

                    collectionReference
                            .whereEqualTo("userId", userId)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                                    if (e != null){
                                        return;
                                    }

                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                            JournalApi journalApi = JournalApi.getInstance();
                                            journalApi.setUserId(userId);
                                            journalApi.setUsername(snapshot.getString("username"));

                                            //Log.d(TAG, " "+snapshot.getString("username")+" "+snapshot.getString("userId"));

                                            startActivity(new Intent(MainActivity.this,
                                                    JournalListActivity.class));
                                            finish();
                                        }
                                    }
                                }
                            });
                }
            }
        };

        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startButton:
                //Start Login Activity
                startActivity(new Intent(MainActivity.this,
                        LoginActivity.class));
                break;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (firebaseAuth != null)
            firebaseAuth.removeAuthStateListener(authStateListener);
    }
}
