package com.example.masterofcode.sosplit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.example.masterofcode.sosplit.Adapter.MentionsListAdapter;
import com.example.masterofcode.sosplit.CustomisedWidget.CustomFacebookMentionEditText;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class StatusMentionFragment extends Fragment implements CustomFacebookMentionEditText.Listener {
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

    public static StatusMentionFragment newInstance(String param1, String param2) {
        StatusMentionFragment fragment = new StatusMentionFragment();
        return fragment;
    }

    public StatusMentionFragment() {
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
        Resources r = getResources();
        mentionTextview.setAQuery(aq);

        shareButton = (Button) view.findViewById(R.id.share_open_graph);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MOCKSTATUS", mentionTextview.getStatusString());
                showStatusDialog(v);
            }
        });

        mentionNameListview = (ListView) view.findViewById((R.id.listview_mentioned));

        if (savedInstanceState != null) {
            pendingPublishReauthorization =
                    savedInstanceState.getBoolean(PENDING_PUBLISH_KEY, false);
        }

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
        mentionNameListview.setAdapter(new MentionsListAdapter(getActivity(), mentions, aq));
    }

    @Override
    public void customFacebookMentionTextViewMentionAdded(CustomFacebookMentionEditText.Mention mention) {
    }

    private void showStatusDialog(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setTitle("Status");
        alertDialogBuilder.setMessage(mentionTextview.getMOCKStatusString() + "\n\n" + mentionTextview.getStatusString());

        // set neutral button: Exit the app message
        alertDialogBuilder.setNeutralButton("close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // exit the app and go to the HOME
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        // show alert
        alertDialog.show();
    }
}
