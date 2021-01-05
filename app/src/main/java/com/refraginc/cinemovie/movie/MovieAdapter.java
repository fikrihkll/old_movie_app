package com.refraginc.cinemovie.movie;

import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.refraginc.cinemovie.Connector;
import com.refraginc.cinemovie.MainActivity;
import com.refraginc.cinemovie.R;
import com.refraginc.cinemovie.contentprovider.MovieProvider;
import com.refraginc.cinemovie.widget.FavoriteWidget;
import com.squareup.picasso.Picasso;


public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.GridViewHolder> {
    private Context context;

    private Integer movieLoaded;
    private String[] title;
    private String[] id;
    private double[] voting;
    private String[] imgPath;
    private String img = "https://image.tmdb.org/t/p";
    private String[] date;

    private String[] fav = new String[9999];
    private Integer totalFav = 0;

    public MovieAdapter(Context context, Integer movieLoaded, String[] title, String[] id, double[] voting, String[] imgPath, String[] date) {
        this.context = context;
        this.movieLoaded = movieLoaded;
        this.title = title;
        this.id = id;
        this.voting = voting;
        this.imgPath = imgPath;
        this.date = date;
    }


    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from( context ).inflate( R.layout.layout_rc, viewGroup, false );
        return new GridViewHolder( view );
    }


    @Override
    public void onBindViewHolder(@NonNull final GridViewHolder vh, final int i) {
        loadFav();
        //Setting Love Filled Icon to Stored Movie
        vh.btnFav.setTag( R.drawable.ic_love_blank );
        Integer[] favIndex = new Integer[totalFav];
        Integer indexSame = 0;
        for (Integer p = 0; p < totalFav; p++) {
            if (id[i].equals( fav[p] )) {
                favIndex[indexSame] = i;
                indexSame++;

                vh.btnFav.setImageDrawable( context.getResources().getDrawable( R.drawable.ic_love_fill ) );
                vh.btnFav.setTag( R.drawable.ic_love_fill );
            }
        }

        //Load Image into ImageView
        Picasso.get().load( img + "/w500" + imgPath[i] ).into( vh.ivPoster );

        //Set the Texts
        vh.tvTitle.setText( title[i] );
        vh.ratingBar.setText( Double.toString( voting[i] ) );
        vh.tvDate.setText( date[i] );

        //Button favorite Listener
        vh.btnFav.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getDrawableId( vh.btnFav ) == R.drawable.ic_love_blank) {
                    saveFav( i );
                    vh.btnFav.setImageDrawable( context.getResources().getDrawable( R.drawable.ic_love_fill ) );
                    vh.btnFav.setTag( R.drawable.ic_love_fill );
                } else {
                    removeFav( i );
                    vh.btnFav.setImageDrawable( context.getResources().getDrawable( R.drawable.ic_love_blank ) );
                    vh.btnFav.setTag( R.drawable.ic_love_blank );
                }
            }
        } );

        //Button Trailer Dialog Listener
        vh.btnTrailer.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogTrailer( title[i] );
            }
        } );

        //Go to Detail
        vh.tvTitle.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connector con = new Connector();

                con.setId( id[i] );

                Intent intent = new Intent( context, DetailActivity.class );
                intent.putExtra( MainActivity.PARCE_KEY, con );
                context.startActivity( intent );
            }
        } );

        vh.ivPoster.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connector con = new Connector();

                con.setId( id[i] );

                Intent intent = new Intent( context, DetailActivity.class );
                intent.putExtra( MainActivity.PARCE_KEY, con );
                context.startActivity( intent );
            }
        } );

        //Margin
        if (i % 2 == 0) {
            vh.marginLeft.setVisibility( View.GONE );
        } else {

        }
    }

    private void dialogTrailer(@NonNull String name) {
        String nameChanged = "";

        for (Integer i = 0; i < name.length(); i++) {
            if (name.charAt( i ) == ' ' || name.charAt( i ) == '-' || name.charAt( i ) == '/' || name.charAt( i ) == '.' || name.charAt( i ) == ',') {
                nameChanged += "+";
            } else {
                nameChanged += Character.toString( name.charAt( i ) );
            }
        }

        final Dialog dl = new Dialog( context );
        dl.setContentView( R.layout.layout_trailer );

        final WebView webView = dl.findViewById( R.id.webView );
        webView.setWebViewClient( new WebViewClient() );
        webView.loadUrl( "https://www.youtube.com/results?search_query=" + nameChanged + "+trailer" );

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled( true );
        webSettings.setJavaScriptEnabled( true );
        webSettings.setAllowFileAccess( true );
        webSettings.setAppCacheEnabled( true );
        webSettings.setAllowContentAccess( true );
        webSettings.setDomStorageEnabled( true );

        webView.setLayerType( WebView.LAYER_TYPE_HARDWARE, null );

        webView.setWebViewClient( new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                ProgressBar pb = dl.findViewById( R.id.pbTrailer );
                pb.setVisibility( View.GONE );
            }
        } );

        Button btnYt = dl.findViewById( R.id.btnGoYT );
        btnYt.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent web = new Intent( Intent.ACTION_VIEW, Uri.parse( webView.getUrl() ) );
                context.startActivity( web );
            }
        } );

        dl.show();
    }

    private void saveFav(@NonNull Integer indx) {
        ContentValues values = new ContentValues();
        values.put( MovieProvider.KEY_MID, id[indx] );
        values.put( MovieProvider.KEY_TITLE, title[indx] );
        values.put( MovieProvider.KEY_PATH, imgPath[indx] );
        values.put( MovieProvider.KEY_DATE, date[indx] );
        values.put( MovieProvider.KEY_VOTE, voting[indx] );

        context.getContentResolver().insert( MovieProvider.CONTENT_URI, values );

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance( context );
        ComponentName thisWidget = new ComponentName( context, FavoriteWidget.class );
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds( thisWidget );
        appWidgetManager.notifyAppWidgetViewDataChanged( appWidgetIds, R.id.stack_view );

        Toast.makeText( context, "Added to Favorite", Toast.LENGTH_SHORT ).show();
    }

    private void removeFav(@NonNull Integer indx) {
        context.getContentResolver().delete( MovieProvider.CONTENT_URI, "mid=?", new String[]{fav[indx]} );
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance( context );
        ComponentName thisWidget = new ComponentName( context, FavoriteWidget.class );
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds( thisWidget );
        appWidgetManager.notifyAppWidgetViewDataChanged( appWidgetIds, R.id.stack_view );
    }

    private void loadFav() {
        Cursor cursor = context.getContentResolver().query( Uri.parse( "content://com.refraginc.cinemovie.contentprovider.MovieProvider/data" ), null, null, null, null );
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                fav[totalFav] = cursor.getString( cursor.getColumnIndex( "mid" ) );
                totalFav++;
                cursor.moveToNext();
            }
        }
    }

    private int getDrawableId(@NonNull ImageView iv) {
        return (Integer) iv.getTag();
    }

    @Override
    public int getItemCount() {
        return movieLoaded;
    }

    public class GridViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ivPoster, btnFav;
        TextView ratingBar, marginLeft, tvDate;
        Button btnTrailer;

        public GridViewHolder(@NonNull View v) {
            super( v );
            tvTitle = v.findViewById( R.id.tvTitle );
            ivPoster = v.findViewById( R.id.ivPoster );
            btnFav = v.findViewById( R.id.ivFav );
            ratingBar = v.findViewById( R.id.ratingVal );
            btnTrailer = v.findViewById( R.id.btnTrailer );
            marginLeft = v.findViewById( R.id.margLeft );
            tvDate = v.findViewById( R.id.tvDate );
        }
    }
}
