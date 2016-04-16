package com.bqdev.darkReddit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class DetailActivity extends Activity {


    String mImageURL; // 13
    ShareActionProvider mShareActionProvider; // 14

    WebView webV;
    TextView titleV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);


        setContentView(R.layout.activity_detail);

        webV = (WebView)findViewById(R.id.webV);
        titleV = (TextView)findViewById(R.id.detail_title);

        String link = this.getIntent().getExtras().getString("link");

        String comments = this.getIntent().getExtras().getString("comments");
        Log.w("permalink", comments);
        comments = "http://www.reddit.com" + comments + ".compact";

        commentButtonSetup(comments);


        //if the link is to a reddit comment section, avoid the pesky desktop site
        if(link.contains("reddit") && link.contains("comment")){

            link += ".compact";
        }

        String title = this.getIntent().getExtras().getString("title");
        if(title.length() > 65){
            title = title.substring(0,65) + "...";
        }


       webViewSetup();

        webV.loadUrl(link);
        titleV.setText(title);

    }

    private void webViewSetup(){
        webV.getSettings().setBuiltInZoomControls(true);
        webV.setWebViewClient(new WebViewClient());
        webV.getSettings().setJavaScriptEnabled(true);

        webV.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                findViewById(R.id.loading_text).setVisibility(View.VISIBLE);

                findViewById(R.id.detail_title).setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url){

                findViewById(R.id.loading_text).setVisibility(View.GONE);

                findViewById(R.id.detail_title).setVisibility(View.VISIBLE);
            }

        });
    }

    private void commentButtonSetup(String link){

        final String commentsLink = link;
        Button commentButton = (Button)findViewById(R.id.comment_button);

        commentButton.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                webV.loadUrl(commentsLink);
                setupAd();
                return false;
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        if((keyCode == KeyEvent.KEYCODE_BACK) && webV.canGoBack()){
            webV.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setShareIntent() {

        // create an Intent with the contents of the TextView
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                "Book Recommendation!");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mImageURL);

        // Make sure the provider knows
        // it should work with that Intent
        mShareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu
        // this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Access the Share Item defined in menu XML
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);

        // Access the object responsible for
        // putting together the sharing submenu
        if (shareItem != null) {
            mShareActionProvider
                    = (ShareActionProvider) shareItem.getActionProvider();
        }

        setShareIntent();

        return true;
    }

    private void setupAd(){
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("XXXXXXXXX");

        LinearLayout layout = (LinearLayout)findViewById(R.id.adView1);
        layout.addView(adView);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("XXXXXXXX")
                .build();

        adView.loadAd(adRequest);
    }
}
