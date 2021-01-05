package com.refraginc.cinemovie;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;


public class WebActivity extends AppCompatActivity {
    String title = "";
    String nameChanged = "";
    WebView webView;
    ImageView btnBack, btnForward;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_web );

        //Initializing
        toolbar = findViewById( R.id.toolbarWeb );
        webView = findViewById( R.id.webView );
        btnBack = findViewById( R.id.btnBack );
        btnForward = findViewById( R.id.btnForward );

        //Setting up Toolbar
        toolbar.setNavigationIcon( getResources().getDrawable( R.drawable.ic_back ) );
        toolbar.inflateMenu( R.menu.web_menu );
        toolbar.setTitle( title );

        //Get Title Value
        Bundle bd = getIntent().getExtras();
        title = bd.getString( MainActivity.TITLE_KEY, "" );

        //Setting Up
        webView.setWebViewClient( new WebViewClient() );
        changeTitle( title );
        webView.loadUrl( "https://www.google.com/search?q=download+" + title + "+layarkaca21" );

        //Web Setting
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled( true );
        webSettings.setJavaScriptEnabled( true );
        webSettings.setAllowFileAccess( true );
        webSettings.setAppCacheEnabled( true );
        webSettings.setAllowContentAccess( true );
        webSettings.setDomStorageEnabled( true );
        webView.setLayerType( WebView.LAYER_TYPE_HARDWARE, null );

        //Listener
        listener();
    }

    private void changeTitle(String name) {
        nameChanged = "";

        for (Integer i = 0; i < name.length(); i++) {
            if (name.charAt( i ) == ' ' || name.charAt( i ) == '-' || name.charAt( i ) == '/' || name.charAt( i ) == '.' || name.charAt( i ) == ',') {
                nameChanged += "+";
            } else {
                nameChanged += Character.toString( name.charAt( i ) );
            }
        }
    }

    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }

    }

    private void listener() {
        btnBack.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) {
                    webView.goBack();
                }
            }
        } );

        btnForward.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoForward()) {
                    webView.goForward();
                }
            }
        } );

        toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebActivity.this.finish();
            }
        } );

        toolbar.setOnMenuItemClickListener( new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.m_brows) {
                    Intent i = new Intent( Intent.ACTION_VIEW );
                    i.setData( Uri.parse( webView.getUrl() ) );
                    startActivity( i );
                }
                return false;
            }
        } );
    }
}