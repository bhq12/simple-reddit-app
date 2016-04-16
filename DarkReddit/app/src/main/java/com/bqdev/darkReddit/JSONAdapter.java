package com.bqdev.darkReddit;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONAdapter extends BaseAdapter {



    Context mContext;
    LayoutInflater mInflater;
    JSONArray mJsonArray;

    public JSONAdapter(Context context,
                       LayoutInflater inflater) {

        mContext = context;
        mInflater = inflater;
        mJsonArray = new JSONArray();
    }

    public void updateData(JSONArray jsonArray) {

        // update the adapter's dataset
        mJsonArray = jsonArray;

        notifyDataSetChanged();
    }

    @Override public int getCount() {
        return mJsonArray.length();
    }

    @Override public JSONObject getItem(int position) {

        return mJsonArray.optJSONObject(position);
    }

    @Override public long getItemId(int position) {

        // your particular dataset uses String IDs
        // but you have to put something in this method
        return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        // check if the view already exists
        // if so, no need to inflate and findViewById again!
        if (convertView == null) {

            // Inflate the custom row layout from your XML.
            convertView = mInflater.inflate(R.layout.list_item_card, null);

            // create a new "Holder" with subviews
            holder = new ViewHolder();
            holder.thumbnailImageView =
                    (ImageView) convertView
                            .findViewById(R.id.img_thumbnail);
            holder.titleTextView =
                    (TextView) convertView
                            .findViewById(R.id.text_title);

            holder.subredditTextView =
                    (TextView)convertView.findViewById(R.id.subreddit_name);


            // hang onto this holder for future recyclage
            convertView.setTag(holder);
        } else {

            // skip all the expensive inflation/findViewById
            // and just get the holder you already made
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the current book's data in JSON form
        JSONObject jsonObject = getItem(position);
        jsonObject = jsonObject.optJSONObject("data");

        // See if there is a cover ID in the Object
        if (jsonObject.has("thumbnail") &&
            jsonObject.has("over_18")  &&
            jsonObject.optBoolean("over_18") == false) {



            // If so, grab the Cover ID out from the object
            String imageURL = jsonObject.optString("thumbnail");


            // Use Picasso to load the image
            // Temporarily have a placeholder in case it's slow to load
            if(imageURL.startsWith("http")){

            Picasso.with(mContext)
                    .load(imageURL)
                    .placeholder(R.drawable.reddit_icon)
                    .into(holder.thumbnailImageView);


            }
            else {
                holder.thumbnailImageView
                        .setImageResource(R.drawable.reddit_icon);
            }

        }

        else if(jsonObject.has("over_18") && jsonObject.optBoolean("over_18") == true){

            holder.thumbnailImageView.setImageResource(R.drawable.nsfw_icon);
        }

        else {

            // If there is no cover ID in the object, use a placeholder
            holder.thumbnailImageView
                    .setImageResource(R.drawable.reddit_icon);
        }




        // Grab the title and author from the JSON
        String bookTitle = "";
        String authorName = "";
        String subreddit = "";

        if (jsonObject.has("title")) {
            bookTitle = jsonObject.optString("title");
        }

//        if (jsonObject.has("author")) {
//            authorName = jsonObject.optString("author");
//        }
        if(jsonObject.has("subreddit")){
            subreddit = jsonObject.optString("subreddit");
        }
        Log.w("sub", subreddit);

        // Send these Strings to the TextViews for display
        holder.titleTextView.setText(bookTitle);
        //holder.authorTextView.setText(authorName);
        holder.subredditTextView.setText(subreddit);

        return convertView;
    }

    // this is used so you only ever have to do
    // inflation and finding by ID once ever per View
    private static class ViewHolder {
        public ImageView thumbnailImageView;
        public TextView titleTextView;
        //public TextView authorTextView;
        public TextView subredditTextView;
    }
}

