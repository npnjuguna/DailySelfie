package com.njuguna.dailyselfie.document;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;
import com.njuguna.dailyselfie.common.Constants;

import java.util.Map;

public class CRUDWrapper {
    public static Document createDocument(Database database, Map<String, Object> properties) throws CouchbaseLiteException {
        Document document = database.createDocument();
        document.putProperties(properties);
        return document;
    }

    public static Document createDocumentWithId(Database database, String docId, Map<String, Object> properties) throws CouchbaseLiteException {
        Document document = database.getDocument(docId);
        document.putProperties(properties);
        return document;
    }

    public static Document getDocument(Database database, String docId) {
        return database.getDocument(docId);
    }

    public static Document getExistingDocument(Database database, String docId) {
        return database.getExistingDocument(docId);
    }

    public static Boolean updatePutDocument(Database database, String docId, Map<String, Object> properties) throws CouchbaseLiteException {
        Document document = database.getExistingDocument(docId);
        if (document != null) {
            document.putProperties(properties);
            return true;
        } else {
            return false;
        }
    }

    public static Boolean updateDocument(Database database, String docId, final Map<String, Object> properties) throws CouchbaseLiteException {
        Document document = database.getExistingDocument(docId);
        if (document != null) {
            document.update(new Document.DocumentUpdater() {
                @Override
                public boolean update(UnsavedRevision newRevision) {
                    Map<String, Object> updatedProperties = newRevision.getUserProperties();
                    for (Map.Entry<String, Object> entry : properties.entrySet()) {
                        updatedProperties.put(entry.getKey(), entry.getValue());
                    }
                    newRevision.setUserProperties(updatedProperties);
                    return true;
                }
            });
            return true;
        } else {
            return false;
        }
    }

    public static Boolean deletePreserve(Database database, String docId, final String by, final String lat, final String lng) throws CouchbaseLiteException {
        Document document = database.getExistingDocument(docId);
        if (document != null) {
            document.update(new Document.DocumentUpdater() {
                @Override
                public boolean update(UnsavedRevision newRevision) {
                    newRevision.setIsDeletion(true);
                    Map<String, Object> updatedProperties = newRevision.getUserProperties();
                    updatedProperties.put(Constants.PROPERTY_DELETE_TIMESTAMP, System.currentTimeMillis());
                    if ((null != by) && (by.isEmpty())) {
                        updatedProperties.put(Constants.PROPERTY_DELETE_BY, by);
                    }
                    if ((null != lat) && (!lat.isEmpty())) {
                        updatedProperties.put(Constants.PROPERTY_DELETE_LAT, lat);
                    }
                    if ((null != lng) && (!lng.isEmpty())) {
                        updatedProperties.put(Constants.PROPERTY_DELETE_LONG, lng);
                    }
                    newRevision.setUserProperties(updatedProperties);
                    return true;
                }
            });
            return true;
        } else {
            return false;
        }
    }

    public static Boolean deleteNonPreserve(Database database, String docId) throws CouchbaseLiteException {
        Document document = database.getExistingDocument(docId);
        if (document != null) {
            document.delete();
            return true;
        } else {
            return false;
        }
    }
}

