package com.refraginc.cinemovie.broadcaster;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.refraginc.cinemovie.BuildConfig;
import com.refraginc.cinemovie.Connector;
import com.refraginc.cinemovie.MainActivity;
import com.refraginc.cinemovie.R;
import com.refraginc.cinemovie.movie.DetailActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class AlertReceiver extends BroadcastReceiver {
    private static String CHANNEL_ID = "CHANNEL_1";
    private static String CHANNEL_NAME = "Cinemovie Channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        loadDataOffline( context );
        loadData( context );
    }

    private void loadData(final Context mContext) {
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
                        Log.w( "ALARM", "JSON PARSED" );
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
                        //Get random number
                        Random rand = new Random();
                        int n = rand.nextInt( Jarray.length() );
                        n += 0;
                        Integer indx = n;

                        //Set Notif
                        notif( mContext, Get_title[indx], "Released " + Get_date[indx] + ", Let's check it out ", Get_id[indx] );

                    } catch (Exception exception) {

                    }

                }
            }
        } );
    }

    private void loadDataOffline(@NonNull Context mContext) {
        Integer totalBckp = 0;
        String[] id = new String[100];
        String[] title = new String[100];
        String[] voting = new String[100];
        String[] date = new String[100];

        SharedPreferences sharedPref = mContext.getSharedPreferences( "mypref", MODE_PRIVATE );
        Integer i = 0;
        while (i < 50) {
            title[i] = sharedPref.getString( "rlsTitle" + i.toString(), "" );
            id[i] = sharedPref.getString( "rlsId" + i.toString(), "" );
            voting[i] = sharedPref.getString( "rlsVoting" + i.toString(), "" );
            date[i] = sharedPref.getString( "rlsDate" + i.toString(), "" );

            if (date[i].equals( "" )) {
                totalBckp = i;
                i = 50;
            }

            i++;
        }
        notif( mContext, title[totalBckp - 1], "Released " + date[totalBckp - 1] + ", Let's check it out ", id[totalBckp - 1] );

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove( "rlsTitle" + (totalBckp - 1) );
        editor.remove( "rlsId" + (totalBckp - 1) );
        editor.remove( "rlsVoting" + (totalBckp - 1) );
        editor.remove( "rlsDate" + (totalBckp - 1) );
        editor.commit();
    }

    private void notif(@NonNull Context mContext, String head, String body, String id) {
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService( Context.NOTIFICATION_SERVICE );

        Connector con = new Connector();
        con.setId( id );

        Intent resultIntent = new Intent( mContext, DetailActivity.class );
        resultIntent.putExtra( MainActivity.PARCE_KEY, con );
        resultIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        PendingIntent resultPendingIntent = PendingIntent.getActivity( mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT );

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder( mContext, CHANNEL_ID ).setSmallIcon( R.mipmap.ic_launcher ).setLargeIcon( BitmapFactory.decodeResource( mContext.getResources(), R.drawable.cinemovie ) ).setContentTitle( head ).setContentText( body ).setSubText( "New Movie Release Today" ).setContentIntent( resultPendingIntent ).setAutoCancel( true );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel( CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT );
            mBuilder.setChannelId( CHANNEL_ID );
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel( channel );
            }
        }

        Notification notification = mBuilder.build();
        mNotificationManager.notify( 0, notification );
    }
}
