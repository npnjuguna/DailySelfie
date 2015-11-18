package com.njuguna.dailyselfie.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.internal.RevisionInternal;
import com.njuguna.dailyselfie.common.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CBLDatabaseHelper {

    public static final String TAG = CBLDatabaseHelper.class.getName();

    private static final String DATABASE_NAME = Constants.COUCHBASE_DATABASE_NAME;
    private static CBLDatabaseHelper instance;
    private static Manager manager = null;
    private static Database database = null;
    private static DatabaseHelper databaseHelper;
    private static Context context;


    /**
     * Instantiates a new Database helper.
     * <p>
     * Creates a Database manager and opens a connection to default database ${CBLDatabaseHelper::DATABASE_NAME}
     * </p>
     *
     * @param context Android application context
     */
    private CBLDatabaseHelper(Context context) {
        if (null == manager)
            try {

                Manager.enableLogging(com.couchbase.lite.util.Log.TAG, com.couchbase.lite.util.Log.VERBOSE);
                Manager.enableLogging(com.couchbase.lite.util.Log.TAG_SYNC_ASYNC_TASK, com.couchbase.lite.util.Log.VERBOSE);
                Manager.enableLogging(com.couchbase.lite.util.Log.TAG_SYNC, com.couchbase.lite.util.Log.VERBOSE);
                Manager.enableLogging(com.couchbase.lite.util.Log.TAG_QUERY, com.couchbase.lite.util.Log.VERBOSE);
                Manager.enableLogging(com.couchbase.lite.util.Log.TAG_VIEW, com.couchbase.lite.util.Log.VERBOSE);
                Manager.enableLogging(com.couchbase.lite.util.Log.TAG_DATABASE, com.couchbase.lite.util.Log.VERBOSE);

                manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
            } catch (IOException e) {
                Log.e(TAG, "Cannot create NoSQL manager object", e);
                return;
            }

        if (null == database)
            try {
                database = manager.getDatabase(DATABASE_NAME);
                if (database != null) {
                    database.addChangeListener(new Database.ChangeListener() {
                        public void changed(Database.ChangeEvent event) {

                            List<DocumentChange> documentChanges = new ArrayList<>(event.getChanges());
                            // Process the notification here
                            for (DocumentChange documentChange: documentChanges) {
                                if (documentChange.getSourceUrl() != null) {
                                    handleRemoteDocumentChange(documentChange);
                                } else {
                                    handleLocalDocumentChange(documentChange);
                                }
                            }
                        }
                    });
                }
            } catch (CouchbaseLiteException e) {
                Log.e(TAG, "Cannot get database", e);
            }

    }

    public static CBLDatabaseHelper init(Context context) {
        if (null == instance) instance = new CBLDatabaseHelper(context);
        if (null == databaseHelper) databaseHelper = new DatabaseHelper(context);
        CBLDatabaseHelper.context = context;
        return instance;
    }

    public static CBLDatabaseHelper getInstance() {
        return instance;
    }

    /**
     * Close all database connections
     */
    public static void release() {
        if (null != database) database.close();
        if (null != manager) manager.close();
    }

    /**
     * Returns Database instance
     *
     * @return Database database
     */
    public Database getDatabase() {
        return database;
    }

    public Database createDatabase() {
        if ((null == database) || (!database.exists()))
            try {
                database = manager.getDatabase(DATABASE_NAME);
                Log.d(TAG, "Database " + DATABASE_NAME + " created.");
            } catch (CouchbaseLiteException e) {
                Log.e(TAG, "Cannot get database", e);
            }
        return database;
    }

    public static Database reCreateDatabase() {
        if ((null != database) || (database.exists()))
            try {
                database.delete();
                database = manager.getDatabase(DATABASE_NAME);
                Log.d(TAG, "Database " + DATABASE_NAME + " recreated.");
            } catch (CouchbaseLiteException e) {
                Log.e(TAG, "Cannot recreate database", e);
            }
        return database;
    }

    private synchronized void  handleRemoteDocumentChange(DocumentChange documentChange) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        SelfiesDataHelper selfiesDataHelper = new SelfiesDataHelper(context);
        RemindersDataHelper remindersDataHelper = new RemindersDataHelper(context);

        RevisionInternal addedRevision = documentChange.getAddedRevision();
        Log.d(TAG, "Sequence #: " + addedRevision.getSequence());
        Map<String, Object> properties = new HashMap<>(addedRevision.getProperties());


        String type = String.valueOf(properties.get(Constants.PROPERTY_DOC_TYPE));

        if (null == type) return;

        ContentValues contentValues = new ContentValues();
        long rowId;

        contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_GUID,
                String.valueOf(documentChange.getDocumentId()));

        switch (type) {
            case SelfieContract.Selfies.CBL_DOC_TYPE:
                addLocationAuditSyncColumns(contentValues, properties);

                if (properties.get(SelfieContract.Selfies.COLUMN_CAPTION) != null)
                    contentValues.put(SelfieContract.Selfies.COLUMN_CAPTION,
                            String.valueOf(properties.get(SelfieContract.Selfies.COLUMN_CAPTION)));
                if (properties.get(SelfieContract.Selfies.COLUMN_THUMBNAIL_PATH) != null)
                    contentValues.put(SelfieContract.Selfies.COLUMN_THUMBNAIL_PATH,
                            String.valueOf(properties.get(SelfieContract.Selfies.COLUMN_THUMBNAIL_PATH)));
                if (properties.get(SelfieContract.Selfies.COLUMN_ORIGINAL_PATH) != null)
                    contentValues.put(SelfieContract.Selfies.COLUMN_ORIGINAL_PATH,
                            String.valueOf(properties.get(SelfieContract.Selfies.COLUMN_ORIGINAL_PATH)));

                rowId = selfiesDataHelper.getId(String.valueOf(documentChange.getDocumentId()));
                if (addedRevision.isDeleted()) {
                    if (rowId > 0) {
                        db.delete(SelfieContract.Selfies.TABLE_NAME, BaseColumns._ID + "=" + rowId, null);
                    }
                } else {
                    if (rowId > 0) {
                        db.update(SelfieContract.Selfies.TABLE_NAME, contentValues, BaseColumns._ID + "=" + rowId, null);
                    } else {
                        db.insert(SelfieContract.Selfies.TABLE_NAME, null, contentValues);
                    }
                }
                context.getContentResolver().notifyChange(SelfieContract.Selfies.CONTENT_URI, null);
                break;

            case SelfieContract.Reminders.CBL_DOC_TYPE:
                addLocationAuditSyncColumns(contentValues, properties);

                if (properties.get(SelfieContract.Reminders.COLUMN_INIT_TIME) != null)
                    contentValues.put(SelfieContract.Reminders.COLUMN_INIT_TIME,
                            String.valueOf(properties.get(SelfieContract.Reminders.COLUMN_INIT_TIME)));

                rowId = remindersDataHelper.getId(String.valueOf(documentChange.getDocumentId()));
                if (addedRevision.isDeleted()) {
                    if (rowId > 0) {
                        db.delete(SelfieContract.Reminders.TABLE_NAME, BaseColumns._ID + "=" + rowId, null);
                    }
                } else {
                    if (rowId > 0) {
                        db.update(SelfieContract.Reminders.TABLE_NAME, contentValues, BaseColumns._ID + "=" + rowId, null);
                    } else {
                        db.insert(SelfieContract.Reminders.TABLE_NAME, null, contentValues);
                    }
                }
                context.getContentResolver().notifyChange(SelfieContract.Reminders.CONTENT_URI, null);
                break;
        }

    }

    private synchronized void handleLocalDocumentChange(DocumentChange documentChange) {
        RevisionInternal addedRevision = documentChange.getAddedRevision();
        Map<String, Object> properties = new HashMap<>(addedRevision.getProperties());
        String type = String.valueOf(properties.get("type"));
        //TODO: we are currently doing nothing. implement

    }

    private void addLocationAuditSyncColumns(ContentValues contentValues, Map<String, Object> properties) {
        if (properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_DATE) != null)
            contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_DATE,
                    String.valueOf(properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_DATE)));
        if (properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_BY) != null)
            contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_BY,
                    String.valueOf(properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_BY)));
        if (properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_LAT) != null)
            contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_LAT,
                    String.valueOf(properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_LAT)));
        if (properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_LONG) != null)
            contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_LONG,
                    String.valueOf(properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_LONG)));
        if (properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_DATE) != null)
            contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_DATE,
                    String.valueOf(properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_DATE)));
        if (properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_BY) != null)
            contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_BY,
                    String.valueOf(properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_BY)));
        if (properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_LAT) != null)
            contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_LAT,
                    String.valueOf(properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_LAT)));
        if (properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_LONG) != null)
            contentValues.put(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_LONG,
                    String.valueOf(properties.get(SelfieContract.LocationAuditSyncColumns.COLUMN_UPDATE_LONG)));
    }

}