package com.example.masterofcode.facebookmentionedittext.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.example.masterofcode.facebookmentionedittext.CustomisedWidget.CustomFacebookMentionEditText;
import com.example.masterofcode.facebookmentionedittext.R;

import java.util.List;

/**
 * Created by e044983 on 28/3/15.
 */
public class MentionsListAdapter extends BaseAdapter {
    private static final String TAG = "OweMoneyListAdapter";
    List<CustomFacebookMentionEditText.Mention> mentions;
    Context context;
    AQuery aq;

    public MentionsListAdapter(Context context, List<CustomFacebookMentionEditText.Mention> mentions, AQuery aq) {
        this.context = context;
        this.mentions=mentions;
        this.aq = aq;
    }

    public void setMentions(List<CustomFacebookMentionEditText.Mention> mentions){
        this.mentions = mentions;
    }

    public List<CustomFacebookMentionEditText.Mention> getMentions(){

        return mentions;
    }

    @Override
    public int getCount() {
        if(mentions == null) return 0;
        return mentions.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
//        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.mentions_list_item, parent, false);
//        }
        TextView name = (TextView) convertView.findViewById(R.id.textview_name);
        final CustomFacebookMentionEditText.Mention mention = mentions.get(position);
        ImageView imageViewProfilePic = (ImageView) convertView.findViewById(R.id.imageview_profilepic);
        ImageOptions options = new ImageOptions();
//        options.round = 50;
        options.memCache = false;
        options.fileCache = false;
        aq.id(imageViewProfilePic).image(mention.getPhotoURL(),options);

        name.setText(mention.getName());
        return convertView;
    }
}

