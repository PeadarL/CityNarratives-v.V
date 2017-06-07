package com.example.peter.citynarrativesvv;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.peter.citynarrativesvv.POJO.Example;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class LocationDisplayActivity extends AppCompatActivity
{
    public static SqliteHelper DB;

    private BroadcastReceiver receiver;
    private LatLng here;
    private int counter;
    private String narrativeTableName;
    private ArrayList<NarrativeLocation> narrativeLocations;
    private  NarrativeLocation location;

    private static final int REQUEST_IMAGE_CAPTURE = 1;


    private boolean notificationSent;

    //Display Values//
    ImageView imageView;
    TextView narrativeTitle;
    TextView narrativeQuestion;
    TextView distanceTextView;
    TextView visitedTextView;
    Button webLinkOne;
    Button cameraButton;
    Button skipButton;
    Button mapButton;


    String timeStamp; // Used to save image to database and file storage with unique ID

    public static final String TABLE_USERVISITED = "user_table";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_narrative_display);


        here = (LatLng) getIntent().getExtras().get("here");
        narrativeTableName = (String) getIntent().getExtras().get("narrativeTableName");
        narrativeLocations = (ArrayList<NarrativeLocation>) getIntent().getSerializableExtra("narrativeLocations");

        DB = new SqliteHelper(this);

        counter = 0;
        location = narrativeLocations.get(counter);
        notificationSent = false;

        imageView = (ImageView) findViewById(R.id.ImageView);
        visitedTextView = (TextView) findViewById(R.id.visitedText);
        narrativeTitle = (TextView) findViewById(R.id.narrativeTitle);
        narrativeQuestion = (TextView) findViewById(R.id.narrativeQuestion);
        distanceTextView = (TextView) findViewById(R.id.distanceTextView);
        webLinkOne = (Button) findViewById(R.id.webLinkOne);
        cameraButton = (Button) findViewById(R.id.cameraButton);
        skipButton = (Button) findViewById(R.id.skipButton);
        mapButton = (Button) findViewById(R.id.MapsButton);


        drawNarrative(location);

        //Redraw the display with the next location
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NarrativeLocation temp = narrativeLocations.get(counter);
                narrativeLocations.remove(temp);
                narrativeLocations.add(temp);
                location = narrativeLocations.get(counter);
                drawNarrative(location);
                if(!locationReached(location))
                {
                    webLinkOne.setVisibility(View.INVISIBLE);
                    cameraButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null)
                {
                    File photoFile = null;
                    try
                    {
                        photoFile = createImageFile();
                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                    if (photoFile != null)
                    {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(view.getContext(), MapsActivity.class);
                intent.putExtra("here", here);
                intent.putExtra("narrativeLocations", narrativeLocations);
                intent.putExtra("narrativeTableName", narrativeTableName);
                startActivity(intent);
            }
        });
    }

    private void drawNarrative(final NarrativeLocation location) {
        AssetManager assets = getBaseContext().getAssets();
        InputStream stream = null;
        try {
            stream = assets.open(narrativeTableName +"/"+location.getTitle() + ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Drawable image = Drawable.createFromStream(stream, narrativeTableName);
        imageView.setBackground(image);
        narrativeTitle.setText(location.getTitle());
        narrativeQuestion.setText(location.getText());
        notificationSent= false;
        webLinkOne.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(location.getWeblinkOne());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        if(locationReached(location))
        {
            if(!notificationSent)
            {
                sendNotification();
                notificationSent = true; // Used to ensure the notification doesn't send everytime location is updated
            }
            webLinkOne.setVisibility(View.VISIBLE);
            cameraButton.setVisibility(View.VISIBLE);
            DB.updateData("1",location.getTitle(),location.getLatitude(),// Update user progress
                    location.getLongitude(),location.getText(),location.getWeblinkOne(),
                    location.getWeblinkOne(), location.getNarrativeName());
        }
        if(location.getProgress().contains("1"))
        {
            visitedTextView.setVisibility(View.VISIBLE);
        }
        else
        {
         visitedTextView.setVisibility(View.INVISIBLE);
        }
        buildRetrofitSetDistance();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        timeStamp = new SimpleDateFormat("dd-MM-yyyy (HH:MM:ss)").format(new Date()); //Global variable is used to ensure that the exact time is the same between both database and image file name as methods called at different times
        location = narrativeLocations.get(counter);
        String imageFileName = location.getTitle()+ "\n"+timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            location = narrativeLocations.get(counter);
            DB = new SqliteHelper(this);
            //Insert User location into database
            DB.insertData(location.getProgress(),location.getTitle(),location.getLatitude(),
                    location.getLongitude(),location.getText(),location.getWeblinkOne(),
                    location.getTitle()+ "\n"+timeStamp,TABLE_USERVISITED);
            Toast.makeText(getBaseContext(),"Added to database!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean locationReached(NarrativeLocation location)
    {
        LatLng destination = new LatLng(Double.parseDouble(location.getLatitude()), Double.parseDouble(location.getLongitude()));
        double distance = getDistance(here, destination);
        if(distance < .050){ // Check in Kilometres, average accuracy outdoors: 10m-20m, indoors < 50m
            return true;
        }
        return  false;
    }

    private double getDistance(LatLng origin, LatLng destination)// in kilometers
    {
        double distance = distance(origin.longitude, destination.longitude, destination.latitude, origin.latitude);
        return distance;
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

    //Used to write time and distance to location on display
    private void buildRetrofitSetDistance() {
        location = narrativeLocations.get(counter);
        LatLng destination = new LatLng(Double.parseDouble(location.getLatitude()), Double.parseDouble(location.getLongitude()));
        String url = "https://maps.googleapis.com/maps/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitMaps service = retrofit.create(RetrofitMaps.class);
        for (int i = 1; i < narrativeLocations.size(); i++) {
            Call<Example> calls = service.getDistanceDuration("metric", here.latitude + "," + here.longitude, destination.latitude + "," + destination.longitude, "walking");
            calls.enqueue(new Callback<Example>() {
                @Override
                public void onResponse(Response<Example> response, Retrofit retrofit) {
                    try {
                        for (int i = 0; i < response.body().getRoutes().size(); i++) {
                            String distance = response.body().getRoutes().get(i).getLegs().get(i).getDistance().getText();
                            String time = response.body().getRoutes().get(i).getLegs().get(i).getDuration().getText();
                            distanceTextView.setText("Distance:" + distance + ", Duration:" + time);
                        }
                    } catch (Exception e) {
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
    }
    private void sendNotification()
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("You Have Arrived!")
                .setContentText(narrativeLocations.get(counter).getTitle())
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        Intent intent = new Intent(this, LocationDisplayActivity.class);
        intent.putExtra("here", here);
        intent.putExtra("narrativeLocations", narrativeLocations);
        intent.putExtra("narrativeTableName", narrativeTableName);
        PendingIntent activity = PendingIntent.getActivity(this, 0,
                intent,  PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(activity);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // ID allows you to update the notification later on.
        notificationManager.notify(1, builder.build());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(receiver  == null)
        {
            receiver = new BroadcastReceiver() { // Use manifest to receive when the app is in the background
                @Override
                public void onReceive(Context context, Intent intent) {
                    Location now = (Location) intent.getExtras().get("coordinates");
                    here = new LatLng(now.getLatitude(), now.getLongitude());
                    if(locationReached(location))
                    {
                        if(!notificationSent)
                        {
                            sendNotification();
                            notificationSent = true;
                        }
                        webLinkOne.setVisibility(View.VISIBLE);
                        cameraButton.setVisibility(View.VISIBLE);
                        DB.updateData("1",location.getTitle(),location.getLatitude(),
                                location.getLongitude(),location.getText(),location.getWeblinkOne(),
                                location.getWeblinkOne(), location.getNarrativeName());
                    }
                    else
                    {
                        webLinkOne.setVisibility(View.INVISIBLE);
                        cameraButton.setVisibility(View.INVISIBLE);
                    }
                }
            };
        }
        registerReceiver(receiver, new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(receiver!=null){
            unregisterReceiver(receiver);
        }
    }
}