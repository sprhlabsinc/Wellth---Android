package com.wellth.View.Fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.ksoichiro.android.observablescrollview.Scrollable;
import com.github.ksoichiro.android.observablescrollview.TouchInterceptionFrameLayout;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.wellth.Model.AppConfig;
import com.wellth.R;
import com.wellth.View.Activity.AnswerActivity;
import com.wellth.View.Activity.DetailActivity;
import com.wellth.View.Activity.FriendsActivity;
import com.wellth.View.Activity.ProfileHealthIssueActivity;
import com.wellth.View.Adapter.PostAdapter;
import com.wellth.View.Adapter.QuestionAdapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import agency.tango.android.avatarview.AvatarPlaceholder;
import agency.tango.android.avatarview.views.AvatarView;
import cn.jzvd.JZVideoPlayer;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.MediaOptions;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;

import static com.wellth.Model.AppConfig.GET_MAX_POST_COUNT;
import static com.wellth.Model.AppConfig.MAX_PAGE_COUNT;

public class ProfileFragment extends Fragment implements ObservableScrollViewCallbacks {

    private static final int REQUEST_MEDIA = 3001;

    private RelativeLayout layout_top, layout_grid;
    private ObservableListView list_post;
    private QuestionAdapter listAdapter;
    private PtrClassicFrameLayout ptrFrame, rotate_header_grid_view_frame;
    private TouchInterceptionFrameLayout mInterceptionLayout;
    private TextView txt_empty_message_post, txt_empty_message_question;

    private GridView grid_post;
    private PostAdapter gridAdapter;

    private AvatarView img_avatar;
    private AvatarPlaceholder placeholder;

    private Button btn_friends, btn_health_issue, btn_posts, btn_questions;
    private TextView txt_username, txt_location, txt_age;
    private TextView txt_friend_badge, txt_issue_badge;

    private boolean mScrolled;
    private ScrollState mLastScrollState;
    private int pageCount = 0, pageQuestionCount = 0;
    private ArrayList<ParseObject> postList = new ArrayList<ParseObject>();
    private ArrayList<ParseObject> questionList = new ArrayList<ParseObject>();
    private boolean isLoading = false, isQuestionLoading = false;
    private Date currentDate, currentDate1;
    private View footerView;

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateStatus();
        }
    };

    BroadcastReceiver mUpdateLiveBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            for (int i = 0; i < postList.size(); i ++) {
                ParseObject object = postList.get(i);
                if (object.getObjectId().equals(AppConfig.updateObject.getObjectId())) {
                    postList.set(i, AppConfig.updateObject);
                    gridAdapter.notifyDataSetChanged();
                    break;
                }
            }
            for (int i = 0; i < questionList.size(); i ++) {
                ParseObject object = questionList.get(i);
                if (object.getObjectId().equals(AppConfig.updateObject.getObjectId())) {
                    questionList.set(i, AppConfig.updateObject);
                    listAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    };

    BroadcastReceiver mCreateLiveBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = AppConfig.createObject.getString("type");
            ParseUser fromUser = AppConfig.createObject.getParseUser("fromUser");

            if (fromUser.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                if (type.equals("forum") || type.equals("text")) {
                    boolean is_append = true;

                    for (int i = 0; i < questionList.size(); i ++) {
                        ParseObject object = questionList.get(i);
                        if (object.getObjectId().equals(AppConfig.createObject.getObjectId())) {
                            is_append = false;
                            break;
                        }
                    }
                    if (is_append) {
                        questionList.add(0, AppConfig.createObject);
                        listAdapter.notifyDataSetChanged();
                    }
                }
                else if (type.equals("image") || type.equals("video")) {
                    boolean is_append = true;

                    for (int i = 0; i < postList.size(); i ++) {
                        ParseObject object = postList.get(i);
                        if (object.getObjectId().equals(AppConfig.createObject.getObjectId())) {
                            is_append = false;
                            break;
                        }
                    }
                    if (is_append) {
                        postList.add(0, AppConfig.createObject);
                        gridAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    private void updateStatus() {
        placeholder = new AvatarPlaceholder(AppConfig.userInfo.username);
        Picasso.with(getContext())
                .load(AppConfig.userInfo.photo)
                .placeholder(placeholder)
                .into(img_avatar);
        if (!AppConfig.userInfo.type.equals("Email") && AppConfig.userInfo.photo == null) {
            Picasso.with(getContext())
                    .load(AppConfig.userInfo.photoUrl)
                    .placeholder(placeholder)
                    .into(img_avatar);
        }
        txt_username.setText(AppConfig.userInfo.username);
        String country = AppConfig.userInfo.country;
        if (country == null || country.equals("")) {
            txt_location.setVisibility(View.GONE);
        }
        else {
            txt_location.setText(country);
            txt_location.setVisibility(View.VISIBLE);
        }
        if (AppConfig.userInfo.age == 0) {
            txt_age.setVisibility(View.GONE);
        }
        else {
            txt_age.setVisibility(View.VISIBLE);
            txt_age.setText(String.format("Age: %d", AppConfig.userInfo.age));
        }

        txt_issue_badge.setText(String.format("%d", AppConfig.userInfo.issueList.size()));
        getFriends();
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(getResources().getString(R.string.update_profile));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver((mBroadcastReceiver), filter);

        IntentFilter liveFilter = new IntentFilter();
        liveFilter.addAction(getResources().getString(R.string.update_post_real_time));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver((mUpdateLiveBroadcastReceiver), liveFilter);

        IntentFilter liveFilter1 = new IntentFilter();
        liveFilter1.addAction(getResources().getString(R.string.create_post_real_time));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver((mCreateLiveBroadcastReceiver), liveFilter1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);
        footerView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_footer, null, false);
        img_avatar = (AvatarView) view.findViewById(R.id.img_avatar);
        img_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaOptions.Builder builder = new MediaOptions.Builder();
                MediaOptions options = builder.setIsCropped(true).setFixAspectRatio(true)
                        .build();
                MediaPickerActivity.open(ProfileFragment.this, REQUEST_MEDIA, options);
            }
        });
        txt_username = (TextView) view.findViewById(R.id.txt_username);
        txt_location = (TextView) view.findViewById(R.id.txt_location);
        txt_age = (TextView) view.findViewById(R.id.txt_age);

        txt_empty_message_post = (TextView) view.findViewById(R.id.txt_empty_message_post);
        txt_empty_message_question = (TextView) view.findViewById(R.id.txt_empty_message_question);
        txt_empty_message_post.setVisibility(View.GONE);
        txt_empty_message_question.setVisibility(View.GONE);

        txt_friend_badge = (TextView) view.findViewById(R.id.txt_friend_badge);
        txt_issue_badge = (TextView) view.findViewById(R.id.txt_issue_badge);

        btn_friends = (Button) view.findViewById(R.id.btn_friends);
        btn_health_issue = (Button) view.findViewById(R.id.btn_health_issue);
        btn_posts = (Button) view.findViewById(R.id.btn_posts);
        btn_questions = (Button) view.findViewById(R.id.btn_questions);

        btn_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FriendsActivity.class);
                startActivity(intent);
            }
        });
        btn_health_issue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ProfileHealthIssueActivity.class);
                startActivity(intent);
            }
        });
        btn_posts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_posts.setBackgroundResource(R.drawable.rounded_edittext);
                btn_questions.setBackgroundResource(R.drawable.rounded_greycolor);

                mInterceptionLayout.setVisibility(View.GONE);
                layout_grid.setVisibility(View.VISIBLE);
            }
        });
        btn_questions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JZVideoPlayer.releaseAllVideos();

                btn_posts.setBackgroundResource(R.drawable.rounded_greycolor);
                btn_questions.setBackgroundResource(R.drawable.rounded_edittext);

                mInterceptionLayout.setVisibility(View.VISIBLE);
                layout_grid.setVisibility(View.GONE);
            }
        });

        grid_post = (GridView) view.findViewById(R.id.grid_post);
        rotate_header_grid_view_frame = (PtrClassicFrameLayout) view.findViewById(R.id.rotate_header_grid_view_frame);
        rotate_header_grid_view_frame.setLastUpdateTimeRelateObject(this);
        rotate_header_grid_view_frame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                pageCount = 0;
                isLoading = false;
                Calendar cal = new GregorianCalendar();
                currentDate = cal.getTime();

                JZVideoPlayer.releaseAllVideos();
                getPosts();
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });
        rotate_header_grid_view_frame.setResistance(1.7f);
        rotate_header_grid_view_frame.setRatioOfHeaderHeightToRefresh(1.2f);
        rotate_header_grid_view_frame.setDurationToClose(200);
        rotate_header_grid_view_frame.setDurationToCloseHeader(1000);
        // default is false
        rotate_header_grid_view_frame.setPullToRefresh(false);
        // default is true
        rotate_header_grid_view_frame.setKeepHeaderWhenRefresh(true);

        layout_top = (RelativeLayout) view.findViewById(R.id.layout_top);
        layout_grid = (RelativeLayout) view.findViewById(R.id.layout_grid);
        list_post = (ObservableListView) view.findViewById(R.id.list_post);
        list_post.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                long viewId = view.getId();

                if (mScrolled) { return; }

                ParseObject postInfo = (ParseObject) list_post.getItemAtPosition(position);
                AppConfig.selectPost = postInfo;

                if (viewId == R.id.btn_post_answer) {
                    Intent intent = new Intent(getContext(), AnswerActivity.class);
                    startActivity(intent);
                }
                else if (viewId == R.id.btn_share) {
                    String content = postInfo.getString("content");
                    String type = postInfo.getString("type");
                    if (type.equals("image") || type.equals("video")) {
                        ParseFile attachment = postInfo.getParseFile("attachment");
                        File postFile = null;
                        if (attachment != null) {
                            try {
                                postFile = attachment.getFile();
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                        }
                        content = String.format("%s %s/%s", content, AppConfig.PARSE_URL_PATH, postFile.getName());
                    }
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, content);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }
                else if (viewId == R.id.layout_wrapper) {
                    Intent intent = new Intent(getContext(), DetailActivity.class);
                    startActivity(intent);
                }
            }
        });
        list_post.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int threshold = 1;
                int count = list_post.getCount();

                if (scrollState == SCROLL_STATE_IDLE) {
                    if (list_post.getLastVisiblePosition() >= count - threshold && pageCount < MAX_PAGE_COUNT) {
                        getQuestions();     //loading more data
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        listAdapter = new QuestionAdapter(questionList, getContext());
        list_post.setAdapter(listAdapter);

        ptrFrame = (PtrClassicFrameLayout) view.findViewById(R.id.rotate_header_list_view_frame);
        ptrFrame.setLastUpdateTimeRelateObject(this);
        ptrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                pageQuestionCount = 0;
                isQuestionLoading = false;
                Calendar cal = new GregorianCalendar();
                currentDate1 = cal.getTime();

                getQuestions();
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
        int mapHeight = getResources().getDimensionPixelSize(R.dimen.default_profile_header_height);
        mInterceptionLayout.setPadding(0,mapHeight,0,0);
        mInterceptionLayout.setScrollInterceptionListener(mInterceptionListener);

        grid_post.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                long viewId = view.getId();
                if (viewId == R.id.img_post) {
                    ParseObject postInfo = (ParseObject) grid_post.getItemAtPosition(position);
                    AppConfig.selectPost = postInfo;
                    Intent intent = new Intent(getContext(), DetailActivity.class);
                    startActivity(intent);
                }
            }
        });
        grid_post.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                int threshold = 1;
                int count = grid_post.getCount();

                if (scrollState == SCROLL_STATE_IDLE) {
                    if (grid_post.getLastVisiblePosition() >= count - threshold && pageQuestionCount < MAX_PAGE_COUNT) {
                        getPosts();     //loading more data
                    }
                    JZVideoPlayer.releaseAllVideos();
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
        gridAdapter = new PostAdapter(postList, getContext());
        grid_post.setAdapter(gridAdapter);

        updateStatus();

        Calendar cal = new GregorianCalendar();
        currentDate = cal.getTime();
        currentDate1 = cal.getTime();

        getPosts();
        getQuestions();

        return view;
    }

    public void getPosts() {
        if (isLoading == true) return;
        isLoading = true;

        final int GET_MAX_POST_COUNT = 15;

        ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>("Post");
        query1.whereEqualTo("type", "image");

        ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Post");
        query2.whereEqualTo("type", "video");

        final List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.include("fromUser");
        query.include("comments.fromUser");
        query.include("likes.fromUser");
        query.whereLessThanOrEqualTo("createdAt", currentDate);
        query.orderByDescending("createdAt");
        query.setSkip(GET_MAX_POST_COUNT * pageCount);
        query.setLimit(GET_MAX_POST_COUNT);
        query.whereEqualTo("fromUser", ParseUser.getCurrentUser());
        query.whereNotEqualTo("type", "text");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> posts, ParseException e) {
                if (posts != null) {
                    if (pageCount == 0) {
                        postList.clear();
                        if (posts.size() == 0) {
                            txt_empty_message_post.setVisibility(View.VISIBLE);
                        }
                        else {
                            txt_empty_message_post.setVisibility(View.GONE);
                        }
                    }
                    postList.addAll(posts);
                    gridAdapter.notifyDataSetChanged();
                    rotate_header_grid_view_frame.refreshComplete();

                    pageCount ++;
                    if (posts.size() != 0) {
                        isLoading = false;
                    }
                }
            }
        });
    }

    public void getQuestions() {
        if (isQuestionLoading == true) return;
        isQuestionLoading = true;

        list_post.addFooterView(footerView);
        ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>("Post");
        query1.whereEqualTo("type", "text");

        ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Post");
        query2.whereEqualTo("type", "forum");

        final List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.include("fromUser");
        query.include("comments.fromUser");
        query.include("likes.fromUser");
        query.whereLessThanOrEqualTo("createdAt", currentDate1);
        query.orderByDescending("createdAt");
        query.setSkip(GET_MAX_POST_COUNT * pageQuestionCount);
        query.setLimit(GET_MAX_POST_COUNT);
        query.whereEqualTo("fromUser", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> posts, ParseException e) {
                if (posts != null) {
                    if (pageQuestionCount == 0) {
                        questionList.clear();
                        if (posts.size() == 0) {
                            txt_empty_message_question.setVisibility(View.VISIBLE);
                        }
                        else {
                            txt_empty_message_question.setVisibility(View.GONE);
                        }
                    }
                    questionList.addAll(posts);
                    listAdapter.notifyDataSetChanged();
                    ptrFrame.refreshComplete();

                    pageQuestionCount ++;
                    if (posts.size() != 0) {
                        isQuestionLoading = false;
                    }
                    list_post.removeFooterView(footerView);
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
        return (Scrollable) view.findViewById(R.id.list_post);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA) {
            if (resultCode == Activity.RESULT_OK) {
                List<MediaItem> selectedList = MediaPickerActivity
                        .getMediaItemSelected(data);
                if (selectedList != null) {
                    for (MediaItem mediaItem : selectedList) {
                        File image = new File(mediaItem.getPathCropped(getContext()));
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                        int height = bitmap.getHeight();
                        int width = bitmap.getWidth();
                        int limit = 200;
                        if (width < height && width > limit) {
                            bitmap = Bitmap.createScaledBitmap(bitmap, limit, limit * height / width, true);
                        }
                        else if (height > limit) {
                            bitmap = Bitmap.createScaledBitmap(bitmap, limit * width / height, limit, true);
                        }
                        img_avatar.setImageBitmap(bitmap);

                        File imageFile = new File(Environment.getExternalStorageDirectory() + File.separator + "avatar.jpg");
                        OutputStream os = null;
                        try {
                            os = new BufferedOutputStream(new FileOutputStream(imageFile));
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
                            os.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        updateProfileImage(imageFile);

                        break;
                    }
                }
            }
        }
    }

    private void updateProfileImage(final File imageFile) {
        ParseUser user = ParseUser.getCurrentUser();
        BufferedInputStream in = null;
        byte[] bytes = new byte[0];
        try {
            in = new BufferedInputStream(new FileInputStream(imageFile));
            bytes = AppConfig.getBytes(in);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        final ParseFile file = new ParseFile(imageFile.getName() , bytes);
        user.put("photo", file);
        user.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    try {
                        AppConfig.userInfo.photo = file.getFile();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                    LocalBroadcastManager mBroadcaster = LocalBroadcastManager.getInstance(getContext());
                    Intent intent = new Intent();
                    intent.setAction(getResources().getString(R.string.update_profile));
                    mBroadcaster.sendBroadcast(intent);

                    Snackbar.make(btn_friends, getResources().getString(R.string.message_profile_image_changed), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else {
                    Snackbar.make(btn_friends, e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    public void getFriends() {
        final ArrayList<ParseUser> friendList = new ArrayList<ParseUser>();

        ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>("Follower");
        query1.whereEqualTo("toUser", ParseUser.getCurrentUser());

        ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Follower");
        query2.whereEqualTo("fromUser", ParseUser.getCurrentUser());

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> query3 = ParseQuery.or(queries);
        query3.include("fromUser");
        query3.include("toUser");
        query3.whereEqualTo("status", "accept");
        query3.orderByDescending("updatedAt");
        query3.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException error) {
                if (error == null) {
                    friendList.clear();
                    for (int j = 0; j < parseObjects.size(); j++) {
                        ParseObject object = parseObjects.get(j);
                        ParseUser fromUser = object.getParseUser("fromUser");
                        if (fromUser.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                            boolean is_add = true;
                            for (int i = 0; i < friendList.size(); i ++) {
                                if (friendList.get(i).getUsername().equals(object.getParseUser("toUser").getUsername())) {
                                    is_add = false;
                                    break;
                                }
                            }
                            if (is_add)
                                friendList.add(object.getParseUser("toUser"));
                        }
                        else {
                            boolean is_add = true;
                            for (int i = 0; i < friendList.size(); i ++) {
                                if (friendList.get(i).getUsername().equals(object.getParseUser("fromUser").getUsername())) {
                                    is_add = false;
                                    break;
                                }
                            }
                            if (is_add)
                                friendList.add(object.getParseUser("fromUser"));
                        }
                    }
                    AppConfig.userInfo.friendList = friendList;
                    txt_friend_badge.setText(String.format("%d", friendList.size()));
                }
            }
        });
    }
}
