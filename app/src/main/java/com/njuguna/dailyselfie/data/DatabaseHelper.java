/*
 * Copyright (c) 2014 Patrick Njuguna <njuguna@njuguna.com>
 *
 */

package com.njuguna.dailyselfie.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.njuguna.dailyselfie.util.SqlParser;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * Helper class for managing the SQLite database. Creates the database and
 * handles upgrades
 * 
 * @author Patrick Njuguna <njuguna@njuguna.com>
 * 
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	/**
	 * Tag for logging
	 */
	private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();

	/**
	 * Name of the database
	 */
	public static final String DATABASE_NAME = "smartfarmer";

	/**
	 * Database version. With any change to the database schema, this number
	 * must increase
	 */
	private static final int DATABASE_VERSION = 12;

	/**
	 * assets directory that contains the database creation and initialization
	 * script
	 */
	private static final String ASSETS_SQL_DIR = "sql";

	/**
	 * creation and initialization script
	 */
	private static final String INIT_SQL_FILE = "init.sql";
	
	/**
	 * application context member variable
	 */
	private Context mContext;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            Application context
	 */
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
            Log.i(LOG_TAG, "executing sql script to create tables and import data");
            execSqlFile( INIT_SQL_FILE, db );
        } catch( IOException exception ) {
            throw new RuntimeException("init scripts import failed", exception );
        }
	
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(LOG_TAG, "Upgrading smartfarmer database from version " + oldVersion
				+ " to " + newVersion);

		int upgradeVersion = oldVersion;

		// Pattern for upgrade blocks:
		//
		//    if (upgradeVersion == [the DATABASE_VERSION you set] - 1) {
		//        .. your upgrade logic..
		//        upgradeVersion = [the DATABASE_VERSION you set]
		//    }

        if (upgradeVersion < 9) {
            CBLDatabaseHelper.reCreateDatabase();
            onCreate(db);
            upgradeVersion = 9;
            Log.i(LOG_TAG, "Upgraded smartfarmer database to version " + upgradeVersion);
        }

        if (upgradeVersion == 9) {
            db.beginTransaction();
            try {
                db.execSQL("ALTER TABLE tasks ADD description TEXT");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            upgradeVersion = 10;
            Log.i(LOG_TAG, "Upgraded smartfarmer database to version " + upgradeVersion);
        }

		if (upgradeVersion == 10) {
			db.beginTransaction();
			try {
				db.execSQL("DROP INDEX IF EXISTS toi_types_oId_ndx; CREATE INDEX IF NOT EXISTS toi_types_oId_ndx ON toi_types (oId)");
				db.execSQL("DROP INDEX IF EXISTS toi_types_cBy_ndx; CREATE INDEX IF NOT EXISTS toi_types_cBy_ndx ON toi_types (cBy)");
				db.execSQL("DROP INDEX IF EXISTS toi_types_ic_ndx; CREATE INDEX IF NOT EXISTS toi_types_ic_ndx ON toi_types (ic)");
				db.execSQL("DROP INDEX IF EXISTS tois_cBy_ndx; CREATE INDEX IF NOT EXISTS tois_cBy_ndx ON tois (cBy)");
				db.execSQL("DROP INDEX IF EXISTS tois_oId_ndx; CREATE INDEX IF NOT EXISTS tois_oId_ndx ON tois (oId)");
				db.execSQL("DROP INDEX IF EXISTS uoms_cBy_ndx; CREATE INDEX IF NOT EXISTS uoms_cBy_ndx ON uoms (cBy)");
				db.execSQL("DROP INDEX IF EXISTS uoms_oId_ndx; CREATE INDEX IF NOT EXISTS uoms_oId_ndx ON uoms (oId)");
				db.execSQL("DROP INDEX IF EXISTS uoms_ic_ndx; CREATE INDEX IF NOT EXISTS uoms_ic_ndx ON uoms (ic)");
				db.execSQL("DROP INDEX IF EXISTS uom_conversions_cBy_ndx; CREATE INDEX IF NOT EXISTS uom_conversions_cBy_ndx ON uom_conversions (cBy)");
				db.execSQL("DROP INDEX IF EXISTS uom_conversions_oId_ndx; CREATE INDEX IF NOT EXISTS uom_conversions_oId_ndx ON uom_conversions (oId)");
				db.execSQL("DROP INDEX IF EXISTS uom_conversions_ic_ndx; CREATE INDEX IF NOT EXISTS uom_conversions_ic_ndx ON uom_conversions (ic)");
				db.execSQL("DROP INDEX IF EXISTS commodity_types_cBy_ndx; CREATE INDEX IF NOT EXISTS commodity_types_cBy_ndx ON commodity_types (cBy)");
				db.execSQL("DROP INDEX IF EXISTS commodity_types_oId_ndx; CREATE INDEX IF NOT EXISTS commodity_types_oId_ndx ON commodity_types (oId)");
				db.execSQL("DROP INDEX IF EXISTS commodity_types_ic_ndx; CREATE INDEX IF NOT EXISTS commodity_types_ic_ndx ON commodity_types (ic)");
				db.execSQL("DROP INDEX IF EXISTS commodities_cBy_ndx; CREATE INDEX IF NOT EXISTS commodities_cBy_ndx ON commodities (cBy)");
				db.execSQL("DROP INDEX IF EXISTS commodities_oId_ndx; CREATE INDEX IF NOT EXISTS commodities_oId_ndx ON commodities (oId)");
				db.execSQL("DROP INDEX IF EXISTS commodities_ic_ndx; CREATE INDEX IF NOT EXISTS commodities_ic_ndx ON commodities (ic)");
				db.execSQL("DROP INDEX IF EXISTS commodity_varieties_cBy_ndx; CREATE INDEX IF NOT EXISTS commodity_varieties_cBy_ndx ON commodity_varieties (cBy)");
				db.execSQL("DROP INDEX IF EXISTS commodity_varieties_oId_ndx; CREATE INDEX IF NOT EXISTS commodity_varieties_oId_ndx ON commodity_varieties (oId)");
				db.execSQL("DROP INDEX IF EXISTS commodity_varieties_ic_ndx; CREATE INDEX IF NOT EXISTS commodity_varieties_ic_ndx ON commodity_varieties (ic)");
				db.execSQL("DROP INDEX IF EXISTS task_types_cBy_ndx; CREATE INDEX IF NOT EXISTS task_types_cBy_ndx ON task_types (cBy)");
				db.execSQL("DROP INDEX IF EXISTS task_types_oId_ndx; CREATE INDEX IF NOT EXISTS task_types_oId_ndx ON task_types (oId)");
				db.execSQL("DROP INDEX IF EXISTS task_types_ic_ndx; CREATE INDEX IF NOT EXISTS task_types_ic_ndx ON task_types (ic)");
				db.execSQL("DROP INDEX IF EXISTS attendances_cBy_ndx; CREATE INDEX IF NOT EXISTS attendances_cBy_ndx ON attendances (cBy)");
				db.execSQL("DROP INDEX IF EXISTS attendances_oId_ndx; CREATE INDEX IF NOT EXISTS attendances_oId_ndx ON attendances (oId)");
				db.execSQL("DROP INDEX IF EXISTS tasks_cBy_ndx; CREATE INDEX IF NOT EXISTS tasks_cBy_ndx ON tasks (cBy)");
				db.execSQL("DROP INDEX IF EXISTS tasks_oId_ndx; CREATE INDEX IF NOT EXISTS tasks_oId_ndx ON tasks (oId)");
				db.execSQL("DROP INDEX IF EXISTS transactions_cBy_ndx; CREATE INDEX IF NOT EXISTS transactions_cBy_ndx ON transactions (cBy)");
				db.execSQL("DROP INDEX IF EXISTS transactions_oId_ndx; CREATE INDEX IF NOT EXISTS transactions_oId_ndx ON transactions (oId)");
				db.execSQL("DROP INDEX IF EXISTS splits_cBy_ndx; CREATE INDEX IF NOT EXISTS splits_cBy_ndx ON splits (cBy)");
				db.execSQL("DROP INDEX IF EXISTS splits_oId_ndx; CREATE INDEX IF NOT EXISTS splits_oId_ndx ON splits (oId)");
				db.execSQL("DROP INDEX IF EXISTS comments_cBy_ndx; CREATE INDEX IF NOT EXISTS comments_cBy_ndx ON comments (cBy)");
				db.execSQL("DROP INDEX IF EXISTS comments_oId_ndx; CREATE INDEX IF NOT EXISTS comments_oId_ndx ON comments (oId)");
				db.execSQL("DROP INDEX IF EXISTS messages_cBy_ndx; CREATE INDEX IF NOT EXISTS messages_cBy_ndx ON messages (cBy)");
				db.execSQL("DROP INDEX IF EXISTS messages_oId_ndx; CREATE INDEX IF NOT EXISTS messages_oId_ndx ON messages (oId)");
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			upgradeVersion = 11;
			Log.i(LOG_TAG, "Upgraded smartfarmer database to version " + upgradeVersion);
		}

		if (upgradeVersion == 11) {
			db.beginTransaction();
			try {
				db.execSQL("ALTER TABLE splits ADD u_val REAL default 1");
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			upgradeVersion = 12;
			Log.i(LOG_TAG, "Upgraded smartfarmer database to version " + upgradeVersion);
		}

	}

	/**
	 * method to read from sql statements file and execute them on sqlite db
	 * 
	 * @param sqlFile
	 * @param db
	 * @throws SQLException
	 * @throws IOException
	 */
	protected void execSqlFile(String sqlFile, SQLiteDatabase db)
			throws SQLException, IOException {
		Log.i(LOG_TAG, MessageFormat.format("  exec sql file: {0}", sqlFile));
		for (String sqlInstruction : SqlParser.parseSqlFile(ASSETS_SQL_DIR + "/"
				+ sqlFile, mContext.getAssets())) {
			Log.i(LOG_TAG, MessageFormat.format("    sql: {0}", sqlInstruction));
			db.execSQL(sqlInstruction);
		}
	}

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}
