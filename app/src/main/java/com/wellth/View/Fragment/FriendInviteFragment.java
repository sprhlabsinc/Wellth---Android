package com.wellth.View.Fragment;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.ksoichiro.android.observablescrollview.Scrollable;
import com.github.ksoichiro.android.observablescrollview.TouchInterceptionFrameLayout;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wellth.Model.AppConfig;
import com.wellth.R;
import com.wellth.View.Adapter.FriendInviteAdapter;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

public class FriendInviteFragment extends Fragment implements ObservableScrollViewCallbacks {

    private RelativeLayout layout_top;
    private ObservableListView list_friend;
    private FriendInviteAdapter listAdapter;
    private PtrClassicFrameLayout ptrFrame;
    private TouchInterceptionFrameLayout mInterceptionLayout;

    private EditText edit_search;
    private ImageButton btn_search;

    private boolean mScrolled;
    private ScrollState mLastScrollState;

    private ArrayList<ParseUser> friendList = new ArrayList<ParseUser>();

    public static FriendInviteFragment newInstance() {
        FriendInviteFragment fragment = new FriendInviteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_friend_invite, container, false);

        edit_search = (EditText) view.findViewById(R.id.edit_search);
        btn_search = (ImageButton) view.findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFriends();
            }
        });

        layout_top = (RelativeLayout) view.findViewById(R.id.layout_top);
        list_friend = (ObservableListView) view.findViewById(R.id.list_friend);
        list_friend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                long viewId = view.getId();
                if (mScrolled) { return; }

                if (viewId == R.id.btn_invite) {
                    if (AppConfig.userInfo.issueList.size() == 0) {
                        Snackbar.make(view, getResources().getString(R.string.message_not_exist_health_issue), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        return;
                    }
                    final KProgressHUD pDialog = KProgressHUD.create(getContext())
                            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                            .setLabel("Checking request ...")
                            .setDetailsLabel("")
                            .setCancellable(false)
                            .setAnimationSpeed(2)
                            .setDimAmount(0.5f)
                            .show();
                    final ParseUser toUser =(ParseUser) (list_friend.getItemAtPosition(position));

                    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Follower");
                    query.whereEqualTo("fromUser", ParseUser.getCurrentUser());
                    query.whereEqualTo("toUser", toUser);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object1, ParseException e) {
                            if (object1 == null) {
                                ParseObject object = new ParseObject("Follower");
                                object.put("fromUser", ParseUser.getCurrentUser());
                                object.put("toUser", toUser);
                                object.put("status", "pending");
                                object.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Snackbar.make(list_friend, getResources().getString(R.string.message_invite_success), Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                            AppConfig.sendPush(toUser.getString("token"), String.format("%s just sent a friend request to you.", ParseUser.getCurrentUser().getString("fullname")));
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
                                String status = object1.getString("status");

                                if (status.equals("accept")) {
                                    Snackbar.make(list_friend, getResources().getString(R.string.message_invite_exist), Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                                else if (status.equals("cancel") || status.equals("delete")) {
                                    object1.put("status", "pending");
                                    object1.saveInBackground();

                                    Snackbar.make(list_friend, getResources().getString(R.string.message_invite_success), Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                    AppConfig.sendPush(toUser.getString("token"), String.format("%s just sent a friend request to you.", ParseUser.getCurrentUser().getString("fullname")));
                                }
                                else if (status.equals("pending")) {
                                    Snackbar.make(list_friend, getResources().getString(R.string.message_sent_invite), Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                                pDialog.dismiss();
                            }
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
                getFriends();
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
        int mapHeight = getResources().getDimensionPixelSize(R.dimen.default_add_friend_header_height);
        mInterceptionLayout.setPadding(0,mapHeight,0,0);
        mInterceptionLayout.setScrollInterceptionListener(mInterceptionListener);

        getFriends();

        return view;
    }

//    public void getFriends1(final String search) {
//        ParseQuery<ParseUser> query = ParseUser.getQuery();
//        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
//        query.orderByDescending("updatedAt");
//        query.findInBackground(new FindCallback<ParseUser>() {
//            @Override
//            public void done(List<ParseUser> userObjects, ParseException error) {
//                if (userObjects != null) {
//                    friendList.clear();
//                    for (int i = 0; i < userObjects.size(); i++) {
//                        ParseUser user = userObjects.get(i);
//                        String username = user.getString("fullname").toLowerCase();
//                        List<String> list = user.getList("healthissue");
//                        if (!username.contains(search.toLowerCase()) && !AppConfig.hasString(list, search)) {
//                            continue;
//                        }
//                        friendList.add(user);
//                        if (friendList.size() > GET_MAX_POST_COUNT * 2) {
//                            break;
//                        }
//                    }
//                    listAdapter = new FriendInviteAdapter(friendList, getContext());
//                    list_friend.setAdapter(listAdapter);
//                    ptrFrame.refreshComplete();
//                }
//            }
//        });
//    }
//
//    public void getFriends2(final String search) {
////        final KProgressHUD pDialog = KProgressHUD.create(getContext())
////                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
////                .setLabel("Searching friends ...")
////                .setDetailsLabel("")
////                .setCancellable(false)
////                .setAnimationSpeed(2)
////                .setDimAmount(0.5f)
////                .show();
//
//        ParseQuery<ParseObject> query3 = new ParseQuery<ParseObject>("Follower");
//        query3.include("toUser");
//        query3.whereEqualTo("fromUser", ParseUser.getCurrentUser());
//        query3.findInBackground(new FindCallback<ParseObject>() {
//            @Override
//            public void done(final List<ParseObject> parseObjects, ParseException error) {
//                if (parseObjects != null) {
//                    ParseQuery<ParseUser> query = ParseUser.getQuery();
//                    query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
//                    query.findInBackground(new FindCallback<ParseUser>() {
//                        @Override
//                        public void done(List<ParseUser> userObjects, ParseException error) {
//                            if (userObjects != null) {
//                                friendList.clear();
//                                for (int i = 0; i < userObjects.size(); i++) {
//                                    boolean isNew = false;
//                                    ParseUser user = userObjects.get(i);
//                                    String username = user.getString("fullname").toLowerCase();
//                                    List<String> list = user.getList("healthissue");
//                                    if (!username.contains(search.toLowerCase()) && !AppConfig.hasString(list, search)) {
//                                        continue;
//                                    }
//
//                                    for (int j = 0; j < parseObjects.size(); j ++) {
//                                        ParseObject object = parseObjects.get(j);
//                                        if (user.getObjectId().equals(object.getParseUser("toUser").getObjectId())) {
//                                            isNew = true;
//                                            break;
//                                        }
//                                    }
//                                    if (!isNew)
//                                        friendList.add(user);
//                                    if (friendList.size() > GET_MAX_POST_COUNT) {
//                                        break;
//                                    }
//                                }
//                                listAdapter = new FriendInviteAdapter(friendList, getContext());
//                                list_friend.setAdapter(listAdapter);
//                                ptrFrame.refreshComplete();
////                                pDialog.dismiss();
//                            }
//                        }
//                    });
//                } else {
//                    ptrFrame.refreshComplete();
////                    pDialog.dismiss();
//                }
//            }
//        });
//    }

    private void getFriends() {
        String searchString = edit_search.getText().toString().trim();

        List list1 = new ArrayList();
        list1.add(searchString.toLowerCase());
        ParseQuery<ParseUser> query1 = ParseUser.getQuery();
        query1.whereMatches("fullname", searchString, "i");

        ParseQuery<ParseUser> query2 = ParseUser.getQuery();
        query2.whereContainedIn("healthissue", list1);

        final List<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseUser> query = ParseQuery.or(queries);
        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.orderByDescending("createdAt");
        query.setLimit(20);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> userObjects, ParseException error) {
                if (userObjects != null) {
                    friendList.clear();
                    for (int i = 0; i < userObjects.size(); i++) {
                        friendList.add(userObjects.get(i));
                    }
                    listAdapter = new FriendInviteAdapter(friendList, getContext());
                    list_friend.setAdapter(listAdapter);
                    ptrFrame.refreshComplete();
                }
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
