package com.refraginc.cinemovie.tv_show;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.refraginc.cinemovie.BuildConfig;
import com.refraginc.cinemovie.Connector;
import com.refraginc.cinemovie.MainActivity;
import com.refraginc.cinemovie.R;
import com.refraginc.cinemovie.SettingActivity;
import com.refraginc.cinemovie.WebActivity;
import com.refraginc.cinemovie.contentprovider.TvProvider;
import com.refraginc.cinemovie.widget.FavoriteWidget;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DetailActivityTvShow extends AppCompatActivity {
    private static String KEY_TITLE = "DT_TITLE";
    private static String KEY_OVERV = "DT_OVERV";
    private static String KEY_DATE = "DT_DATE";
    private static String KEY_GENRE = "DT_GENRE";
    private static String KEY_COMP = "DT_COMP";
    private static String KEY_PATH = "DT_PATH";
    private static String KEY_BAR = "DT_BAR";
    private static String KEY_POP = "DT_POP";
    private static String KEY_VOTE = "DT_VOTE";
    private static String KEY_SEA = "DT_SEA";
    private static String KEY_EP = "DT_EP";
    private static String KEY_POSTER = "DT_POSTER";
    private static String KEY_IMGBAR = "DT_IMGBAR";

    private CollapsingToolbarLayout collapseBar;
    private ImageView barImage, ivPoster;
    private TextView tvOverview, tvVote, tvGenre, tvCompany, tvDate, tvPop, tvTitle, tvSeason, tvEpisode;
    private Toolbar toolbar;
    private ProgressBar voteBar, pbLoad;
    private ConstraintLayout loading;
    private NestedScrollView loaded;
    private Button btnDownload, btnTrailer;

    private String title = "";
    private String imgPath = "";
    private String backImgPath = "";
    private String overview = "";
    private String genres = "";
    private String companies = "";
    private String date = "";
    private Integer vote = 0;
    private Float popularity;
    private String id = "";
    private String season = "";
    private String episode = "";
    private String[] fav = new String[9999];
    private Integer totalFav = 0;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_detail_tv_show );

        btnTrailer = findViewById( R.id.tvBtnTrailer );
        btnDownload = findViewById( R.id.btnDownloadTv );
        pbLoad = findViewById( R.id.progressBar2Tv );
        loading = findViewById( R.id.loadingTv );
        loaded = findViewById( R.id.loadedTv );
        tvTitle = findViewById( R.id.tvTitleTv );
        voteBar = findViewById( R.id.voteBarTv );
        ivPoster = findViewById( R.id.ivPosterTv );
        collapseBar = findViewById( R.id.collapseBarTv );
        barImage = findViewById( R.id.app_bar_imageTv );
        tvOverview = findViewById( R.id.tvOverviewTv );
        tvVote = findViewById( R.id.tvVoteTv );
        tvGenre = findViewById( R.id.tvGenreTv );
        tvCompany = findViewById( R.id.tvCompanyTv );
        tvDate = findViewById( R.id.tvDateTv );
        tvPop = findViewById( R.id.tvPopTv );
        tvSeason = findViewById( R.id.tvSeasonTv );
        tvEpisode = findViewById( R.id.tvEpisodeTv );
        toolbar = findViewById( R.id.toolbarTv );

        toolbar.setNavigationIcon( getResources().getDrawable( R.drawable.ic_back ) );
        toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        } );
        toolbar.inflateMenu( R.menu.detail_menu );

        //Reload the Data
        if (savedInstanceState != null) {
            title = savedInstanceState.getString( KEY_TITLE, "" );
            collapseBar.setTitle( title );
            collapseBar.setCollapsedTitleTextColor( getResources().getColor( R.color.black ) );
            collapseBar.setExpandedTitleColor( getResources().getColor( R.color.white ) );
            backImgPath = savedInstanceState.getString( KEY_IMGBAR, "" );
            imgPath = savedInstanceState.getString( KEY_POSTER, "" );
            Picasso.get().load( "https://image.tmdb.org/t/p/w500" + savedInstanceState.getString( KEY_BAR, backImgPath ) ).into( barImage );
            Picasso.get().load( "https://image.tmdb.org/t/p/w500" + savedInstanceState.getString( KEY_PATH, imgPath ) ).into( ivPoster );
            tvOverview.setText( savedInstanceState.getString( KEY_OVERV, "" ) );
            tvCompany.setText( savedInstanceState.getString( KEY_COMP, "" ) );
            tvGenre.setText( savedInstanceState.getString( KEY_GENRE, "" ) );
            tvDate.setText( savedInstanceState.getString( KEY_DATE, "" ) );
            tvPop.setText( savedInstanceState.getString( KEY_POP, "" ) );
            vote = Integer.parseInt( savedInstanceState.getString( KEY_VOTE, "" ) );
            tvVote.setText( vote.toString() + "%" );
            voteBar.setProgress( vote );
            tvTitle.setText( title );
            tvSeason.setText( savedInstanceState.getString( KEY_SEA, "" ) );
            tvEpisode.setText( savedInstanceState.getString( KEY_EP, "" ) );

            loading.setVisibility( View.GONE );

            btnDownload.setVisibility( View.VISIBLE );

            onSaveInstanceState( new Bundle() );

        } else {
            //Get Parccelable Data

            Connector con = getIntent().getParcelableExtra( MainActivity.PARCE_KEY );

            id = con.getId();
            loadData( id );
        }
        listener();
        loadDataRelease();
        loadFav();
    }

    public void loadData(String id) {
        pbLoad.setVisibility( View.VISIBLE );
        loading.setBackgroundColor( getResources().getColor( R.color.white ) );

        OkHttpClient client = new OkHttpClient();

        String url = "";
        url = "https://api.themoviedb.org/3/tv/" + id + "?api_key=" + BuildConfig.TMDB_API_KEY + "&language=en-US";


        Request request = new Request.Builder().url( url ).build();

        client.newCall( request ).enqueue( new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        //Hide PB
                        pbLoad.setVisibility( View.GONE );
                        loading.setBackground( getResources().getDrawable( R.drawable.ic_reload ) );
                        reload();
                        Toast.makeText( DetailActivityTvShow.this, "Something went wrong, Please try later", Toast.LENGTH_SHORT ).show();
                    }
                } );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    final String responseData = response.body().string();

                    try {
                        //PARSING DATA
                        JSONObject Jobject = new JSONObject( responseData );

                        overview = Jobject.getString( "overview" );
                        JSONArray Jarray = Jobject.getJSONArray( "genres" );

                        for (int i = 0; i < Jarray.length(); i++) {
                            JSONObject object = Jarray.getJSONObject( i );
                            genres += "#" + object.getString( "name" );
                        }

                        title = Jobject.getString( "name" );
                        backImgPath = Jobject.getString( "backdrop_path" );
                        popularity = (float) Jobject.getDouble( "popularity" );
                        imgPath = Jobject.getString( "poster_path" );

                        JSONArray JarrayCom = Jobject.getJSONArray( "production_companies" );
                        for (int i = 0; i < JarrayCom.length(); i++) {
                            JSONObject object = JarrayCom.getJSONObject( i );
                            companies += object.getString( "name" ) + "\n";
                        }

                        date = Jobject.getString( "first_air_date" );
                        season = Integer.toString( Jobject.getInt( "number_of_seasons" ) );
                        episode = Integer.toString( Jobject.getInt( "number_of_episodes" ) );
                        vote = (int) ((float) Jobject.getDouble( "vote_average" )) * 10;

                        runOnUiThread( new Runnable() {
                            @Override
                            public void run() {
                                collapseBar.setTitle( title );
                                collapseBar.setCollapsedTitleTextColor( getResources().getColor( R.color.black ) );
                                collapseBar.setExpandedTitleColor( getResources().getColor( R.color.white ) );
                                Picasso.get().load( "https://image.tmdb.org/t/p/w500" + backImgPath ).into( barImage );
                                Picasso.get().load( "https://image.tmdb.org/t/p/w500" + imgPath ).into( ivPoster );
                                tvOverview.setText( overview );
                                tvCompany.setText( companies );
                                tvGenre.setText( genres );
                                tvDate.setText( date );
                                tvPop.setText( popularity.toString() );
                                tvVote.setText( vote.toString() + "%" );
                                voteBar.setProgress( vote );
                                tvTitle.setText( title );
                                tvSeason.setText( season + " Season(s)" );
                                tvEpisode.setText( episode + " Episode(s)" );

                                onSaveInstanceState( new Bundle() );

                                //Hide PB
                                loading.setVisibility( View.GONE );
                            }
                        } );


                    } catch (Exception exception) {

                    }

                }
            }
        } );
    }

    public void reload() {
        loading.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData( id );
            }
        } );
    }

    public void listener() {
        btnDownload.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( DetailActivityTvShow.this, WebActivity.class );
                Bundle bd = new Bundle();
                bd.putString( MainActivity.TITLE_KEY, title );
                intent.putExtras( bd );
                startActivity( intent );
            }
        } );

        toolbar.setOnMenuItemClickListener( new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.m_lgsetting_detail:
                        Intent mIntent = new Intent( Settings.ACTION_LOCALE_SETTINGS );
                        startActivity( mIntent );
                        break;
                    case R.id.m_notif_detail:
                        startActivity( new Intent( DetailActivityTvShow.this, SettingActivity.class ) );
                        break;
                    case R.id.m_add_fav:
                        saveFav();
                        break;
                }
                return false;
            }
        } );

        btnTrailer.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogTrailer( title );
            }
        } );
    }

    private void loadDataRelease() {
        Calendar cal = Calendar.getInstance();

        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat( myFormat );

        String dateToday = sdf.format( cal.getTime() );

        OkHttpClient client = new OkHttpClient();
        String url = "https://api.themoviedb.org/3/discover/movie?api_key=" + BuildConfig.TMDB_API_KEY + "&primary_release_date.gte=" + dateToday + "&primary_release_date.lte=" + dateToday;


        Request request = new Request.Builder().url( url ).build();

        client.newCall( request ).enqueue( new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    final String responseData = response.body().string();

                    try {
                        JSONObject Jobject = new JSONObject( responseData );
                        final JSONArray Jarray = Jobject.getJSONArray( "results" );

                        //Variables
                        final String[] Get_title = new String[Jarray.length()];
                        final String[] Get_id = new String[Jarray.length()];
                        final Double[] Get_voting = new Double[Jarray.length()];
                        final String[] Get_date = new String[Jarray.length()];

                        //Getting Data from the JSON Array
                        for (int i = 0; i < Jarray.length(); i++) {
                            JSONObject object = Jarray.getJSONObject( i );
                            Get_title[i] = object.getString( "title" );
                            Get_id[i] = Integer.toString( object.getInt( "id" ) );
                            Get_voting[i] = object.getDouble( "vote_average" );
                            Get_date[i] = object.getString( "release_date" );
                        }

                        //Save into Shared Preference
                        SharedPreferences sharedPref = getSharedPreferences( "mypref", MODE_PRIVATE );
                        SharedPreferences.Editor editor = sharedPref.edit();

                        for (Integer i = 0; i < Jarray.length(); i++) {
                            editor.putString( "rlsTitle" + i.toString(), Get_title[i] );
                            editor.putString( "rlsId" + i.toString(), Get_id[i] );
                            editor.putString( "rlsVoting" + i.toString(), Double.toString( Get_voting[i] ) );
                            editor.putString( "rlsDate" + i.toString(), Get_date[i] );
                        }

                        editor.commit();

                    } catch (Exception exception) {

                    }

                }
            }
        } );
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

        final Dialog dl = new Dialog( DetailActivityTvShow.this );
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
                DetailActivityTvShow.this.startActivity( web );
            }
        } );

        dl.show();
    }

    private void saveFav() {
        if (totalFav == 0) {
            ContentValues values = new ContentValues();
            values.put( TvProvider.KEY_MID, id );
            values.put( TvProvider.KEY_TITLE, title );
            values.put( TvProvider.KEY_PATH, imgPath );
            values.put( TvProvider.KEY_DATE, date );
            values.put( TvProvider.KEY_VOTE, vote );

            DetailActivityTvShow.this.getContentResolver().insert( TvProvider.CONTENT_URI, values );

            Toast.makeText( DetailActivityTvShow.this, "Added to Favorite", Toast.LENGTH_SHORT ).show();
        } else {
            for (Integer i = 0; i < totalFav; i++) {
                if (fav[i].equals( id )) {
                    Toast.makeText( DetailActivityTvShow.this, getResources().getString( R.string.strAlready ), Toast.LENGTH_LONG ).show();
                } else if (i == totalFav - 1 && !fav[i].equals( id )) {
                    ContentValues values = new ContentValues();
                    values.put( TvProvider.KEY_MID, id );
                    values.put( TvProvider.KEY_TITLE, title );
                    values.put( TvProvider.KEY_PATH, imgPath );
                    values.put( TvProvider.KEY_DATE, date );
                    values.put( TvProvider.KEY_VOTE, vote );

                    DetailActivityTvShow.this.getContentResolver().insert( TvProvider.CONTENT_URI, values );

                    Toast.makeText( DetailActivityTvShow.this, "Added to Favorite", Toast.LENGTH_SHORT ).show();
                }
            }
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance( getApplicationContext() );
        ComponentName thisWidget = new ComponentName( getApplicationContext(), FavoriteWidget.class );
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds( thisWidget );
        appWidgetManager.notifyAppWidgetViewDataChanged( appWidgetIds, R.id.stack_view );

        loadFav();
    }

    private void loadFav() {
        Cursor cursor = DetailActivityTvShow.this.getContentResolver().query( Uri.parse( "content://com.refraginc.cinemovie.contentprovider.TvProvider/data" ), null, null, null, null );
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                fav[totalFav] = cursor.getString( cursor.getColumnIndex( "mid" ) );
                totalFav++;
                cursor.moveToNext();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState( outState );
        //Save Instance
        outState.putString( KEY_TITLE, tvTitle.getText().toString() );
        outState.putString( KEY_BAR, backImgPath );
        outState.putString( KEY_PATH, imgPath );
        outState.putString( KEY_OVERV, tvOverview.getText().toString() );
        outState.putString( KEY_COMP, tvCompany.getText().toString() );
        outState.putString( KEY_GENRE, tvGenre.getText().toString() );
        outState.putString( KEY_DATE, tvDate.getText().toString() );
        outState.putString( KEY_POP, tvPop.getText().toString() );
        outState.putString( KEY_VOTE, vote.toString() );
        outState.putString( KEY_IMGBAR, backImgPath );
        outState.putString( KEY_POSTER, imgPath );
        outState.putString( KEY_SEA, tvSeason.getText().toString() );
        outState.putString( KEY_EP, tvEpisode.getText().toString() );

    }

    public void onBackPressed() {
        if(!loadingIsVisible())
            DetailActivityTvShow.this.finish();
    }

    @NonNull
    private Boolean loadingIsVisible(){
        if(loading.getVisibility()==View.VISIBLE){
            return true;
        }else{
            return false;
        }
    }
}
