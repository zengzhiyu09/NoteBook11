package com.example.notebook11;


import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.database.Cursor;
import android.content.ContentValues;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BackupRestoreManager {

    private static final String BACKUP_FILE_NAME = "notes_backup.json";
    private Context context;
    private NoteDatabase dbHelper;

    public BackupRestoreManager(Context context) {
        this.context = context;
        this.dbHelper = new NoteDatabase(context);
    }

    @SuppressLint("Range")
    public void backupNotes() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(NoteDatabase.TABLE_NAME, null, null, null, null, null, null);
        JSONArray jsonArray = new JSONArray();

        while (cursor.moveToNext()) {
            JSONObject noteJson = new JSONObject();
            try {
                //noteJson.put(NoteDatabase.ID, cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)));
                noteJson.put(NoteDatabase.TITLE, cursor.getString(cursor.getColumnIndex(NoteDatabase.TITLE)));
                noteJson.put(NoteDatabase.CONTENT, cursor.getString(cursor.getColumnIndex(NoteDatabase.CONTENT)));
                noteJson.put(NoteDatabase.TIME, cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME)));
                noteJson.put(NoteDatabase.TYPE, cursor.getInt(cursor.getColumnIndex(NoteDatabase.TYPE)));
                jsonArray.put(noteJson);
            } catch (Exception e) {
                Log.e("BackupRestoreManager", "Error creating JSON object: " + e.getMessage());
            }
        }
        cursor.close();
        if(jsonArray.length()!=0) {
            File backupFile = new File(context.getExternalFilesDir(null), BACKUP_FILE_NAME);
            try (FileOutputStream fos = new FileOutputStream(backupFile)) {
                fos.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
                Log.d("BackupRestoreManager", "Backup completed successfully.");
            } catch (IOException e) {
                Log.e("BackupRestoreManager", "Error writing backup file: " + e.getMessage());
            }
        }
    }

    public void restoreNotes() {
        File backupFile = new File(context.getExternalFilesDir(null), BACKUP_FILE_NAME);
        if (!backupFile.exists()) {
            Toast.makeText(context,"备份文件不存在",Toast.LENGTH_SHORT).show();
            Log.d("BackupRestoreManager", "Backup file does not exist.");
            return;
        }

        try (FileInputStream fis = new FileInputStream(backupFile)) {
            byte[] bytes = new byte[(int) backupFile.length()];
            fis.read(bytes);
            String jsonStr = new String(bytes, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(jsonStr);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject noteJson = jsonArray.getJSONObject(i);
                    ContentValues contentValues = new ContentValues();
                    //contentValues.put(NoteDatabase.ID, noteJson.getLong(NoteDatabase.ID));
                    contentValues.put(NoteDatabase.TITLE, noteJson.getString(NoteDatabase.TITLE));
                    contentValues.put(NoteDatabase.CONTENT, noteJson.getString(NoteDatabase.CONTENT));
                    contentValues.put(NoteDatabase.TIME, noteJson.getString(NoteDatabase.TIME));
                    contentValues.put(NoteDatabase.TYPE, noteJson.getInt(NoteDatabase.TYPE));
                    db.insert(NoteDatabase.TABLE_NAME, null, contentValues);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            Log.d("BackupRestoreManager", "Restore completed successfully.");
        } catch (Exception e) {
            Log.e("BackupRestoreManager", "Error reading backup file: " + e.getMessage());
        }
    }
}