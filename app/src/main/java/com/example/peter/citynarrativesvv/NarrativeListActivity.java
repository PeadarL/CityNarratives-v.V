package com.example.peter.citynarrativesvv;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class NarrativeListActivity extends AppCompatActivity {

    public static final String TABLE_JOYCE = "joyce_table";//Tablenames should be read from database to avoid tight coupling
    public static final String TABLE_MUSIC = "music_table";


    private SqliteHelper DB;
    private ArrayList<Narrative> narratives;
    private ArrayList<NarrativeLocation> narrativeLocations;
    private ArrayList<String> narrativeNames;
    private LatLng here;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_narrative_list);

        here = (LatLng) getIntent().getExtras().get("here");
        DB = new SqliteHelper(this);
        narrativeNames = new ArrayList<>();
        narrativeNames.add(TABLE_JOYCE);
        narrativeNames.add(TABLE_MUSIC);


        narratives = new ArrayList<>(); // Used to Store the tours
        for(int i = 0; i< narrativeNames.size(); i++)
        {
            String tableName = narrativeNames.get(i);
            narrativeLocations = new ArrayList<>();// Used to store each location in an individual tour

            Cursor res = DB.getAllData(tableName); // Get all locations from datrabase for specific tour
            while(res.moveToNext())
            {
                NarrativeLocation location = new NarrativeLocation(tableName,res.getString(1),res.getString(2),res.getString(3),res.getString(4),
                        res.getString(5),res.getString(6),res.getString(7));
                narrativeLocations.add(location);
            }
            Narrative addNarrative = new Narrative(tableName, tableName, narrativeLocations);
            narratives.add(addNarrative);
        }

        RecyclerView mRecyclerView;
        RecyclerView.LayoutManager mLayoutManager;

        NarrativeAdapter adapter = new NarrativeAdapter(getBaseContext(), narratives, here);// Pass tours and current location to the Adapter to be bound to the view
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(adapter);
    }
}