package top.someones.cardmatch.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String CREATE_TABLE_RESOURCES = "CREATE TABLE Resources (uuid primary key,name,author,show,version REAL,resPath,cover,frontRes,backRes,weight INTEGER DEFAULT 0)";
    private static final String CREATE_TABLE_GAME_HISTORY = "CREATE TABLE GameHistory (time primary key,uuid,score INTEGER)";

    public DatabaseHelper(Context context) {
        super(context, "CardMatch.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_RESOURCES);
        db.execSQL(CREATE_TABLE_GAME_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("databaseHelper", "Version:" + oldVersion + "-->" + newVersion);
    }

}
