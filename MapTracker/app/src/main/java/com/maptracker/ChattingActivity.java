package com.maptracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.models.CommentListModel;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerGrantResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.utils.PreferencesKeys;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class ChattingActivity extends AppCompatActivity {


    @BindView(R.id.ivSend)
    ImageView ivSend;
    @BindView(R.id.etMsg)
    EditText etMsg;

    //NestedScrollView nsvComment;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;


    // =========================================================================
    // Properties
    // =========================================================================

    private static final String TAG = "=ChattingActivity=";

    private boolean isFirstMessage = true;
    private boolean mRequestingLocationUpdates = false;
    private MenuItem mFollowButton;

    // PubNub
    private PubNub mPubNub;
    private String channelName = "channel-chat";


    NotificationAdapter notificationAdapter;
    public ArrayList<CommentListModel> arrayListAllCommentListModel = new ArrayList<>();

    String strMessage = "";
    String strName = "";

    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            App.showLog("=======onCreate===");
            setContentView(R.layout.activity_chatting);
            ButterKnife.bind(this);


            RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
            // Clear the realm from last time
            //Realm.deleteRealm(realmConfiguration);
            realm = Realm.getInstance(realmConfiguration);


            // Get Channel Name
            Intent intent = getIntent();
        /*channelName = intent.getExtras().getString("channel");
        Log.d(TAG, "Passed Channel Name: " + channelName);*/

            if (App.sharePrefrences.getStringPref(PreferencesKeys.strUserMobileNo) != null && App.sharePrefrences.getStringPref(PreferencesKeys.strUserMobileNo).length() > 1) {
                channelName = App.sharePrefrences.getStringPref(PreferencesKeys.strUserMobileNo);


                Log.d(TAG, "Passed Channel Name--sharePrefrences--: " + channelName);

                channelName = channelName + "_chat";
                Log.d(TAG, "Passed Channel Name--sharePrefrences--: " + channelName);
            }

            ivSend = (ImageView) findViewById(R.id.ivSend);
            etMsg = (EditText) findViewById(R.id.etMsg);
            //nsvComment = (NestedScrollView) findViewById(R.id.nsvComment);
            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

            setCommentData();

            ivSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Broadcast information on PubNub Channel
                    strMessage = etMsg.getText().toString().trim();

                    if (mPubNub != null && strMessage.length() > 0) {
                        PubNubManager.broadcastLocationChat(mPubNub, channelName, strMessage);
                        etMsg.setText("");
                    }
                }
            });


            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // auto start view
                    Log.d(TAG, "'Follow Friend's Location' Button Pressed");
                    mRequestingLocationUpdates = !mRequestingLocationUpdates;
                    if (mRequestingLocationUpdates) {
                        startFollowingLocation();
                        mFollowButton.setTitle("Stop Viewing Your Friend's Location");
                    }
                }
            }, 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        App.showLog("=======onCreateOptionsMenu===");
        getMenuInflater().inflate(R.menu.follow, menu);
        mFollowButton = menu.findItem(R.id.follow_locations);
        return true;
    }


    // =========================================================================
    // Button CallBacks
    // =========================================================================

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        App.showLog("=======onOptionsItemSelected===");
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.follow_locations:
                Log.d(TAG, "'Follow Friend's Location' Button Pressed");
                mRequestingLocationUpdates = !mRequestingLocationUpdates;
                if (mRequestingLocationUpdates) {
                    startFollowingLocation();
                    mFollowButton.setTitle("Stop Viewing Your Friend's Location");
                }
                if (!mRequestingLocationUpdates) {
                    stopFollowingLocation();
                    mFollowButton.setTitle("Start Viewing Your Friend's Location");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void startFollowingLocation() {
        App.showLog("=======startFollowingLocation===");
        // Start PubNub
        mPubNub = PubNubManager.startPubnub();
        mPubNub.grant()
                .channels(Arrays.asList(channelName, "channel-chatall"))
                //.authKeys(Arrays.asList("Authkey-555", "Authkey-666"))
                .manage(true)
                .read(true)
                .write(true)
                .async(new PNCallback<PNAccessManagerGrantResult>() {
                    @Override
                    public void onResponse(PNAccessManagerGrantResult result, PNStatus status) {
                        if (status.isError()) {
                            System.out.println("grant failed!");
                        } else {
                            System.out.println("Grant passed!");
                            System.out.println(result);
                        }
                    }
                });


        mPubNub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                Log.e("====", "====status====status========" + status);
                if (status.getOperation() != null) {
                    switch (status.getOperation()) {
                        // let's combine unsubscribe and subscribe handling for ease of use
                        case PNSubscribeOperation:
                        case PNUnsubscribeOperation:
                            // note: subscribe statuses never have traditional
                            // errors, they just have categories to represent the
                            // different issues or successes that occur as part of subscribe
                            switch (status.getCategory()) {
                                case PNConnectedCategory:
                                    // this is expected for a subscribe, this means there is no error or issue whatsoever
                                case PNReconnectedCategory:
                                    // this usually occurs if subscribe temporarily fails but reconnects. This means
                                    // there was an error but there is no longer any issue
                                case PNDisconnectedCategory:
                                    // this is the expected category for an unsubscribe. This means there
                                    // was no error in unsubscribing from everything
                                case PNUnexpectedDisconnectCategory:
                                    // this is usually an issue with the internet connection, this is an error, handle appropriately
                                case PNAccessDeniedCategory:
                                    // this means that PAM does allow this client to subscribe to this
                                    // channel and channel group configuration. This is another explicit error
                                default:
                                    // More errors can be directly specified by creating explicit cases for other
                                    // error categories of `PNStatusCategory` such as `PNTimeoutCategory` or `PNMalformedFilterExpressionCategory` or `PNDecryptionErrorCategory`
                            }

                        case PNHeartbeatOperation:
                            // heartbeat operations can in fact have errors, so it is important to check first for an error.
                            // For more information on how to configure heartbeat notifications through the status
                            // PNObjectEventListener callback, consult <link to the PNCONFIGURATION heartbeart config>
                            if (status.isError()) {
                                // There was an error with the heartbeat operation, handle here
                                Log.e("====", "====status====isError Yes========" + status);
                            } else {
                                // heartbeat operation was successful
                                Log.e("====", "====status====isError No========" + status);
                            }
                        default: {
                            // Encountered unknown status type
                        }
                    }
                } else {
                    // After a reconnection see status.getCategory()
                }
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                Log.e("====", "====message======");
                try {
                    Log.e("====", "====message======" + message);
                    Log.e("====", "====message======" + message.getMessage());
                    Log.e("====", "====message======" + message.getChannel());
                    Log.e("====", "====message======" + message.getPublisher());


                    JSONObject mainObject = null;

                    mainObject = new JSONObject(message.getMessage().toString());


                    // {"message":"hello how are you ?","id":"1","alt":"0.0","name":"Josh","data":"add any data","lng":"72.501697","usertype":"1","lat":"23.0019311"}


                    if (mainObject.has("message")) {
                        strMessage = mainObject.getString("message");
                        Log.e("====", "==message==strMessage ==" + strMessage);

                    }
                    if (mainObject.has("name")) {
                        strName = mainObject.getString("name");
                        Log.e("====", "==name==strName ==" + strName);

                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (strMessage.length() > 1 && strName.length() > 1) {
                                CommentListModel commentListModel = new CommentListModel(strMessage, strName, channelName);
                                setupDataInsert(commentListModel);
                                arrayListAllCommentListModel.add(commentListModel);

                                if (notificationAdapter != null) {
                                    notificationAdapter.notifyDataSetChanged();
                                    recyclerView.smoothScrollToPosition(notificationAdapter.getItemCount());
                                }
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                Log.e("====", "====presence======");
            }
        });
        mPubNub.subscribe()
                .channels(Arrays.asList(channelName)) // subscribe to channels
                .execute();


    }


    private void setCommentData() {
        App.showLog("=======setCommentData===");

        arrayListAllCommentListModel = getAllRecords();


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChattingActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);

        notificationAdapter = new NotificationAdapter(ChattingActivity.this, arrayListAllCommentListModel);
        recyclerView.setAdapter(notificationAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        //nsvComment.getParent().requestChildFocus(nsvComment, nsvComment);

    }


    private void setupDataInsert(CommentListModel commentListModel) {
        try {

            ArrayList<CommentListModel> arrayListDLocationModel = new ArrayList<>();
            arrayListDLocationModel.add(commentListModel);

            if (arrayListDLocationModel != null && arrayListDLocationModel.size() > 0) {


                realm.beginTransaction();
                Collection<CommentListModel> realmDLocationModel = realm.copyToRealm(arrayListDLocationModel);
                realm.commitTransaction();


                ArrayList<CommentListModel> arrTemp = new ArrayList<CommentListModel>(realmDLocationModel);
                for (int i = 0; i < arrTemp.size(); i++) {
                    App.showLog(i + "===data -- ==" + arrTemp.get(i).strMessage);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private ArrayList<CommentListModel> getAllRecords() {
        try {

            RealmResults<CommentListModel> arrCommentListModel = realm.where(CommentListModel.class).findAll();

            App.showLog("===arrCommentListModel==" + arrCommentListModel);

            List<CommentListModel> arraCommentListModel = arrCommentListModel;

            for (int k = 0; k < arraCommentListModel.size(); k++) {
                App.showLog(k + "===arraCommentListModel===strMessage==" + arraCommentListModel.get(k).strMessage);
            }


            return new ArrayList<CommentListModel>(arraCommentListModel);

/*
            RealmQuery<CommentListModel> query = realm.where(CommentListModel.class);
            *//*
            for (String id : ids) {
                query.or().equalTo("myField", id);
            }*//*

            RealmResults<CommentListModel> results = query.findAll();
            App.showLog("===results=="+results);

            */
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VersionViewHolder> {
        ArrayList<CommentListModel> mArrListCommentListModel;
        Context mContext;


        public NotificationAdapter(Context context, ArrayList<CommentListModel> arrayListFollowers) {
            mArrListCommentListModel = arrayListFollowers;
            mContext = context;
        }

        @Override
        public VersionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.raw_comments, viewGroup, false);
            VersionViewHolder viewHolder = new VersionViewHolder(view);
            return viewHolder;
        }


        @Override
        public void onBindViewHolder(final VersionViewHolder versionViewHolder, final int i) {
            try {
                CommentListModel commentListModel = mArrListCommentListModel.get(i);


                if (commentListModel.strName.equalsIgnoreCase(App.sharePrefrences.getStringPref(PreferencesKeys.strUserName))) {
                    versionViewHolder.rlUserData.setVisibility(View.VISIBLE);
                    versionViewHolder.tvName.setText(commentListModel.strName);
                    versionViewHolder.tvComment.setText(commentListModel.strMessage);

                    versionViewHolder.rlUserDataOther.setVisibility(View.GONE);
                } else {
                    versionViewHolder.rlUserData.setVisibility(View.GONE);

                    versionViewHolder.rlUserDataOther.setVisibility(View.VISIBLE);
                    versionViewHolder.tvNameOther.setText(commentListModel.strName);
                    versionViewHolder.tvCommentOther.setText(commentListModel.strMessage);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mArrListCommentListModel.size();
        }


        class VersionViewHolder extends RecyclerView.ViewHolder {

            TextView tvName, tvComment;
            TextView tvNameOther, tvCommentOther;

            RelativeLayout rlMain;
            RelativeLayout rlUserData;
            RelativeLayout rlUserDataOther;

            public VersionViewHolder(View itemView) {
                super(itemView);


                rlMain = (RelativeLayout) itemView.findViewById(R.id.rlMain);
                rlUserData = (RelativeLayout) itemView.findViewById(R.id.rlUserData);
                rlUserDataOther = (RelativeLayout) itemView.findViewById(R.id.rlUserDataOther);

                tvName = (TextView) itemView.findViewById(R.id.tvName);
                tvComment = (TextView) itemView.findViewById(R.id.tvComment);
                tvNameOther = (TextView) itemView.findViewById(R.id.tvNameOther);
                tvCommentOther = (TextView) itemView.findViewById(R.id.tvCommentOther);


            }

        }
    }


    private void stopFollowingLocation() {
        //111
        mPubNub.unsubscribeAll();
        isFirstMessage = true;
    }

    @Override
    protected void onDestroy() {
        stopFollowingLocation();
        super.onDestroy();
    }
}
