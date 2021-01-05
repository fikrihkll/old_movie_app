package com.refraginc.cinemovie.movie;

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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.refraginc.cinemovie.BuildConfig;
import com.refraginc.cinemovie.Connector;
import com.refraginc.cinemovie.MainActivity;
import com.refraginc.cinemovie.R;
import com.refraginc.cinemovie.SettingActivity;
import com.refraginc.cinemovie.WebActivity;
import com.refraginc.cinemovie.contentprovider.MovieProvider;
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

public class DetailActivity extends AppCompatActivity {
    private static String KEY_TITLE = "DT_TITLE";
    private static String KEY_OVERV = "DT_OVERV";
    private static String KEY_DATE = "DT_DATE";
    private static String KEY_GENRE = "DT_GENRE";
    private static String KEY_COMP = "DT_COMP";
    private static String KEY_PATH = "DT_PATH";
    private static String KEY_BAR = "DT_BAR";
    private static String KEY_POP = "DT_POP";
    private static String KEY_VOTE = "DT_VOTE";
    private static String KEY_POSTER = "DT_POSTER";
    private static String KEY_IMGBAR = "DT_IMGBAR";
    private static String KEY_REALVOTE = "DT_REALVOTE";

    private CollapsingToolbarLayout collapseBar;
    private ImageView barImage, ivPoster;
    private TextView tvOverview, tvVote, tvGenre, tvCompany, tvDate, tvPop, tvTitle;
    private Toolbar toolbar;
    private ProgressBar voteBar, pbLoad;
    private ConstraintLayout loading;
    private Button btnDownload, btnTrailer;
    private RatingBar rateBar;

    private String title = "";
    private String imgPath = "";
    private String backImgPath = "";
    private String overview = "";
    private String genres = "";
    private String companies = "";
    private String date = "";
    private Integer vote = 0;
    private Float popularity;
    private Float realVote;
    private String id = "";
    private String[] fav = new String[9999];
    private Integer totalFav = 0;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_detail );

        btnTrailer = findViewById( R.id.mBtnTrailer );
        btnDownload = findViewById( R.id.btnDownload );
        pbLoad = findViewById( R.id.progressBar2 );
        loading = findViewById( R.id.loadingSS );
        tvTitle = findViewById( R.id.tvTitle );
        voteBar = findViewById( R.id.voteBar );
        ivPoster = findViewById( R.id.ivPoster );
        collapseBar = findViewById( R.id.collapseBar );
        barImage = findViewById( R.id.app_bar_image );
        tvOverview = findViewById( R.id.tvOverview );
        tvVote = findViewById( R.id.tvVote );
        tvGenre = findViewById( R.id.tvGenre );
        tvCompany = findViewById( R.id.tvCompany );
        tvDate = findViewById( R.id.tvDate );
        tvPop = findViewById( R.id.tvPop );
        toolbar = findViewById( R.id.toolbar );
        rateBar = findViewById( R.id.rateBar );

        toolbar.setNavigationIcon( getResources().getDrawable( R.drawable.ic_back ) );
        toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        } );
        toolbar.inflateMenu( R.menu.detail_menu );

        //Reload the data

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
            realVote = savedInstanceState.getFloat( KEY_REALVOTE, 0 );
            rateBar.setRating( realVote );
            voteBar.setProgress( vote );
            tvTitle.setText( title );

            btnDownload.setVisibility( View.VISIBLE );

            loading.setVisibility( View.GONE );

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

        url = "https://api.themoviedb.org/3/movie/" + id + "?api_key=" + BuildConfig.TMDB_API_KEY + "&language=en-US";

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
                        Toast.makeText( DetailActivity.this, "Something went wrong, Please try later", Toast.LENGTH_SHORT ).show();
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

                        title = Jobject.getString( "title" );
                        backImgPath = Jobject.getString( "backdrop_path" );
                        popularity = (float) Jobject.getDouble( "popularity" );
                        imgPath = Jobject.getString( "poster_path" );

                        JSONArray JarrayCom = Jobject.getJSONArray( "production_companies" );
                        for (int i = 0; i < JarrayCom.length(); i++) {
                            JSONObject object = JarrayCom.getJSONObject( i );
                            companies += object.getString( "name" ) + "\n";
                        }

                        date = Jobject.getString( "release_date" );
                        vote = (int) ((float) Jobject.getDouble( "vote_average" )) * 10;
                        realVote = ((float) Jobject.getDouble( "vote_average" )) / 2;


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
                                rateBar.setRating( realVote );
                                voteBar.setProgress( vote );
                                tvTitle.setText( title );

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
                Intent intent = new Intent( DetailActivity.this, WebActivity.class );
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
                        startActivity( new Intent( DetailActivity.this, SettingActivity.class ) );
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

    private void saveFav() {
        if (totalFav == 0) {
            ContentValues values = new ContentValues();
            values.put( MovieProvider.KEY_MID, id );
            values.put( MovieProvider.KEY_TITLE, title );
            values.put( MovieProvider.KEY_PATH, imgPath );
            values.put( MovieProvider.KEY_DATE, date );
            values.put( MovieProvider.KEY_VOTE, vote );

            DetailActivity.this.getContentResolver().insert( MovieProvider.CONTENT_URI, values );

            Toast.makeText( DetailActivity.this, "Added to Favorite", Toast.LENGTH_SHORT ).show();
        } else {
            for (Integer i = 0; i < totalFav; i++) {
                if (fav[i].equals( id )) {
                    Toast.makeText( DetailActivity.this, getResources().getString( R.string.strAlready ), Toast.LENGTH_LONG ).show();
                } else if (i == totalFav - 1 && !fav[i].equals( id )) {
                    ContentValues values = new ContentValues();
                    values.put( MovieProvider.KEY_MID, id );
                    values.put( MovieProvider.KEY_TITLE, title );
                    values.put( MovieProvider.KEY_PATH, imgPath );
                    values.put( MovieProvider.KEY_DATE, date );
                    values.put( MovieProvider.KEY_VOTE, vote );

                    DetailActivity.this.getContentResolver().insert( MovieProvider.CONTENT_URI, values );

                    Toast.makeText( DetailActivity.this, "Added to Favorite", Toast.LENGTH_SHORT ).show();
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
        Cursor cursor = DetailActivity.this.getContentResolver().query( Uri.parse( "content://com.refraginc.cinemovie.contentprovider.MovieProvider/data" ), null, null, null, null );
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                fav[totalFav] = cursor.getString( cursor.getColumnIndex( "mid" ) );
                totalFav++;
                cursor.moveToNext();
            }
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

        final Dialog dl = new Dialog( DetailActivity.this );
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
                DetailActivity.this.startActivity( web );
            }
        } );

        dl.show();
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
        outState.putFloat( KEY_REALVOTE, realVote );
    }

    public void onBackPressed() {
        if(!loadingIsVisible())
            DetailActivity.this.finish();
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
