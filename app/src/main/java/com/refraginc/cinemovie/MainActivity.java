package com.refraginc.cinemovie;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.refraginc.cinemovie.broadcaster.AlertReceiver;
import com.refraginc.cinemovie.broadcaster.AlertReceiverDaily;
import com.refraginc.cinemovie.favorite.FavoriteActivity;
import com.refraginc.cinemovie.livechat.ChatData;
import com.refraginc.cinemovie.livechat.LiveChatAdapter;
import com.refraginc.cinemovie.search.SearchAdapter;
import com.refraginc.cinemovie.search.SearchData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static String PARCE_KEY = "PARCELABLE";
    public static String TITLE_KEY = "PARCELABLE";
    private Handler hd;
    private Runnable rn;
    private String id = "";
    private ArrayList<ChatData> dataChat;
    private RecyclerView rcChat;
    private LiveChatAdapter adapter;
    private Dialog dl;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Toolbar mainToolbar;
    private RecyclerView sList;
    private TextView btnFindMovie;
    private TextView btnFindTv;
    private LinearLayout findTopic;
    private SearchAdapter searchAdapter;
    private ArrayList<SearchData> data;
    private FirebaseFirestore db;
    private Map<String, Object> user;

    private Boolean messageOpened=false;
    private String findSelected = "movie";

    public static boolean isTimeAutomatic(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt( c.getContentResolver(), Settings.Global.AUTO_TIME, 0 ) == 1;
        } else {
            return android.provider.Settings.System.getInt( c.getContentResolver(), android.provider.Settings.System.AUTO_TIME, 0 ) == 1;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        tabLayout = findViewById( R.id.tabLayout );
        db = FirebaseFirestore.getInstance();
        user = new HashMap<>();
        dl = new Dialog( MainActivity.this );

        //Setting Up Search RecyclerView
        data = new ArrayList<>();
        dataChat = new ArrayList<>();
        searchAdapter = new SearchAdapter( MainActivity.this, data );
        sList = findViewById( R.id.sList );
        sList.setLayoutManager( new LinearLayoutManager( MainActivity.this ) );

        //Setting up View Pager
        viewPager = findViewById( R.id.viewPager );
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter( getSupportFragmentManager() );
        viewPager.setAdapter( viewPagerAdapter );

        //Setting up for Toolbar
        mainToolbar = findViewById( R.id.mainToolbar );
        setSupportActionBar( mainToolbar );
        mainToolbar.setTitle( "Cinemovie" );
        mainToolbar.setTitleTextColor( getResources().getColor( R.color.black ) );

        //Setting up Searching Elements
        findTopic = findViewById( R.id.findTopic );
        btnFindMovie = findViewById( R.id.btnFindMovie );
        btnFindTv = findViewById( R.id.btnFindTv );
        btnFindMovie.setTextColor( getResources().getColor( R.color.white ) );
        btnFindTv.setTextColor( getResources().getColor( R.color.black ) );
        btnFindMovie.setBackground( getResources().getDrawable( R.drawable.border_selected ) );
        findTopic.setVisibility( View.GONE );
        hd = new Handler();

        listener();
        setReminder();
        loadDataRelease();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate( R.menu.toolbar_menu, menu );

        SearchManager searchManager = (SearchManager) getSystemService( SEARCH_SERVICE );
        if (searchManager != null) {
            SearchView searchView = (SearchView) (menu.findItem( R.id.m_search )).getActionView();
            searchView.setSearchableInfo( searchManager.getSearchableInfo( getComponentName() ) );
            searchView.setIconifiedByDefault( false );
            searchView.setQueryHint( getResources().getString( R.string.strSearchHint ) );
            searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    find( query );
                    return true;
                }

                @Override
                public boolean onQueryTextChange(final String newText) {
                    if (rn == null) {
                        rn = new Runnable() {
                            @Override
                            public void run() {
                                find( newText );
                            }
                        };
                        hd.postDelayed( rn, 1000 );
                    } else {
                        hd.removeCallbacks( rn );
                        rn = new Runnable() {
                            @Override
                            public void run() {
                                find( newText );
                            }
                        };
                        hd.postDelayed( rn, 1000 );
                    }
                    return false;
                }
            } );


            searchView.addOnAttachStateChangeListener( new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {

                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    data.clear();
                    searchAdapter.notifyDataSetChanged();
                    sList.setVisibility( View.GONE );
                    findTopic.setVisibility( View.GONE );
                    tabLayout.setVisibility( View.VISIBLE );
                    viewPager.setVisibility( View.VISIBLE );
                }
            } );
        }
        return true;
    }

    public void listener() {
        mainToolbar.setOnMenuItemClickListener( new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.m_favorite:
                        startActivity( new Intent( MainActivity.this, FavoriteActivity.class ) );
                        MainActivity.this.finish();
                        break;
                    case R.id.m_lgsetting:
                        Intent mIntent = new Intent( Settings.ACTION_LOCALE_SETTINGS );
                        startActivity( mIntent );
                        break;
                    case R.id.m_search:
                        sList.setVisibility( View.VISIBLE );
                        findTopic.setVisibility( View.VISIBLE );
                        tabLayout.setVisibility( View.GONE );
                        viewPager.setVisibility( View.GONE );
                        break;
                    case R.id.m_notif:
                        startActivity( new Intent( MainActivity.this, SettingActivity.class ) );
                        break;
                    case R.id.m_liveChat:
                        liveChatDialog();
                        break;
                }
                return false;
            }
        } );

        tabLayout.addOnTabSelectedListener( new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem( tab.getPosition(), true );
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        } );

        viewPager.addOnPageChangeListener( new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                tabLayout.setScrollPosition( i, v, true );
            }

            @Override
            public void onPageSelected(int i) {
                TabLayout.Tab tab = tabLayout.getTabAt( i );
                if (tab != null) {
                    tab.select();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        } );

        //Find Topic Change
        btnFindTv.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFindTv.setTextColor( getResources().getColor( R.color.white ) );
                btnFindTv.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

                btnFindMovie.setTextColor( getResources().getColor( R.color.black ) );
                btnFindMovie.setBackground( getResources().getDrawable( R.drawable.border ) );

                findSelected = "tv";
            }
        } );

        btnFindMovie.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFindMovie.setTextColor( getResources().getColor( R.color.white ) );
                btnFindMovie.setBackground( getResources().getDrawable( R.drawable.border_selected ) );

                btnFindTv.setTextColor( getResources().getColor( R.color.black ) );
                btnFindTv.setBackground( getResources().getDrawable( R.drawable.border ) );

                findSelected = "movie";
            }
        } );

    }

    //Find will be called from Search View at Listener Function
    public void find(String query) {
        data.clear();
        sList.setAdapter( searchAdapter );
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.themoviedb.org/3/search/" + findSelected + "?api_key=" + BuildConfig.TMDB_API_KEY + "&language=en-US&query=" + query + "&page=1";

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
                        final String[] Get_imgPath = new String[Jarray.length()];
                        final Double[] Get_voting = new Double[Jarray.length()];
                        final String[] Get_date = new String[Jarray.length()];

                        if (findSelected.equals( "movie" )) {
                            //Getting Data from the JSON Array
                            for (int i = 0; i < Jarray.length(); i++) {
                                JSONObject object = Jarray.getJSONObject( i );
                                Get_title[i] = object.getString( "title" );
                                Get_id[i] = Integer.toString( object.getInt( "id" ) );
                                Get_voting[i] = object.getDouble( "vote_average" );
                                Get_imgPath[i] = object.getString( "poster_path" );
                                Get_date[i] = object.getString( "release_date" );
                            }
                        } else {
                            //Getting Data from the JSON Array
                            for (int i = 0; i < Jarray.length(); i++) {
                                JSONObject object = Jarray.getJSONObject( i );
                                Get_title[i] = object.getString( "name" );
                                Get_id[i] = Integer.toString( object.getInt( "id" ) );
                                Get_voting[i] = object.getDouble( "vote_average" );
                                Get_imgPath[i] = object.getString( "poster_path" );
                                Get_date[i] = object.getString( "first_air_date" );
                            }
                        }


                        MainActivity.this.runOnUiThread( new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < Jarray.length(); i++) {
                                    SearchData parce = new SearchData();

                                    parce.setTitle( Get_title[i] );
                                    parce.setId( Get_id[i] );
                                    parce.setImgPath( Get_imgPath[i] );
                                    parce.setRating( Double.toString( Get_voting[i] ) );
                                    parce.setDate( Get_date[i] );
                                    parce.setType( findSelected );

                                    data.add( parce );
                                }

                                //ReSet Adapter
                                sList.setAdapter( searchAdapter );

                                //Hide Loading


                            }
                        } );

                    } catch (Exception ignored) {

                    }

                }
            }
        } );
    }

    //Check, is The Setting Active or not
    public void setReminder() {
        SharedPreferences sharedPref = getSharedPreferences( "mypref", MODE_PRIVATE );
        String daily = sharedPref.getString( "Daily", "" );
        String release = sharedPref.getString( "Release", "" );

        if (daily.isEmpty()) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString( "Daily", "active" );
            editor.putString( "Release", "active" );
            editor.commit();

            startReminderDaily();
            startReminderRelease();
        } else {
            if (daily.equals( "active" )) {
                startReminderDaily();
            } else {
                AlarmManager alarmManager = (AlarmManager) getSystemService( Context.ALARM_SERVICE );
                Intent intent = new Intent( MainActivity.this, AlertReceiverDaily.class );
                PendingIntent pendingIntent = PendingIntent.getBroadcast( MainActivity.this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT );
                alarmManager.cancel( pendingIntent );
            }

            if (release.equals( "active" )) {
                startReminderRelease();
            } else {
                AlarmManager alarmManager = (AlarmManager) getSystemService( Context.ALARM_SERVICE );
                Intent intent = new Intent( MainActivity.this, AlertReceiver.class );
                PendingIntent pendingIntent = PendingIntent.getBroadcast( MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
                alarmManager.cancel( pendingIntent );
            }
        }
    }

    //These will be called at setReminder Function
    public void startReminderRelease() {
        //START NOTIF
        Intent i = new Intent( MainActivity.this, AlertReceiver.class );
        PendingIntent pi = PendingIntent.getBroadcast( MainActivity.this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT );
        AlarmManager alarmManager = (AlarmManager) getSystemService( ALARM_SERVICE );

        Calendar triggerhCal = Calendar.getInstance();
        Calendar currentCal = Calendar.getInstance();

        triggerhCal.set( Calendar.HOUR_OF_DAY, 8 );
        triggerhCal.set( Calendar.MINUTE, 0 );
        triggerhCal.set( Calendar.SECOND, 0 );

        long intendedTime = triggerhCal.getTimeInMillis();
        long currentTime = currentCal.getTimeInMillis();

        if (intendedTime >= currentTime) {
            alarmManager.setRepeating( AlarmManager.RTC, intendedTime, AlarmManager.INTERVAL_DAY, pi );
        } else {
            triggerhCal.add( Calendar.DAY_OF_MONTH, 1 );
            intendedTime = triggerhCal.getTimeInMillis();

            alarmManager.setRepeating( AlarmManager.RTC, intendedTime, AlarmManager.INTERVAL_DAY, pi );
        }
        //START NOTIF
    }

    private void startReminderDaily() {
        //START NOTIF
        Intent i = new Intent( MainActivity.this, AlertReceiverDaily.class );
        PendingIntent pi = PendingIntent.getBroadcast( MainActivity.this, 1, i, PendingIntent.FLAG_UPDATE_CURRENT );
        AlarmManager alarmManager = (AlarmManager) getSystemService( ALARM_SERVICE );

        Calendar triggerhCal = Calendar.getInstance();
        Calendar currentCal = Calendar.getInstance();

        triggerhCal.set( Calendar.HOUR_OF_DAY, 7 );
        triggerhCal.set( Calendar.MINUTE, 0 );
        triggerhCal.set( Calendar.SECOND, 0 );

        long intendedTime = triggerhCal.getTimeInMillis();
        long currentTime = currentCal.getTimeInMillis();

        if (intendedTime >= currentTime) {
            alarmManager.setRepeating( AlarmManager.RTC, intendedTime, AlarmManager.INTERVAL_DAY, pi );
        } else {
            triggerhCal.add( Calendar.DAY_OF_MONTH, 1 );
            intendedTime = triggerhCal.getTimeInMillis();

            alarmManager.setRepeating( AlarmManager.RTC, intendedTime, AlarmManager.INTERVAL_DAY, pi );
        }
        //START NOTIF
    }

    //Load Movie that will be Released to Prevent when the Device isn't connected to the Internet at 08:00 AM
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

    //This is Dialog for LiveChat
    private void liveChatDialog() {
        dl.setContentView( R.layout.layout_chat );
        final Boolean[] regClick = {true};

        //Identifying
        final EditText etMsg = dl.findViewById( R.id.etMsg );
        ImageButton btnSend = dl.findViewById( R.id.btnSend );
        final RelativeLayout layReg = dl.findViewById( R.id.layReg );
        final ConstraintLayout layChat = dl.findViewById( R.id.layChat );
        final EditText etId = dl.findViewById( R.id.etId );
        Button btnReg = dl.findViewById( R.id.btnReg );

        rcChat = dl.findViewById( R.id.rcChat );
        adapter = new LiveChatAdapter( MainActivity.this, dataChat );
        rcChat.setLayoutManager( new LinearLayoutManager( MainActivity.this ) );
        rcChat.setAdapter( adapter );

        //Get ID
        SharedPreferences sharedPref = getSharedPreferences( "mypref", MODE_PRIVATE );
        id = sharedPref.getString( "CHATID", "" );
        if (id.isEmpty()) {
            layChat.setVisibility( View.GONE );
            Toast.makeText( MainActivity.this, "Create New ID", Toast.LENGTH_LONG ).show();
        } else {
            layChat.setVisibility( View.VISIBLE );
            layReg.setVisibility( View.GONE );
        }

        if(messageOpened==false)
            messageListener();


        //DIALOG LISTENER------------------------------------------------------

        //Send Message
        btnSend.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat df = new SimpleDateFormat( "yyyy.MM.dd 'at' HH:mm:ss " );
                String date = df.format( Calendar.getInstance().getTime() );

                user.put( "from", id );
                user.put( "msg", etMsg.getText().toString() );
                user.put( "date", date );

                etMsg.setText( "" );

                db.collection( "Room" ).document( date + "-" + id ).set( user ).addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                } ).addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText( MainActivity.this, "Message can't be sent", Toast.LENGTH_LONG ).show();
                    }
                } );
            }
        } );

        //Register the ID
        btnReg.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (regClick[0]) {
                    id = etId.getText().toString();

                    FirebaseFirestore getId = FirebaseFirestore.getInstance();
                    final Map<String, Object> newUser = new HashMap<>();
                    newUser.put( "id", id );

                    getId.collection( "RoomID" ).document( id ).get().
                            addOnCompleteListener( new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            regClick[0] = false;
                                            Toast.makeText( MainActivity.this, "ID already exist, Change your New ID", Toast.LENGTH_LONG ).show();
                                        } else {
                                            FirebaseFirestore setId = FirebaseFirestore.getInstance();

                                            setId.collection( "RoomID" ).document( id ).set( newUser ).addOnCompleteListener( new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    SharedPreferences sharedPref = getSharedPreferences( "mypref", MODE_PRIVATE );
                                                    SharedPreferences.Editor editor = sharedPref.edit();
                                                    editor.putString( "CHATID", id );
                                                    editor.commit();

                                                    layReg.setVisibility( View.GONE );
                                                    layChat.setVisibility( View.VISIBLE );
                                                }
                                            } );
                                        }
                                    } else {

                                    }
                                }
                            } );
                }
            }
        } );

        dl.show();

        if (!isTimeAutomatic( MainActivity.this )) {
            dl.dismiss();
            Toast.makeText( MainActivity.this, "Please Set Your Date and Time to be Auto", Toast.LENGTH_LONG ).show();
        }
    }

    //messageListener will be called when the user clicked liveChatDialog
    private void messageListener() {
        messageOpened=true;

        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        final Query query = rootRef.collection( "Room" ).orderBy( "date", Query.Direction.DESCENDING ).limit( 1 );
        query.addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    DateFormat df = new SimpleDateFormat( "yyyy.MM.dd 'at' HH:mm:ss " );
                    String date = df.format( Calendar.getInstance().getTime() );

                    ChatData con = new ChatData();
                    con.setFrom( doc.getString( "from" ) );
                    con.setMessage( doc.getString( "msg" ) );
                    con.setDate( date );
                    con.setMyId( id );

                    dataChat.add( con );
                }
                if (dl.isShowing()) {
                    rcChat.setAdapter( adapter );
                    rcChat.smoothScrollToPosition( rcChat.getHeight() );
                }
            }
        } );
    }
}