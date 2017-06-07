package com.example.peter.citynarrativesvv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;


public class HomeActivity extends AppCompatActivity{

    private  SqliteHelper DB;
    private BroadcastReceiver receiver;
    private LatLng here;

    Button displayNarrativesButton;
    Button seeVisitedButton;
    TextView searchLocationView;
    ImageView welcomeImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Populate the database with default tours if tables don't already exist
        DB = new SqliteHelper(this);
        if(!DB.tableExists())
        {
            DB.populateDatabase();
        }

        displayNarrativesButton = (Button) findViewById(R.id.selectNarrativeButton);
        displayNarrativesButton.setText("Please Wait");
        seeVisitedButton = (Button) findViewById(R.id.seeVisitedButton);
        welcomeImageView = (ImageView) findViewById(R.id.welcomImage);


        //Add backround Image
        AssetManager assets = getBaseContext().getAssets();
        InputStream stream = null;
        try {
            stream = assets.open("home_images/Dublin Blue Vector.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Drawable image = Drawable.createFromStream(stream,"home_images");
        welcomeImageView.setBackground(image);

        searchLocationView = (TextView) findViewById(R.id.yourLocation);

        //Check location permission
        if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            Intent i = new Intent(getApplicationContext(), GPS_Service.class);//If permission granted, start location services
            startService(i);
        }

        //see tours
        displayNarrativesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(here != null)
                {
                    Intent myIntent = new Intent(getApplicationContext(), NarrativeListActivity.class);
                    myIntent.putExtra("here", here);
                    startActivity(myIntent);
                }
                else
                {
                    Toast.makeText(getBaseContext(),"Wait", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //see visited locations
        seeVisitedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent myIntent = new Intent(getApplicationContext(), VisitedListActivity.class);
                startActivity(myIntent);
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(receiver  == null)
        {
            //Receive GPS_Service updates
            receiver = new BroadcastReceiver() { // Use manifest to receive when the app is in the background
                @Override
                public void onReceive(Context context, Intent intent) {
                    Location now = (Location) intent.getExtras().get("coordinates");
                    here = new LatLng(now.getLatitude(), now.getLongitude());
                    displayNarrativesButton.setText("Select a Tour");
                    searchLocationView.setText("");
                    //For testing purposes this location is set to be equal to the users in this build
                    DB.updateData("0","DIT",String.valueOf(here.latitude),String.valueOf(here.longitude),"Dublin Institute of Technology, Kevin St.","https://en.wikipedia.org/wiki/Dublin_Institute_of_Technology","null","music_table");
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
