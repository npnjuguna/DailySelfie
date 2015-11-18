package com.njuguna.dailyselfie.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.njuguna.dailyselfie.model.Selfie;

import java.util.LinkedList;
import java.util.List;

public class SelfiesFtsDataHelper extends DataObjectHelper {

	public SelfiesFtsDataHelper(Context context) {
		super(context);
	}

	public long upsert(Selfie selfie){
		ContentValues contentValues = getContentValues(selfie);

		long rowId = -1;
		if ((rowId = getId(selfie.getGuid())) > 0){
			//if selfie already exists, then just update
			Log.d(TAG, "Updating existing selfie");
			mContentResolver.update(ContentUris.withAppendedId(SelfieContract.SelfiesFts.CONTENT_URI, rowId),
					contentValues, null, null);
		} else {
			Log.d(TAG, "Adding new selfie to db");
			rowId = ContentUris.parseId(mContentResolver.insert(SelfieContract.SelfiesFts.CONTENT_URI, contentValues));
		}

		return rowId;
	}

	private static ContentValues getContentValues(Selfie selfie) {
		// @formatter:off
		ContentValues contentValues = new ContentValues();
		contentValues.put(SelfieContract.SelfiesFts.COLUMN_GUID, selfie.getGuid());
		contentValues.put(SelfieContract.SelfiesFts.COLUMN_CAPTION, selfie.getCaption());
		contentValues.put(SelfieContract.SelfiesFts.COLUMN_THUMBNAIL_PATH, selfie.getThumbnailPath());
		contentValues.put(SelfieContract.SelfiesFts.COLUMN_ORIGINAL_PATH, selfie.getOriginalPath());
		// @formatter:on
		return contentValues;
	}

	public int update(long id, String columnKey, String newValue){
		return updateRecord(SelfieContract.SelfiesFts.CONTENT_URI, id, columnKey, newValue);
	}

	public boolean destructiveDelete(long rowId){
		Log.d(TAG, "Delete selfie with rowId: " + rowId);
		boolean result = true;
		// TODO implement destructive delete later

		result &= deleteRecord(SelfieContract.SelfiesFts.CONTENT_URI, rowId);
		return result;
	}

	public boolean childrenPreservingDelete(long id, long reassignId){
		// TODO implement children preserving delete later
		return destructiveDelete(id);
	}

	public Selfie buildInstance(Cursor c){
		Selfie selfie = new Selfie();

		selfie.setGuid(c.getString(c.getColumnIndexOrThrow(SelfieContract.SelfiesFts.COLUMN_GUID)));
		selfie.setCaption(c.getString(c.getColumnIndexOrThrow(SelfieContract.SelfiesFts.COLUMN_CAPTION)));
		selfie.setThumbnailPath(c.getString(c.getColumnIndexOrThrow(SelfieContract.SelfiesFts.COLUMN_THUMBNAIL_PATH)));
		selfie.setOriginalPath(c.getString(c.getColumnIndexOrThrow(SelfieContract.SelfiesFts.COLUMN_ORIGINAL_PATH)));

		return selfie;
	}

	public long getId(String guid){
		return getId(SelfieContract.SelfiesFts.CONTENT_URI, guid);
	}

	public Selfie get(long rowId){
		Selfie selfie = null;
		Log.v(TAG, "Fetching selfie with id " + rowId);
		Cursor c =	fetchRecord(SelfieContract.SelfiesFts.CONTENT_URI, rowId);
		if (c != null && c.moveToFirst()){
			selfie = buildInstance(c);
			c.close();
		}
		return selfie;
	}

	public Selfie get(String guid){
		return get(getId(guid));
	}

	public String getGuid(long id){
		return getGuid(SelfieContract.SelfiesFts.CONTENT_URI, SelfieContract.SelfiesFts.TABLE_NAME, id);
	}

	public List<Selfie> getAll(String sortOrder){
		LinkedList<Selfie> selfies = new LinkedList<Selfie>();
		Cursor c = fetchAllRecords(sortOrder);

		if (c == null)
			return selfies;

		while(c.moveToNext()){
			selfies.add(buildInstance(c));
		}
		c.close();
		return selfies;
	}

	public Cursor fetchAllRecords(String sortOrder){
		Log.v(TAG, "Fetching all selfies from db");
		Cursor cursor = mContentResolver.query(SelfieContract.SelfiesFts.CONTENT_URI,
				null, null, null, sortOrder);
		return cursor;
	}
}
