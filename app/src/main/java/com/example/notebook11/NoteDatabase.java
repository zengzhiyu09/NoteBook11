package com.example.notebook11;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NoteDatabase extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "note_table";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String ID = "_id";
    public static final String TIME = "time";
    public static final String TYPE = "type";

    public NoteDatabase(Context context){ super(context,"notes.db",null,1);}
    //注意顺序 id title content time mode(tag)
    // title允许为空
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME
                    +"("
                    + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TITLE +" TEXT,"
                    + CONTENT+ " TEXT NOT NULL,"
                    + TIME + " TEXT NOT NULL UNIQUE,"
                    + TYPE + " INTERGER DEFAULT 1)");
    }//ID从1开始自增

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
