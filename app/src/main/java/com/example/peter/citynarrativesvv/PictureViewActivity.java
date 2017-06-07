package com.example.peter.citynarrativesvv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.ExifInterface;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;

public class PictureViewActivity extends AppCompatActivity {
    ImageView pictureView;
    File storageDir;
    NarrativeLocation location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_view);

        pictureView = (ImageView) findViewById(R.id.pictureImageView);

        location = (NarrativeLocation) getIntent().getExtras().get("location");
        storageDir = (File) getIntent().getExtras().get("storageLocation");

        final File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES); //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); // This can be used to instead make the images public
        File img = new File(storageDir,location.getWeblinkTwo());
        for (File f : storageDir.listFiles())
        {
            String fileName = location.getWeblinkTwo();
            if(f.toString().contains(fileName))// Check to see if file in file storage contains the given name, a direct path may not always be given as some devices will add additional unique IDs to stored files
            {
                img = new File(f.toString());
            }
        }
        if(img.toString() != null && img.toString().length() != 0){

            ExifInterface exifInterface= null;
            try {
                exifInterface = new ExifInterface(img.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);
            // Change display orientation based on the picture being viewed
            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                default:
            }
            //Picasso.with(this).load(file).placeholder(R.drawable.cast_album_art_placeholder).into(pictureView); //Alternative, Noticeably slower than Glide
            Glide.with(this).load(img).into(pictureView);
        }
        else {
            Toast.makeText(this, "Uri is empty", Toast.LENGTH_SHORT).show();
        }

        // Used to remove image on screen tap
        pictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder( v.getRootView().getContext())
                        .setTitle( "Remove Image?" )
                        .setMessage( "Are you sure you want to remove this image?" )
                        .setPositiveButton( "Remove", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SqliteHelper db = new SqliteHelper(getBaseContext());
                                db.deleteData(location.getNarrativeName(), "user_table");
                                Intent intent = new Intent(getApplicationContext(),VisitedListActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                Toast.makeText(getBaseContext(), "Removed",
                                        Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getBaseContext(), "Cancelled",
                                        Toast.LENGTH_LONG).show();
                            }
                        } )
                        .show();
            }
        });
    }
}
