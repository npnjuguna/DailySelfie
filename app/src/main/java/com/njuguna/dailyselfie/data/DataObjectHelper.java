/*
 * Copyright (c) 2014 Patrick Njuguna <njuguna@njuguna.com>
 *
 */

package com.njuguna.dailyselfie.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.couchbase.lite.Database;
import com.njuguna.dailyselfie.app.SelfieApplication;

/**
 * Adapter to be used for creating and opening the database for read/write operations.
 * The adapter abstracts several methods for database access and should be subclassed
 * by any other adapters to database-backed data models.
 * @author Patrick Njuguna <njuguna@njuguna.com>
 *
 */
public abstract class DataObjectHelper {
	/**
	 * Tag for logging
	 */
	protected static final String TAG = DataObjectHelper.class.getSimpleName();

    /**
	 * Content Resolver
	 */
	protected ContentResolver mContentResolver;
	
	/**
	 * Application context
	 */
	protected Context mContext;

	protected Database mCBLDatabase;
	
	/**
	 * Opens (or creates if it doesn't exist) the database for reading and writing
	 * @param context Application context to be used for opening database
	 */
	public DataObjectHelper(Context context) {
		mContext = context;
		mContentResolver = context.getContentResolver();
		mCBLDatabase = ((SelfieApplication) context.getApplicationContext()).getDatabase();
	}

	public static void putLocationAuditSyncColumns(LocationAuditedSynchedModel locationAuditedSynchedModel, ContentValues contentValues) {
		contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_GUID,                 locationAuditedSynchedModel.getGuid());
		contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_DATE,          locationAuditedSynchedModel.getCreateDate());
		contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_BY,            locationAuditedSynchedModel.getCreateBy());
		contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_LAT,           locationAuditedSynchedModel.getCreateLat());
		contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_LONG,          locationAuditedSynchedModel.getCreateLong());
		contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_DATE,          locationAuditedSynchedModel.getUpdateDate());
		contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_BY,            locationAuditedSynchedModel.getUpdateBy());
		contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_LAT,           locationAuditedSynchedModel.getUpdateLat());
		contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_LONG,          locationAuditedSynchedModel.getUpdateLong());
	}

	public static void setLocationAuditSyncColumns(Cursor cursor, LocationAuditedSynchedModel locationAuditedSynchedModel) {
        locationAuditedSynchedModel.setGuid(cursor.getString(cursor.getColumnIndexOrThrow(SelfieContract.GuidColumns.COLUMN_GUID)));
        locationAuditedSynchedModel.setCreateDate(cursor.isNull(cursor.getColumnIndexOrThrow(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_DATE)) ? null : cursor.getLong(cursor.getColumnIndexOrThrow(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_DATE)));
        locationAuditedSynchedModel.setCreateBy(cursor.getString(cursor.getColumnIndexOrThrow(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_BY)));
        locationAuditedSynchedModel.setCreateLat(cursor.isNull(cursor.getColumnIndexOrThrow(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_LAT)) ? null : cursor.getDouble(cursor.getColumnIndexOrThrow(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_LAT)));
		locationAuditedSynchedModel.setCreateLong(cursor.isNull(cursor.getColumnIndexOrThrow(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_LONG)) ? null : cursor.getDouble(cursor.getColumnIndexOrThrow(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_LONG)));
		locationAuditedSynchedModel.setUpdateDate(cursor.isNull(cursor.getColumnIndexOrThrow(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_DATE)) ? null : cursor.getLong(cursor.getColumnIndexOrThrow(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_DATE)));
		locationAuditedSynchedModel.setUpdateBy(cursor.getString(cursor.getColumnIndexOrThrow(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_BY)));
		locationAuditedSynchedModel.setUpdateLat(cursor.isNull(cursor.getColumnIndexOrThrow(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_LAT)) ? null : cursor.getDouble(cursor.getColumnIndexOrThrow(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_LAT)));
		locationAuditedSynchedModel.setUpdateLong(cursor.isNull(cursor.getColumnIndexOrThrow(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_LONG)) ? null : cursor.getDouble(cursor.getColumnIndexOrThrow(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_LONG)));
	}

	/**
	 * Returns the context used to create this adapter
     * @return Android application context
     */
    public Context getContext(){
        return mContext.getApplicationContext();
    }

	/**
	 * Retrieves record with id <code>rowId</code> from table <code>uri</code>
	 * @param uri Name of table where record is found
	 *@param rowId ID of record to be retrieved  @return {@link Cursor} to record retrieved
	 */
	protected Cursor fetchRecord(Uri uri, long rowId){
		return mContentResolver.query(ContentUris.withAppendedId(uri, rowId), null, null, null, null);
	}
	
	/**
	 * Retrieves all records from database table <code>uri</code>
	 * @param uri Name of table in database
	 * @return {@link Cursor} to all records in table <code>uri</code>
	 */
	protected Cursor fetchAllRecords(Uri uri){
		return mContentResolver.query(uri, null, null, null, null);
	}

	/**
	 * Deletes record with ID <code>rowID</code> from database table <code>uri</code> 
	 * @param uri Name of table in database
	 *@param rowId ID of record to be deleted  @return <code>true</code> if deletion was successful, <code>false</code> otherwise
	 */
	protected boolean deleteRecord(Uri uri, long rowId){
		return mContentResolver.delete(ContentUris.withAppendedId(uri, rowId), null, null) > 0;
	}

    /**
     * Deletes all records in the database
     * @return Number of deleted records
     */
    protected int deleteAllRecords(Uri uri){
        return mContentResolver.delete(uri, null, null);
    }

    protected int updateRecord(Uri uri, long rowId, String columnKey, String newValue){
        ContentValues contentValues = new ContentValues();
        contentValues.put(columnKey, newValue);

        return mContentResolver.update(ContentUris.withAppendedId(uri, rowId), contentValues, null, null);
    }

	protected int updateAllRecords(Uri uri, String columnKey, String newValue){
        ContentValues contentValues = new ContentValues();
        contentValues.put(columnKey, newValue);

        return mContentResolver.update(uri, contentValues, null, null);
    }

	/**
	 * Returns the global unique identifier for the record ID <code>id</code>
	 *
     * @param tableName
     * @param rowId Database record id
     * @return Global Unique identifier string of the record
	 */
	protected String getGuid(Uri uri, String tableName, long rowId){
		String guid = null;
		Cursor c = mContentResolver.query(ContentUris.withAppendedId(uri, rowId),
				new String[]{BaseColumns._ID, SelfieContract.GuidColumns.COLUMN_GUID}, null, null, null);
		if (c != null && c.moveToFirst()){
			guid = c.getString(c.getColumnIndexOrThrow(SelfieContract.GuidColumns.COLUMN_GUID));
			c.close();
		}
        if (c != null) c.close();
		return guid;
	}

	/**
	 * Fetch a record id from the database which has a global unique ID <code>guid</code>
	 *
     * @param guid Global Unique Identifier of the record to be retrieved
     * @return Database row ID of record with GUID <code>guid</code>
	 */
	protected long getId(Uri uri, String guid){
		Cursor cursor = mContentResolver.query(SelfieUris.withAppendedGUID(uri, guid),
				new String[] {BaseColumns._ID, SelfieContract.GuidColumns.COLUMN_GUID},
                null, null, null);
		long result = -1;
		if (cursor != null && cursor.moveToFirst()){
			Log.v(TAG, "Returning id id");
			result = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
			cursor.close();
		}
		if (cursor != null) cursor.close();
		return result;
	}
	
	protected boolean recordExits(Uri uri, long rowId) {
		Cursor cursor = mContentResolver.query(ContentUris.withAppendedId(uri, rowId), new String[] {BaseColumns._ID},
                null, null, null);
		boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
		return exists;
	}

	protected boolean recordExits(Uri uri, String tableName, String guid) {
		Cursor cursor = mContentResolver.query(SelfieUris.withAppendedGUID(uri, guid), new String[] {BaseColumns._ID},
                null, null, null);
		boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
		return exists;
	}
}
