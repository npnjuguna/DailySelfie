package com.njuguna.dailyselfie.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.njuguna.dailyselfie.model.Reminder;

import java.util.LinkedList;
import java.util.List;

public class RemindersDataHelper extends DataObjectHelper {

	public RemindersDataHelper(Context context) {
		super(context);
	}

	public long upsert(Reminder reminder){
		ContentValues contentValues = getContentValues(reminder);

		long rowId;
		if ((rowId = getId(reminder.getGuid())) > 0){
			//if reminder already exists, then just update
			Log.d(TAG, "Updating existing reminder");
			mContentResolver.update(ContentUris.withAppendedId(SelfieContract.Reminders.CONTENT_URI, rowId),
					contentValues, null, null);
		} else {
			Log.d(TAG, "Adding new reminder to db");
			rowId = ContentUris.parseId(mContentResolver.insert(SelfieContract.Reminders.CONTENT_URI, contentValues));
		}

		return rowId;
	}

	private static ContentValues getContentValues(Reminder reminder) {
		// @formatter:off
		ContentValues contentValues = new ContentValues();
		putLocationAuditSyncColumns(reminder, contentValues);
		contentValues.put(SelfieContract.Reminders.COLUMN_INIT_TIME,                reminder.getInitTime());
		// @formatter:on
		return contentValues;
	}

	public int update(long id, String columnKey, String newValue){
		return updateRecord(SelfieContract.Reminders.CONTENT_URI, id, columnKey, newValue);
	}

	public boolean destructiveDelete(long rowId){
		Log.d(TAG, "Delete reminder with rowId: " + rowId);
		boolean result;
		// TODO implement destructive delete later

		result = deleteRecord(SelfieContract.Reminders.CONTENT_URI, rowId);
		return result;
	}

	public boolean childrenPreservingDelete(long id, long reassignId){
		// TODO implement children preserving delete later
		return destructiveDelete(id);
	}

	public Reminder buildInstance(Cursor c){
		Reminder reminder = new Reminder();

		setLocationAuditSyncColumns(c, reminder);
		reminder.setInitTime(c.getLong(c.getColumnIndexOrThrow(SelfieContract.Reminders.COLUMN_INIT_TIME)));

		return reminder;
	}

	public long getId(String guid){
		return getId(SelfieContract.Reminders.CONTENT_URI, guid);
	}

	public Reminder get(long rowId){
		Reminder reminder = null;
		Log.v(TAG, "Fetching reminder with id " + rowId);
		Cursor c =	fetchRecord(SelfieContract.Reminders.CONTENT_URI, rowId);
		if (c != null && c.moveToFirst()){
			reminder = buildInstance(c);
			c.close();
		}
		return reminder;
	}

	public Reminder get(String guid){
		return get(getId(guid));
	}

	public String getGuid(long id){
		return getGuid(SelfieContract.Reminders.CONTENT_URI, SelfieContract.Reminders.TABLE_NAME, id);
	}

	public List<Reminder> getAll(String sortOrder){
		LinkedList<Reminder> reminders = new LinkedList<>();
		Cursor c = fetchAllRecords(sortOrder);

		if (c == null)
			return reminders;

		while(c.moveToNext()){
			reminders.add(buildInstance(c));
		}
		c.close();
		return reminders;
	}

	public Cursor fetchAllRecords(String sortOrder){
		Log.v(TAG, "Fetching all reminders from db");
		return mContentResolver.query(SelfieContract.Reminders.CONTENT_URI,
				null, null, null, sortOrder);
	}
}
