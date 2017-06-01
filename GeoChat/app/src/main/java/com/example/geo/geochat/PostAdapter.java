package com.example.geo.geochat;

/**
 * Created by gustavo on 5/16/17.
 */
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;


public class PostAdapter extends ArrayAdapter<PhotoPost>{
    private static String TAG = "LOG_CAT";

    public PostAdapter(Context context, int resource, List<PhotoPost> objects) {
        super(context, resource, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.feed_post, parent, false);
        }

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.photoImageView);
        TextView textView = (TextView) convertView.findViewById(R.id.messageTextView);

        PhotoPost post = getItem(getCount()-(position+1));

        boolean isPhoto = post.getPhotoURL()!=null;
        if(isPhoto){
            textView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
            //handle setting the image, need to download glide
            Glide.with(photoImageView.getContext())
                    .load(post.getPhotoURL())
                    .into(photoImageView);
        } else{
            String lom = post.getText();
            Log.i(TAG,"its haaappening " + lom);
            textView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            textView.setText(post.getText());
        }
        return convertView;
    }
}
