package com.example.self.ui;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.self.R;
import com.example.self.model.Journal;
import com.squareup.picasso.Picasso;

import java.util.List;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {


    private Context contex;
    private List<Journal> journallist;

    public JournalRecyclerAdapter(Context contex, List<Journal> journallist) {
        this.contex = contex;
        this.journallist = journallist;
    }

    @NonNull
    @Override
    public JournalRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(contex)
                .inflate(R.layout.journal_row, parent, false);

        return new ViewHolder(view, contex);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalRecyclerAdapter.ViewHolder viewHolder, int position) {

        Journal journal = journallist.get(position);
        String imageUrl;

        viewHolder.title.setText(journal.getTitle());
        viewHolder.thoughts.setText(journal.getThought());
        imageUrl = journal.getImageUrl();


        //timeAdded time ago..
        //src = https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87

        String timeAgo =(String) DateUtils.getRelativeTimeSpanString(journal.getTimeAdded().getTime());

        viewHolder.dateAddedTextView.setText(timeAgo);

        //Use Picasso library to download and show image

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.secenry2)
                .fit()
                .into(viewHolder.image);
    }

    @Override
    public int getItemCount() {
        return journallist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView title, thoughts, dateAddedTextView;
        public ImageView shareButton;
        public ImageView image;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            contex = ctx;

            title = itemView.findViewById(R.id.journal_title_list);
            thoughts = itemView.findViewById(R.id.journal_thought_list);
            dateAddedTextView = itemView.findViewById(R.id.journal_timestamp_list);
            image = itemView.findViewById(R.id.journal_image_list);
            shareButton = itemView.findViewById(R.id.journal_row_share);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = "Title : "+ title.getText().toString().trim()
                            +"\nThought : "+thoughts.getText().toString().trim();
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Take a break!! \nBe Happy.");
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                    contex.startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }
            });
        }
    }

}
