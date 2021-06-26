package top.someones.cardmatch.core

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context?) : SQLiteOpenHelper(context, "CardMatch.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_RESOURCES)
        db.execSQL(CREATE_TABLE_GAME_HISTORY)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.i("databaseHelper", "Version:$oldVersion-->$newVersion")
    }

    companion object {
        private const val CREATE_TABLE_RESOURCES =
            "CREATE TABLE Resources (uuid primary key,name,author,show,version REAL,resPath,cover,frontRes,backRes,weight INTEGER DEFAULT 0)"
        private const val CREATE_TABLE_GAME_HISTORY =
            "CREATE TABLE GameHistory (time primary key,uuid,score INTEGER)"
    }
}