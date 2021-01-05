package com.refraginc.cinemovie.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.refraginc.cinemovie.R;

import java.util.ArrayList;

public class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    public final ArrayList<String> mWidgetItems = new ArrayList<>();
    public final ArrayList<String> mWIdgetTitle = new ArrayList<>();
    public final ArrayList<String> mWidgetUrl = new ArrayList<>();
    private final Context mContext;

    StackRemoteViewsFactory(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        mWidgetUrl.clear();
        mWIdgetTitle.clear();
        mWidgetItems.clear();

        Cursor cursor = mContext.getContentResolver().query( Uri.parse( "content://com.refraginc.cinemovie.contentprovider.MovieProvider/data" ), null, null, null, null );

        // querying ke database
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                mWidgetItems.add( cursor.getString( cursor.getColumnIndex( "vote" ) ) );
                mWIdgetTitle.add( cursor.getString( cursor.getColumnIndex( "title" ) ) );
                mWidgetUrl.add( "https://image.tmdb.org/t/p/w500" + cursor.getString( cursor.getColumnIndex( "path" ) ) );
                cursor.moveToNext();
            }
        }

        Cursor cursor2 = mContext.getContentResolver().query( Uri.parse( "content://com.refraginc.cinemovie.contentprovider.TvProvider/data" ), null, null, null, null );

        // querying ke database
        if (cursor2.moveToFirst()) {
            while (!cursor2.isAfterLast()) {
                mWidgetItems.add( cursor2.getString( cursor2.getColumnIndex( "vote" ) ) );
                mWIdgetTitle.add( cursor2.getString( cursor2.getColumnIndex( "title" ) ) );
                mWidgetUrl.add( "https://image.tmdb.org/t/p/w500" + cursor2.getString( cursor2.getColumnIndex( "path" ) ) );
                cursor2.moveToNext();
            }
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mWidgetItems.size();
    }

    @Override
    public RemoteViews getViewAt(final int position) {
        RemoteViews rv = new RemoteViews( mContext.getPackageName(), R.layout.widget_item );
        rv.setTextViewText( R.id.tvTitleW, mWIdgetTitle.get( position ) );
        try {
            Bitmap b = Glide.with( mContext ).asBitmap().load( mWidgetUrl.get( position ) ).submit( 512, 512 ).get();
            rv.setImageViewBitmap( R.id.imageViewW, b );
        } catch (Exception e) {

        }

        Bundle extras = new Bundle();
        extras.putString( FavoriteWidget.EXTRA_ITEM, mWIdgetTitle.get( position ) );
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras( extras );
        rv.setOnClickFillInIntent( R.id.imageViewW, fillInIntent );
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
