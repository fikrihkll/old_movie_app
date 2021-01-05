package com.refraginc.cinemovie.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

public class TvProvider extends ContentProvider {
    public static final String KEY_MID = "mid";
    public static final String KEY_TITLE = "title";
    public static final String KEY_PATH = "path";
    public static final String KEY_DATE = "date";
    public static final String KEY_VOTE = "vote";
    static final String PROVIDER_NAME = "com.refraginc.cinemovie.contentprovider.TvProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/data";
    public static final Uri CONTENT_URI = Uri.parse( URL );
    static final int TV = 1;
    static final int TV_ID = 2;
    static final UriMatcher uriMatcher;
    static final String DATABASE_NAME = "CTVDB";
    static final String TABLE_NAME = "TV";
    static final int DATABASE_VERSION = 1;
    // column tables
    static final String _ID = "_id";
    static final String CREATE_DB_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + _ID + " INTEGER PRIMARY KEY," + KEY_MID + " TEXT," + KEY_TITLE + " TEXT," + KEY_PATH + " TEXT," + KEY_DATE + " TEXT," + KEY_VOTE + " TEXT" + ")";
    private static HashMap<String, String> PROJECTION_MAP;

    static {
        uriMatcher = new UriMatcher( UriMatcher.NO_MATCH );
        uriMatcher.addURI( PROVIDER_NAME, "data", TV );
        uriMatcher.addURI( PROVIDER_NAME, "data/#", TV_ID );
    }

    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper( context );

        db = dbHelper.getWritableDatabase();
        return db != null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = db.insert( TABLE_NAME, "", values );

        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId( CONTENT_URI, rowID );
            getContext().getContentResolver().notifyChange( _uri, null );
            return _uri;
        }

        throw new SQLException( "Failed to add a record into " + uri );
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables( TABLE_NAME );

        switch (uriMatcher.match( uri )) {
            case TV:
                qb.setProjectionMap( PROJECTION_MAP );
                break;

            case TV_ID:
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get( 1 ) );
                break;

            default:
        }

        if (sortOrder == null || sortOrder == "") {
            sortOrder = KEY_MID;
        }

        Cursor c = qb.query( db, projection, selection, selectionArgs, null, null, sortOrder );
        c.setNotificationUri( getContext().getContentResolver(), uri );
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match( uri )) {
            case TV:
                count = db.delete( TABLE_NAME, selection, selectionArgs );
                break;

            case TV_ID:
                String id = uri.getPathSegments().get( 1 );
                count = db.delete( TABLE_NAME, _ID + " = " + id + (!TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : ""), selectionArgs );
                break;
            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );
        }

        getContext().getContentResolver().notifyChange( uri, null );
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match( uri )) {
            case TV:
                count = db.update( TABLE_NAME, values, selection, selectionArgs );
                break;

            case TV_ID:
                count = db.update( TABLE_NAME, values, _ID + " = " + uri.getPathSegments().get( 1 ) + (!TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : ""), selectionArgs );
                break;
            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );
        }

        getContext().getContentResolver().notifyChange( uri, null );
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match( uri )) {
            case TV:
                return "vnd.android.cursor.dir/vnd.refraginc.students";
            case TV_ID:
                return "vnd.android.cursor.item/vnd.refraginc.students";
            default:
                throw new IllegalArgumentException( "Unsupported URI: " + uri );
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super( context, DATABASE_NAME, null, DATABASE_VERSION );
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL( CREATE_DB_TABLE );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME );
            onCreate( db );
        }
    }
}
