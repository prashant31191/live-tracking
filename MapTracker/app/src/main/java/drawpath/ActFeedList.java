package drawpath;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.maptracker.App;
import com.maptracker.R;
import com.utils.AppFlags;
import com.utils.CircularImageView;

import java.util.ArrayList;

import drawpath.model.RouteListModel;
import drawpath.model.SelectPlaceModel;

public class ActFeedList extends AppCompatActivity {

    String TAG = "=ActNotification=";
    RecyclerView recyclerView;
    MaterialRefreshLayout materialRefreshLayout;
    NotificationAdapter notificationAdapter;
    TextView tvNodataTag;
    LinearLayout llNodataTag;


    String strFrom = "", strTitle = "Feed";
    int page = 0;
    String strTotalResult = "0";
    public ArrayList<RouteListModel> arrayListAllRouteListModel;

    private Paint p = new Paint();

    FloatingActionButton fbAddPost;



    String strTagChooseImage = "choose image";
    String strTagGallery = "Gallery";
    String strTagCamera = "Camera";
    Bitmap photoBitmap;
    int CAMERA_REQESTCODE = 104;
    String sdCardPath;
    Uri mImageCaptureUri;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ViewGroup.inflate(this, R.layout.act_feed, llContainerSub);

        try {
            setContentView(R.layout.act_feed);

            getIntentData();
            initialization();
            
            setClickEvent();

            sdCardPath = Environment.getExternalStorageDirectory().toString();
            arrayListAllRouteListModel = new ArrayList<>();
            tvNodataTag.setTypeface(App.getFont_Regular());


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
            materialRefreshLayout.setLoadMore(true);


            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActFeedList.this);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setHasFixedSize(true);


            initSwipe();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT) {
                    notificationAdapter.removeItem(position);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if (dX < 0) {


                        /*p.setColor(Color.RED);
                        c.drawRect(background,p);*/

                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        p.setColor(Color.GRAY);
                        p.setTextSize(35);
                        c.drawText("will be removed", background.centerX(), background.centerY(), p);
                        //versionViewHolder.tvName.setTypeface(App.getFont_Regular());

                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
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

private void setAdapterDate()
{
    try {
        arrayListAllRouteListModel = getSaticDataList();

        if (arrayListAllRouteListModel != null && arrayListAllRouteListModel.size() > 0) {
                notificationAdapter = new NotificationAdapter(ActFeedList.this, arrayListAllRouteListModel);
                recyclerView.setAdapter(notificationAdapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setVisibility(View.VISIBLE);
                //tvNodataTag.setVisibility(View.GONE);
                llNodataTag.setVisibility(View.GONE);
            }
        }
    catch(Exception e)
        {
            e.printStackTrace();
        }
}
    private ArrayList<RouteListModel> getSaticDataList() {
        ArrayList<RouteListModel> notificationListModels = new ArrayList<>();

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
        notificationListModels.add(new RouteListModel("this is test", "default.png"));
        notificationListModels.add(new RouteListModel("this is test", "default.png"));

        return notificationListModels;
    }

    public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VersionViewHolder> {
        ArrayList<RouteListModel> mArrListNotificationListModel;
        Context mContext;


        public NotificationAdapter(Context context, ArrayList<RouteListModel> arrayListFollowers) {
            mArrListNotificationListModel = arrayListFollowers;
            mContext = context;
        }

        @Override
        public VersionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.raw_feed, viewGroup, false);
            VersionViewHolder viewHolder = new VersionViewHolder(view);
            return viewHolder;
        }


        @Override
        public void onBindViewHolder(final VersionViewHolder versionViewHolder, final int i) {
            try {
                RouteListModel notificationListModel = mArrListNotificationListModel.get(i);
                versionViewHolder.tvName.setText(Html.fromHtml("<b>" + notificationListModel.title + "</b>"));


                versionViewHolder.cardItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {

                            ArrayList<SelectPlaceModel> arrayListSelectPlaceModel = new ArrayList<>();

                            /*LatLng latLng = new LatLng()
                            arrayListSelectPlaceModel.add(new SelectPlaceModel())
                            */
                            AppFlags.arrayListSelectPlaceModel = arrayListSelectPlaceModel;

                            /*Intent intent = new Intent(ActFeedList.this, ActFeedDetail.class);
                            intent.putExtra(AppFlags.tagFrom, "ActFeedList");
                            startActivity(intent);*/
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

            mArrListNotificationListModel.remove(position);
            notifyItemRemoved(position);


        }


        class VersionViewHolder extends RecyclerView.ViewHolder {
            CardView cardItemLayout;
            TextView tvName, tvDateTime;
            CircularImageView ivUserPhoto;
            RelativeLayout rlMain;

            public VersionViewHolder(View itemView) {
                super(itemView);

                cardItemLayout = (CardView) itemView.findViewById(R.id.cardlist_item);
                rlMain = (RelativeLayout) itemView.findViewById(R.id.rlMain);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
                ivUserPhoto = (CircularImageView) itemView.findViewById(R.id.ivUserPhoto);
                tvDateTime = (TextView) itemView.findViewById(R.id.tvDateTime);

                tvName.setTypeface(App.getFont_Regular());
                tvDateTime.setTypeface(App.getFont_Regular());
            }

        }
    }
}