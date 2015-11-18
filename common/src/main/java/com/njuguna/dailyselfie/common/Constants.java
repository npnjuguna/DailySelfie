package com.njuguna.dailyselfie.common;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Constants {
    public static final int AUTH_TYPE_BASIC = 1;
    public static final int AUTH_TYPE_GOOGLE_PLUS = 2;
    public static final int AUTH_TYPE_TWITTER_DIGITS = 3;
    public static final int AUTH_TYPE_FACEBOOK = 4;

    public static final String DOC_TYPE_USER = "u";
    public static final String DOC_TYPE_SESSION = "s";
    public static final String DOC_TYPE_USER_MAP = "um";

    public static final String ID_PREFIX_BASIC_USER_MAP = "ba::";
    public static final String ID_PREFIX_GOOGLE_USER_MAP = "gm::";
    public static final String ID_PREFIX_TWITTER_DIGITS_USER_MAP = "td::";
    public static final String ID_PREFIX_FACEBOOK_USER_MAP = "fb::";

    public static final String PROPERTY_DOC_TYPE = "t";
    public static final String PROPERTY_DELETE_TIMESTAMP = "dlTime";
    public static final String PROPERTY_DELETE_BY = "dlBy";
    public static final String PROPERTY_DELETE_LAT = "dlLat";
    public static final String PROPERTY_DELETE_LONG = "dlLng";
    public static final String PROPERTY_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static final int HTTP_CONNECTION_TIMEOUT_MILLIS = 60 * 1000;
    public static final String COUCHBASE_DATABASE_NAME = "selfie";

    public static final int MAX_WIDTH_PIXELS = 2560;
    public static final int MAX_HEIGHT_PIXELS = 2560;
    public static final int MAX_IMAGE_SIZE_BYTES = 11059200;
    public static final int THUMBNAIL_WIDTH_PIXELS = 256;
    public static final int THUMBNAIL_HEIGHT_PIXELS = 256;

    public final static DateFormat DATE_FORMATTER = DateFormat.getDateInstance();
    public final static DateFormat TIME_FORMATTER = DateFormat.getTimeInstance(DateFormat.SHORT);
    public final static DateFormat DATE_TIME_FORMATTER = DateFormat.getDateTimeInstance();



    public final static NumberFormat DECIMAL_FORMATTER = DecimalFormat.getInstance();
    public final static NumberFormat INTERGER_FORMATTER = NumberFormat.getIntegerInstance();

}
