package com.example.peter.citynarrativesvv;

import android.location.*;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Narrative {
    private String Title;
    private String TableName;
    private ArrayList<NarrativeLocation> Locations;

    Narrative(String title,String tableName,ArrayList<NarrativeLocation> locations)
    {
        this.Title = title;
        this.TableName = tableName;
        this.Locations=locations;
    }

    public String getTitle() {
        return Title;
    }

    public String getTableName() {
        return TableName;
    }

    public ArrayList<NarrativeLocation> getLocations() {
        return Locations;
    }

    public void setLocations(ArrayList<NarrativeLocation> locations)
    {
        Locations = locations;
    }
}