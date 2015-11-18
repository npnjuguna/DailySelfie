package com.njuguna.dailyselfie.util;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.BaseColumns;

import com.njuguna.dailyselfie.common.Constants;
import com.njuguna.dailyselfie.data.SelfieContract;
import com.njuguna.dailyselfie.model.Selfie;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataUtils {

    public static Map<String, Object> makeMapFromContentValues(ContentValues contentValues) {
        Map<String, Object> mapFromContentValues = new HashMap<String, Object>();
        Set<Map.Entry<String, Object>> valueSet = contentValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            if ((!entry.getKey().equals(BaseColumns._ID))
                    && (!entry.getKey().equals(SelfieContract.GuidColumns.COLUMN_GUID))
                    && (!entry.getKey().equals(SelfieContract.Selfies.COLUMN_SAVED_IMAGE))
                    ) {
                mapFromContentValues.put(entry.getKey(), entry.getValue());
            }
        }
        return mapFromContentValues;
    }

    public static ContentValues extractFtsValues(ContentValues values) {
        ContentValues ftsValues = new ContentValues(values);

        for (Map.Entry<String, Object> entry : values.valueSet()) {
            if (!SelfieContract.SelfiesFts.FTS_KEYS.contains(entry.getKey())) {
                ftsValues.remove(entry.getKey());
            }
        }

        return ftsValues;
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(Constants.PROPERTY_DATE_FORMAT);
        Calendar calendar = GregorianCalendar.getInstance();
        return dateFormatter.format(calendar.getTime());
    }

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static File createOriginalImageFile(Selfie selfie) throws IOException {
        // Create an image file name
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir + File.separator + selfie.getGuid() + ".jpg");
        image.createNewFile();

        // Save a file: path for use with ACTION_VIEW intents
        selfie.setOriginalPath(image.getAbsolutePath());
        return image;
    }

    public static File createSavedImageFile(Selfie selfie) throws IOException {
        // Create an image file name
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir + File.separator + selfie.getGuid() + "_saved" + ".jpg");
        image.createNewFile();

        // Save a file: path for use with ACTION_VIEW intents
        selfie.setSavedPath(image.getAbsolutePath());
        return image;
    }

    public static File createThumbnailImageFile(Selfie selfie) throws IOException {
        // Create an image file name
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir + File.separator + selfie.getGuid() + "_thumbnail" + ".jpg");
        image.createNewFile();

        // Save a file: path for use with ACTION_VIEW intents
        selfie.setThumbnailPath(image.getAbsolutePath());
        return image;
    }

    public static void saveThumbnailImage(Bitmap image, Selfie selfie) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(createThumbnailImageFile(selfie));
            image.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveImage(Bitmap image, Selfie selfie) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(createSavedImageFile(selfie));
            image.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap generateSavedBitmap(Bitmap original) {
        int scaleFactor = Math.min(original.getWidth()/Constants.MAX_WIDTH_PIXELS, original.getHeight()/Constants.MAX_HEIGHT_PIXELS);
        if (scaleFactor > 0) {
            return Bitmap.createScaledBitmap(original, original.getWidth() / scaleFactor, original.getHeight() / scaleFactor, false);
        } else {
            return original;
        }
    }

    public static Bitmap generateThumbnailBitmap(Bitmap original) {
        int scaleFactor = Math.min(original.getWidth()/Constants.THUMBNAIL_WIDTH_PIXELS, original.getHeight()/Constants.THUMBNAIL_HEIGHT_PIXELS);
        if (scaleFactor > 0) {
            return Bitmap.createScaledBitmap(original, original.getWidth() / scaleFactor, original.getHeight() / scaleFactor, false);
        } else {
            return original;
        }
    }
}
