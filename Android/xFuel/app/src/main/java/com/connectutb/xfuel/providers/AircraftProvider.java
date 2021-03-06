package com.connectutb.xfuel.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.connectutb.xfuel.tools.DbManager;


public class AircraftProvider extends ContentProvider implements AircraftContract {

    private DbManager db;
    private final UriMatcher matcher = new UriMatcher(0);

    private final static int URI_ALL_AIRCRAFT = 1;
    private final static int URI_ONE_AIRCRAFT = 2;

    public AircraftProvider(){
        matcher.addURI(AIRCRAFT_AUTHORITY, AIRCRAFT_ITEM + "/#", URI_ONE_AIRCRAFT);
        matcher.addURI(AIRCRAFT_AUTHORITY, AIRCRAFT_ITEM, URI_ALL_AIRCRAFT);
    }

    @Override
    public boolean onCreate() {
        db = new DbManager(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor c;
        SQLiteDatabase database = db.getWritableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_AIRCRAFT);
        switch (matcher.match(uri)){
            case URI_ONE_AIRCRAFT:
                long id = ContentUris.parseId(uri);
                c = db.findAircraftById(id);
                break;
            case URI_ALL_AIRCRAFT:
                c = qb.query(database, projection, selection, selectionArgs, null, null, null, null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        try {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (NullPointerException ex){
            return null;
        }
        return c;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        Uri result = null;

        SQLiteDatabase database = db.getWritableDatabase();
        long rowID = database.insert(TABLE_AIRCRAFT, null, contentValues);
        if (rowID > 0) {
            result = ContentUris.withAppendedId(AIRCRAFT_CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(result, null);
        }

        return result;
    }

    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase database = db.getWritableDatabase();
        int count;

        switch (matcher.match(uri)){
            case URI_ONE_AIRCRAFT:
                String segment = uri.getLastPathSegment();
                String whereClause = AIRCRAFT_ID + "=" + segment
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
                count = database.delete(TABLE_AIRCRAFT, whereClause, whereArgs);
                break;
            case URI_ALL_AIRCRAFT:
                count = database.delete(TABLE_AIRCRAFT, where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (count >0 ){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
