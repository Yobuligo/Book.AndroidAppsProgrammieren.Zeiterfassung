package com.yobuligo.zeiterfassung.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.sql.Time;
import java.util.Locale;

public class TimeDataProvider extends ContentProvider {

    private static final UriMatcher _URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private DbHelper dbHelper = null;
    private static final String _ID_WHERE = BaseColumns._ID + "=?";
    private static final String _NOT_FINISHED_WHERE = "IFNULL(" + TimeDataContract.TimeData.Columns.END_TIME + ",'')=''";

    static {
        //Lookup for the list
        _URI_MATCHER.addURI(
                TimeDataContract.AUTHORITY, //base URI
                TimeDataContract.TimeData.CONTENT_DIRECTORY, //subfolder of entity
                TimeDataTable.ITEM_LIST_ID  //unique ID
        );

        _URI_MATCHER.addURI(
                TimeDataContract.AUTHORITY, //base URI
                TimeDataContract.TimeData.CONTENT_DIRECTORY + "/#", //subfolder of entity with a specific entity ID (#)
                TimeDataTable.ITEM_ID  //unique ID
        );

        _URI_MATCHER.addURI(
                TimeDataContract.AUTHORITY,
                TimeDataContract.TimeData.NOT_FINISHED_CONTENT_DIRECTORY,
                TimeDataTable.NOT_FINISHED_ITEM_ID
        );
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //Resolve the Uri
        final int uriType = _URI_MATCHER.match(uri);
        Cursor data;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //determine action for the requested URI
        switch (uriType) {
            case TimeDataTable.ITEM_LIST_ID:
                data = db.query(TimeDataTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TimeDataTable.ITEM_ID:
                final long id = ContentUris.parseId(uri);
                data = db.query(TimeDataTable.TABLE_NAME, projection, _ID_WHERE, idAsArray(id), null, null, null);
                break;
            case TimeDataTable.NOT_FINISHED_ITEM_ID:
                data = db.query(TimeDataTable.TABLE_NAME, projection, _NOT_FINISHED_WHERE, selectionArgs, null, null, sortOrder);
                break;
            default:
                //raise exception as the URI is unknown
                throw new IllegalArgumentException(String.format(Locale.GERMANY, "Unbekannte URI: %S", uri));
        }

        //entry successful deleted
        if (data != null) {
            data.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return data;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        //Resolve the Uri
        final int uriType = _URI_MATCHER.match(uri);
        String type = null;

        //determine the data type
        switch (uriType) {
            case TimeDataTable.ITEM_LIST_ID:
                type = TimeDataContract.TimeData.CONTENT_TYPE;
                break;
            case TimeDataTable.ITEM_ID:
            case TimeDataTable.NOT_FINISHED_ITEM_ID:
                type = TimeDataContract.TimeData.CONTENT_ITEM_TYPE;
        }

        return type;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        //Resolve the Uri
        final int uriType = _URI_MATCHER.match(uri);

        //Initialize the values
        Uri insertUri = null;
        long newItemId = -1;

        //determine action for the requested URI
        switch (uriType) {
            case TimeDataTable.ITEM_LIST_ID:
            case TimeDataTable.ITEM_ID:
            case TimeDataTable.NOT_FINISHED_ITEM_ID:
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                newItemId = db.insert(TimeDataTable.TABLE_NAME, null, values);
                db.close();
                break;
            default:
                //raise exception as the URI is unknown
                throw new IllegalArgumentException(String.format(Locale.GERMANY, "Unbekannte URI: %S", uri));
        }

        //entry successful added
        if (newItemId > 0) {
            //create URI
            insertUri = ContentUris.withAppendedId(TimeDataContract.TimeData.CONTENT_URI, newItemId);

            //Notify about the data changes
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return insertUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        //Resolve the Uri
        final int uriType = _URI_MATCHER.match(uri);
        int deleteItems = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //determine action for the requested URI
        switch (uriType) {
            case TimeDataTable.ITEM_LIST_ID:
                deleteItems = db.delete(TimeDataTable.TABLE_NAME, selection, selectionArgs);
                db.close();
                break;
            case TimeDataTable.ITEM_ID:
                final long id = ContentUris.parseId(uri);
                final String idWhere = BaseColumns._ID + "=?";
                final String[] idArgs = new String[]{String.valueOf(id)};
                deleteItems = db.delete(TimeDataTable.TABLE_NAME, idWhere, idArgs);
                db.close();
                break;
            case TimeDataTable.NOT_FINISHED_ITEM_ID:
                deleteItems = db.delete(TimeDataTable.TABLE_NAME, _NOT_FINISHED_WHERE, selectionArgs);
                db.close();
                break;
            default:
                //raise exception as the URI is unknown
                throw new IllegalArgumentException(String.format(Locale.GERMANY, "Unbekannte URI: %S", uri));
        }

        //entry successful deleted
        if (deleteItems > 0) {
            //Notify about the data changes
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deleteItems;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        //Resolve the Uri
        final int uriType = _URI_MATCHER.match(uri);
        int updatedItems = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //determine action for the requested URI
        switch (uriType) {
            case TimeDataTable.ITEM_LIST_ID:
                updatedItems = db.update(TimeDataTable.TABLE_NAME, values, selection, selectionArgs);
                db.close();
                break;
            case TimeDataTable.ITEM_ID:
                final long id = ContentUris.parseId(uri);
                final String idWhere = BaseColumns._ID + "=?";
                final String[] idArgs = new String[]{String.valueOf(id)};
                updatedItems = db.update(TimeDataTable.TABLE_NAME, values, idWhere, idArgs);
                db.close();
                break;
            case TimeDataTable.NOT_FINISHED_ITEM_ID:
                updatedItems = db.update(TimeDataTable.TABLE_NAME, values, _NOT_FINISHED_WHERE, null);
                db.close();
                break;
            default:
                //raise exception as the URI is unknown
                throw new IllegalArgumentException(String.format(Locale.GERMANY, "Unbekannte URI: %S", uri));
        }

        //entry successful updated
        if (updatedItems > 0) {
            //Notify about the data changes
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updatedItems;
    }

    private String[] idAsArray(long id) {
        return new String[]{String.valueOf(id)};
    }
}
