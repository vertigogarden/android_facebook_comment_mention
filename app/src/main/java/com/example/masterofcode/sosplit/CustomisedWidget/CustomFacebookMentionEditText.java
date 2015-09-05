package com.example.masterofcode.sosplit.CustomisedWidget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.example.masterofcode.sosplit.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by e044983 on 28/3/15.
 */
public class CustomFacebookMentionEditText extends EditText {
    private static final String TAG = "CustomFacebook";
    private boolean mentionInProgress;
    int rbottom;
    private Context context;
    private static float y;
    private float ascend;
    private float _baseline;
    private ListView facebookFriendlist;
    private RelativeLayout facebookFriendlistLayout;
    private JSONArray friendlistArray;
    private ArrayList<JSONObject> selectedFriendlist;

    private String statusString;
    private String currentMention;

    //for mentions tracking
    private HashMap<Mention, Integer> mentionStartHash = new HashMap<Mention, Integer>();
    private HashMap<Mention, Integer> mentionEndHash = new HashMap<Mention, Integer>();

    private Set<Mention> mentionsSet = new HashSet<>();

    private static ArrayList<Mention> mentionList = new ArrayList<Mention>();
    private int mentionStart;
    private int mentionEnd;

    private float widgetY=0;

    private Listener listener;

    private AQuery aq;

    public CustomFacebookMentionEditText(Context context) {
        super(context);
        constructorNewMethods(context);
    }

    public CustomFacebookMentionEditText(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        constructorNewMethods(context);
    }

    public CustomFacebookMentionEditText(Context context, AttributeSet attrs){
        super(context, attrs);
        constructorNewMethods(context);
    }

    private void constructorNewMethods(Context context) {
        this.context = context;
        this.addTextChangedListener(textWatcher);
        this.setOnTouchListener(touchListener);
        selectedFriendlist = new ArrayList<JSONObject>();
    }

    private OnTouchListener touchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mentionFriend(false, null, 0);
            return false;
        }
    };

//    public void setWidgetY(float widgetY){this.widgetY = widgetY;}

    public void setAQuery(AQuery aq){
        this.aq = aq;
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            CharSequence charChanged =s;
//            if(s.length()>1) {
//                charChanged = s.subSequence(start + count, start + after);
//            }
//            Toast mytoast= Toast.makeText(context,
//                    "s: "+ s
//                            +"\n start:"+start
//                            +"\n count:"+count
//                            +"\n after:"+after
//                            +"\n charChanged:"+charChanged
//                    , Toast.LENGTH_SHORT);
//            // for center horizontal
//            mytoast.show();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            resetFacebookFriendlist2();
//            int pos = getSelectionStart();
//            Layout layout = getLayout();
//            int line = layout.getLineForOffset(pos);
//            int baseline = layout.getLineBaseline(line);
//            int ascent = layout.getLineAscent(line);
//            int descent = layout.getLineDescent(line);
//            float x = layout.getPrimaryHorizontal(pos);
//            ascend = ascent;
//            _baseline = baseline;
//            y = baseline + descent;
//            displayTosat();
//            displayFacebookFriendlist2();

            // If text added
            if (count > before) {

                // calculate list position
                int pos = getSelectionStart();
                Layout layout = getLayout();
                if (layout == null) return;
                int line = layout.getLineForOffset(pos);
                int baseline = layout.getLineBaseline(line);
                int ascent = layout.getLineAscent(line);
                int descent = layout.getLineDescent(line);
                float x = layout.getPrimaryHorizontal(pos);
                ascend = ascent;
                _baseline = baseline;
                y = getWidgetY();


                Character charChanged = s.charAt(start + count - 1);
//                CharSequence charChanged = s;
//                charChanged = s.subSequence(start + before, start + count);
                Character charAtHead = s.charAt(start);

                if (charAtHead.equals('@')) {
                    // begin a new mention
                    mentionInProgress = true;
                    mentionStart = start;
                    currentMention = "";
                } else if (charAtHead.equals('\r') || charAtHead.equals('\n') || charAtHead.equals(' ')) {
                    mentionInProgress = false;
                    currentMention = "";
                }
                if (mentionInProgress) {
//                    if(!charChanged.equals('@'))
                    currentMention += charChanged;
                    displayFacebookFriendlist2();
                }
                textAdded(start + before, count - before);

//                Toast mytoast = Toast.makeText(context,
//                        "s: " + s
//                                + "\n start:" + start
//                                + "\n before:" + before
//                                + "\n count:" + count
//                                + "\n charChanged:" + charChanged
//                                + "\n charAtHead:" + charAtHead
//                                + "\n currentMention:" + currentMention
//                        , Toast.LENGTH_SHORT);
//                // for center horizontal
//                mytoast.show();
            } else if( count == before){
                // ignore
            } else {
                textRemoved(start, before - count);
                if (mentionInProgress) {
//                    if(currentMention.length()>0)
                    currentMention = currentMention.substring(0, currentMention.length() - 1);
                    //                if(charChanged.equals("@")){
                    if (currentMention.length() > 0)
                        displayFacebookFriendlist2();
                    else mentionInProgress = false;
                }

//                Toast mytoast = Toast.makeText(context,
//                        "s: " + s
//                                + "\n start:" + start
//                                + "\n before:" + before
//                                + "\n count:" + count
//                                + "\n currentMention:" + currentMention
//                        , Toast.LENGTH_SHORT);
//                // for center horizontal
//                mytoast.show();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

//    @Override
//    protected void onSelectionChanged(int selStart, int selEnd) {
//        // cancel current
//        mentionFriend(false, null, 0);
//    }



    public void setFreindlist(JSONArray friendlistArray){
        this.friendlistArray = friendlistArray;
    }

//    private void displayTosat(){
//
//        final Window mRootWindow = ((Activity) context).getWindow();
//        View mRootView = mRootWindow.getDecorView().findViewById(android.R.id.content);
//        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(
//                new ViewTreeObserver.OnGlobalLayoutListener() {
//                    public void onGlobalLayout(){
//                        Rect r = new Rect();
//                        View view = mRootWindow.getDecorView();
//                        view.getWindowVisibleDisplayFrame(r);
//                        // r.left, r.top, r.right, r.bottom
//                        rbottom = r.bottom;
//                    }
//                });
//
//
//        int height = (int)(_baseline + getY());
//        Toast mytoast= Toast.makeText(context,
//                "main: "+ height
//                +"\nfocus y: "+ y
//                +"\nrbottom: " +rbottom
//                +"\nwindow height: " + mRootView.getHeight()
//                +"\ngetY: "+getY()
//                +"\nascent: " + ascend
//                +"\nbaseline: " + _baseline
//                , Toast.LENGTH_SHORT);
//        // for center horizontal
//        mytoast.setGravity(Gravity.TOP, 0, height);
//        mytoast.show();
//    }

//    private void displayFacebookFriendlist(){
//        if(facebookFriendlistLayout == null){
//
//            facebookFriendlistLayout = new RelativeLayout(context);
//
//            // Defining the RelativeLayout layout parameters.
//            // In this case I want to fill its parent
//            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.MATCH_PARENT, 300);
//
//            ((Activity)context).getWindow().addContentView(facebookFriendlistLayout, rlp);
//
//        }
//        facebookFriendlistLayout.setX(0);
//        facebookFriendlistLayout.setY(y + getY());
//        facebookFriendlistLayout.setBackgroundColor(Color.YELLOW);
//        facebookFriendlistLayout.setVisibility(VISIBLE);
////        if(facebookFriendlist==null){
////            facebookFriendlist = new ListView(context);
////
////        }
//    }

    private void displayFacebookFriendlist2(){
        if(facebookFriendlist == null){

            facebookFriendlist = new ListView(context);

            // Defining the RelativeLayout layout parameters.
            // In this case I want to fill its parent
            ListView.LayoutParams lvp = new ListView.LayoutParams(
                    ListView.LayoutParams.MATCH_PARENT, 300);

            facebookFriendlist.setPadding(40, 0, 40, 0);
//            facebookFriendlist.setBackgroundColor(Color.parseColor("#aaffffff"));
            ((Activity)context).getWindow().addContentView(facebookFriendlist, lvp);

        }
        final FacebookFriendlistAdapter friendAdapter = new FacebookFriendlistAdapter(context, getFilteredList());
        facebookFriendlist.setX(0);
        facebookFriendlist.setY(y);
        facebookFriendlist.setVisibility(VISIBLE);
        facebookFriendlist.setAdapter(friendAdapter);
        facebookFriendlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(getSelectionStart() < 0) return;
                try{
                    JSONObject friendObject = friendAdapter.getList().getJSONObject(position);
                    String displayName = friendObject.getString("name");
                    getText().replace(getSelectionStart()-currentMention.length() +1,
                            getSelectionStart(), "", 0,0);
                    getText().insert(getSelectionStart(), displayName);
                    mentionFriend(true, friendObject, displayName.length()+mentionStart);

                }catch (JSONException e){
                    Log.e(TAG, e.toString());
                    Toast.makeText(context, "error display name", Toast.LENGTH_SHORT).show();
                }

            }
        });
//        expand();
//        if(facebookFriendlist==null){
//            facebookFriendlist = new ListView(context);
//
//        }
    }


    private void mentionFriend(Boolean success, JSONObject friendObject, int mentionEnd){
        mentionInProgress = false;
        currentMention = "";
        resetFacebookFriendlist2();

        if(success){
            Mention mention = new Mention(friendObject, mentionStart, mentionEnd);
            mentionList.add(mention);
//            listener.customFacebookMentionTextViewMentionAdded(mention);
            if(listener!=null)
                listener.customFacebookMentionTextViewMentionChanged(mentionList);
        }
    }

//    private void resetFacebookFriendlist(){
//        if(facebookFriendlistLayout == null) return;
//        facebookFriendlist.setVisibility(GONE);
//    }

    private void resetFacebookFriendlist2(){
        if(facebookFriendlist == null) return;
        facebookFriendlist.setVisibility(INVISIBLE);
//        collapse();
    }

    public String getStatusString(){
        statusString = getText().toString();

        if(mentionList.size()==0) return statusString;

        Collections.sort(mentionList, new Comparator<Mention>() {

            public int compare(Mention o1, Mention o2) {
                return o1.mentionEnd - o2.mentionEnd;
            }
        });

        String statusPostString = "";
        int nextSplitIndex = 0;
//        ArrayList<String> stringQueue = new <String>();
        for(int i=0; i<mentionList.size(); i++){
            Mention mention = mentionList.get(i);

            Log.d(TAG, "mention " + mention.mentionStart + " " +mention.mentionEnd + " " +nextSplitIndex);

            statusPostString += getSubString(statusString, nextSplitIndex, mention.mentionStart);
            statusPostString += "@["+mention.id+"]";
            // if las mention, get remaining strings
            if(i == mentionList.size()-1){
                statusPostString += getSubString(statusString, mention.mentionEnd +1, statusString.length());
            }else{
                nextSplitIndex = mention.mentionEnd+1;
            }
        }
        return  statusPostString;
    }

    /**
     * To delete
     */
    public String getMOCKStatusString(){
        statusString = getText().toString();

        if(mentionList.size()==0) return statusString;

        Collections.sort(mentionList, new Comparator<Mention>() {

            public int compare(Mention o1, Mention o2) {
                return o1.mentionEnd - o2.mentionEnd;
            }
        });

        String statusPostString = "";
        int nextSplitIndex = 0;
//        ArrayList<String> stringQueue = new <String>();
        for(int i=0; i<mentionList.size(); i++){
            Mention mention = mentionList.get(i);

            Log.d(TAG, "mention " + mention.mentionStart + " " +mention.mentionEnd + " " +nextSplitIndex);

            statusPostString += getSubString(statusString, nextSplitIndex, mention.mentionStart);
            statusPostString += "@["+mention.name+"]";
            // if las mention, get remaining strings
            if(i == mentionList.size()-1){
                statusPostString += getSubString(statusString, mention.mentionEnd +1, statusString.length());
            }else{
                nextSplitIndex = mention.mentionEnd+1;
            }
        }
        return  statusPostString+"||";
    }

    private String getSubString(String statusString, int nextSplitIndex, int mentionStart) {
        if(nextSplitIndex==mentionStart) return "";
        else return statusString.substring(nextSplitIndex, mentionStart);
    }

    private void textAdded(int positionAdded, int count){
        List<Integer> removeMentionList = new ArrayList<Integer>();
        for(int i=0; i<mentionList.size(); i++){
            Mention mention = mentionList.get(i);
            if(positionAdded>mention.mentionStart && positionAdded<= mention.mentionEnd){
                removeMentionList.add(i);
            }else {
                if (mention.mentionStart >= positionAdded) mention.mentionStart+=count;
                if (mention.mentionEnd >= positionAdded) mention.mentionEnd+=count;
            }
        }

        // remove mentions with interupted mentions
        if(mentionList.size()>0) mentionsChanged(removeMentionList);
    }

    private void textRemoved(int positionRemoved, int count){
        List<Integer> removeMentionList = new ArrayList<Integer>();
        for(int i=0; i<mentionList.size(); i++){
            Mention mention = mentionList.get(i);
            if(positionRemoved>mention.mentionStart && positionRemoved<= mention.mentionEnd){
                removeMentionList.add(i);
            }else {
                if (mention.mentionStart >= positionRemoved) mention.mentionStart-=count;
                if (mention.mentionEnd >= positionRemoved) mention.mentionEnd-=count;
            }
        }

        // remove mentions with interupted mentions
        if(mentionList.size()>0) mentionsChanged(removeMentionList);
    }

    public void mentionsChanged(List<Integer> removeMentionList){
        for(int i:removeMentionList){
            mentionList.remove(i);
        }
        if(listener!=null)
            listener.customFacebookMentionTextViewMentionChanged(mentionList);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public JSONArray getFilteredList() {
//        return friendlistArray;
        if(currentMention==null || currentMention.length()<=1) return friendlistArray;
        else{
            String tempCurrentMention = currentMention.substring(1, currentMention.length());
            JSONArray jArr = new JSONArray();
            for(int i=0; i<friendlistArray.length(); i++){
                JSONObject jo = friendlistArray.optJSONObject(i);
                if(jo!=null && jo.optString("name","name").toLowerCase().contains(tempCurrentMention.toLowerCase()))
                    jArr.put(jo);
            }
            return jArr;
        }
    }

    public float getWidgetY() {
        int[] location = new int[2];
        getLocationInWindow(location);
        return location[1];
    }


    /**********
     * CLASSSES
     ***********/
    private class FacebookFriendlistAdapter extends ArrayAdapter<JSONObject> {
        JSONArray friendlistJsonArray;
        public FacebookFriendlistAdapter(Context context, JSONArray friendlistJsonArray) {
            super(context, android.R.layout.simple_list_item_1);
            this.friendlistJsonArray = friendlistJsonArray;
        }

        @Override
        public int getCount() {
            if(friendlistArray==null) return 0;
            return friendlistJsonArray.length();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            LayoutInflater inflater = (LayoutInflater) context
//                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            View FriendlistRow = inflater.inflate(R.layout.friendlist_row, parent, false);
            String name = "-";
            String imageURL = "";
            try{
                name = friendlistJsonArray.getJSONObject(position).getString("name");
                imageURL = friendlistJsonArray.getJSONObject(position).getJSONObject("picture").getJSONObject("data").getString("url");
            }catch (JSONException e){

            }

            LinearLayout ll = new LinearLayout(context);
//            ll.setOrientation(LinearLayout.HORIZONTAL);
//            ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 50));

            ll.setBackgroundColor(Color.parseColor("#ffffff"));

            ImageView imageView = new ImageView(context);
            //setting image resource
            imageView.setImageResource(R.drawable.ic_launcher);

            ImageOptions options = new ImageOptions();
//            options.round = 50;
            options.memCache = false;
            options.fileCache = false;
            aq.id(imageView).image(imageURL, options);
            //setting image position
            imageView.setLayoutParams(new ViewGroup.LayoutParams(100, 100));

            ll.addView(imageView);


            LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100);
            TextView tv1 = new TextView(context);
            tv1.setText(name);
            tv1.setLayoutParams(pp);
            tv1.setPadding(15,0,0,0);
            ll.addView(tv1);



//            TextView tv2 = new TextView(context);
//            tv2.setText("WORLD");
//            ll.addView(tv2);

            return ll;
//            return FriendlistRow;
        }

        public JSONArray getList(){return friendlistJsonArray;}
    }

    public class Mention {
        String id;
        String name;
        String photoURL;
        double amount;
        int mentionStart;
        int mentionEnd;
        public Mention(JSONObject friendObject, int mentionStart, int mentionEnd){

            this.id = friendObject.optString("id", "");
            this.name = friendObject.optString("name", "");
            this.photoURL = friendObject.optJSONObject("picture").optJSONObject("data").optString("url");
            this.mentionStart=mentionStart;
            this.mentionEnd=mentionEnd;
            this.amount = 0;
        }

        public String getId(){return id;}
        public String getName(){return name;}
        public String getPhotoURL(){return photoURL;}
        public double getAmount(){ return amount;}//return amount;}
        public void setAmount(double amount){this.amount=amount;}
    }

    public interface Listener {
        public void customFacebookMentionTextViewMentionChanged(List<Mention> mentions);
        public void customFacebookMentionTextViewMentionAdded(Mention mention);
    }
}

