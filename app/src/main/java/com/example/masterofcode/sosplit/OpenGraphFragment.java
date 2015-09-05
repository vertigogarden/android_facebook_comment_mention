package com.example.masterofcode.sosplit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.example.masterofcode.sosplit.Adapter.OweMoneyListAdapter;
import com.example.masterofcode.sosplit.CustomisedWidget.CustomFacebookMentionEditText;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class OpenGraphFragment extends Fragment implements CustomFacebookMentionEditText.Listener {
    private static final String TAG = "OpenGraphFragment";

    private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";

    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");

    private CustomFacebookMentionEditText mentionTextview;

    private ListView mentionNameListview;

    private boolean pendingPublishReauthorization = false;

    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session,
                         final SessionState state,
                         final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private Button shareButton;

    private GraphUser graphUser;

    private AQuery aq;

    public static OpenGraphFragment newInstance(String param1, String param2) {
        OpenGraphFragment fragment = new OpenGraphFragment();
        return fragment;
    }

    public OpenGraphFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
        aq = new AQuery(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_open_graph, container, false);
        mentionTextview = (CustomFacebookMentionEditText) view.findViewById(R.id.mentionTextview);
        mentionTextview.setListener(this);
        LinearLayout ll = (LinearLayout) view.findViewById(R.id.header);
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, r.getDisplayMetrics());
//        mentionTextview.setWidgetY(getRelativeTop(mentionTextview));
//        int[] location = new int[2];
//        mentionTextview.getLocationInWindow(location);
//        mentionTextview.setWidgetY(location[1]);
        mentionTextview.setAQuery(aq);

        shareButton = (Button) view.findViewById(R.id.share_open_graph);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MOCKSTATUS", mentionTextview.getStatusString());
//                Log.d(TAG, "widgetY check:" + getRelativeTop(mentionTextview));
//                try {
//                    postToServer();
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }
        });

        mentionNameListview = (ListView) view.findViewById((R.id.listview_mentioned));

        if (savedInstanceState != null) {
            pendingPublishReauthorization =
                    savedInstanceState.getBoolean(PENDING_PUBLISH_KEY, false);
        }

        ImageView balloon = (ImageView) view.findViewById(R.id.imageview_balloon);
        TranslateAnimation mAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.2f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f);
        mAnimation.setDuration(8000);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        balloon.setAnimation(mAnimation);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiHelper.onResume();
//
//        mentionTextview.setWidgetY(mentionTextview.getY());
//        Log.d(TAG, "widgetY:" + mentionTextview.getY());
    }

    private int getRelativeTop(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) myView.getParent());
    }

    private void getAllFriends(){
        Session session = Session.getActiveSession();
        if (session != null) {
//            Toast.makeText(getActivity()
//                            .getApplicationContext(),
//                    "session not null",
//                    Toast.LENGTH_LONG).show();

            // Check for publish permissions
            List<String> permissions = session.getPermissions();
            if (!isSubsetOf(PERMISSIONS, permissions)) {
                pendingPublishReauthorization = true;
                Session.NewPermissionsRequest newPermissionsRequest = new Session
                        .NewPermissionsRequest(this, PERMISSIONS);
                session.requestNewPublishPermissions(newPermissionsRequest);
            }
            new Request(
                    session,
                    "/me/taggable_friends",
                    null,
                    HttpMethod.GET,
                    new Request.Callback() {
                        public void onCompleted(Response response) {

                            Log.d(TAG, response.toString());
                            try {
                                JSONArray flArray = response.getGraphObject().getInnerJSONObject().getJSONArray("data");
                                Log.d(TAG, flArray.length() + "");
                                mentionTextview.setFreindlist(flArray);
                                mentionTextview.setVisibility(View.VISIBLE);
                            }catch (JSONException e){
                                Log.e(TAG, e.toString());
                            }
//                            JSONArray = (new JSONObject(response.getGraphObject())).get("data");
                        }
                    }
            ).executeAsync();
        }
    }

    private void makeMeRequest(final Session session) {
        // Make an API call to get user data and define a
        // new callback to handle the response.
        if(graphUser!= null) return;
        Request request = Request.newMeRequest(session,
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        // If the response is successful
                        if (session == Session.getActiveSession()) {
                            if (user != null) {
                                // Set the id for the ProfilePictureView
                                // view that in turn displays the profile picture.
//                                profilePictureView.setProfileId(user.getId());
                                // Set the Textview's text to the user's name.
                                graphUser = user;
                                Log.d(TAG, user.getInnerJSONObject().toString());
                                Toast.makeText(getActivity(), "Profile Loaded", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (response.getError() != null) {
                            // Handle errors, will do so later.
                        }
                    }
                });
        request.executeAsync();
    }

    /*
     * Helper method to check a collection for a string.
     */
    private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
        for (String string : subset) {
            if (!superset.contains(string)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
        uiHelper.onSaveInstanceState(outState);
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            shareButton.setVisibility(View.VISIBLE);
            if (pendingPublishReauthorization &&
                    state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
                pendingPublishReauthorization = false;
//                publishStory();
            }
            getAllFriends();
            makeMeRequest(session);
        } else if (state.isClosed()) {
            shareButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void customFacebookMentionTextViewMentionChanged(List<CustomFacebookMentionEditText.Mention> mentions) {

        mentionNameListview.setAdapter(new OweMoneyListAdapter(getActivity(), mentions, aq));
    }

    @Override
    public void customFacebookMentionTextViewMentionAdded(CustomFacebookMentionEditText.Mention mention) {
//        addRow(mention);
    }

    public void postToServer() throws UnsupportedEncodingException, JSONException {

        final HttpClient client = new DefaultHttpClient();
        final HttpPost post = new HttpPost("http://sosplit.herokuapp.com/transfers");
        post.setHeader("Content-Type", "application/json");
//        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
//        pairs.add(new BasicNameValuePair("key1", "value1"));
//        pairs.add(new BasicNameValuePair("key2", "value2"));
//        post.setEntity(new UrlEncodedFormEntity(pairs));

        JSONObject jsonobj = new JSONObject();

        Session session = Session.getActiveSession();
        List<String> permissions = session.getPermissions();
        if (!isSubsetOf(PERMISSIONS, permissions)) {
            pendingPublishReauthorization = true;
            Session.NewPermissionsRequest newPermissionsRequest = new Session
                    .NewPermissionsRequest(this, PERMISSIONS);
            session.requestNewPublishPermissions(newPermissionsRequest);
        }
        JSONObject postObj = new JSONObject().put("accessToken", session.getAccessToken());

        JSONObject receiver = new JSONObject();
        receiver.put("id", graphUser.getId());
        receiver.put("photoUrl", "http://commons.wikimedia.org/wiki/Example_images#/media/File:Example.svg");
        receiver.put("display", graphUser.getFirstName());

        jsonobj.put("post", postObj);
        jsonobj.put("receiver", receiver);
        jsonobj.put("requests", getJSONMentions(((OweMoneyListAdapter)mentionNameListview.getAdapter()).getMentions()));
//        jsonobj.put("requests", getJSONMentions(((OweMoneyListAdapter)mListView.getAdapter()).getMentions()));
        jsonobj.put("message", mentionTextview.getStatusString());
        StringEntity se = new StringEntity(jsonobj.toString());
        post.setEntity(se);



        final Handler handler = new Handler();

        Thread thread = new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            final HttpResponse respones = client.execute(post);
                            Log.d(TAG, "HTTP RESPONSE:" +respones.toString());
                            String responseText = null;
                            try {
                                responseText = EntityUtils.toString(respones.getEntity());

                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (respones.getStatusLine().getStatusCode() == 200) {
                                            Toast.makeText(getActivity(), "Status Posted!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), "Please share again.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

//                                Log.d(TAG, "HTTP RESPONSE TEXT:" +responseText);
//                                Log.d(TAG, "HTTP RESPONSE TEXT:" +respones.getStatusLine().getStatusCode());
                                }catch (ParseException e) {
                                e.printStackTrace();
                                Log.i("Parse Exception", e + "");

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        thread.start();
    }

    private JSONArray getJSONMentions(List<CustomFacebookMentionEditText.Mention> mentions) {
        JSONArray mentionArray = new JSONArray();
        for(CustomFacebookMentionEditText.Mention mention: mentions){
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", mention.getId());
                jsonObject.put("photoUrl", mention.getPhotoURL());
                jsonObject.put("display", mention.getName());
                jsonObject.put("amount", mention.getAmount());
                mentionArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mentionArray;
    }

}
