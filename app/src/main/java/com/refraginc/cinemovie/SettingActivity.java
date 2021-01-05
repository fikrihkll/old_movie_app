package com.refraginc.cinemovie;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.refraginc.cinemovie.broadcaster.AlertReceiver;
import com.refraginc.cinemovie.broadcaster.AlertReceiverDaily;

import java.util.Calendar;

public class SettingActivity extends AppCompatActivity {

    Toolbar toolbar;
    private Switch sDaily, sRelease;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_setting );

        sDaily = findViewById( R.id.switchDaily );
        sRelease = findViewById( R.id.switchRelease );
        toolbar = findViewById( R.id.toolbarSetting );

        setSupportActionBar( toolbar );
        getSupportActionBar().setTitle( "Setting" );
        toolbar.setNavigationIcon( getResources().getDrawable( R.drawable.ic_back ) );

        checkActivation();
        changeActivation();
        listener();
    }

    private void checkActivation() {
        SharedPreferences sharedPref = getSharedPreferences( "mypref", MODE_PRIVATE );
        String daily = sharedPref.getString( "Daily", "" );
        String release = sharedPref.getString( "Release", "" );

        if (daily.equals( "active" )) {
            sDaily.setChecked( true );
        } else {
            sDaily.setChecked( false );
        }

        if (release.equals( "active" )) {
            sRelease.setChecked( true );
        } else {
            sRelease.setChecked( false );
        }

    }

    private void changeActivation() {
        //Daily Listener
        sDaily.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    AlarmManager alarmManager = (AlarmManager) getSystemService( Context.ALARM_SERVICE );
                    Intent intent = new Intent( SettingActivity.this, AlertReceiverDaily.class );
                    PendingIntent pendingIntent = PendingIntent.getBroadcast( SettingActivity.this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT );
                    alarmManager.cancel( pendingIntent );

                    SharedPreferences sharedPref = getSharedPreferences( "mypref", MODE_PRIVATE );
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString( "Daily", "deactive" );
                    editor.commit();
                } else {
                    Intent i = new Intent( SettingActivity.this, AlertReceiverDaily.class );
                    PendingIntent pi = PendingIntent.getBroadcast( SettingActivity.this, 1, i, PendingIntent.FLAG_UPDATE_CURRENT );
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

                    SharedPreferences sharedPref = getSharedPreferences( "mypref", MODE_PRIVATE );
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString( "Daily", "active" );
                    editor.commit();
                }
            }
        } );

        //Release Listener
        sRelease.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    AlarmManager alarmManager = (AlarmManager) getSystemService( Context.ALARM_SERVICE );
                    Intent intent = new Intent( SettingActivity.this, AlertReceiver.class );
                    PendingIntent pendingIntent = PendingIntent.getBroadcast( SettingActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
                    alarmManager.cancel( pendingIntent );

                    SharedPreferences sharedPref = getSharedPreferences( "mypref", MODE_PRIVATE );
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString( "Release", "deactive" );
                    editor.commit();
                } else {
                    Intent i = new Intent( SettingActivity.this, AlertReceiver.class );
                    PendingIntent pi = PendingIntent.getBroadcast( SettingActivity.this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT );
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

                    SharedPreferences sharedPref = getSharedPreferences( "mypref", MODE_PRIVATE );
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString( "Release", "active" );
                    editor.commit();
                }
            }
        } );
    }

    private void listener() {
        toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingActivity.this.finish();
            }
        } );
    }
}
