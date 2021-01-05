package com.refraginc.cinemovie.favorite;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.refraginc.cinemovie.MainActivity;
import com.refraginc.cinemovie.R;

public class FavoriteActivity extends AppCompatActivity {
    private String selectedFav = "movie";

    private String[] favId = new String[9999];
    private String[] title = new String[9999];
    private String[] date = new String[9999];
    private String[] imgPath = new String[9999];
    private double[] voting = new double[9999];

    private Integer totalFav = 0;

    private RecyclerView rcFav;
    private TabLayout tabLayout;
    private Toolbar toolbarFav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_favorite );


        rcFav = findViewById( R.id.rcFav );
        rcFav.setLayoutManager( new GridLayoutManager( FavoriteActivity.this, 2 ) );
        tabLayout = findViewById( R.id.tabLayoutFav );
        toolbarFav = findViewById( R.id.toolbarFav );
        toolbarFav.setTitle( " Favorites" );
        toolbarFav.setLogo( getResources().getDrawable( R.drawable.ic_love_fill ) );
        toolbarFav.setNavigationIcon( getResources().getDrawable( R.drawable.ic_back ) );

        loadFav();

        listener();
    }

    private void loadFav() {
        if (selectedFav.equals( "movie" )) {
            Cursor cursor = FavoriteActivity.this.getContentResolver().query( Uri.parse( "content://com.refraginc.cinemovie.contentprovider.MovieProvider/data" ), null, null, null, null );
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    favId[totalFav] = cursor.getString( cursor.getColumnIndex( "mid" ) );
                    title[totalFav] = cursor.getString( cursor.getColumnIndex( "title" ) );
                    imgPath[totalFav] = cursor.getString( cursor.getColumnIndex( "path" ) );
                    date[totalFav] = cursor.getString( cursor.getColumnIndex( "date" ) );
                    voting[totalFav] = Double.parseDouble( cursor.getString( cursor.getColumnIndex( "vote" ) ) );
                    totalFav++;
                    cursor.moveToNext();
                }
            }

            rcFav.setAdapter( new FavoriteAdapter( FavoriteActivity.this, totalFav, title, favId, voting, imgPath, date, selectedFav ) );

        } else {
            Cursor cursor = FavoriteActivity.this.getContentResolver().query( Uri.parse( "content://com.refraginc.cinemovie.contentprovider.TvProvider/data" ), null, null, null, null );
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    favId[totalFav] = cursor.getString( cursor.getColumnIndex( "mid" ) );
                    title[totalFav] = cursor.getString( cursor.getColumnIndex( "title" ) );
                    imgPath[totalFav] = cursor.getString( cursor.getColumnIndex( "path" ) );
                    date[totalFav] = cursor.getString( cursor.getColumnIndex( "date" ) );
                    voting[totalFav] = Double.parseDouble( cursor.getString( cursor.getColumnIndex( "vote" ) ) );
                    totalFav++;
                    cursor.moveToNext();
                }
            }
            rcFav.setAdapter( new FavoriteAdapter( FavoriteActivity.this, totalFav, title, favId, voting, imgPath, date, selectedFav ) );

        }

    }

    private void listener() {
        toolbarFav.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent( FavoriteActivity.this, MainActivity.class ) );
                FavoriteActivity.this.finish();
            }
        } );

        tabLayout.addOnTabSelectedListener( new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == 0) {
                    selectedFav = "movie";
                } else {
                    selectedFav = "tvshow";
                }
                totalFav = 0;

                loadFav();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        } );
    }

    public void onBackPressed() {
        startActivity( new Intent( FavoriteActivity.this, MainActivity.class ) );
        FavoriteActivity.this.finish();
    }
}
