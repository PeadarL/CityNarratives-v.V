package com.example.peter.citynarrativesvv;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class VisitedAdapter extends RecyclerView.Adapter<VisitedAdapter.ViewHolder> {

    private final Context context;
    private LayoutInflater inflater;
    private ArrayList<NarrativeLocation> visitedLocations;

    public VisitedAdapter(Context context, ArrayList<NarrativeLocation> visitedLocations )
    {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.visitedLocations = visitedLocations;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.visited_item_view, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
    final NarrativeLocation location = visitedLocations.get(position);
        holder.wikipediaLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Uri uri = Uri.parse(location.getWeblinkOne());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            }
        });


        holder.visitedImageTitle.setText(location.getWeblinkTwo());
        final File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES); //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File img = new File(storageDir,location.getWeblinkTwo());
        for (File f : storageDir.listFiles())
        {
            String fileName = location.getWeblinkTwo();
            if(f.toString().contains(fileName))
            {
                img = new File(f.toString());
            }
        }
        if(img.exists()){
            Glide.with(context).load(img).into(holder.visitedImageView);
        }

        holder.visitedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PictureViewActivity.class);
                intent.putExtra("location", location);
                intent.putExtra("storageLocation",storageDir);
                context.startActivity(intent);
            }
        });

        holder.mapLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra("visitedLocation",location);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return visitedLocations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView visitedImageView;
        Button wikipediaLink;
        Button mapLink;
        TextView visitedImageTitle;

        public ViewHolder(View itemView)
        {
            super(itemView);
            visitedImageView = (ImageView) itemView.findViewById(R.id.visitedImage);
            visitedImageTitle = (TextView) itemView.findViewById(R.id.imageTitle);
            wikipediaLink = (Button) itemView.findViewById(R.id.visitedLink);
            mapLink = (Button) itemView.findViewById(R.id.visitedLocation);
        }
    }
}
