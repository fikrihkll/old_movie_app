package com.refraginc.cinemovie.movie;


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
public class MovieFragment extends Fragment {
    private static String KEY_TITLE = "STATE_TITLE";
    private static String KEY_ID = "STATE_ID";
    private static String KEY_VOTING = "STATE_VOTING";
    private static String KEY_IMGPATH = "STATE_IMGPATH";
    private static String KEY_DATE = "STATE_DATE";
    private static String KEY_LOADED = "STATE_LOADED";
    private static String KEY_BOOL = "STATE_BOOL";
    private static String KEY_TOPIC = "STATE_TOPIC";
    private final String[] title = new String[9999];
    private final String[] id = new String[9999];
    private final double[] voting = new double[9999];
    private final String[] imgPath = new String[9999];
    private final String[] date = new String[9999];
    private RecyclerView rcMovie;
    private NestedScrollView movie_scroll;
    private HorizontalScrollView hsv_movie;
    private TextView btnLoadMore;
    private ProgressBar pb;
    private TextView btnPop, btnTopRated, btnUpc, btnNowPl;
    private Integer lastScrollVal = 0;
    private Integer totalHeight = 0;
    private Integer page = 1;
    private Integer movieLoaded = 0;
    private Integer wasLoaded = 0;
    private Boolean loadOnce = true;
    private String topicSelected = "top_rated";

    public MovieFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.fragment_movie, container, false );

        rcMovie = view.findViewById( R.id.rcMovie );
        rcMovie.setLayoutManager( new GridLayoutManager( getContext(), 2 ) );

        movie_scroll = view.findViewById( R.id.movie_scroll );
        hsv_movie = view.findViewById( R.id.hsv_movie );
        btnLoadMore = view.findViewById( R.id.btnLoadMore );
        pb = view.findViewById( R.id.pb );
        btnPop = view.findViewById( R.id.btnPop );
        btnTopRated = view.findViewById( R.id.btnTopRated );
        btnUpc = view.findViewById( R.id.btnUpc );
        btnNowPl = view.findViewById( R.id.btnNowPl );

        //Reload the Data

        if (savedInstanceState != null) {
            movieLoaded = savedInstanceState.getInt( KEY_LOADED, 0 );
            for (Integer i = 0; i < movieLoaded; i++) {
                title[i] = savedInstanceState.getString( i + KEY_TITLE, "" );
                id[i] = savedInstanceState.getString( i + KEY_ID, "" );
                voting[i] = Double.parseDouble( savedInstanceState.getString( i + KEY_VOTING, "" ) );
                imgPath[i] = savedInstanceState.getString( i + KEY_IMGPATH, "" );
                date[i] = savedInstanceState.getString( i + KEY_DATE, "" );
            }
            loadOnce = savedInstanceState.getBoolean( KEY_BOOL, true );
            topicSelected = savedInstanceState.getString( KEY_TOPIC, "" );
            wasLoaded = movieLoaded;

            rcMovie.setAdapter( new MovieAdapter( getContext(), movieLoaded, title, id, voting, imgPath, date ) );

            reselectTopic();


        } else {
            //Load Data
            loadData( page, topicSelected );

            //Topic
            btnTopRated.setTextColor( getResources().getColor( R.color.white ) );
            btnTopRated.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

            btnNowPl.setTextColor( getResources().getColor( R.color.black ) );
            btnPop.setTextColor( getResources().getColor( R.color.black ) );
            btnUpc.setTextColor( getResources().getColor( R.color.black ) );


            //Listener Next Page
            nextPage();
            listener();
        }

        return view;
    }

    private void loadData(final Integer voidPage, String topic) {
        //Show PB and hide Load More
        pb.setVisibility( View.VISIBLE );
        btnLoadMore.setVisibility( View.INVISIBLE );

        //String for page
        String stPage = Integer.toString( voidPage );

        OkHttpClient client = new OkHttpClient();
        String url = "https://api.themoviedb.org/3/movie/" + topic + "?api_key=" + BuildConfig.TMDB_API_KEY + "&language=en-US&page=" + stPage;

        Request request = new Request.Builder().url( url ).build();

        client.newCall( request ).enqueue( new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        page--;
                        loadOnce = false;
                        btnLoadMore.setVisibility( View.VISIBLE );
                        pb.setVisibility( View.GONE );
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
                            Get_title[i] = object.getString( "title" );
                            Get_id[i] = Integer.toString( object.getInt( "id" ) );
                            Get_voting[i] = object.getDouble( "vote_average" );
                            Get_imgPath[i] = object.getString( "poster_path" );
                            Get_date[i] = object.getString( "release_date" );
                        }

                        getActivity().runOnUiThread( new Runnable() {
                            @Override
                            public void run() {
                                movie_scroll.setSmoothScrollingEnabled( false );

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
                                rcMovie.setAdapter( new MovieAdapter( getContext(), movieLoaded, title, id, voting, imgPath, date ) );
                                movie_scroll.scrollTo( 0, lastScrollVal );

                                //Hide PB and show Load More
                                btnLoadMore.setVisibility( View.VISIBLE );
                                pb.setVisibility( View.INVISIBLE );

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
            movie_scroll.setOnScrollChangeListener( new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    totalHeight = movie_scroll.getChildAt( 0 ).getHeight();
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
        btnLoadMore.setOnClickListener( new View.OnClickListener() {
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
        btnTopRated.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (topicSelected.equals( "top_rated" )) {

                } else if (!pb.isShown()) {
                    rcMovie.setAdapter( new MovieAdapter( getContext(), 0, title, id, voting, imgPath, date ) );

                    topicSelected = "top_rated";
                    lastScrollVal = 0;
                    totalHeight = 0;
                    page = 1;
                    movieLoaded = 0;
                    wasLoaded = 0;
                    loadOnce = true;

                    btnTopRated.setTextColor( getResources().getColor( R.color.white ) );
                    btnTopRated.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

                    btnNowPl.setTextColor( getResources().getColor( R.color.black ) );
                    btnNowPl.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnPop.setTextColor( getResources().getColor( R.color.black ) );
                    btnPop.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnUpc.setTextColor( getResources().getColor( R.color.black ) );
                    btnUpc.setBackground( getResources().getDrawable( R.drawable.border ) );

                    loadData( page, topicSelected );
                }
            }
        } );

        btnNowPl.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (topicSelected.equals( "now_playing" )) {

                } else if (!pb.isShown()) {
                    rcMovie.setAdapter( new MovieAdapter( getContext(), 0, title, id, voting, imgPath, date ) );

                    topicSelected = "now_playing";
                    lastScrollVal = 0;
                    totalHeight = 0;
                    page = 1;
                    movieLoaded = 0;
                    wasLoaded = 0;
                    loadOnce = true;

                    btnTopRated.setTextColor( getResources().getColor( R.color.black ) );
                    btnTopRated.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnNowPl.setTextColor( getResources().getColor( R.color.white ) );
                    btnNowPl.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

                    btnPop.setTextColor( getResources().getColor( R.color.black ) );
                    btnPop.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnUpc.setTextColor( getResources().getColor( R.color.black ) );
                    btnUpc.setBackground( getResources().getDrawable( R.drawable.border ) );

                    loadData( page, topicSelected );
                }
            }
        } );

        btnPop.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (topicSelected.equals( "popular" )) {

                } else if (!pb.isShown()) {
                    rcMovie.setAdapter( new MovieAdapter( getContext(), 0, title, id, voting, imgPath, date ) );

                    topicSelected = "popular";
                    lastScrollVal = 0;
                    totalHeight = 0;
                    page = 1;
                    movieLoaded = 0;
                    wasLoaded = 0;
                    loadOnce = true;

                    btnTopRated.setTextColor( getResources().getColor( R.color.black ) );
                    btnTopRated.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnNowPl.setTextColor( getResources().getColor( R.color.black ) );
                    btnNowPl.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnPop.setTextColor( getResources().getColor( R.color.white ) );
                    btnPop.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

                    btnUpc.setTextColor( getResources().getColor( R.color.black ) );
                    btnUpc.setBackground( getResources().getDrawable( R.drawable.border ) );

                    loadData( page, topicSelected );
                }
            }
        } );

        btnUpc.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (topicSelected.equals( "upcoming" )) {

                } else if (!pb.isShown()) {
                    rcMovie.setAdapter( new MovieAdapter( getContext(), 0, title, id, voting, imgPath, date ) );

                    topicSelected = "upcoming";
                    lastScrollVal = 0;
                    totalHeight = 0;
                    page = 1;
                    movieLoaded = 0;
                    wasLoaded = 0;
                    loadOnce = true;

                    btnTopRated.setTextColor( getResources().getColor( R.color.black ) );
                    btnTopRated.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnNowPl.setTextColor( getResources().getColor( R.color.black ) );
                    btnNowPl.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnPop.setTextColor( getResources().getColor( R.color.black ) );
                    btnPop.setBackground( getResources().getDrawable( R.drawable.border ) );

                    btnUpc.setTextColor( getResources().getColor( R.color.white ) );
                    btnUpc.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

                    loadData( page, topicSelected );
                }
            }
        } );
    }

    private void reselectTopic() {
        if (topicSelected.equals( "top_rated" )) {
            btnTopRated.setTextColor( getResources().getColor( R.color.white ) );
            btnTopRated.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

            btnNowPl.setTextColor( getResources().getColor( R.color.black ) );
            btnNowPl.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnPop.setTextColor( getResources().getColor( R.color.black ) );
            btnPop.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnUpc.setTextColor( getResources().getColor( R.color.black ) );
            btnUpc.setBackground( getResources().getDrawable( R.drawable.border ) );
        } else if (topicSelected.equals( "now_playing" )) {
            btnTopRated.setTextColor( getResources().getColor( R.color.black ) );
            btnTopRated.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnNowPl.setTextColor( getResources().getColor( R.color.white ) );
            btnNowPl.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

            btnPop.setTextColor( getResources().getColor( R.color.black ) );
            btnPop.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnUpc.setTextColor( getResources().getColor( R.color.black ) );
            btnUpc.setBackground( getResources().getDrawable( R.drawable.border ) );
        } else if (topicSelected.equals( "popular" )) {
            btnTopRated.setTextColor( getResources().getColor( R.color.black ) );
            btnTopRated.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnNowPl.setTextColor( getResources().getColor( R.color.black ) );
            btnNowPl.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnPop.setTextColor( getResources().getColor( R.color.white ) );
            btnPop.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

            btnUpc.setTextColor( getResources().getColor( R.color.black ) );
            btnUpc.setBackground( getResources().getDrawable( R.drawable.border ) );
        } else if (topicSelected.equals( "upcoming" )) {
            btnTopRated.setTextColor( getResources().getColor( R.color.black ) );
            btnTopRated.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnNowPl.setTextColor( getResources().getColor( R.color.black ) );
            btnNowPl.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnPop.setTextColor( getResources().getColor( R.color.black ) );
            btnPop.setBackground( getResources().getDrawable( R.drawable.border ) );

            btnUpc.setTextColor( getResources().getColor( R.color.white ) );
            btnUpc.setBackground( getResources().getDrawable( R.drawable.border_selected ) );
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
