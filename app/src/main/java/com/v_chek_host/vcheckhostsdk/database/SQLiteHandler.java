package com.v_chek_host.vcheckhostsdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.v_chek_host.vcheckhostsdk.model.entity.ModelStatus;

import java.util.ArrayList;
import java.util.List;


public class SQLiteHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "vChek";
    private static final String TABLE_MODEL = "local_models";
    private static final String KEY_ID = "id";
    private static final String KEY_MODEL_ID = "model_id";
    private static final String KEY_MODEL_NAME = "model_name";
    private static final String KEY_MODEL_TYPE = "model_type";
    private static final String KEY_MODEL_ITERATION = "model_iteration";
    private static final String KEY_MODEL_VERSION = "model_version";
    private static final String KEY_MODEL_WIDTH = "model_width";
    private static final String KEY_MODEL_HEIGHT = "model_height";
    private static final String KEY_MODEL_FILE_NAME = "model_file_name";
    private static final String KEY_MODEL_FRAME_TYPE = "model_frame_type";
    private static final String KEY_MODEL_URL = "model_url";
    private static final String KEY_MODEL_DOWNLOAD_STATUS = "model_download_status";


    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_MODEL + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + KEY_MODEL_ID + " INTEGER,"
            + KEY_MODEL_NAME + " TEXT,"+ KEY_MODEL_TYPE + " TEXT," + KEY_MODEL_ITERATION + " TEXT,"
            + KEY_MODEL_VERSION + " INTEGER,"+ KEY_MODEL_WIDTH + " INTEGER,"+ KEY_MODEL_HEIGHT + " INTEGER,"
            + KEY_MODEL_FILE_NAME + " TEXT,"+ KEY_MODEL_URL + " TEXT,"+ KEY_MODEL_DOWNLOAD_STATUS + " INTEGER,"
            + KEY_MODEL_FRAME_TYPE + " INTEGER"+ ")";
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODEL);
        // Create tables again
        onCreate(db);
    }

    // code to add the new contact
    public void addContact(ModelStatus modelStatus) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MODEL_ID, modelStatus.get_model_id());
        values.put(KEY_MODEL_NAME, modelStatus.get_model_name());
        values.put(KEY_MODEL_TYPE, modelStatus.get_model_type());
        values.put(KEY_MODEL_ITERATION, modelStatus.get_model_iteration());
        values.put(KEY_MODEL_VERSION, modelStatus.get_model_version());
        values.put(KEY_MODEL_WIDTH, modelStatus.get_model_width());
        values.put(KEY_MODEL_HEIGHT, modelStatus.get_model_height());
        values.put(KEY_MODEL_FILE_NAME, modelStatus.get_model_file_name());
        values.put(KEY_MODEL_URL, modelStatus.get_model_url());
        values.put(KEY_MODEL_DOWNLOAD_STATUS, modelStatus.get_model_download_status());
        values.put(KEY_MODEL_FRAME_TYPE, modelStatus.get_model_frame_type());

        // Inserting Row
        db.insert(TABLE_MODEL, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    // code to get the single contact
    ModelStatus getContact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MODEL, new String[] { KEY_ID}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        ModelStatus contact = new ModelStatus(Integer.parseInt(cursor.getString(0)));
        // return contact
        return contact;
    }

    // code to get all contacts in a list view
    public List<ModelStatus> getAllContacts() {
        List<ModelStatus> modelList = new ArrayList<ModelStatus>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_MODEL;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ModelStatus modelStatus = new ModelStatus();
                modelStatus.set_model_id(Integer.parseInt(cursor.getString(1)));
                modelStatus.set_model_name(cursor.getString(2));
                modelStatus.set_model_download_status(Integer.parseInt(cursor.getString(10)));
                // Adding contact to list
                modelList.add(modelStatus);
            } while (cursor.moveToNext());
        }

        // return contact list
        return modelList;
    }

    public boolean getModelDownloadStatus(int modelExist,int modelId,int version)
    {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        boolean hasData = false;
        Cursor cursor = null;
        try {
            String selectQuery;
            switch (modelExist){
                case 1:
                    selectQuery = "SELECT * FROM " + TABLE_MODEL + " where "+KEY_MODEL_ID +" = "+modelId;
                    break;
                case 2:
                    selectQuery = "SELECT * FROM " + TABLE_MODEL + " where "+KEY_MODEL_ID +" = "+modelId+" and " +
                            KEY_MODEL_DOWNLOAD_STATUS +"="+ 1 + " and "+KEY_MODEL_VERSION + "="+version;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + modelExist);
            }
            
            cursor = sqLiteDatabase.rawQuery(selectQuery, null);
            if (cursor.getCount() > 0) {
                hasData = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null) {
                cursor.close();
            }
            if (sqLiteDatabase.isOpen()) {
                sqLiteDatabase.close();
            }
        }
        return hasData;
    }

    public int updateDownloadStatus(int modelId,int version) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MODEL_DOWNLOAD_STATUS, 1);
        if(version!=0)
            values.put(KEY_MODEL_VERSION, version);
        // updating row
        return db.update(TABLE_MODEL, values, KEY_MODEL_ID + " = ?",
                new String[] { String.valueOf(modelId) });
    }



    // code to update the single contact
    public int updateContact(ModelStatus modelStatus) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MODEL_NAME, modelStatus.get_model_name());
        values.put(KEY_MODEL_FRAME_TYPE, modelStatus.get_model_frame_type());

        // updating row
        return db.update(TABLE_MODEL, values, KEY_ID + " = ?",
                new String[] { String.valueOf(modelStatus.get_model_id()) });
    }

    // Deleting single contact
    public void deleteModel(ModelStatus modelStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MODEL, KEY_ID + " = ?",
                new String[] { String.valueOf(modelStatus.get_model_id()) });
        db.close();
    }

    // Getting contacts Count
    public int getModelCount() {
        String countQuery = "SELECT  * FROM " + TABLE_MODEL;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }
}