package com.njuguna.dailyselfie;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.couchbase.lite.Database;
import com.njuguna.dailyselfie.data.CBLDatabaseHelper;
import com.njuguna.dailyselfie.data.DatabaseHelper;

public class TestDB extends AndroidTestCase {

	public static final String TAG = TestDB.class.getSimpleName();

    public void testCreateDB() throws Throwable {
        mContext.deleteDatabase(DatabaseHelper.DATABASE_NAME);
        SQLiteDatabase db = new DatabaseHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();

        CBLDatabaseHelper cblDatabaseHelper;
        cblDatabaseHelper = CBLDatabaseHelper.init(mContext);

        assertEquals(true, null != cblDatabaseHelper.getDatabase());
        assertEquals(true, cblDatabaseHelper.getDatabase().exists());
        cblDatabaseHelper.getDatabase().delete();
        assertEquals(false, cblDatabaseHelper.getDatabase().exists());
        Database cblDatabase;
        cblDatabase = cblDatabaseHelper.createDatabase();
        assertEquals(true, cblDatabase.exists());
        assertEquals(true, cblDatabaseHelper.getDatabase().exists());


        Log.d(TAG, "testCreateDB() run");
    }

}
