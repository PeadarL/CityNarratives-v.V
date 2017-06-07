package com.example.peter.citynarrativesvv;

import android.os.Parcel;
import android.os.Parcelable;

public class NarrativeLocation implements Parcelable {

    private String NarrativeName;
    private String Progress;
    private String Title;
    private String Latitude;
    private String Longitude;
    private String Text;
    private String WeblinkOne;
    private String WeblinkTwo;

    public NarrativeLocation()
    {

    }

    public NarrativeLocation(String narrativeName, String progress, String title, String latitude, String longitude,
                             String text, String weblinkOne, String weblinkTwo)
    {
        this.NarrativeName = narrativeName;
        this.Progress = progress;
        this.Title = title;
        this.Latitude = latitude;
        this.Longitude = longitude;
        this.Text = text;
        this.WeblinkOne = weblinkOne;
        this.WeblinkTwo = weblinkTwo;
    }


    public String getNarrativeName() {
        return NarrativeName;
    }

    public String getProgress() {
        return Progress;
    }

    public String getTitle() {
        return Title;
    }

    public String getLatitude() {
        return Latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getWeblinkOne() {
        return WeblinkOne;
    }

    public String getWeblinkTwo() {
        return WeblinkTwo;
    }


    //Parcelable Methods
    private NarrativeLocation(Parcel in)
    {
        NarrativeName = in.readString();
        Progress = in.readString();
        Title = in.readString();
        Latitude = in.readString();
        Longitude = in.readString();
        Text = in.readString();
        WeblinkOne = in.readString();
        WeblinkTwo = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(NarrativeName);
        out.writeString(Progress);
        out.writeString(Title);
        out.writeString(Latitude);
        out.writeString(Longitude);
        out.writeString(Text);
        out.writeString(WeblinkOne);
        out.writeString(WeblinkTwo);
    }

    public static  final Parcelable.Creator<NarrativeLocation> CREATOR =
            new Parcelable.Creator<NarrativeLocation>()
            {
                @Override
                public NarrativeLocation createFromParcel(Parcel source)
                {
                    return new NarrativeLocation(source);
                }
                @Override
                public NarrativeLocation[] newArray(int size)
                {
                    return new NarrativeLocation[size];
                }
            };
}
