package com.example.notebook11;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class CRUD { //增删查改


    SQLiteOpenHelper dbHandler;
    SQLiteDatabase db;

    private static final String[] columns = {
            NoteDatabase.ID,
            NoteDatabase.TITLE,
            NoteDatabase.CONTENT,
            NoteDatabase.TIME,
            NoteDatabase.TYPE,

    };

    public CRUD(Context context){ dbHandler = new NoteDatabase(context);}

    public void open(){ db = dbHandler.getWritableDatabase();}

    public void close(){ dbHandler.close();}

    public Note addNote(Note note){  //安卓原生contentValue
        ContentValues contentValues = new ContentValues();
        contentValues.put(NoteDatabase.TITLE,note.getTitle());
        contentValues.put(NoteDatabase.CONTENT,note.getContent());
        contentValues.put(NoteDatabase.TIME,note.getTime());
        contentValues.put(NoteDatabase.TYPE,note.getTag());
        // Insert the new row, returning the primary key value of the new row
        long insertId = db.insert(NoteDatabase.TABLE_NAME,null,contentValues);//返回自增长id
        note.setId(insertId);
        return note;
    }
//cursor:游标，指针
    public List<Note> getTagNotes(int tag, boolean orderByTime){
        String orderBy = null;
        if(orderByTime) orderBy = NoteDatabase.TIME+" DESC";
        List<Note> noteList = new ArrayList<>();
        Cursor  cursor = db.query(NoteDatabase.TABLE_NAME,columns,NoteDatabase.TYPE+"=?",
                new String[]{String.valueOf(tag)},null,null,orderBy);
        if(cursor != null) {
            while (cursor.moveToNext()) {

                noteList.add(new Note(cursor.getLong(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getInt(4)));//偷懒了


            }
            Log.d("notelist",noteList.toString());
            return noteList;
        }
        Log.d("getNoteNull","error in create cursor");
        return null;
    }
    @SuppressLint("Range")
    public long getNoteId(Note note){
        Cursor  cursor = db.query(NoteDatabase.TABLE_NAME,columns,NoteDatabase.TIME+"=?",
                new String[]{String.valueOf(note.getTime())},null,null,null);
        if(cursor != null){
            Log.d("cursor","count"+cursor.getCount());
            cursor.moveToFirst();
            return cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID));
        }
        return 0;
    }

    @SuppressLint("Range") //getColumnIndex ==-1 它给我补的
    public List<Note> getAllNotes(boolean orderByTime){
        String orderBy = null;
        if(orderByTime) orderBy = NoteDatabase.TIME+" DESC";
        Cursor cursor = db.query(NoteDatabase.TABLE_NAME,columns,null,null,null,null,orderBy );
        List<Note> noteList = new ArrayList<>();
        if(cursor.getCount() >0){
            while (cursor.moveToNext()){
                Note note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)));
                note.setContent(cursor.getString(cursor.getColumnIndex(NoteDatabase.CONTENT)));
                note.setTitle(cursor.getString(cursor.getColumnIndex(NoteDatabase.TITLE)));
                note.setTime(cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME)));
                note.setTag(cursor.getInt(cursor.getColumnIndex(NoteDatabase.TYPE)));
                noteList.add(note);

            }
        }
        return noteList;
    }

    public int updateNode(Note note){
        ContentValues contentValues = new ContentValues();
        contentValues.put(NoteDatabase.TITLE,note.getTitle());
        contentValues.put(NoteDatabase.CONTENT,note.getContent());
        contentValues.put(NoteDatabase.TIME,note.getTime());
        contentValues.put(NoteDatabase.TYPE,note.getTag());
        return db.update(NoteDatabase.TABLE_NAME,contentValues,NoteDatabase.ID + "="+note.getId(),null);
    }

    public void deleteNote(Note note){
        db.delete(NoteDatabase.TABLE_NAME,NoteDatabase.ID+"="+note.getId(),null);
    }


}

