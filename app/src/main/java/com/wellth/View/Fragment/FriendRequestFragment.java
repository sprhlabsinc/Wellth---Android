package com.wellth.View.Fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.ksoichiro.android.observablescrollview.Scrollable;
import com.github.ksoichiro.android.observablescrollview.TouchInterceptionFrameLayout;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.onesignal.shortcutbadger.ShortcutBadger;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wellth.Model.AppConfig;
import com.wellth.R;
import com.wellth.View.Adapter.FriendRequestAdapter;
import com.wellth.View.Adapter.IssueAdapter;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

public class FriendRequestFragment extends Fragment implements ObservableScrollViewCallbacks {

    private RelativeLayout layout_top;
    private ObservableListView list_friend;
    private FriendRequestAdapter listAdapter;
    private PtrClassicFrameLayout ptrFrame;
    private TouchInterceptionFrameLayout mInterceptionLayout;

    private TextView txt_request;

    private boolean mScrolled;
    private ScrollState mLastScrollState;

    private ArrayList<ParseUser> requestList = new ArrayList<ParseUser>();

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getRequests();
        }
    };

    public static FriendRequestFragment newInstance() {
        FriendRequestFragment fragment = new FriendRequestFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(getResources().getString(R.string.update_request));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver((mBroadcastReceiver), filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_friend_request, container, false);

        txt_request = (TextView) view.findViewById(R.id.txt_request);

        layout_top = (RelativeLayout) view.findViewById(R.id.layout_top);
        list_friend = (ObservableListView) view.findViewById(R.id.list_friend);
        list_friend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                long viewId = view.getId();
                final ParseUser fromUser =(ParseUser) (list_friend.getItemAtPosition(position));
                if (mScrolled) { return; }

                if (viewId == R.id.btn_accept) {

                    final KProgressHUD pDialog = KProgressHUD.create(getContext())
                            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                            .setLabel("Accepting request ...")
                            .setDetailsLabel("")
                            .setCancellable(false)
                            .setAnimationSpeed(2)
                            .setDimAmount(0.5f)
                            .show();
                    ParseQuery<ParseObject> query3 = new ParseQuery<ParseObject>("Follower");
                    query3.whereEqualTo("fromUser", fromUser);
                    query3.whereEqualTo("toUser", ParseUser.getCurrentUser());
                    query3.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            if (e == null) {
                                object.put("status", "accept");
                                object.saveInBackground(new SaveCallback() {
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            requestList.remove(position);
                                            listAdapter.notifyDataSetChanged();
                                            txt_request.setText(String.format("%d New Friend Requests", requestList.size()));
                                            Snackbar.make(list_friend, getResources().getString(R.string.message_request_accept), Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                            AppConfig.sendPush(fromUser.getString("token"), String.format("%s just accepted your request.", ParseUser.getCurrentUser().getString("fullname")));

                                            LocalBroadcastManager mBroadcaster = LocalBroadcastManager.getInstance(getContext());
                                            Intent intent = new Intent();
                                            intent.setAction(getResources().getString(R.string.update_profile));
                                            mBroadcaster.sendBroadcast(intent);

                                            ShortcutBadger.removeCount(getContext());
                                        }
                                        else {
                                            Snackbar.make(list_friend, e.getMessage(), Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
                                        pDialog.dismiss();
                                    }
                                });
                            }
                            else {
                                Snackbar.make(list_friend, e.getMessage(), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                                pDialog.dismiss();
                            }
                        }
                    });
                }
                else if (viewId == R.id.btn_cancel) {
                    final KProgressHUD pDialog = KProgressHUD.create(getContext())
                            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                            .setLabel("Canceling request ...")
                            .setDetailsLabel("")
                            .setCancellable(false)
                            .setAnimationSpeed(2)
                            .setDimAmount(0.5f)
                            .show();
                    ParseQuery<ParseObject> query3 = new ParseQuery<ParseObject>("Follower");
                    query3.whereEqualTo("fromUser", fromUser);
                    query3.whereEqualTo("toUser", ParseUser.getCurrentUser());
                    query3.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            if (e == null) {
                                object.put("status", "cancel");
                                object.saveInBackground(new SaveCallback() {
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            requestList.remove(position);
                                            listAdapter.notifyDataSetChanged();
                                            txt_request.setText(String.format("%d New Friend Requests", requestList.size()));
                                            Snackbar.make(list_friend, getResources().getString(R.string.message_request_cancel), Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                            AppConfig.sendPush(fromUser.getString("token"), String.format("%s just canceled your request.", ParseUser.getCurrentUser().getString("fullname")));

                                            ShortcutBadger.removeCount(getContext());
                                        } else {
                                            Snackbar.make(list_friend, e.getMessage(), Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
                                        pDialog.dismiss();
                                    }
                                });
                            }
                            else {
                                Snackbar.make(list_friend, e.getMessage(), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                                pDialog.dismiss();
                            }
                        }
                    });
                }
                else if (viewId == R.id.btn_health_issue) {
                    final Dialog customDialog;
                    LayoutInflater inflater = (LayoutInflater) getLayoutInflater(getArguments());
                    View customView = inflater.inflate(R.layout.health_issue_dialog, null);

                    customDialog = new Dialog(getContext());
                    customDialog.setContentView(customView);
                    customDialog.setTitle(getResources().getString(R.string.health_issue));
                    Window window = customDialog.getWindow();
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    customDialog.show();

                    List<String> healthList = fromUser.getList("healthissue");

                    final ListView list_issue = (ListView) customDialog.findViewById(R.id.list_healthitem);
                    final IssueAdapter listAdapter = new IssueAdapter(healthList, getContext());
                    list_issue.setAdapter(listAdapter);

                    list_issue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view1, int position1, long id) {
                            long viewId = view1.getId();
                            String health = (String) (list_issue.getItemAtPosition(position1));
                        }
                    });
                    ImageButton btnClose = (ImageButton) customDialog.findViewById(R.id.btnClose);
                    btnClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            customDialog.dismiss();
                        }
                    });
                }
            }
        });

        ptrFrame = (PtrClassicFrameLayout) view.findViewById(R.id.rotate_header_list_view_frame);
        ptrFrame.setLastUpdateTimeRelateObject(this);
        ptrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                    getRequests();
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });
        ptrFrame.setResistance(1.7f);
        ptrFrame.setRatioOfHeaderHeightToRefresh(1.2f);
        ptrFrame.setDurationToClose(200);
        ptrFrame.setDurationToCloseHeader(1000);
        // default is false
        ptrFrame.setPullToRefresh(false);
        // default is true
        ptrFrame.setKeepHeaderWhenRefresh(true);

        mInterceptionLayout = (TouchInterceptionFrameLayout) view.findViewById(R.id.container);
        int mapHeight = getResources().getDimensionPixelSize(R.dimen.default_friend_request_header_height);
        mInterceptionLayout.setPadding(0,mapHeight,0,0);
        mInterceptionLayout.setScrollInterceptionListener(mInterceptionListener);

        getRequests();

        return view;
    }

    public void getRequests() {
        final ParseQuery<ParseObject> query3 = new ParseQuery<ParseObject>("Follower");
        query3.include("fromUser");
        query3.whereEqualTo("toUser", ParseUser.getCurrentUser());
        query3.whereEqualTo("status", "pending");
        query3.orderByDescending("updatedAt");
        query3.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException error) {
                if (parseObjects != null) {
                    requestList.clear();
                    for (int j = 0; j < parseObjects.size(); j++) {
                        ParseObject object = parseObjects.get(j);
                        requestList.add(object.getParseUser("fromUser"));
                    }
                    listAdapter = new FriendRequestAdapter(requestList, getContext());
                    list_friend.setAdapter(listAdapter);

                    txt_request.setText(String.format("%d New Friend Requests", requestList.size()));
                }
                ptrFrame.refreshComplete();
            }
        });
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (!mScrolled) {
            adjustToolbar(scrollState);
        }
    }

    private Scrollable getCurrentScrollable() {
        View view = getView();
        if (view == null) {
            return null;
        }
        return (Scrollable) view.findViewById(R.id.list_friend);
    }
    protected int getScreenHeight() {
        Context context = getActivity();
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int spacing =  Math.round(AppConfig.tabbar_height * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));

        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return size.y -actionBarHeight - statusBarHeight - spacing;
    }

    private void adjustToolbar(ScrollState scrollState) {
        //View toolbarView = getActivity().findViewById(R.id.toolbar);
        View toolbarView = (View)layout_top;
        int toolbarHeight = toolbarView.getHeight();
        final Scrollable scrollable = getCurrentScrollable();
        if (scrollable == null) {
            return;
        }
//        int scrollY = scrollable.getCurrentScrollY();
//        if (scrollState == ScrollState.DOWN) {
//            showToolbar();
//        } else if (scrollState == ScrollState.UP) {
//            if (toolbarHeight <= scrollY) {
//                hideToolbar();
//            } else {
//                showToolbar();
//            }
//        } else if (!toolbarIsShown() && !toolbarIsHidden()) {
//            // Toolbar is moving but doesn't know which to move:
//            // you can change this to hideToolbar()
//            showToolbar();
//        }
    }
    private boolean toolbarIsShown() {
        return ViewHelper.getTranslationY(mInterceptionLayout) == 0;
    }

    private boolean toolbarIsHidden() {
        View view = getView();
        if (view == null) {
            return false;
        }
        //View toolbarView = getActivity().findViewById(R.id.toolbar);
        View toolbarView = (View)layout_top;
        return ViewHelper.getTranslationY(mInterceptionLayout) == -toolbarView.getHeight();
    }

    private void showToolbar() {
        animateToolbar(0);
    }

    private void hideToolbar() {
        //View toolbarView = getActivity().findViewById(R.id.toolbar);
        View toolbarView = (View)layout_top;
        animateToolbar(-toolbarView.getHeight());
    }

    private void animateToolbar(final float toY) {
        float layoutTranslationY = ViewHelper.getTranslationY(mInterceptionLayout);
        if (layoutTranslationY != toY) {
            ValueAnimator animator = ValueAnimator.ofFloat(ViewHelper.getTranslationY(mInterceptionLayout), toY).setDuration(200);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float translationY = (float) animation.getAnimatedValue();
                    //View toolbarView = getActivity().findViewById(R.id.toolbar);
                    View toolbarView = (View)layout_top;
                    ViewHelper.setTranslationY(mInterceptionLayout, translationY);
                    ViewHelper.setTranslationY(toolbarView, translationY);
                    if (translationY < 0) {
                        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) mInterceptionLayout.getLayoutParams();
                        lp.height = (int) (-translationY + getScreenHeight());
                        mInterceptionLayout.requestLayout();
                    }
                }
            });
            animator.start();
        }
    }

    private TouchInterceptionFrameLayout.TouchInterceptionListener mInterceptionListener = new TouchInterceptionFrameLayout.TouchInterceptionListener() {
        @Override
        public boolean shouldInterceptTouchEvent(MotionEvent ev, boolean moving, float diffX, float diffY) {
//            if (!mScrolled && mSlop < Math.abs(diffX) && Math.abs(diffY) < Math.abs(diffX)) {
//                // Horizontal scroll is maybe handled by ViewPager
//                return false;
//            }

            Scrollable scrollable = getCurrentScrollable();
            if (scrollable == null) {
                mScrolled = false;
                return false;
            }

            // If interceptionLayout can move, it should intercept.
            // And once it begins to move, horizontal scroll shouldn't work any longer.
            //View toolbarView = getActivity().findViewById(R.id.toolbar);
            //View toolbarView = getActivity().findViewById(R.id.toolbar);
            int toolbarHeight = layout_top.getHeight();
            int translationY = (int) ViewHelper.getTranslationY(mInterceptionLayout);
            boolean scrollingUp = 0 < diffY;
            boolean scrollingDown = diffY < 0;
            if (scrollingUp) {
                if (translationY < 0) {
                    mScrolled = true;
                    mLastScrollState = ScrollState.UP;
                    return true;
                }
            } else if (scrollingDown) {
                if (-toolbarHeight < translationY) {
                    mScrolled = true;
                    mLastScrollState = ScrollState.DOWN;
                    return true;
                }
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScrolled = false;
                }
            }, AppConfig.SCROLLING_DELAY_TIME);

            return false;
        }

        @Override
        public void onDownMotionEvent(MotionEvent ev) {
        }

        @Override
        public void onMoveMotionEvent(MotionEvent ev, float diffX, float diffY) {
            //View toolbarView = getActivity().findViewById(R.id.toolbar);
            View toolbarView = (View)layout_top;
            float translationY = ScrollUtils.getFloat(ViewHelper.getTranslationY(mInterceptionLayout) + diffY, -toolbarView.getHeight(), 0);
            ViewHelper.setTranslationY(mInterceptionLayout, translationY);
            ViewHelper.setTranslationY(toolbarView, translationY);
            if (translationY < 0) {
                ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) mInterceptionLayout.getLayoutParams();
                lp.height = (int) (-translationY + getScreenHeight());
                mInterceptionLayout.requestLayout();
            }
        }

        @Override
        public void onUpOrCancelMotionEvent(MotionEvent ev) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScrolled = false;
                }
            }, AppConfig.SCROLLING_DELAY_TIME);

            adjustToolbar(mLastScrollState);
        }
    };
}
