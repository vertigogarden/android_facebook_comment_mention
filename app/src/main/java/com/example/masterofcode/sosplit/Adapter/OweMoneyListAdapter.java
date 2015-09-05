package com.example.masterofcode.sosplit.Adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.example.masterofcode.sosplit.CustomisedWidget.CustomFacebookMentionEditText;
import com.example.masterofcode.sosplit.R;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;

/**
 * Created by e044983 on 28/3/15.
 */
public class OweMoneyListAdapter extends BaseAdapter {
    private static final String TAG = "OweMoneyListAdapter";
    List<CustomFacebookMentionEditText.Mention> mentions;
    Context context;
    AQuery aq;

    public OweMoneyListAdapter(Context context, List<CustomFacebookMentionEditText.Mention> mentions, AQuery aq) {
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
        convertView = inflater.inflate(R.layout.owe_money_list_item, parent, false);
//        }
        TextView name = (TextView) convertView.findViewById(R.id.textview_name);
        final CustomFacebookMentionEditText.Mention mention = mentions.get(position);
        final EditText amount = (EditText) convertView.findViewById(R.id.edittext_amount);
        ImageView imageViewProfilePic = (ImageView) convertView.findViewById(R.id.imageview_profilepic);
        ImageOptions options = new ImageOptions();
//        options.round = 50;
        options.memCache = false;
        options.fileCache = false;
        aq.id(imageViewProfilePic).image(mention.getPhotoURL(),options);

        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            private String current = "";
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(current)){
                    amount.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[$,£,.]", "");

//                    String.format( "$%.2f", parsed/100 );
                    double parsed = Double.parseDouble(cleanString);
                    String formatted = String.format( "$%.2f", parsed/100 );

//                    String formatted = NumberFormat.getCurrencyInstance().format((parsed/100));
                    mention.setAmount(parsed/100);
                    current = formatted;
                    amount.setText(formatted);
                    amount.setSelection(formatted.length());

                    amount.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        amount.setText(mention.getAmount()+"");
        name.setText(mention.getName());
        return convertView;
    }
}

