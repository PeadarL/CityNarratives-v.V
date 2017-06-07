package com.example.peter.citynarrativesvv;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;

public class VisitedListActivity extends AppCompatActivity {

    ArrayList<NarrativeLocation> list;
    public static final String TABLE_USERVISITED = "user_table";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visited_locations);

        SqliteHelper helper = new SqliteHelper(this);
        Cursor res = helper.getAllData(TABLE_USERVISITED);
        list = new ArrayList<>();

        while(res.moveToNext()){
            NarrativeLocation location = new NarrativeLocation(res.getString(0),res.getString(1),res.getString(2),res.getString(3),res.getString(4),
                    res.getString(5),res.getString(6),res.getString(7));
            list.add(location);
        }

        RecyclerView mRecyclerView;
        RecyclerView.LayoutManager mLayoutManager;

        VisitedAdapter adapter = new VisitedAdapter(this, list);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mLayoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(adapter);
    }
}
