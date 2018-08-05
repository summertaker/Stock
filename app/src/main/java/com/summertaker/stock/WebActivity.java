package com.summertaker.stock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.summertaker.stock.common.BaseActivity;

public class WebActivity extends BaseActivity {

    private WebView mWebView;
    private String mUrl;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_activity);

        mContext = WebActivity.this;

        initBaseActivity(mContext);
        initGesture();

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        mUrl = intent.getStringExtra("url");

        if (title != null && !title.isEmpty()) {
            setActionBarTitle(title);
        }

        mWebView = findViewById(R.id.webView);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);

        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.loadUrl(mUrl);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                Intent search = new Intent(this, SearchActivity.class);
                startActivity(search);
                return true;
            case R.id.action_open_in_new:
                Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
                startActivity(open);
                return true;
            case R.id.action_finish:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            String cookies = CookieManager.getInstance().getCookie(url);
            Log.e(TAG, ">> COOKIES\n" + cookies);
            /*
             PHPSESSID=ckq3gf3niq9ush51vpvkp7s0nm; mobile=true; user-agent=154734bf181769af3bbb219f924580e0; xe_logged=true
            */
        }
        /*
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().equals("www.example.com")) {
                // This is possession web site, so do not override; let possession WebView readKey the page
                return false;
            }
            // Otherwise, the link is not for a page on possession site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);

            return true;
        }
        */
    }

    @Override
    protected void onSwipeRight() {
        finish();
    }

    @Override
    protected void onSwipeLeft() {

    }

}
