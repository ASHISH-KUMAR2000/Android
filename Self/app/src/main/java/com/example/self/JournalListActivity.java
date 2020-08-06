package com.example.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.self.model.Journal;
import com.example.self.ui.JournalApi;
import com.example.self.ui.JournalRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class JournalListActivity extends AppCompatActivity {


    //private static final String TAG = "JournalListActivity";
    //Connecting to Firestore
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    private List<Journal> journalList;
    private RecyclerView recyclerView;
    private JournalRecyclerAdapter journalRecyclerAdapter;

    private CollectionReference collectionReference = db.collection("Journal");
    private TextView noJournalEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        noJournalEntry = findViewById(R.id.list_empty);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_action_add:
                //Go to Journal List Activity
                if (user != null && firebaseAuth != null) {
                    startActivity(new Intent(JournalListActivity.this,
                            PostJournalActivity.class));
                }
                break;
            case R.id.menu_action_signout:
                //Signout user and goto Main Activity
                if( user != null && firebaseAuth != null) {
                    firebaseAuth.signOut();
                    JournalApi journalApi = JournalApi.getInstance();
                    journalApi.setUsername(null);
                    journalApi.setUserId(null);

                    startActivity(new Intent(JournalListActivity.this,
                            MainActivity.class));
                    finish();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        journalList = new ArrayList<>();
        collectionReference.whereEqualTo("userId", JournalApi.getInstance()
                .getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for(QueryDocumentSnapshot journals : queryDocumentSnapshots) {
                                //Log.d(TAG, " "+journals.getData().get("timeAdded"));

                                //journals.toObject
                                String title, thought, imageUrl , userId;
                                String username;
                                Timestamp timeAdded;

                                title = (String) journals.get("title");
                                thought = (String) journals.get("thought");
                                imageUrl = (String) journals.get("imageUrl");
                                userId = (String) journals.get("userId");
                                username = (String) journals.get("username");

                                //To convert firestore.timestamp to sql.timestamp
                                long val =  journals.getTimestamp("timeAdded").getSeconds()*1000;
                                timeAdded = new Timestamp(val);



                                Journal journal = new Journal(title,
                                        thought,
                                        imageUrl,
                                        userId,
                                        username,
                                        timeAdded);
                                journalList.add(journal);
                            }

                            Collections.sort(journalList, new Comparator<Journal>() {
                                @Override
                                public int compare(Journal o1, Journal o2) {
                                    try {
                                        //sorting in ascending order
                                        return o2.getTimeAdded().compareTo(o1.getTimeAdded());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        return 0;
                                    }
                                }
                            });

                            //Invoke recycler View
                            journalRecyclerAdapter = new JournalRecyclerAdapter(JournalListActivity.this,
                                    journalList);
                            recyclerView.setAdapter(journalRecyclerAdapter);
                            journalRecyclerAdapter.notifyDataSetChanged();
                        } else {
                            //Log.d(TAG, JournalApi.getInstance().getUserId()+" "+JournalApi.getInstance().getUsername());
                            noJournalEntry.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


    }
}
