package com.njuguna.dailyselfie.data;

import android.content.ContentUris;
import android.net.Uri;

public class SelfieUris extends ContentUris {
	public static String parseGUID(Uri contentUri) {
		return contentUri.getLastPathSegment();
    }
	public static Uri.Builder appendGUID(Uri.Builder builder, String guid) {
        return builder.appendEncodedPath(guid);
    }
	public static Uri withAppendedGUID(Uri contentUri, String guid) {
        return appendGUID(contentUri.buildUpon(), guid).build();
    }
}
