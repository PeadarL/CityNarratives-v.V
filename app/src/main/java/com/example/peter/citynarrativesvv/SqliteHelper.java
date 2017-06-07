package com.example.peter.citynarrativesvv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class SqliteHelper extends SQLiteOpenHelper
{
    // All values were stored to database and converted programmatically in each activity
    // This was subsequently altered to store actual value types, however methods for populating Cursors and passing objects
    // between acitvities required data type conversion anyway, so the database was left as is
    // However this decision should be researched further

    private static final String DATABASE_NAME = "Narratives.db";
    private static final String TABLE_JOYCE = "joyce_table";
    private static final String TABLE_MUSIC = "music_table";
    private static final String TABLE_USERVISITED = "user_table";

    private static final String COL_1 = "ID";
    private static final String COL_2 = "Progress";
    private static final String COL_3 = "LocationName";
    private static final String COL_4= "Latitude";
    private static final String COL_5 = "Longitude";
    private static final String COL_6 = "Text";
    private static final String COL_7 = "WebLinkOne";
    private static final String COL_8 = "WebLinkTwo";



    public SqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {

    }

    public void createTable(String tableName)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "PROGRESS TEXT," +"LOCATIONNAME TEXT," +"LATITUDE TEXT," + "LONGITUDE TEXT," +"TEXT TEXT," +
                "WEBLINKONE TEXT," + "WEBLINKTWO TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOYCE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MUSIC);
        onCreate(db);
    }

    public void insertData(String progress,String locationName,String latitude ,String longitude,String text,
                              String weblinkOne, String weblinkTwo, String tableName)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, progress);
        contentValues.put(COL_3, locationName);
        contentValues.put(COL_4, latitude);
        contentValues.put(COL_5, longitude);
        contentValues.put(COL_6, text);
        contentValues.put(COL_7, weblinkOne);
        contentValues.put(COL_8, weblinkTwo);
       db.insert(tableName, null,contentValues);
    }

    public boolean updateData(String progress,String locationName,String latitude ,String longitude,String text,
                              String weblinkOne, String weblinkTwo, String tableName)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, progress);
        contentValues.put(COL_3, locationName);
        contentValues.put(COL_4, latitude);
        contentValues.put(COL_5, longitude);
        contentValues.put(COL_6, text);
        contentValues.put(COL_7, weblinkOne);
        contentValues.put(COL_8, weblinkTwo);
        db.update(tableName,contentValues,"LOCATIONNAME = ?",new String[]{locationName});
        return true;
    }

    public Integer deleteData(String id,String tableName)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(tableName,"ID = ?", new String[] {id});
    }

    public Cursor getAllData(String tableName)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ tableName, null);
        return res;
    }

    public Cursor getRow(String id,String tableName)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ tableName + " where id = " + id , null);
        return res;
    }


    public void dropTable(String tableName)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
    }

    boolean tableExists()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String tableName = TABLE_JOYCE;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    public void populateDatabase() {
        createTable(TABLE_JOYCE);
        createTable(TABLE_MUSIC);

        createTable(TABLE_USERVISITED);

        insertData("0","The Music Wall of Fame","53.3449983", "-6.2642679","The Music Wall of Fame, part of the Irish Rock 'n' Roll museum experience. It was unveiled in 2003.","https://en.wikipedia.org/wiki/Irish_Music_Hall_of_Fame","null",TABLE_MUSIC);
        insertData("0","Phil Lynott","53.341269", "-6.260700","Phil Lynott was the lead singer of the band Thin Lizzy. This dedicated statue was unveiled in 2005.","https://en.wikipedia.org/wiki/Phil_Lynott","null",TABLE_MUSIC);
        insertData("0","Grafton St.","53.343214", "-6.259342","Grafton St. is one of the main pedestrian streets of Dublin city, hosting numerous buskers to entertain the crowds.","https://en.wikipedia.org/wiki/Grafton_Street","null",TABLE_MUSIC);

        insertData("0","James Joyce","53.349859", "-6.259756","James Joyce paralled Homer's Odyssey on the streets of Dublin with his famous novel Ulysses. He is considered one of the most influential writers of the 20th Century.","https://en.wikipedia.org/wiki/James_Joyce","null",TABLE_JOYCE);
        insertData("0","Jonathan Swift","53.339862", "-6.272194","Johnathan Swift, who wrote Gulliver's Travels, was the dean of St.Patrick's Cathedral and is buried here.","https://en.wikipedia.org/wiki/Jonathan_Swift","null",TABLE_JOYCE);
        insertData("0","Oscar Wilde","53.340815", "-6.250472","Most famous for his plays and novel, The Picture of Dorian Gray, this statue was commissioned in 1997 to remember him.","https://en.wikipedia.org/wiki/Oscar_Wilde","null",TABLE_JOYCE);
        insertData("0","Brendan Behan","53.361758", " -6.260221","Brendan sits by the Royal Canal, a sentiment shared in the song, The Auld Triangle, from his play The Quare Fellow.","https://en.wikipedia.org/wiki/Brendan_Behan","null",TABLE_JOYCE);

        insertData("0","DIT","53.337064", " -6.267764","Dublin Institute of Technology, Kevin St.","https://en.wikipedia.org/wiki/Dublin_Institute_of_Technology","null",TABLE_MUSIC);
    }
}
