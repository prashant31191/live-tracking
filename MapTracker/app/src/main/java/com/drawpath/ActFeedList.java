package com.drawpath;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.azsdk.swipe.SwipeHelper;
import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.drawpath.model.RouteListModel;
import com.drawpath.model.SelectPlaceModel;
import com.maptracker.App;
import com.maptracker.R;
import com.utils.AppFlags;
import com.utils.CircularImageView;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class ActFeedList extends AppCompatActivity {

    String TAG = "=ActFeedList=";
    RecyclerView recyclerView;
    MaterialRefreshLayout materialRefreshLayout;
    NotificationAdapter notificationAdapter;
    TextView tvNodataTag;
    LinearLayout llNodataTag;


    String strFrom = "";
    int page = 0;
    public List<RouteListModel> arrayListAllRouteListModel;


    FloatingActionButton fbAddPost;


    String sdCardPath;

    Realm realm;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            App.showLog("=TAG="+TAG);

            setContentView(R.layout.act_feed);

            getIntentData();
            initialization();

            setClickEvent();

            sdCardPath = Environment.getExternalStorageDirectory().toString();
            arrayListAllRouteListModel = new ArrayList<>();
            tvNodataTag.setTypeface(App.getFont_Regular());

            realm = Realm.getInstance(App.getRealmConfiguration());
            setAdapterData();
        } catch (Exception e) {
            // TODO: handle exceptione.
            e.printStackTrace();
        }
    }

    private void initialization() {
        try {
            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            tvNodataTag = (TextView) findViewById(R.id.tvNodataTag);
            llNodataTag = (LinearLayout) findViewById(R.id.llNodataTag);
            materialRefreshLayout = (MaterialRefreshLayout) findViewById(R.id.refresh);


            fbAddPost = (FloatingActionButton) findViewById(R.id.fbAddPost);

            materialRefreshLayout.setIsOverLay(true);
            materialRefreshLayout.setWaveShow(true);
            materialRefreshLayout.setWaveColor(0x55ffffff);

            //tvNodataTag.setVisibility(View.GONE);
            llNodataTag.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            materialRefreshLayout.setLoadMore(false);


            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActFeedList.this);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setHasFixedSize(true);

            initSwipe();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSwipe() {
        try {
            int ButtonWidth = 90;
            int ButtonText = 18;


            SwipeHelper swipeHelper = new SwipeHelper(ActFeedList.this, recyclerView, ButtonWidth, ButtonText) {
                @Override
                public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                    underlayButtons.add(new SwipeHelper.UnderlayButton(
                            "Delete",
                            0,
                            Color.parseColor("#B30000"),
                            Color.parseColor("#FFFFFF"),
                            new SwipeHelper.UnderlayButtonClickListener() {
                                @Override
                                public void onClick(int pos) {
                                    // TODO: onDelete

                                    App.showLog("=Delete====pos==" + pos);

                                    if (notificationAdapter != null) {
                                        notificationAdapter.removeItem(pos);
                                    }
                                }
                            }
                    ));
                    underlayButtons.add(new SwipeHelper.UnderlayButton(
                            "Share",
                            0,
                            Color.parseColor("#FF9502"),
                            Color.parseColor("#FFFFFF"),
                            new SwipeHelper.UnderlayButtonClickListener() {
                                @Override
                                public void onClick(int pos) {
                                    // TODO: OnTransfer
                                    App.showLog("=Transfer====pos==" + pos);
                                }
                            }
                    ));
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void getIntentData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getString(App.ITAG_FROM) != null && bundle.getString(App.ITAG_FROM).length() > 0) {
                strFrom = bundle.getString(App.ITAG_FROM);
            }
        }

    }


    private void setClickEvent() {

        fbAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActFeedList.this, ActPostARoute.class);
                intent.putExtra(AppFlags.tagFrom, "ActFeedList");
                startActivity(intent);
            }
        });


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    // Scrolling up
                    /*fabtoolbar.hide();
                    isBottombarShowing = false;*/
                } else {
                    // Scrolling down
                    /*fabtoolbar.hide();
                    isBottombarShowing = false;*/
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Do something
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    // Do something
                } else {
                    // Do something
                }
            }
        });


        materialRefreshLayout.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(final MaterialRefreshLayout materialRefreshLayout) {
                //refreshing...
                if (App.isInternetAvail(ActFeedList.this)) {
                    //asyncGetNotificationList();

                    page = 0;
                    arrayListAllRouteListModel = new ArrayList<>();

                    setAdapterData();

                    // refresh complete
                    materialRefreshLayout.finishRefresh();
                    // load more refresh complete
                    materialRefreshLayout.finishRefreshLoadMore();


                } else {
                    App.showSnackBar(recyclerView, AppFlags.strNetError);
                }
            }

            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
                try {

                    // refresh complete
                    materialRefreshLayout.finishRefresh();
                    // load more refresh complete
                    materialRefreshLayout.finishRefreshLoadMore();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setAdapterData() {
        try {
            arrayListAllRouteListModel = getSaticDataList();

            if (arrayListAllRouteListModel != null && arrayListAllRouteListModel.size() > 0) {
                notificationAdapter = new NotificationAdapter(ActFeedList.this, arrayListAllRouteListModel);
                recyclerView.setAdapter(notificationAdapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setVisibility(View.VISIBLE);

                llNodataTag.setVisibility(View.GONE);
            } else {
                llNodataTag.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<RouteListModel> getSaticDataList() {
        List<RouteListModel> notificationListModels = new ArrayList<>();

    /*    notificationListModels.add(new RouteListModel("this is test", "default.png"));
        notificationListModels.add(new RouteListModel("this is test", "default.png"));
        notificationListModels.add(new RouteListModel("this is test", "default.png"));
        notificationListModels.add(new RouteListModel("this is test", "default.png"));
        notificationListModels.add(new RouteListModel("this is test", "default.png"));
        notificationListModels.add(new RouteListModel("this is test", "default.png"));
        notificationListModels.add(new RouteListModel("this is test", "default.png"));
        notificationListModels.add(new RouteListModel("this is test", "default.png"));
        notificationListModels.add(new RouteListModel("this is test", "default.png"));
        notificationListModels.add(new RouteListModel("this is test", "default.png"));
        notificationListModels.add(new RouteListModel("this is test", "default.png"));
        notificationListModels.add(new RouteListModel("this is test", "default.png"));
        notificationListModels.add(new RouteListModel("this is test", "default.png"));
        notificationListModels.add(new RouteListModel("this is test", "default.png"));*/

        notificationListModels =   App.fetchOfflineRouteList(realm);



        return notificationListModels;
    }

    public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VersionViewHolder> {
        List<RouteListModel> mArrListNotificationListModel;
        Context mContext;


        public NotificationAdapter(Context context, List<RouteListModel> arrayListFollowers) {
            mArrListNotificationListModel = arrayListFollowers;
            mContext = context;
        }

        @Override
        public VersionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_feed, viewGroup, false);
            VersionViewHolder viewHolder = new VersionViewHolder(view);
            return viewHolder;
        }


        @Override
        public void onBindViewHolder(final VersionViewHolder versionViewHolder, final int i) {
            try {
                RouteListModel notificationListModel = mArrListNotificationListModel.get(i);
                versionViewHolder.tvRouteFrom.setText(notificationListModel.route_from);
                versionViewHolder.tvRouteTo.setText(notificationListModel.route_to);
                versionViewHolder.tvKms.setText(notificationListModel.route_kms +"\nkms");
                versionViewHolder.tvTextPost.setText(notificationListModel.detail);
                versionViewHolder.tvDateTime.setText(App.getLongToYYMMDDDate(notificationListModel.timestamp));


                versionViewHolder.cardItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {

                            ArrayList<SelectPlaceModel> arrayListSelectPlaceModel = new ArrayList<>();
                            {
                                if(mArrListNotificationListModel.get(i).realmListSelectPlaceModel !=null)
                                {
                                    for(SelectPlaceModel selectPlaceModel : mArrListNotificationListModel.get(i).realmListSelectPlaceModel)
                                    {
                                        arrayListSelectPlaceModel.add(selectPlaceModel);
                                    }
                                }


                                AppFlags.arrayListSelectPlaceModel = arrayListSelectPlaceModel;

                                Intent intent = new Intent(ActFeedList.this, ActFeedAddDetail.class);
                                intent.putExtra(AppFlags.tagFrom, "ActFeedList");
                                if(mArrListNotificationListModel.get(i).detail !=null)
                                intent.putExtra(AppFlags.tagDetail, mArrListNotificationListModel.get(i).detail);
                                intent.putExtra(AppFlags.tagIsEdit, "0");
                                startActivity(intent);

                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mArrListNotificationListModel.size();
        }


        public void removeItem(int position) {

            try {
                mArrListNotificationListModel.remove(position);
                notifyItemRemoved(position);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notificationAdapter.notifyDataSetChanged();
                    }
                },1000);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        class VersionViewHolder extends RecyclerView.ViewHolder {
            CardView cardItemLayout;
            TextView tvRouteFrom, tvRouteTo,tvKms,tvTextPost, tvDateTime;
            RelativeLayout rlMain;

            public VersionViewHolder(View itemView) {
                super(itemView);

                cardItemLayout = (CardView) itemView.findViewById(R.id.cardlist_item);
                rlMain = (RelativeLayout) itemView.findViewById(R.id.rlMain);

                tvRouteFrom = (TextView) itemView.findViewById(R.id.tvRouteFrom);
                tvRouteTo = (TextView) itemView.findViewById(R.id.tvRouteTo);
                tvKms = (TextView) itemView.findViewById(R.id.tvKms);
                tvTextPost = (TextView) itemView.findViewById(R.id.tvTextPost);
                tvDateTime = (TextView) itemView.findViewById(R.id.tvDateTime);

                tvRouteFrom.setSelected(true);
                tvRouteTo.setSelected(true);

                tvRouteFrom.setTypeface(App.getFont_Regular());
                tvRouteTo.setTypeface(App.getFont_Regular());
                tvKms.setTypeface(App.getFont_Regular());
                tvTextPost.setTypeface(App.getFont_Regular());
                tvDateTime.setTypeface(App.getFont_Regular());
            }

        }
    }
}