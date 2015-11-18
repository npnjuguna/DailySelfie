package com.njuguna.dailyselfie.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.HashSet;
import java.util.Set;

public final class SelfieContract {

	public static final String CONTENT_AUTHORITY = "com.njuguna.dailyselfie.provider";

	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	public static final String PATH_SELFIES = Selfies.TABLE_NAME;
	public static final String PATH_SELFIES_FTS = SelfiesFts.TABLE_NAME;
	public static final String PATH_REMIDERS = Reminders.TABLE_NAME;


    public interface LocationAuditSyncColumns {

		/**
		 * Global Unique Identifier.
		 */
		String COLUMN_GUID = "guid";
		/**
		 * Create date.
		 */
		String COLUMN_CREATE_DATE = "cDate";
		/**
		 * Create by user GUID.
		 */
		String COLUMN_CREATE_BY = "cBy";
		/**
		 * Create latitude.
		 */
		String COLUMN_CREATE_LAT = "cLat";
		/**
		 * Create longitude.
		 */
		String COLUMN_CREATE_LONG = "cLng";
		/**
		 * Create android id.
		 */
		String COLUMN_UPDATE_DATE = "uDate";
		/**
		 * Update by user GUID.
		 */
		String COLUMN_UPDATE_BY = "uBy";
		/**
		 * Update latitude.
		 */
		String COLUMN_UPDATE_LAT = "uLat";
		/**
		 * Update longitude.
		 */
		String COLUMN_UPDATE_LONG = "uLng";
	}

	public interface GuidColumns {

		/**
		 * guid.
		 */
		String COLUMN_GUID = "guid";

	}

	public static final class Selfies implements BaseColumns, LocationAuditSyncColumns {

		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_SELFIES).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/"
				+ CONTENT_AUTHORITY + "/" + PATH_SELFIES;

		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/"
				+ CONTENT_AUTHORITY + "/" + PATH_SELFIES;

		/**
		 * caption.
		 */
		public static final String COLUMN_CAPTION = "caption";
		/**
		 * thumbnail_path.
		 */
		public static final String COLUMN_THUMBNAIL_PATH = "thumbnail_path";
		/**
		 * original_path.
		 */
		public static final String COLUMN_ORIGINAL_PATH = "original_path";
		/**
		 * saved_path.
		 */
		public static final String COLUMN_SAVED_PATH = "saved_path";
		/**
		 * saved_image.
		 */
		public static final String COLUMN_SAVED_IMAGE = "saved_image";
		/**
		 * Name of selfies table
		 */
		public static final String TABLE_NAME = "selfies";
        public static final String CBL_DOC_TYPE = "slf";

		public static Uri buildSelfieUri(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}

		public static Uri buildSelfieUri(String guid) {
			return SelfieUris.withAppendedGUID(CONTENT_URI, guid);
		}

	}

	public static final class SelfiesFts implements GuidColumns {

		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_SELFIES_FTS).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/"
				+ CONTENT_AUTHORITY + "/" + PATH_SELFIES_FTS;

		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/"
				+ CONTENT_AUTHORITY + "/" + PATH_SELFIES_FTS;

		/**
		 * caption.
		 */
		public static final String COLUMN_CAPTION = "caption";
		/**
		 * thumbnail_path.
		 */
		public static final String COLUMN_THUMBNAIL_PATH = "thumbnail_path";
		/**
		 * original_path.
		 */
		public static final String COLUMN_ORIGINAL_PATH = "original_path";
		/**
		 * Name of selfies_fts table
		 */
		public static final String TABLE_NAME = "selfies_fts";
		public static final Set<String> FTS_KEYS = new HashSet<>();
		static {
			FTS_KEYS.add(BaseColumns._ID);
			FTS_KEYS.add(SelfieContract.GuidColumns.COLUMN_GUID);
			FTS_KEYS.add(SelfieContract.SelfiesFts.COLUMN_CAPTION);
			FTS_KEYS.add(SelfieContract.SelfiesFts.COLUMN_THUMBNAIL_PATH);
			FTS_KEYS.add(SelfieContract.SelfiesFts.COLUMN_ORIGINAL_PATH);
		}

		public static Uri buildSelfieFtsUri(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}

		public static Uri buildSelfieFtsUri(String guid) {
			return SelfieUris.withAppendedGUID(CONTENT_URI, guid);
		}

	}

	public static final class Reminders implements BaseColumns,
			LocationAuditSyncColumns {

		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_REMIDERS).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/"
				+ CONTENT_AUTHORITY + "/" + PATH_REMIDERS;

		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/"
				+ CONTENT_AUTHORITY + "/" + PATH_REMIDERS;

		/**
		 * init_time.
		 */
		public static final String COLUMN_INIT_TIME = "init_time";

		/**
		 * Name of reminders table
		 */
		public static final String TABLE_NAME = "reminders";
        public static final String CBL_DOC_TYPE = "rmd";

		public static Uri buildReminderUri(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}

		public static Uri buildReminderUri(String guid) {
			return SelfieUris.withAppendedGUID(CONTENT_URI, guid);
		}

	}

}
