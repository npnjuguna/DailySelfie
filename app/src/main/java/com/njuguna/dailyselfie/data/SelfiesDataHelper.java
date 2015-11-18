package com.njuguna.dailyselfie.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;
import com.njuguna.dailyselfie.model.Selfie;
import com.njuguna.dailyselfie.util.DataUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class SelfiesDataHelper extends DataObjectHelper {

	public SelfiesDataHelper(Context context) {
		super(context);
	}

	public long upsert(Selfie selfie){
		ContentValues contentValues = getContentValues(selfie);

		long rowId = -1;
		if ((rowId = getId(selfie.getGuid())) > 0){
			Log.d(TAG, "Updating existing selfie...");
			mContentResolver.update(ContentUris.withAppendedId(SelfieContract.Selfies.CONTENT_URI, rowId),
					contentValues, null, null);
		} else {
			Log.d(TAG, "Adding new selfie...");
			rowId = ContentUris.parseId(mContentResolver.insert(SelfieContract.Selfies.CONTENT_URI, contentValues));
		}

		// saveSelfieImageAsAttachment(selfie);
		return rowId;
	}

	private void saveSelfieImageAsAttachment(Selfie selfie) {
		// save the image as an attachment to cbl
		InputStream inputStream = null;
		try {
            File storageDir = Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    selfie.getGuid(),  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            inputStream = new BufferedInputStream(new FileInputStream(image));

            Document doc = mCBLDatabase.getDocument(selfie.getGuid());
            UnsavedRevision newRev = doc.getCurrentRevision().createRevision();
            newRev.setAttachment(selfie.getGuid() + ".jpg", "image/jpeg", inputStream);
            newRev.save();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}

	private static ContentValues getContentValues(Selfie selfie) {
		// @formatter:off
		ContentValues contentValues = new ContentValues();
		putLocationAuditSyncColumns(selfie, contentValues);
		contentValues.put(SelfieContract.Selfies.COLUMN_CAPTION, selfie.getCaption());
		contentValues.put(SelfieContract.Selfies.COLUMN_THUMBNAIL_PATH, selfie.getThumbnailPath());
		contentValues.put(SelfieContract.Selfies.COLUMN_ORIGINAL_PATH, selfie.getOriginalPath());
		contentValues.put(SelfieContract.Selfies.COLUMN_SAVED_PATH, selfie.getSavedPath());
		contentValues.put(SelfieContract.Selfies.COLUMN_SAVED_IMAGE, DataUtils.getBytes(selfie.getSavedBitmap()));
		// @formatter:on
		return contentValues;
	}

	public int update(long id, String columnKey, String newValue){
		return updateRecord(SelfieContract.Selfies.CONTENT_URI, id, columnKey, newValue);
	}

	public boolean destructiveDelete(long rowId){
		Log.d(TAG, "Delete selfie with rowId: " + rowId);
		boolean result = true;
		// TODO implement destructive delete later

		result &= deleteRecord(SelfieContract.Selfies.CONTENT_URI, rowId);
		return result;
	}

	public boolean childrenPreservingDelete(long id, long reassignId){
		// TODO implement children preserving delete later
		return destructiveDelete(id);
	}

	public Selfie buildInstance(Cursor c){
		Selfie selfie = new Selfie();

		setLocationAuditSyncColumns(c, selfie);
		selfie.setCaption(c.getString(c.getColumnIndexOrThrow(SelfieContract.Selfies.COLUMN_CAPTION)));
		selfie.setThumbnailPath(c.getString(c.getColumnIndexOrThrow(SelfieContract.Selfies.COLUMN_THUMBNAIL_PATH)));
		selfie.setOriginalPath(c.getString(c.getColumnIndexOrThrow(SelfieContract.Selfies.COLUMN_ORIGINAL_PATH)));
		selfie.setSavedPath(c.getString(c.getColumnIndexOrThrow(SelfieContract.Selfies.COLUMN_SAVED_PATH)));
		selfie.setSavedBitmap(DataUtils.getImage(c.getBlob(c.getColumnIndexOrThrow(SelfieContract.Selfies.COLUMN_SAVED_IMAGE))));

		return selfie;
	}

	public long getId(String guid){
		return getId(SelfieContract.Selfies.CONTENT_URI, guid);
	}

	public Selfie get(long rowId){
		Selfie selfie = null;
		Log.v(TAG, "Fetching selfie with id " + rowId);
		Cursor c =	fetchRecord(SelfieContract.Selfies.CONTENT_URI, rowId);
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
		return getGuid(SelfieContract.Selfies.CONTENT_URI, SelfieContract.Selfies.TABLE_NAME, id);
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
		Cursor cursor = mContentResolver.query(SelfieContract.Selfies.CONTENT_URI,
				null, null, null, sortOrder);
		return cursor;
	}
}
