package com.bqdev.darkReddit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    String sorting;

    ArrayList<String> searchedSubReddits;
    ArrayAdapter subRedditAdapter;

    Button refresh;

    Button hotSort;
    Button topSort;
    Button risingSort;
    Button newSort;

    Button mainButton; // 2
    AutoCompleteTextView mainAutoComplete; // 3
    ListView mainListView; // 4
    JSONAdapter mJSONAdapter; // 10
    ArrayList<String> mNameList = new ArrayList<String>();
    ShareActionProvider mShareActionProvider; // 6
    private static final String PREFS = "prefs"; // 7
    private static final String PREF_NAME = "name"; // 7
    SharedPreferences mSharedPreferences; // 7
    private static final String QUERY_URL
            = "http://www.reddit.com/r/";

    private String urlPostfix;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        urlPostfix = "/.json?limit=100";

        requestWindowFeature(Window.FEATURE_NO_TITLE);


        //stop the keyboard popping up before EditText is tapped
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(com.bqdev.darkReddit.R.layout.activity_main);

        setup();


    }

    @Override
    public void onPause(){
        super.onPause();
        saveSearchedSubReddits(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        populatePopularSubReddits();

        loadSearchedSubReddits(this);
        subRedditAdapter.notifyDataSetChanged();

    }

    private void setup(){

        setupAutocompleteBox();
        setupListView();
        setupSortingButtons();
        setupOtherButtons();
        setupAd();

        sorting = "/hot";
        highlightSearchType(0);

        findSubReddit("all");
    }

    private void setupAutocompleteBox(){


        searchedSubReddits = new ArrayList<String>();
        //populatePopularSubReddits();
        //loadSearchedSubReddits(this);

        subRedditAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, searchedSubReddits);

        mainAutoComplete = (AutoCompleteTextView) findViewById(com.bqdev.darkReddit.R.id.main_auto_complete);
        mainAutoComplete.setAdapter(subRedditAdapter);
        mainAutoComplete.setThreshold(1);
        //subRedditAdapter.notifyDataSetChanged();

        mainAutoComplete.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionID, KeyEvent keyEvent) {

                if (actionID == EditorInfo.IME_ACTION_SEARCH) {

                    onClick(textView);
                }
                return false;
            }
        });

    }

    private void setupListView(){
        //  Access the ListView
        mainListView = (ListView) findViewById(com.bqdev.darkReddit.R.id.main_listview);

        //  Set this activity to react to list items being pressed
        mainListView.setOnItemClickListener(this);

        //  Create a JSONAdapter for the ListView
        mJSONAdapter = new JSONAdapter(this, getLayoutInflater());

        // Set the ListView to use the ArrayAdapter
        mainListView.setAdapter(mJSONAdapter);
    }

    private void setupSortingButtons(){

        topSort = (Button)findViewById(com.bqdev.darkReddit.R.id.topSort);
        hotSort = (Button)findViewById(com.bqdev.darkReddit.R.id.hotSort);
        risingSort = (Button)findViewById(com.bqdev.darkReddit.R.id.risingSort);
        newSort = (Button)findViewById(com.bqdev.darkReddit.R.id.newSort);

        topSort.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                highlightSearchType(2);
                return topSortTimeFrameDecision(view, motionEvent);
            }
        });

        hotSort.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                sorting = "/hot";
                urlPostfix = "/.json?limit=100";
                highlightSearchType(0);
                mainListView.smoothScrollToPosition(0);
                String currentSub = mainAutoComplete.getText().toString();
                if(currentSub.length() != 0)
                    findSubReddit(currentSub);
                else
                    findSubReddit("all");


                return false;
            }
        });

        risingSort.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                sorting = "/rising";
                urlPostfix = "/.json?limit=100";
                highlightSearchType(3);
                mainListView.smoothScrollToPosition(0);
                String currentSub = mainAutoComplete.getText().toString();
                if(currentSub.length() != 0)
                    findSubReddit(currentSub);
                else
                    findSubReddit("all");

                return false;
            }
        });

        newSort.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                sorting = "/new";
                urlPostfix = "/.json?limit=100";
                highlightSearchType(1);
                mainListView.smoothScrollToPosition(0);
                String currentSub = mainAutoComplete.getText().toString();
                if(currentSub.length() != 0)
                    findSubReddit(currentSub);
                else
                    findSubReddit("all");

                return false;
            }
        });
    }

    private void setupOtherButtons(){
        //give the non sorting buttons on click functions
        mainButton = (Button) findViewById(com.bqdev.darkReddit.R.id.main_button);
        mainButton.setOnClickListener(this);

        refresh = (Button) findViewById(com.bqdev.darkReddit.R.id.refresh_button);
        refresh.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mainListView.smoothScrollToPosition(0);
                String currentSub = mainAutoComplete.getText().toString();
                if(currentSub.length() != 0)
                    findSubReddit(currentSub);
                else
                    findSubReddit("all");
                return false;
            }
        });
    }

    private boolean topSortTimeFrameDecision(View view, MotionEvent motionEvent){
        sorting = "/top";

        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

            CharSequence times[] = new CharSequence[]{"today", "this week", "this month", "this year", "all time"};

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Pick a time frame");
            builder.setItems(times, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on colors[which]
                    if(which == 0){
                        urlPostfix = "/.json?limit=100&sort=top&t=day";
                    }
                    else if (which == 1){
                        urlPostfix = "/.json?limit=100&sort=top&t=week";
                    }
                    else if (which == 2){
                        urlPostfix = "/.json?limit=100&sort=top&t=month";
                    }
                    else if (which == 3){
                        urlPostfix = "/.json?limit=100&sort=top&t=year";
                    }
                    else if (which == 4){
                        urlPostfix = "/.json?limit=100&sort=top&t=all";
                    }

                    mainListView.smoothScrollToPosition(0);
                    String currentSub = mainAutoComplete.getText().toString();
                    if(currentSub.length() != 0)
                        findSubReddit(currentSub);
                    else
                        findSubReddit("all");

                    Log.w("done", "done");
                }
            });
            builder.show();

        }



        mainListView.smoothScrollToPosition(0);
        String currentSub = mainAutoComplete.getText().toString();
        if(currentSub.length() != 0)
            findSubReddit(currentSub);
        else
            findSubReddit("all");



        return false;
    }

    @Override public void onClick(View v) {

        // 9. Take what was typed into the EditText and use in search
        String search = mainAutoComplete.getText().toString();
        if(search.length() > 0) {
            findSubReddit(search);
            mainListView.setSelection(0);
        }
    }

    @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        String link =
                mJSONAdapter.getItem(position).optJSONObject("data").optString("url");

        String title =
                mJSONAdapter.getItem(position).optJSONObject("data").optString("title");

        String comments =
                mJSONAdapter.getItem(position).optJSONObject("data").optString("permalink");

        // create an Intent to take you over to a new DetailActivity
        Intent detailIntent = new Intent(this, DetailActivity.class);

        // pack away the data about the cover
        // into your Intent before you head out
        detailIntent.putExtra("comments", comments);
        detailIntent.putExtra("link", link);
        detailIntent.putExtra("title", title);

        // TODO: add any other data you'd like as Extras

        // start the next Activity using your prepared Intent
        startActivity(detailIntent);
    }

    private void findSubReddit(String searchString) {
        //collapse the keyboard if it is present, then serach for the subreddit named "searchString"
        //and display it in the listview
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
        catch (NullPointerException e){
            Log.w("no keyboard present", e);
        }


        if( !searchedSubReddits.contains(searchString)) {

            searchedSubReddits.add(searchString);
            subRedditAdapter.notifyDataSetChanged();
        }

        // Prepare your search string to be put in a URL
        // It might have reserved characters or something
        String urlString = "";
        try {
            urlString = URLEncoder.encode(searchString, "UTF-8");
        } catch (UnsupportedEncodingException e) {

            // if this fails for some reason, let the user know why
            e.printStackTrace();
            Toast.makeText(this,
                    "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }

        // Create a client to perform networking
        AsyncHttpClient client = new AsyncHttpClient();

        //start progress bar
        setProgressBarIndeterminateVisibility(true);

        // Have the client get a JSONArray of data
        // and define how to respond
        client.get(QUERY_URL + urlString + sorting + urlPostfix,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        JSONArray jsonA = null;
                        try {

                            jsonA = jsonObject.optJSONObject("data").getJSONArray("children");

                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                        // 11. stop progress bar
                        setProgressBarIndeterminateVisibility(false);

                        // update the data in your custom method.
                        mJSONAdapter.updateData(jsonA);
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Throwable throwable,
                                          JSONObject error) {

                        // 11. stop progress bar
                        setProgressBarIndeterminateVisibility(false);

                        // Display a "Toast" message
                        // to announce the failure
                        Toast.makeText(getApplicationContext(),
                                "Error: "
                                        + statusCode
                                        + " "
                                        + throwable.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();

                        // Log error message
                        // to help solve any problems
                        Log.e("omg android",
                                statusCode
                                        + " "
                                        + throwable.getMessage());
                    }
                });


    }

    public boolean saveSearchedSubReddits(Context context){

        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putInt("ArraySize", searchedSubReddits.size());


        for(int i = 0; i < searchedSubReddits.size(); i++){

            editor.remove("SubReddit" + i);
            editor.putString("SubReddit" + i, (String)searchedSubReddits.get(i));

        }

        return editor.commit();

    }

    public void loadSearchedSubReddits(Context context){

        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        //searchedSubReddits.clear();

        int size = sp.getInt("ArraySize", 0);


        for(int i = 0; i < size; i++){

            String sub = sp.getString("SubReddit" + i, null);

            if( !searchedSubReddits.contains(sub) ) {
                searchedSubReddits.add(sub);
            }
        }



    }

    private void populatePopularSubReddits(){

        ArrayList<String> popsearchedSubReddits = new ArrayList<String>();
        popsearchedSubReddits.add("all");
        popsearchedSubReddits.add("funny");
        popsearchedSubReddits.add("pics");
        popsearchedSubReddits.add("AskReddit");
        popsearchedSubReddits.add("todayilearned");
        popsearchedSubReddits.add("worldnews");
        popsearchedSubReddits.add("science");
        popsearchedSubReddits.add("blog");
        popsearchedSubReddits.add("IAmA");
        popsearchedSubReddits.add("videos");

        for(int i = 0; i < popsearchedSubReddits.size(); i++){

            String sub = popsearchedSubReddits.get(i);

            if( !searchedSubReddits.contains(sub) ){
                searchedSubReddits.add(sub);
            }


        }
        subRedditAdapter.notifyDataSetChanged();
    }

    private void highlightSearchType(int button){
        //Change the colour of the current sort type's button to let the user know
        //what they are currently sorting by [hot, new, top, rising]
        if(button == 0){
            hotSort.setBackgroundColor(Color.rgb(200,200,200));
            newSort.setBackgroundColor(Color.rgb(255,255,255));
            topSort.setBackgroundColor(Color.rgb(255,255,255));
            risingSort.setBackgroundColor(Color.rgb(255,255,255));
        }
        else if(button == 1){
            hotSort.setBackgroundColor(Color.rgb(255,255,255));
            newSort.setBackgroundColor(Color.rgb(200,200,200));
            topSort.setBackgroundColor(Color.rgb(255,255,255));
            risingSort.setBackgroundColor(Color.rgb(255,255,255));

        }
        else if(button == 2){
            hotSort.setBackgroundColor(Color.rgb(255,255,255));
            newSort.setBackgroundColor(Color.rgb(255,255,255));
            topSort.setBackgroundColor(Color.rgb(200,200,200));
            risingSort.setBackgroundColor(Color.rgb(255,255,255));

        }
        else if(button == 3){
            hotSort.setBackgroundColor(Color.rgb(255,255,255));
            newSort.setBackgroundColor(Color.rgb(255,255,255));
            topSort.setBackgroundColor(Color.rgb(255,255,255));
            risingSort.setBackgroundColor(Color.rgb(200,200,200));

        }


    }

    private void setupAd(){
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("XXXXXXXXXXX");

        LinearLayout layout = (LinearLayout)findViewById(R.id.adView2);
        layout.addView(adView);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("XXXXXXXXXXXXX")
                .build();

        adView.loadAd(adRequest);
    }
}
