package com.example.peter.citynarrativesvv;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.peter.citynarrativesvv.POJO.Example;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class NarrativeAdapter extends RecyclerView.Adapter<NarrativeAdapter.ViewHolder>
{
    private final Context context;
    private LayoutInflater inflater;
    private ArrayList<Narrative> narratives;
    private LatLng currentLocation;
    private double distance;


    public NarrativeAdapter(Context context, ArrayList<Narrative> narratives, LatLng currentLocation)
    {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.narratives = narratives;
        this.currentLocation = currentLocation;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = inflater.inflate(R.layout.narrative_item_view, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)
    {
        final Narrative narrative = narratives.get(position); // Add each Tour to the Recycler View based on position
        narrative.setLocations(getShortestRoute(narrative.getLocations()));
        int progress = 0;


        String title = getStringResourceByName(narrative.getTitle());// get string resources by name rather than integer ID
        String tableName = narrative.getTableName();
        holder.title.setText(title);
        holder.title.setTextColor(Color.parseColor("#FFFFFF"));
        holder.visitedLocations.setTextColor(Color.parseColor("#FFFFFF"));
        holder.startTourButton.setTextColor(Color.parseColor("#FFFFFF"));


        AssetManager assets = context.getAssets();
        InputStream stream = null;
        try
        {
            stream = assets.open(tableName+"/"+ title + ".jpg");// Add image background
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        for (NarrativeLocation location :narrative.getLocations())
        {
            progress = progress + Integer.parseInt(location.getProgress());
        }
        holder.progressBar.setMax(narrative.getLocations().size());
        holder.progressBar.setProgress(progress);

        Drawable image = Drawable.createFromStream(stream, tableName);
        holder.itemView.setBackground(image);

        holder.startTourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(holder.itemView.getContext(), LocationDisplayActivity.class);
                intent.putExtra("narrativeTableName", narrative.getTableName());
                intent.putExtra("narrativeLocations", narrative.getLocations());
                intent.putExtra("here", currentLocation);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    //Method used to add the closest location first, then the closest to this location etc.
    private ArrayList<NarrativeLocation> getShortestRoute(ArrayList<NarrativeLocation> narrativeLocations)
    {
        ArrayList<NarrativeLocation> returnList = new ArrayList<>();

        NarrativeLocation origin = new NarrativeLocation("Origin","","",String.valueOf(currentLocation.latitude), String.valueOf(currentLocation.longitude),"","","");
        NarrativeLocation addLocation = null;

        returnList.add(0, origin);// User location added to the beginning of the list
        int counterOne = 0;
        while(narrativeLocations.size() != 0) // while loop finishes when all locations removed from this list and added to returned list
        {
            NarrativeLocation startLocation = returnList.get(counterOne);
            LatLng startLatlng = new LatLng(Double.parseDouble(startLocation.getLatitude()), Double.parseDouble(startLocation.getLongitude()));
            distance = 1000000;

            for (NarrativeLocation location: narrativeLocations)
            {
                double comparisonDistance = distance;
                //NarrativeLocation endLocation = narrativeLocations.get(counterTwo);
                LatLng endLatlng = new LatLng(Double.parseDouble(location.getLatitude()), Double.parseDouble(location.getLongitude()));

                //getDistance(startLatlng, endLatlng);
                //getGoogleAPIDistance(startLatlng, endLatlng);// inefficient as it requires the app to wait for a response from the async task
                if(comparisonDistance ==  distance)
                {
                    getDistance(startLatlng,endLatlng);
                }
                if (comparisonDistance > distance) {
                    addLocation = location;
                }
            }
            counterOne++;
            returnList.add(addLocation);
            Log.i("Locations: ", addLocation.getTitle());
            narrativeLocations.remove(addLocation);
        }
        returnList.remove(0);
        return  returnList;
    }

    @Override
    public int getItemCount()
    {
        return narratives.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView title;
        TextView visitedLocations;
        ProgressBar progressBar;
        Button startTourButton;
        public ViewHolder(View itemView)
        {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.titleView);
            visitedLocations = (TextView) itemView.findViewById(R.id.visitedLocations);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            startTourButton = (Button) itemView.findViewById(R.id.startTour);
        }
    }

    private void getDistance(LatLng origin, LatLng destination)
    {
        distance = distance(origin.longitude, destination.longitude, destination.latitude, origin.latitude);
    }

    private double radians(double angle)
    {
        return angle *((2* Math.PI)/360);
    }
    private double distance(double long1, double long2, double lat1, double lat2)
    {
        double radius = 6371;
        double long_rads1 = radians(long1);
        double long_rads2 = radians(long2);
        double lat_rads1 = radians(lat1);
        double lat_rads2 = radians(lat2);
        double formulaOne = Math.sin(lat_rads1) * Math.sin(lat_rads2) * Math.cos((long_rads1-long_rads2));
        double formulaTwo = Math.cos(lat_rads1) * Math.cos(lat_rads2);
        return Math.acos((formulaOne+formulaTwo)) * radius;
    }

    private void getGoogleAPIDistance(LatLng origin, LatLng destination){
        String url = "https://maps.googleapis.com/maps/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitMaps service = retrofit.create(RetrofitMaps.class);
        Call<Example> call = service.getDistanceDuration("metric", origin.latitude + "," + origin.longitude, destination.latitude + "," + destination.longitude, "walking");
        call.enqueue(new Callback<Example>()
        {
            @Override
            public void onResponse(Response<Example> response, Retrofit retrofit)
            {
                try
                {
                    for (int i = 0; i < response.body().getRoutes().size(); i++)
                    {
                        distance = response.body().getRoutes().get(i).getLegs().get(i).getDistance().getValue(); // In meters
                    }
                }
                catch (Exception e)
                {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Throwable t) {
                Log.d("onFailure", t.toString());
            }
        });
    }

    private String getStringResourceByName(String aString) {
        String packageName = context.getPackageName();
        int resId = context.getResources()
                .getIdentifier(aString, "string", packageName);
        if (resId == 0) {
            return aString;
        } else {
            return context.getString(resId);
        }
    }
}