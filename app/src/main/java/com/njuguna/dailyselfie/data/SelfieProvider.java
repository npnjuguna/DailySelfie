package com.njuguna.dailyselfie.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;
import com.njuguna.dailyselfie.app.SelfieApplication;
import com.njuguna.dailyselfie.common.Constants;
import com.njuguna.dailyselfie.document.CRUDWrapper;
import com.njuguna.dailyselfie.util.SelectionBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.njuguna.dailyselfie.util.DataUtils.extractFtsValues;
import static com.njuguna.dailyselfie.util.DataUtils.makeMapFromContentValues;

public class SelfieProvider extends ContentProvider {

    private static final String TAG = SelfieProvider.class.getSimpleName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DatabaseHelper mOpenHelper;
    private CBLDatabaseHelper cblDatabaseHelper;
    private SelfieApplication mSelfieApplication;
    private static final String JOIN_OUTER = "LEFT OUTER";
    private static final String JOIN_INNER = "INNER";

    private static final int SELFIES = 1000;
    private static final int SELFIES_ID = 1001;
    private static final int SELFIES_GUID = 1002;
    private static final int SELFIES_FTS = 1100;
    private static final int SELFIES_FTS_ID = 1101;
    private static final int SELFIES_FTS_GUID = 1102;
    private static final int REMINDERS = 1200;
    private static final int REMINDERS_ID = 1201;
    private static final int REMINDERS_GUID = 1202;

    private SelfiesDataHelper selfiesDataHelper;
    private SelfiesFtsDataHelper selfiesFtsDataHelper;
    private RemindersDataHelper remindersDataHelper;

    @Override
    public synchronized Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);

        // avoid the expensive string concatenation below if not loggable
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "uri=" + uri + " match=" + match + " proj=" + Arrays.toString(projection) +
                    " selection=" + selection + " args=" + Arrays.toString(selectionArgs) + ")");
        }

        switch (match) {
            default: {
                // Most cases are handled with simple SelectionBuilder
                final SelectionBuilder builder = buildExpandedSelection(uri);
                String tableName = uri.getPathSegments().get(0);
                if (null != mSelfieApplication.getUser())
                {
                    switch (match) {
                        default:
                            builder.where(tableName + "." + SelfieContract.LocationAuditSyncColumns.COLUMN_CREATE_BY + " = ?", mSelfieApplication.getUser().getId());
                            break;
                    }

                }

                Cursor cursor = builder
                        .where(selection, selectionArgs)
                        .query(db, true, projection, sortOrder, null);
                Context context = getContext();
                if (null != context) {
                    cursor.setNotificationUri(context.getContentResolver(), uri);
                }
                return cursor;
            }
        }
    }

    private static String makeTwoTableJoin(String joinType,
                                           String foreignTable, String foreignKey, String referenceTable,
                                           String key) {
        return " " + foreignTable + " " + joinType + " JOIN " + referenceTable + " ON " + foreignTable + "." + foreignKey + " = " + referenceTable + "." + key + " ";
    }

    private static String makeMultiTableJoin(String mainTable,
                                             HashMap<String, String[]> constraintParameters) {

        StringBuilder sbInTables = new StringBuilder(" ").append(mainTable);

        for (Entry<String, String[]> entry : constraintParameters.entrySet()) {
            String referenceTable = entry.getKey();
            String[] constraintParams = entry.getValue();

            sbInTables.append(" ").append(constraintParams[0]).append(" JOIN ")
                    .append(referenceTable).append(" ON ")
                    .append(constraintParams[1]).append(".")
                    .append(constraintParams[2]).append(" = ")
                    .append(referenceTable).append(".")
                    .append(constraintParams[3]).append(" ");
        }

        return sbInTables.append(" ").toString();
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = SelfieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, SelfieContract.PATH_SELFIES,
                SELFIES);
        matcher.addURI(authority, SelfieContract.PATH_SELFIES + "/#",
                SELFIES_ID);
        matcher.addURI(authority, SelfieContract.PATH_SELFIES + "/*",
                SELFIES_GUID);

        matcher.addURI(authority, SelfieContract.PATH_SELFIES_FTS,
                SELFIES_FTS);
        matcher.addURI(authority, SelfieContract.PATH_SELFIES_FTS + "/#",
                SELFIES_FTS_ID);
        matcher.addURI(authority, SelfieContract.PATH_SELFIES_FTS + "/*",
                SELFIES_FTS_GUID);

        matcher.addURI(authority, SelfieContract.PATH_REMIDERS,
                REMINDERS);
        matcher.addURI(authority, SelfieContract.PATH_REMIDERS + "/#",
                REMINDERS_ID);
        matcher.addURI(authority, SelfieContract.PATH_REMIDERS + "/*",
                REMINDERS_GUID);

        return matcher;
    }

    @Override
    public synchronized String getType(@NonNull Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case SELFIES:
                return SelfieContract.Selfies.CONTENT_TYPE;
            case SELFIES_ID:
            case SELFIES_GUID:
                return SelfieContract.Selfies.CONTENT_ITEM_TYPE;
            case SELFIES_FTS:
                return SelfieContract.SelfiesFts.CONTENT_TYPE;
            case SELFIES_FTS_ID:
            case SELFIES_FTS_GUID:
                return SelfieContract.SelfiesFts.CONTENT_ITEM_TYPE;
            case REMINDERS:
                return SelfieContract.Reminders.CONTENT_TYPE;
            case REMINDERS_ID:
            case REMINDERS_GUID:
                return SelfieContract.Reminders.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public synchronized Uri insert(@NonNull Uri uri, ContentValues values) {

        Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString());
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final Database cblDb = cblDatabaseHelper.getDatabase();
        final int match = sUriMatcher.match(uri);
        Map<String, Object> properties = makeMapFromContentValues(values);
        Uri retUri;

        switch (match) {
            case SELFIES: {
                properties.put(Constants.PROPERTY_DOC_TYPE, SelfieContract.Selfies.CBL_DOC_TYPE);
                long _id = 0;
                try {
                    db.beginTransaction();
                    _id = db.insertOrThrow(SelfieContract.Selfies.TABLE_NAME, null, values);
                    final ContentValues ftsValues = extractFtsValues(values);
                    if ((null != ftsValues) && (ftsValues.size() > 0)) db.insertOrThrow(SelfieContract.SelfiesFts.TABLE_NAME, null, ftsValues);
                    db.setTransactionSuccessful();
                } catch(SQLException e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                }
                notifyChange(uri);
                retUri = SelfieContract.Selfies.buildSelfieUri(_id);
                break;
            }
            case REMINDERS: {
                properties.put(Constants.PROPERTY_DOC_TYPE, SelfieContract.Reminders.CBL_DOC_TYPE);
                long _id = db.insertOrThrow(SelfieContract.Reminders.TABLE_NAME, null, values);
                notifyChange(uri);
                retUri = SelfieContract.Reminders.buildReminderUri(_id);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        try {
            String guid = values.getAsString(SelfieContract.GuidColumns.COLUMN_GUID);
            if ((null != guid) && (!guid.isEmpty()) && (null != cblDb) && (cblDb.exists())) {
                Document document = CRUDWrapper.createDocumentWithId(cblDb,
                        guid, properties);


                // add image as an attachment
                if (match == SELFIES) {
                    InputStream inputStream = new ByteArrayInputStream(values.getAsByteArray(SelfieContract.Selfies.COLUMN_SAVED_IMAGE));
                    UnsavedRevision newRev = document.getCurrentRevision().createRevision();
                    newRev.setAttachment(guid + ".jpg", "image/jpeg", inputStream);
                    newRev.save();
                }

                Log.d(TAG, "Document written to database with ID = " + document.getId());
            } else {
                if ((null == guid) || (guid.isEmpty())) {
                    Log.d(TAG, "Could not write CBL document. GUID problem");
                } else {
                    Log.d(TAG, "Could not write CBL document. SmartFarmerApplication problem");
                }
            }
        } catch (CouchbaseLiteException e) {
            // TODO: add specific handle for ID conflict
            Log.e(TAG, "CouchbaseLiteException. Code: " + e.getCBLStatus() + " Message: " + e.getCBLStatus(), e);
        }
        return retUri;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        selfiesDataHelper = new SelfiesDataHelper(getContext());
        selfiesFtsDataHelper = new SelfiesFtsDataHelper(getContext());
        remindersDataHelper = new RemindersDataHelper(getContext());
        cblDatabaseHelper = CBLDatabaseHelper.init(getContext());
        mSelfieApplication = (SelfieApplication) getContext().getApplicationContext();
        return true;
    }

    @Override
    public synchronized int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Log.v(TAG, "delete(uri=" + uri);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int rowsDeleted = 0;

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case SELFIES_FTS: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        builder.where(selection, selectionArgs);
        // delete the records in the cbl
        deleteDocuments(makeListOfDocumentGuidsUsingBuilder(db, builder));

        try {
            db.beginTransaction();
            rowsDeleted = builder.delete(db);
            if (match == SELFIES) {
                builder.table(SelfieContract.SelfiesFts.TABLE_NAME);
                builder.delete(db);
            }
            db.setTransactionSuccessful();
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            notifyChange(uri);
        }
        return rowsDeleted;
    }

    @Override
    public synchronized int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.v(TAG, "update(uri=" + uri + ", values=" + values.toString());
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int rowsUpdated = 0;

        final int match = sUriMatcher.match(uri);
        Map<String, Object> updatedProperties = makeMapFromContentValues(values);
        List<String> docIDsToBeUpdated;

        switch (match) {
            case SELFIES_FTS: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        switch (match) {
            case SELFIES: {
                try {
                    db.beginTransaction();
                    rowsUpdated = builder.where(selection, selectionArgs).update(db, values);
                    builder.table(SelfieContract.SelfiesFts.TABLE_NAME);
                    final ContentValues ftsValues = extractFtsValues(values);
                    if ((null != ftsValues) && (ftsValues.size() > 0)) builder.update(db, ftsValues);
                    db.setTransactionSuccessful();
                } catch(SQLException e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                }
            }
            break;
            default: {
                rowsUpdated = builder.where(selection, selectionArgs).update(db, values);
            }
        }

        // update the documents in cbl
        docIDsToBeUpdated = makeListOfDocumentGuidsUsingBuilder(db, builder);
        updateDocumentsWithProperties(docIDsToBeUpdated, updatedProperties);

        if (rowsUpdated != 0) {
            notifyChange(uri);
        }
        return rowsUpdated;
    }

    private List<String> buildDocsToBeUpdatedList(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        List<String> docIDsToBeUpdated; // section to generate the list of documents that need to updated in CBL
        switch (match) {
            // "selfies"
            case SELFIES: {
                docIDsToBeUpdated = makeListOfDocumentGUIDs(db, SelfieContract.Selfies.TABLE_NAME, selection, selectionArgs);
                break;
            }
            // "selfies/#"
            case SELFIES_ID: {
                docIDsToBeUpdated = Collections.singletonList(selfiesDataHelper.getGuid(ContentUris.parseId(uri)));
                break;
            }
            // "selfies/*"
            case SELFIES_GUID: {
                docIDsToBeUpdated = Collections.singletonList(SelfieUris.parseGUID(uri));
                break;
            }

            // "reminders"
            case REMINDERS: {
                docIDsToBeUpdated = makeListOfDocumentGUIDs(db, SelfieContract.Reminders.TABLE_NAME, selection, selectionArgs);
                break;
            }
            // "reminders/#"
            case REMINDERS_ID: {
                docIDsToBeUpdated = Collections.singletonList(remindersDataHelper.getGuid(ContentUris.parseId(uri)));
                break;
            }
            // "reminders/*"
            case REMINDERS_GUID: {
                docIDsToBeUpdated = Collections.singletonList(SelfieUris.parseGUID(uri));
                break;
            }

            default: {
                throw new UnsupportedOperationException("Unknown uri for " + match + ": " + uri);
            }
        }
        return docIDsToBeUpdated;
    }

    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            // "selfies"
            case SELFIES: {
                return builder.table(SelfieContract.Selfies.TABLE_NAME);
            }
            // "selfies/#"
            case SELFIES_ID: {
                return builder.table(SelfieContract.Selfies.TABLE_NAME)
                        .where(BaseColumns._ID + " = ?", Long.toString(ContentUris.parseId(uri)));
            }
            // "selfies/*"
            case SELFIES_GUID: {
                return builder.table(SelfieContract.Selfies.TABLE_NAME)
                        .where(SelfieContract.GuidColumns.COLUMN_GUID + " = ?", uri.getLastPathSegment());
            }

            // "reminders"
            case REMINDERS: {
                return builder.table(SelfieContract.Reminders.TABLE_NAME);
            }
            // "reminders/#"
            case REMINDERS_ID: {
                return builder.table(SelfieContract.Reminders.TABLE_NAME)
                        .where(BaseColumns._ID + " = ?", Long.toString(ContentUris.parseId(uri)));
            }
            // "reminders/*"
            case REMINDERS_GUID: {
                return builder.table(SelfieContract.Reminders.TABLE_NAME)
                        .where(SelfieContract.GuidColumns.COLUMN_GUID + " = ?", uri.getLastPathSegment());
            }

            default: {
                throw new UnsupportedOperationException("Unknown uri for " + match + ": " + uri);
            }
        }
    }

    private SelectionBuilder buildExpandedSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // "selfies"
            case SELFIES: {
                return builder.table(SelfieContract.Selfies.TABLE_NAME);
            }
            // "selfies/#"
            case SELFIES_ID: {
                return builder.table(SelfieContract.Selfies.TABLE_NAME)
                        .where(SelfieContract.Selfies.TABLE_NAME + "." + BaseColumns._ID + " = ?", Long.toString(ContentUris.parseId(uri)));
            }
            // "selfies/*"
            case SELFIES_GUID: {
                return builder.table(SelfieContract.Selfies.TABLE_NAME)
                        .where(SelfieContract.Selfies.TABLE_NAME + "." + SelfieContract.GuidColumns.COLUMN_GUID + " = ?", uri.getLastPathSegment());
            }

            // "selfies_fts"
            case SELFIES_FTS: {
                return builder.table(SelfieContract.SelfiesFts.TABLE_NAME);
            }
            // "selfies_fts/#"
            case SELFIES_FTS_ID: {
                return builder.table(SelfieContract.SelfiesFts.TABLE_NAME)
                        .where(SelfieContract.SelfiesFts.TABLE_NAME + "." + BaseColumns._ID + " = ?", Long.toString(ContentUris.parseId(uri)));
            }
            // "selfies_fts/*"
            case SELFIES_FTS_GUID: {
                return builder.table(SelfieContract.SelfiesFts.TABLE_NAME)
                        .where(SelfieContract.SelfiesFts.TABLE_NAME + "." + SelfieContract.GuidColumns.COLUMN_GUID + " = ?", uri.getLastPathSegment());
            }

            // "reminders"
            case REMINDERS: {
                return builder.table(SelfieContract.Reminders.TABLE_NAME);
            }
            // "reminders/#"
            case REMINDERS_ID: {
                return builder.table(SelfieContract.Reminders.TABLE_NAME)
                        .where(SelfieContract.Reminders.TABLE_NAME + "." + BaseColumns._ID + " = ?", Long.toString(ContentUris.parseId(uri)));
            }
            // "reminders/*"
            case REMINDERS_GUID: {
                return builder.table(SelfieContract.Reminders.TABLE_NAME)
                        .where(SelfieContract.Reminders.TABLE_NAME + "." + SelfieContract.GuidColumns.COLUMN_GUID + " = ?", uri.getLastPathSegment());
            }

            default: {
                throw new UnsupportedOperationException("Unknown uri for " + match + ": " + uri);
            }
        }
    }

    private void notifyChange(Uri uri) {
        Context context = getContext();
        if (null != context) {
            context.getContentResolver().notifyChange(uri, null);
        }
    }

    private void updateDocumentsWithProperties(List<String> docIDsToBeUpdated, Map<String, Object> updatedProperties) {
        if (docIDsToBeUpdated.size() > 0) {
            for (String docID : docIDsToBeUpdated) {
                Document document;
                Database cblDb = cblDatabaseHelper.getDatabase();
                document = cblDb.getDocument(docID);
                Map<String, Object> properties = new HashMap<>(document.getProperties());
                properties.putAll(updatedProperties);
                try {
                    document.putProperties(properties);
                    com.couchbase.lite.util.Log.d(TAG, "updated retrievedDocument=" + String.valueOf(document.getProperties()));
                } catch (CouchbaseLiteException e) {
                    com.couchbase.lite.util.Log.e(TAG, "Cannot update document", e);
                }
            }
        }
    }

    private void deleteDocuments(List<String> docIDsToBeDeleted) {
        if (docIDsToBeDeleted.size() > 0) {
            for (String docID : docIDsToBeDeleted) {
                try {
                    CRUDWrapper.deletePreserve(cblDatabaseHelper.getDatabase(), docID,
                            (null != mSelfieApplication.getUser() ? mSelfieApplication.getUser().getId() : ""),
                            "999.0", "999.0");
                    com.couchbase.lite.util.Log.d(TAG, "Deleted document, deletion status = " + cblDatabaseHelper.getDatabase().getDocument(docID).isDeleted());
                } catch (CouchbaseLiteException e) {
                    com.couchbase.lite.util.Log.e(TAG, "Cannot delete document", e);
                }
            }
        }
    }

    private List<String> makeListOfDocumentGUIDs(SQLiteDatabase db, String tableName, String selection, String[] selectionArgs) {
        List<String> docIDsToBeTouched = new ArrayList<>();
        Cursor cursor = db.query(tableName,
                new String[] {SelfieContract.GuidColumns.COLUMN_GUID},
                selection, selectionArgs, null, null,null);

        if (cursor != null){
            while ( cursor.moveToNext()) {
                docIDsToBeTouched.add(cursor.getString(cursor.getColumnIndex(SelfieContract.GuidColumns.COLUMN_GUID)));
            }
            cursor.close();
        }
        return docIDsToBeTouched;
    }

    private List<String> makeListOfDocumentGuidsUsingBuilder(SQLiteDatabase db, SelectionBuilder builder) {
        List<String> docIDsToBeTouched = new ArrayList<>();
        Cursor cursor = builder.query(db, true,
                new String[]{SelfieContract.GuidColumns.COLUMN_GUID},
                null, null);

        if (cursor != null){
            while ( cursor.moveToNext()) {
                docIDsToBeTouched.add(cursor.getString(cursor.getColumnIndex(SelfieContract.GuidColumns.COLUMN_GUID)));
            }
            cursor.close();
        }
        return docIDsToBeTouched;
    }

}
