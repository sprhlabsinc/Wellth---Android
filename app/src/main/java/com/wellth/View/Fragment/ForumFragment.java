package com.wellth.View.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

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
import com.squareup.picasso.Picasso;
import com.wellth.Model.AppConfig;
import com.wellth.R;
import com.wellth.View.Activity.AnswerActivity;
import com.wellth.View.Activity.AskQuestionActivity;
import com.wellth.View.Activity.DetailActivity;
import com.wellth.View.Adapter.QuestionAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import agency.tango.android.avatarview.AvatarPlaceholder;
import agency.tango.android.avatarview.views.AvatarView;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

import static com.wellth.Model.AppConfig.GET_MAX_POST_COUNT;
import static com.wellth.Model.AppConfig.MAX_PAGE_COUNT;

public class ForumFragment extends Fragment implements ObservableScrollViewCallbacks {

    private RelativeLayout layout_top;
    private ObservableListView list_post;
    private QuestionAdapter listAdapter;
    private PtrClassicFrameLayout ptrFrame;
    private TouchInterceptionFrameLayout mInterceptionLayout;

    private AvatarView img_avatar;
    private AvatarPlaceholder placeholder;
    private EditText edit_search;
    private ImageButton btn_search;
    private RelativeLayout layout_ask_question;

    private boolean mScrolled;
    private ScrollState mLastScrollState;
    private int pageCount = 0;
    private ArrayList<ParseObject> postList = new ArrayList<ParseObject>();
    private boolean isLoading = false;
    private Date currentDate;
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
            if (type.equals("forum")) {
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
                    listAdapter.notifyDataSetChanged();
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
    }

    public static ForumFragment newInstance() {
        ForumFragment fragment = new ForumFragment();
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
        View view =  inflater.inflate(R.layout.fragment_forum, container, false);
        footerView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_footer, null, false);
        img_avatar = (AvatarView) view.findViewById(R.id.img_avatar);
        updateStatus();
        edit_search = (EditText) view.findViewById(R.id.edit_search);

        btn_search = (ImageButton) view.findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageCount = 0;
                isLoading = false;
                Calendar cal = new GregorianCalendar();
                currentDate = cal.getTime();
                getPosts();
            }
        });

        layout_ask_question = (RelativeLayout) view.findViewById(R.id.layout_ask_question);
        layout_ask_question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AskQuestionActivity.class);
                intent.putExtra("type", "forum");
                startActivity(intent);
            }
        });

        layout_top = (RelativeLayout) view.findViewById(R.id.layout_top);
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
                        getPosts();     //loading more data
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        listAdapter = new QuestionAdapter(postList, getContext());
        list_post.setAdapter(listAdapter);

        Calendar cal = new GregorianCalendar();
        currentDate = cal.getTime();

        getPosts();

        ptrFrame = (PtrClassicFrameLayout) view.findViewById(R.id.rotate_header_list_view_frame);
        ptrFrame.setLastUpdateTimeRelateObject(this);
        ptrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                pageCount = 0;
                isLoading = false;
                Calendar cal = new GregorianCalendar();
                currentDate = cal.getTime();

                getPosts();
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
        int mapHeight = getResources().getDimensionPixelSize(R.dimen.default_forum_header_height);
        mInterceptionLayout.setPadding(0,mapHeight,0,0);
        mInterceptionLayout.setScrollInterceptionListener(mInterceptionListener);

        return view;
    }

    public void getPosts() {
        if (isLoading == true) return;
        isLoading = true;
        list_post.addFooterView(footerView);
        String searchString = edit_search.getText().toString().trim();

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Post");
        query.include("fromUser");
        query.include("comments.fromUser");
        query.include("likes.fromUser");
        query.whereLessThanOrEqualTo("createdAt", currentDate);
        query.orderByDescending("createdAt");
        query.setSkip(GET_MAX_POST_COUNT * pageCount);
        query.setLimit(GET_MAX_POST_COUNT);
        query.whereMatches("content", searchString, "i");
        query.whereEqualTo("type", "forum");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> posts, ParseException e) {
                if (posts != null) {
                    if (pageCount == 0) {
                        postList.clear();
                    }

                    postList.addAll(posts);
                    listAdapter.notifyDataSetChanged();
                    ptrFrame.refreshComplete();

                    pageCount ++;
                    if (posts.size() != 0) {
                        isLoading = false;
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
}
