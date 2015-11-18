package com.njuguna.dailyselfie;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.couchbase.lite.Document;
import com.njuguna.dailyselfie.app.Preferences;
import com.njuguna.dailyselfie.common.util.IDUtils;
import com.njuguna.dailyselfie.data.CBLDatabaseHelper;
import com.njuguna.dailyselfie.data.SelfieContract;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestProvider extends AndroidTestCase {

    public static final String TAG = TestProvider.class.getSimpleName();
    public static final String TEST_LAT = "-1.26242240";
    public static final String TEST_LONG = "36.8079847";
    private static final long USER_NUMBER = 9;
    public static final String[] TEST_SELFIES_GUID = {
            IDUtils.generateBase64GUIDwNum(USER_NUMBER), 
            IDUtils.generateBase64GUIDwNum(USER_NUMBER),
    };
    public static final String[] TEST_REMINDERS_GUID = {
            IDUtils.generateBase64GUIDwNum(USER_NUMBER), 
            IDUtils.generateBase64GUIDwNum(USER_NUMBER),
    };
    public static String[] TEST_USER_GUID = {""};

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Preferences prefs = new Preferences(mContext);
        TEST_USER_GUID[0] = IDUtils.generateBase64GUIDwNum(USER_NUMBER);
    }

    static List<ContentValues> createSelfieValues() {
        List<ContentValues> testValues = new ArrayList<>();

        // selfie 1
        ContentValues testValue = new ContentValues();
        testValue.put(SelfieContract.Selfies.COLUMN_GUID, TEST_SELFIES_GUID[0]);
        testValue.put(SelfieContract.Selfies.COLUMN_CREATE_DATE, System.currentTimeMillis());
        testValue.put(SelfieContract.Selfies.COLUMN_CREATE_BY, TEST_USER_GUID[0]);
        testValue.put(SelfieContract.Selfies.COLUMN_CREATE_LAT, TEST_LAT);
        testValue.put(SelfieContract.Selfies.COLUMN_CREATE_LONG, TEST_LONG);
        testValue.put(SelfieContract.Selfies.COLUMN_UPDATE_DATE, System.currentTimeMillis());
        testValue.put(SelfieContract.Selfies.COLUMN_UPDATE_BY, TEST_USER_GUID[0]);
        testValue.put(SelfieContract.Selfies.COLUMN_UPDATE_LAT, TEST_LAT);
        testValue.put(SelfieContract.Selfies.COLUMN_UPDATE_LONG, TEST_LONG);
        testValue.put(SelfieContract.Selfies.COLUMN_CAPTION,"Caption 1");
        testValue.put(SelfieContract.Selfies.COLUMN_THUMBNAIL_PATH, TEST_SELFIES_GUID[0]);
        testValue.put(SelfieContract.Selfies.COLUMN_ORIGINAL_PATH, TEST_SELFIES_GUID[0]);
        testValues.add(testValue);

        // selfie 2
        testValue = new ContentValues();
        testValue.put(SelfieContract.Selfies.COLUMN_GUID, TEST_SELFIES_GUID[1]);
        testValue.put(SelfieContract.Selfies.COLUMN_CREATE_DATE, System.currentTimeMillis());
        testValue.put(SelfieContract.Selfies.COLUMN_CREATE_BY, TEST_USER_GUID[0]);
        testValue.put(SelfieContract.Selfies.COLUMN_CREATE_LAT, TEST_LAT);
        testValue.put(SelfieContract.Selfies.COLUMN_CREATE_LONG, TEST_LONG);
        testValue.put(SelfieContract.Selfies.COLUMN_UPDATE_DATE, System.currentTimeMillis());
        testValue.put(SelfieContract.Selfies.COLUMN_UPDATE_BY, TEST_USER_GUID[0]);
        testValue.put(SelfieContract.Selfies.COLUMN_UPDATE_LAT, TEST_LAT);
        testValue.put(SelfieContract.Selfies.COLUMN_UPDATE_LONG, TEST_LONG);
        testValue.put(SelfieContract.Selfies.COLUMN_CAPTION,"Caption 2");
        testValue.put(SelfieContract.Selfies.COLUMN_THUMBNAIL_PATH, TEST_SELFIES_GUID[1]);
        testValue.put(SelfieContract.Selfies.COLUMN_ORIGINAL_PATH, TEST_SELFIES_GUID[1]);
        testValues.add(testValue);

        return testValues;
    }

    static List<ContentValues> createRemiderValues() {
        List<ContentValues> testValues = new ArrayList<>();

        // reminder 1
        ContentValues testValue = new ContentValues();
        testValue.put(SelfieContract.Reminders.COLUMN_GUID, TEST_REMINDERS_GUID[0]);
        testValue.put(SelfieContract.Reminders.COLUMN_CREATE_DATE, System.currentTimeMillis());
        testValue.put(SelfieContract.Reminders.COLUMN_CREATE_BY, TEST_USER_GUID[0]);
        testValue.put(SelfieContract.Reminders.COLUMN_CREATE_LAT, TEST_LAT);
        testValue.put(SelfieContract.Reminders.COLUMN_CREATE_LONG, TEST_LONG);
        testValue.put(SelfieContract.Reminders.COLUMN_UPDATE_DATE, System.currentTimeMillis());
        testValue.put(SelfieContract.Reminders.COLUMN_UPDATE_BY, TEST_USER_GUID[0]);
        testValue.put(SelfieContract.Reminders.COLUMN_UPDATE_LAT, TEST_LAT);
        testValue.put(SelfieContract.Reminders.COLUMN_UPDATE_LONG, TEST_LONG);
        testValue.put(SelfieContract.Reminders.COLUMN_INIT_TIME, System.currentTimeMillis());
        testValues.add(testValue);

        // reminder 2
        testValue = new ContentValues();
        testValue.put(SelfieContract.Reminders.COLUMN_GUID, TEST_REMINDERS_GUID[1]);
        testValue.put(SelfieContract.Reminders.COLUMN_CREATE_DATE, System.currentTimeMillis());
        testValue.put(SelfieContract.Reminders.COLUMN_CREATE_BY, TEST_USER_GUID[0]);
        testValue.put(SelfieContract.Reminders.COLUMN_CREATE_LAT, TEST_LAT);
        testValue.put(SelfieContract.Reminders.COLUMN_CREATE_LONG, TEST_LONG);
        testValue.put(SelfieContract.Reminders.COLUMN_UPDATE_DATE, System.currentTimeMillis());
        testValue.put(SelfieContract.Reminders.COLUMN_UPDATE_BY, TEST_USER_GUID[0]);
        testValue.put(SelfieContract.Reminders.COLUMN_UPDATE_LAT, TEST_LAT);
        testValue.put(SelfieContract.Reminders.COLUMN_UPDATE_LONG, TEST_LONG);
        testValue.put(SelfieContract.Reminders.COLUMN_INIT_TIME, System.currentTimeMillis());
        testValues.add(testValue);

        return testValues;
    }

    private void deleteAllRecords() {
        deleteAllFromURI(SelfieContract.Selfies.CONTENT_URI);
        assertTrue(isURIEmpty(SelfieContract.Selfies.CONTENT_URI));
        assertTrue(isURIEmpty(SelfieContract.SelfiesFts.CONTENT_URI));
        deleteAllFromURI(SelfieContract.Reminders.CONTENT_URI);
        assertTrue(isURIEmpty(SelfieContract.Reminders.CONTENT_URI));
    }


    public void test1000ClearProvider() {
        deleteAllRecords();
    }

    public void test1110SelfiesProvider() {

        // keep the id for last record inserted to use in update test
        long updateTestRowID;

        // get test data for two records
        List<ContentValues> testValues = createSelfieValues();

        // delete all the records in the table and test
        deleteAllFromURI(SelfieContract.Selfies.CONTENT_URI);
        assertTrue(isURIEmpty(SelfieContract.Selfies.CONTENT_URI));

        // test insert and read back two values created by
        // createTestObjectValues()
        updateTestRowID = insertAndReadTestValues(testValues, SelfieContract.Selfies.CONTENT_URI);

        // test to check there are two records in the query
        assertEquals(testValues.size(), getURICount(SelfieContract.Selfies.CONTENT_URI, null, null));

        // test update list name
        ContentValues updatedValues = new ContentValues(testValues.get(testValues.size()-1));
        updatedValues.put(SelfieContract.Selfies._ID, updateTestRowID);
        updatedValues.put(SelfieContract.Selfies.COLUMN_CAPTION, "updated caption");

        int count = mContext.getContentResolver().update(ContentUris.withAppendedId(SelfieContract.Selfies.CONTENT_URI, updateTestRowID),
                updatedValues, null, null);

        // check to ensure that only one record was updated
        assertEquals(count, 1);

        // verify the updated values
        Cursor cursor = mContext.getContentResolver().query(
                SelfieContract.Selfies.buildSelfieUri(updateTestRowID), null, null, null, null);
        validateCursor(cursor, updatedValues);

        count = mContext.getContentResolver().delete(SelfieContract.Selfies.CONTENT_URI,
                SelfieContract.Selfies._ID + "= ?",
                new String[]{Long.toString(updateTestRowID)});

        assertEquals(count, 1); // one record was deleted

        // check to ensure that it is no longer in the db
        assertFalse(isRecordExist(SelfieContract.Selfies.CONTENT_URI, updateTestRowID));

        // test to check there is one records in the query
        assertEquals(testValues.size() - 1, getURICount(SelfieContract.Selfies.CONTENT_URI, null, null));

        // clear test effects and insert for future use
        for (int i = 0; i < TEST_SELFIES_GUID.length; i++) {
            TEST_SELFIES_GUID[i] = IDUtils.generateBase64GUIDwNum(USER_NUMBER);
        }
        testValues = createSelfieValues();

        deleteAllFromURI(SelfieContract.Selfies.CONTENT_URI);
        assertTrue(isURIEmpty(SelfieContract.Selfies.CONTENT_URI));
        updateTestRowID = insertAndReadTestValues(testValues, SelfieContract.Selfies.CONTENT_URI);
        assertEquals(testValues.size(), getURICount(SelfieContract.Selfies.CONTENT_URI, null, null));

        // test operations with id
        // query test
        cursor = mContext.getContentResolver().query(
                SelfieContract.Selfies.buildSelfieUri(updateTestRowID), null, null, null, null);
        validateCursor(cursor, testValues.get(testValues.size()-1));
        assertEquals(1, cursor.getCount());

        // test operations with guid
        // query test
        cursor = mContext.getContentResolver()
                .query(SelfieContract.Selfies.buildSelfieUri(testValues.get(testValues.size() - 1).getAsString(
                        SelfieContract.Selfies.COLUMN_GUID)), null, null, null, null);
        validateCursor(cursor, testValues.get(testValues.size()-1));
        assertEquals(1, cursor.getCount());
    }

    public void test1120RemidersProvider() {

        // keep the id for last record inserted to use in update test
        long updateTestRowID;

        // get test data for two records
        List<ContentValues> testValues = createRemiderValues();

        // delete all the records in the table and test
        deleteAllFromURI(SelfieContract.Reminders.CONTENT_URI);
        assertTrue(isURIEmpty(SelfieContract.Reminders.CONTENT_URI));

        // test insert and read back two values created by
        // createTestObjectValues()
        updateTestRowID = insertAndReadTestValues(testValues, SelfieContract.Reminders.CONTENT_URI);

        // test to check there are two records in the query
        assertEquals(testValues.size(), getURICount(SelfieContract.Reminders.CONTENT_URI, null, null));

        // test update list name
        ContentValues updatedValues = new ContentValues(testValues.get(testValues.size()-1));
        updatedValues.put(SelfieContract.Reminders._ID, updateTestRowID);
        updatedValues.put(SelfieContract.Reminders.COLUMN_INIT_TIME, System.currentTimeMillis());

        int count = mContext.getContentResolver().update(ContentUris.withAppendedId(SelfieContract.Reminders.CONTENT_URI, updateTestRowID),
                updatedValues, null, null);

        // check to ensure that only one record was updated
        assertEquals(count, 1);

        // verify the updated values
        Cursor cursor = mContext.getContentResolver().query(
                SelfieContract.Reminders.buildReminderUri(updateTestRowID), null, null, null, null);
        validateCursor(cursor, updatedValues);

        count = mContext.getContentResolver().delete(SelfieContract.Reminders.CONTENT_URI,
                SelfieContract.Reminders._ID + "= ?",
                new String[]{Long.toString(updateTestRowID)});

        assertEquals(count, 1); // one record was deleted

        // check to ensure that it is no longer in the db
        assertFalse(isRecordExist(SelfieContract.Reminders.CONTENT_URI, updateTestRowID));

        // test to check there is one records in the query
        assertEquals(testValues.size() - 1, getURICount(SelfieContract.Reminders.CONTENT_URI, null, null));

        // clear test effects and insert for future use
        for (int i = 0; i < TEST_REMINDERS_GUID.length; i++) {
            TEST_REMINDERS_GUID[i] = IDUtils.generateBase64GUIDwNum(USER_NUMBER);
        }
        testValues = createRemiderValues();

        deleteAllFromURI(SelfieContract.Reminders.CONTENT_URI);
        assertTrue(isURIEmpty(SelfieContract.Reminders.CONTENT_URI));
        updateTestRowID = insertAndReadTestValues(testValues, SelfieContract.Reminders.CONTENT_URI);
        assertEquals(testValues.size(), getURICount(SelfieContract.Reminders.CONTENT_URI, null, null));

        // test operations with id
        // query test
        cursor = mContext.getContentResolver().query(
                SelfieContract.Reminders.buildReminderUri(updateTestRowID), null, null, null, null);
        validateCursor(cursor, testValues.get(testValues.size()-1));
        assertEquals(1, cursor.getCount());

        // test operations with guid
        // query test
        cursor = mContext.getContentResolver()
                .query(SelfieContract.Reminders.buildReminderUri(testValues.get(testValues.size() - 1).getAsString(
                        SelfieContract.Reminders.COLUMN_GUID)), null, null, null, null);
        validateCursor(cursor, testValues.get(testValues.size()-1));
        assertEquals(1, cursor.getCount());
    }

    private void deleteAllFromURI(Uri contentUri) {
        mContext.getContentResolver().delete(contentUri, null, null);
    }

    private boolean isURIEmpty(Uri contentUri) {
        Cursor cursor;
        cursor = mContext.getContentResolver().query(contentUri, null, null,
                null, null);
        boolean retVar = cursor.getCount() == 0;
        cursor.close();
        return retVar;
    }

    private boolean isRecordExist(Uri contentUri, long rowID) {
        Cursor cursor;
        cursor = mContext.getContentResolver().query(
                ContentUris.withAppendedId(contentUri, rowID), null, null,
                null, null);
        boolean retVar = cursor.getCount() > 0;
        cursor.close();
        return retVar;
    }

    private int getURICount(Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = mContext.getContentResolver().query(uri,
                new String[]{" count(*) as count "}, selection,
                selectionArgs, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return 0;
        } else {
            cursor.moveToFirst();
            int result = cursor.getInt(0);
            cursor.close();
            return result;
        }
    }

    private long insertAndReadTestValues(List<ContentValues> testValues, Uri contentUri) {

        long lastRowID = 0;

        for (ContentValues testValue : testValues) {

            Uri uri = mContext.getContentResolver().insert(contentUri,
                    testValue);
            long rowId = ContentUris.parseId(uri);

            // Verify we got a row back.
            assertTrue(rowId != -1);
            Log.d(TAG, "New " + contentUri + " row id: " + rowId);
            // Log.d(TAG, "New values for " + contentUri + ": " + testValue);

            // Data's inserted. IN THEORY. Now pull some out to stare at it and
            // verify it made the round trip.

            Cursor cursor = mContext.getContentResolver().query(uri,
                    null, null,null, null);

            // Log.d(TAG, "Cursor: " + contentUri + ": " + DatabaseUtils.dumpCursorToString(cursor));

            lastRowID = rowId;
            assertTrue(cursor.getCount() == 1);
            validateCursor(cursor, testValue);

            // validate the document was saved in cbl
            Document document = CBLDatabaseHelper.getInstance().getDatabase().getExistingDocument(testValue.getAsString(SelfieContract.GuidColumns.COLUMN_GUID));
            assertTrue(null != document);

            validateDocument(document, testValue);


        }
        return lastRowID;
    }

    private void validateDocument(Document document, ContentValues testValue) {
        Log.d(TAG, "CBL document: " + document);

        Map<String, Object> properties = document.getProperties();
        assertTrue(null != properties);

        Set<Map.Entry<String, Object>> valueSet = testValue.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            if ((null != columnName) && (!columnName.equals(SelfieContract.GuidColumns.COLUMN_GUID))) {
                String expectedValue = entry.getValue() != null ? entry.getValue().toString() : "";
                Log.d(TAG, entry.getKey() + ": expected value: " + expectedValue + " cbl document property value: " + properties.get(columnName));
                assertEquals(expectedValue, properties.get(columnName).toString());
            }
        }


    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        // Log.d(TAG, "validateCursor:valueCursor " + DatabaseUtils.dumpCurrentRowToString(valueCursor));
        // Log.d(TAG, "validateCursor:expectedValues " + expectedValues);

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
// TODO: improve test to handle join columns more robustly
// removed check for column that is in cursor but not in values due to joins
// assertFalse(idx == -1);
            if (idx != -1) {
                String expectedValue = entry.getValue() != null ? entry.getValue().toString() : null;
                // Log.d(TAG, entry.getKey() + ": expected value: " + expectedValue + " cursor value: " + valueCursor.getString(idx));
                assertEquals(expectedValue, valueCursor.getString(idx));
            }
        }
        valueCursor.close();
    }
}
