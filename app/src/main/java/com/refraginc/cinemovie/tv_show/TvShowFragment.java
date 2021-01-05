package com.refraginc.cinemovie.tv_show;


import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.refraginc.cinemovie.BuildConfig;
import com.refraginc.cinemovie.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class TvShowFragment extends Fragment {
    private static String KEY_TITLE = "TVSTATE_TITLE";
    private static String KEY_ID = "TVSTATE_ID";
    private static String KEY_VOTING = "TVSTATE_VOTING";
    private static String KEY_IMGPATH = "TVSTATE_IMGPATH";
    private static String KEY_DATE = "TVSTATE_DATE";
    private static String KEY_LOADED = "TVSTATE_LOADED";
    private static String KEY_BOOL = "TVSTATE_BOOL";
    private static String KEY_TOPIC = "TVSTATE_TOPIC";
    private final String[] title = new String[9999];
    private final String[] id = new String[9999];
    private final double[] voting = new double[9999];
    private final String[] imgPath = new String[9999];
    private final String[] date = new String[9999];
    GridLayoutManager lm = new GridLayoutManager( getContext(), 2 );
    private RecyclerView rcTv;
    private NestedScrollView tv_scroll;
    private HorizontalScrollView hsv_tv;
    private TextView btnLoadMoreTv;
    private ProgressBar pbTv;
    private TextView btnPopTv, btnTopRatedTv, btnOTA, btnAN;
    private Integer lastScrollVal = 0;
    private Integer totalHeight = 0;
    private Integer page = 1;
    private Integer movieLoaded = 0;
    private Integer wasLoaded = 0;
    private Boolean loadOnce = true;
    private String topicSelected = "top_rated";

    public TvShowFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.fragment_tv_show, container, false );

        //Initializing
        rcTv = view.findViewById( R.id.rcTv );
        rcTv.setLayoutManager( lm );

        tv_scroll = view.findViewById( R.id.tv_scroll );
        btnLoadMoreTv = view.findViewById( R.id.btnLoadMoreTv );
        pbTv = view.findViewById( R.id.pbTv );
        btnPopTv = view.findViewById( R.id.btnPopTv );
        btnTopRatedTv = view.findViewById( R.id.btnTopRatedTv );
        btnOTA = view.findViewById( R.id.btnOTA );
        btnAN = view.findViewById( R.id.btnAN );
        hsv_tv = view.findViewById( R.id.hsv_tv );

        //Reload the Data
        if (savedInstanceState != null) {
            movieLoaded = savedInstanceState.getInt( KEY_LOADED, 0 );
            for (Integer i = 0; i < movieLoaded; i++) {
                title[i] = savedInstanceState.getString( i + KEY_TITLE, "" );
                id[i] = savedInstanceState.getString( i + KEY_ID, "" );
                voting[i] = (Double.parseDouble( savedInstanceState.getString( i + KEY_VOTING, "" ) ));
                imgPath[i] = savedInstanceState.getString( i + KEY_IMGPATH, "" );
                date[i] = savedInstanceState.getString( i + KEY_DATE, "" );
            }
            loadOnce = savedInstanceState.getBoolean( KEY_BOOL, true );
            topicSelected = savedInstanceState.getString( KEY_TOPIC, "" );
            wasLoaded = movieLoaded;

            rcTv.setAdapter( new TvShowAdapter( getContext(), movieLoaded, title, id, voting, imgPath, date ) );

            reselectTopic();

        } else {
            //Load Data
            loadData( page, topicSelected );

            //Topic
            btnTopRatedTv.setTextColor( getResources().getColor( R.color.white ) );
            btnTopRatedTv.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

            btnAN.setTextColor( getResources().getColor( R.color.black ) );
            btnPopTv.setTextColor( getResources().getColor( R.color.black ) );
            btnOTA.setTextColor( getResources().getColor( R.color.black ) );

            //Listener Next Page
            nextPage();
            listener();
        }

        return view;
    }

    private void loadData(final Integer voidPage, String topic) {
        //Show PB and hide Load More
        pbTv.setVisibility( View.VISIBLE );
        btnLoadMoreTv.setVisibility( View.INVISIBLE );

        //String for page
        String stPage = Integer.toString( voidPage );

        OkHttpClient client = new OkHttpClient();
        String url = "https://api.themoviedb.org/3/tv/" + topic + "?api_key=" + BuildConfig.TMDB_API_KEY + "&language=en-US&page=" + stPage;

        Request request = new Request.Builder().url( url ).build();

        client.newCall( request ).enqueue( new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        page--;
                        loadOnce = false;
                        btnLoadMoreTv.setVisibility( View.VISIBLE );
                        pbTv.setVisibility( View.GONE );
                        Toast.makeText( getContext(), "Something went wrong, Please try later", Toast.LENGTH_SHORT ).show();
                    }
                } );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    final String responseData = response.body().string();

                    try {
                        JSONObject Jobject = new JSONObject( responseData );
                        JSONArray Jarray = Jobject.getJSONArray( "results" );

                        movieLoaded += Jarray.length();

                        //Variables
                        final String[] Get_title = new String[Jarray.length()];
                        final String[] Get_id = new String[Jarray.length()];
                        final String[] Get_imgPath = new String[Jarray.length()];
                        final Double[] Get_voting = new Double[Jarray.length()];
                        final String[] Get_date = new String[Jarray.length()];

                        //Getting Data from the JSON Array
                        for (int i = 0; i < Jarray.length(); i++) {
                            JSONObject object = Jarray.getJSONObject( i );
                            Get_title[i] = object.getString( "name" );
                            Get_id[i] = Integer.toString( object.getInt( "id" ) );
                            Get_voting[i] = object.getDouble( "vote_average" );
                            Get_imgPath[i] = object.getString( "poster_path" );
                            Get_date[i] = object.getString( "first_air_date" );
                        }

                        getActivity().runOnUiThread( new Runnable() {
                            @Override
                            public void run() {

                                tv_scroll.setSmoothScrollingEnabled( false );

                                Integer index = 0;
                                for (Integer i = wasLoaded; i < movieLoaded; i++) {
                                    title[i] = Get_title[index];
                                    id[i] = Get_id[index];
                                    imgPath[i] = Get_imgPath[index];
                                    voting[i] = Get_voting[index];
                                    date[i] = Get_date[index];
                                    index++;
                                }

                                wasLoaded = movieLoaded;

                                //ReSet Adapter
                                rcTv.setAdapter( new TvShowAdapter( getContext(), movieLoaded, title, id, voting, imgPath, date ) );
                                tv_scroll.scrollTo( 0, lastScrollVal );

                                //Hide PB and show Load More
                                btnLoadMoreTv.setVisibility( View.VISIBLE );
                                pbTv.setVisibility( View.INVISIBLE );

                                onSaveInstanceState( new Bundle() );

                                //Countdown Timer to give a delay for Load more Data
                                new CountDownTimer( 3000, 1000 ) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {

                                    }

                                    @Override
                                    public void onFinish() {
                                        loadOnce = false;
                                    }
                                }.start();
                            }
                        } );

                    } catch (Exception exception) {

                    }

                }
            }
        } );
    }

    private void nextPage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tv_scroll.setOnScrollChangeListener( new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    totalHeight = tv_scroll.getChildAt( 0 ).getHeight();
                    lastScrollVal = scrollY;
                    if (scrollY >= totalHeight - 2400 && loadOnce == false) {
                        loadOnce = true;
                        page++;
                        loadData( page, topicSelected );
                    }
                }
            } );
        }
    }

    private void listener() {
        btnLoadMoreTv.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    loadData( page, topicSelected );
                } else {
                    page++;
                    loadData( page, topicSelected );
                }
            }
        } );

        //Topic Change
        btnTopRatedTv.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (topicSelected.equals( "top_rated" )) {

                } else if (!pbTv.isShown()) {
                    rcTv.setAdapter( new TvShowAdapter( getContext(), 0, title, id, voting, imgPath, date ) );

                    topicSelected = "top_rated";
                    lastScrollVal = 0;
                    totalHeight = 0;
                    page = 1;
                    movieLoaded = 0;
                    wasLoaded = 0;
                    loadOnce = true;

                    btnTopRatedTv.setTextColor( getResources().getColor( R.color.white ) );
                    btnTopRatedTv.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

                    btnAN.setTextColor( getResources().getColor( R.color.black ) );
                    btnAN.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnPopTv.setTextColor( getResources().getColor( R.color.black ) );
                    btnPopTv.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnOTA.setTextColor( getResources().getColor( R.color.black ) );
                    btnOTA.setBackground( getResources().getDrawable( R.drawable.border ) );

                    loadData( page, topicSelected );
                }
            }
        } );

        btnAN.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (topicSelected.equals( "airing_today" )) {

                } else if (!pbTv.isShown()) {
                    rcTv.setAdapter( new TvShowAdapter( getContext(), 0, title, id, voting, imgPath, date ) );

                    topicSelected = "airing_today";
                    lastScrollVal = 0;
                    totalHeight = 0;
                    page = 1;
                    movieLoaded = 0;
                    wasLoaded = 0;
                    loadOnce = true;

                    btnTopRatedTv.setTextColor( getResources().getColor( R.color.black ) );
                    btnTopRatedTv.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnAN.setTextColor( getResources().getColor( R.color.white ) );
                    btnAN.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

                    btnPopTv.setTextColor( getResources().getColor( R.color.black ) );
                    btnPopTv.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnOTA.setTextColor( getResources().getColor( R.color.black ) );
                    btnOTA.setBackground( getResources().getDrawable( R.drawable.border ) );

                    loadData( page, topicSelected );
                }
            }
        } );

        btnPopTv.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (topicSelected.equals( "popular" )) {

                } else if (!pbTv.isShown()) {
                    rcTv.setAdapter( new TvShowAdapter( getContext(), 0, title, id, voting, imgPath, date ) );

                    topicSelected = "popular";
                    lastScrollVal = 0;
                    totalHeight = 0;
                    page = 1;
                    movieLoaded = 0;
                    wasLoaded = 0;
                    loadOnce = true;

                    btnTopRatedTv.setTextColor( getResources().getColor( R.color.black ) );
                    btnTopRatedTv.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnAN.setTextColor( getResources().getColor( R.color.black ) );
                    btnAN.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnPopTv.setTextColor( getResources().getColor( R.color.white ) );
                    btnPopTv.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

                    btnOTA.setTextColor( getResources().getColor( R.color.black ) );
                    btnOTA.setBackground( getResources().getDrawable( R.drawable.border ) );

                    loadData( page, topicSelected );
                }
            }
        } );

        btnOTA.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (topicSelected.equals( "on_the_air" )) {

                } else if (!pbTv.isShown()) {
                    rcTv.setAdapter( new TvShowAdapter( getContext(), 0, title, id, voting, imgPath, date ) );

                    topicSelected = "on_the_air";
                    lastScrollVal = 0;
                    totalHeight = 0;
                    page = 1;
                    movieLoaded = 0;
                    wasLoaded = 0;
                    loadOnce = true;

                    btnTopRatedTv.setTextColor( getResources().getColor( R.color.black ) );
                    btnTopRatedTv.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnAN.setTextColor( getResources().getColor( R.color.black ) );
                    btnAN.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnPopTv.setTextColor( getResources().getColor( R.color.black ) );
                    btnPopTv.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnOTA.setTextColor( getResources().getColor( R.color.white ) );
                    btnOTA.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

                    loadData( page, topicSelected );
                }
            }
        } );
    }

    private void reselectTopic() {
        if (topicSelected.equals( "top_rated" )) {

            btnTopRatedTv.setTextColor( getResources().getColor( R.color.white ) );
            btnTopRatedTv.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

            btnAN.setTextColor( getResources().getColor( R.color.black ) );
            btnAN.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnPopTv.setTextColor( getResources().getColor( R.color.black ) );
            btnPopTv.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnOTA.setTextColor( getResources().getColor( R.color.black ) );
            btnOTA.setBackground( getResources().getDrawable( R.drawable.border ) );

        } else if (topicSelected.equals( "airing_today" )) {
            btnTopRatedTv.setTextColor( getResources().getColor( R.color.black ) );
            btnTopRatedTv.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnAN.setTextColor( getResources().getColor( R.color.white ) );
            btnAN.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

            btnPopTv.setTextColor( getResources().getColor( R.color.black ) );
            btnPopTv.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnOTA.setTextColor( getResources().getColor( R.color.black ) );
            btnOTA.setBackground( getResources().getDrawable( R.drawable.border ) );
        } else if (topicSelected.equals( "popular" )) {
            btnTopRatedTv.setTextColor( getResources().getColor( R.color.black ) );
            btnTopRatedTv.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnAN.setTextColor( getResources().getColor( R.color.black ) );
            btnAN.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnPopTv.setTextColor( getResources().getColor( R.color.white ) );
            btnPopTv.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

            btnOTA.setTextColor( getResources().getColor( R.color.black ) );
            btnOTA.setBackground( getResources().getDrawable( R.drawable.border ) );
        } else if (topicSelected.equals( "on_the_air" )) {
            btnTopRatedTv.setTextColor( getResources().getColor( R.color.black ) );
            btnTopRatedTv.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnAN.setTextColor( getResources().getColor( R.color.black ) );
            btnAN.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnPopTv.setTextColor( getResources().getColor( R.color.black ) );
            btnPopTv.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnOTA.setTextColor( getResources().getColor( R.color.white ) );
            btnOTA.setBackground( getResources().getDrawable( R.drawable.border_selected ) );
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState( outState );
        //saving State
        outState.putInt( KEY_LOADED, movieLoaded );
        for (Integer i = 0; i < movieLoaded; i++) {
            outState.putString( i + KEY_TITLE, title[i] );
            outState.putString( i + KEY_ID, id[i] );
            outState.putString( i + KEY_VOTING, Double.toString( voting[i] ) );
            outState.putString( i + KEY_IMGPATH, imgPath[i] );
            outState.putString( i + KEY_DATE, date[i] );
        }
        outState.putBoolean( KEY_BOOL, loadOnce );
        outState.putString( KEY_TOPIC, topicSelected );
    }
}
