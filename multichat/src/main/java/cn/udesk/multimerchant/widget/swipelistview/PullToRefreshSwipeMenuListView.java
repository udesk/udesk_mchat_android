package cn.udesk.multimerchant.widget.swipelistview;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import cn.udesk.multimerchant.R;


public class PullToRefreshSwipeMenuListView extends ListView implements OnScrollListener {

    private static final int TOUCH_STATE_NONE = 0;
    private static final int TOUCH_STATE_X = 1;
    private static final int TOUCH_STATE_Y = 2;

    private int MAX_Y = 5;
    private int MAX_X = 3;
    private float mDownX;
    private float mDownY;
    private int mTouchState;
    private int mTouchPosition;
    private SwipeMenuLayout mTouchView;
    private OnSwipeListener mOnSwipeListener;

    private SwipeMenuCreator mMenuCreator;
    private OnMenuItemClickListener mOnMenuItemClickListener;
    private Interpolator mCloseInterpolator;
    private Interpolator mOpenInterpolator;

    private float mLastY = -1; // save event y
    private Scroller mScroller; // used for scroll back
    private OnScrollListener mScrollListener; // user's scroll listener

    // the interface to trigger refresh and load more.
    private IXListViewListener mListViewListener;

    // -- header view
    private PullToRefreshListHeader mHeaderView;
    // header view content, use it to calculate the Header's height. And hide it
    // when disable pull refresh.
    private RelativeLayout mHeaderViewContent;
    private int mHeaderViewHeight; // header view's height
    private boolean mEnablePullRefresh = true;
    private boolean mPullRefreshing = false; // is refreashing.

    private int mScrollBack;
    private final static int SCROLLBACK_HEADER = 0;
    private final static int SCROLLBACK_FOOTER = 1;

    private final static int SCROLL_DURATION = 400;
    private final static int PULL_LOAD_MORE_DELTA = 50;
    private final static float OFFSET_RADIO = 1.8f;

    public PullToRefreshSwipeMenuListView(Context context) {
        super(context);
        init(context);
    }

    public PullToRefreshSwipeMenuListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public PullToRefreshSwipeMenuListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        try {
            mScroller = new Scroller(context, new DecelerateInterpolator());
            super.setOnScrollListener(this);
            // init header view
            mHeaderView = new PullToRefreshListHeader(context);
            mHeaderViewContent = (RelativeLayout) mHeaderView.findViewById(R.id.xlistview_header_content);
            addHeaderView(mHeaderView);
            // init header height
            mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mHeaderViewHeight = mHeaderViewContent.getHeight();
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
            MAX_X = dp2px(MAX_X);
            MAX_Y = dp2px(MAX_Y);
            mTouchState = TOUCH_STATE_NONE;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(new SwipeMenuAdapter(getContext(), adapter) {
            @Override
            public void createMenu(SwipeMenu menu) {
                if (mMenuCreator != null) {
                    mMenuCreator.create(menu);
                }
            }

            @Override
            public void onItemClick(SwipeMenuView view, SwipeMenu menu, int index) {
                if (mOnMenuItemClickListener != null) {
                    mOnMenuItemClickListener.onMenuItemClick(view.getPosition(), menu, index);
                }
                if (mTouchView != null) {
                    mTouchView.smoothCloseMenu();
                }
            }
        });
    }

    public void setCloseInterpolator(Interpolator interpolator) {
        mCloseInterpolator = interpolator;
    }

    public void setOpenInterpolator(Interpolator interpolator) {
        mOpenInterpolator = interpolator;
    }

    public Interpolator getOpenInterpolator() {
        return mOpenInterpolator;
    }

    public Interpolator getCloseInterpolator() {
        return mCloseInterpolator;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }

        try {
            switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();

                int oldPos = mTouchPosition;
                mDownX = ev.getX();
                mDownY = ev.getY();
                mTouchState = TOUCH_STATE_NONE;

                mTouchPosition = pointToPosition((int) ev.getX(), (int) ev.getY());

                if (mTouchPosition == oldPos && mTouchView != null && mTouchView.isOpen()) {
                    mTouchState = TOUCH_STATE_X;
                    mTouchView.onSwipe(ev);
                    return true;
                }

                View view = getChildAt(mTouchPosition - getFirstVisiblePosition());

                if (mTouchView != null && mTouchView.isOpen()) {
                    mTouchView.smoothCloseMenu();
                    mTouchView = null;
                    return super.onTouchEvent(ev);
                }
                if (view instanceof SwipeMenuLayout) {
                    mTouchView = (SwipeMenuLayout) view;
                }
                if (mTouchView != null) {
                    mTouchView.onSwipe(ev);
                }

                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;

                float dy = Math.abs((ev.getY() - mDownY));
                float dx = Math.abs((ev.getX() - mDownX));
                mLastY = ev.getRawY();

                if ((mTouchView == null || !mTouchView.isActive()) && Math.pow(dx, 2) / Math.pow(dy, 2) <= 3) {
                    if (getFirstVisiblePosition() == 0 && (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
                        // the first item is showing, header has shown or pull down.
                        updateHeaderHeight(deltaY / OFFSET_RADIO);
                        invokeOnScrolling();
                    }
                }

                if (mTouchState == TOUCH_STATE_X) {
                    if (mTouchView != null) {
                        mTouchView.onSwipe(ev);
                    }
                    getSelector().setState(new int[] { 0 });
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(ev);
                    return true;
                } else if (mTouchState == TOUCH_STATE_NONE) {
                    if (Math.abs(dy) > MAX_Y) {
                        mTouchState = TOUCH_STATE_Y;
                    } else if (dx > MAX_X) {
                        mTouchState = TOUCH_STATE_X;
                        if (mOnSwipeListener != null) {
                            mOnSwipeListener.onSwipeStart(mTouchPosition);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mLastY = -1; // reset
                if (getFirstVisiblePosition() == 0) {
                    // invoke refresh
                    if (mEnablePullRefresh && mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
                        mPullRefreshing = true;
                        mHeaderView.setState(PullToRefreshListHeader.STATE_REFRESHING);
                        if (mListViewListener != null) {
                            mListViewListener.onRefresh();
                        }
                    }
                    resetHeaderHeight();
                }

                if (mTouchState == TOUCH_STATE_X) {
                    if (mTouchView != null) {
                        mTouchView.onSwipe(ev);
                        if (!mTouchView.isOpen()) {
                            mTouchPosition = -1;
                            mTouchView = null;
                        }
                    }
                    if (mOnSwipeListener != null) {
                        mOnSwipeListener.onSwipeEnd(mTouchPosition);
                    }
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(ev);
                    return true;
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onTouchEvent(ev);
    }

    class ResetHeaderHeightTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            try {
                mPullRefreshing = false;
                mHeaderView.setState(PullToRefreshListHeader.STATE_NORMAL);
                resetHeaderHeight();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void smoothOpenMenu(int position) {
        try {
            if (position >= getFirstVisiblePosition() && position <= getLastVisiblePosition()) {
                View view = getChildAt(position - getFirstVisiblePosition());
                if (view instanceof SwipeMenuLayout) {
                    mTouchPosition = position;
                    if (mTouchView != null && mTouchView.isOpen()) {
                        mTouchView.smoothCloseMenu();
                    }
                    mTouchView = (SwipeMenuLayout) view;
                    mTouchView.smoothOpenMenu();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources()
                .getDisplayMetrics());
    }

    public void setMenuCreator(SwipeMenuCreator menuCreator) {
        this.mMenuCreator = menuCreator;
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        this.mOnMenuItemClickListener = onMenuItemClickListener;
    }

    public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
        this.mOnSwipeListener = onSwipeListener;
    }

    public  interface OnMenuItemClickListener {
        void onMenuItemClick(int position, SwipeMenu menu, int index);
    }

    public  interface OnSwipeListener {
        void onSwipeStart(int position);

        void onSwipeEnd(int position);
    }

    public void setPullRefreshEnable(boolean enable) {
        try {
            mEnablePullRefresh = enable;
            if (!mEnablePullRefresh) { // disable, hide the content
                mHeaderViewContent.setVisibility(View.INVISIBLE);
            } else {
                mHeaderViewContent.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * stop refresh, reset header view.
     */
    public void stopRefresh(boolean success) {
        try {
            if (mPullRefreshing == true) {
                mPullRefreshing = false;
                if(success){
                     mHeaderView.setState(PullToRefreshListHeader.STATE_Success);
                }else{
                     mHeaderView.setState(PullToRefreshListHeader.STATE_Failure);
                }

                postDelayed(new Runnable() {

                    @Override
                    public void run() {
                           resetHeaderHeight();
                    }
                }, 500);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void invokeOnScrolling() {
        try {
            if (mScrollListener instanceof OnXScrollListener) {
                OnXScrollListener l = (OnXScrollListener) mScrollListener;
                l.onXScrolling(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateHeaderHeight(float delta) {

        try {
            mHeaderView.setVisiableHeight((int) delta + mHeaderView.getVisiableHeight());
            if (mEnablePullRefresh && !mPullRefreshing) {
                if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
                    mHeaderView.setState(PullToRefreshListHeader.STATE_READY);
                } else {
                    mHeaderView.setState(PullToRefreshListHeader.STATE_NORMAL);
                }
            }
            setSelection(0); // scroll to top each time
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * reset header view's height.
     */
    private void resetHeaderHeight() {
        try {
            int height = mHeaderView.getVisiableHeight();
            if (height == 0) // not visible.
                return;
            // refreshing and header isn't shown fully. do nothing.
            if (mPullRefreshing && height <= mHeaderViewHeight) {
                return;
            }
            int finalHeight = 0; // default: scroll back to dismiss header.
            // is refreshing, just scroll back to show all the header.
            if (mPullRefreshing && height > mHeaderViewHeight) {
                finalHeight = mHeaderViewHeight;
            }
            mScrollBack = SCROLLBACK_HEADER;
            mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);
            // trigger computeScroll
            invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void computeScroll() {
        try {
            if (mScroller.computeScrollOffset()) {
                if (mScrollBack == SCROLLBACK_HEADER) {
                    mHeaderView.setVisiableHeight(mScroller.getCurrY());
                }
                postInvalidate();
                invokeOnScrolling();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.computeScroll();
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mScrollListener = l;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mScrollListener != null) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // send to user's listener
        if (mScrollListener != null) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public void setXListViewListener(IXListViewListener l) {
        mListViewListener = l;
    }

    /**
     * you can listen ListView.OnScrollListener or this one. it will invoke
     * onXScrolling when header/footer scroll back.
     */
    public interface OnXScrollListener extends OnScrollListener {
         void onXScrolling(View view);
    }

    /**
     * implements this interface to get refresh/load more event.
     */
    public interface IXListViewListener {
         void onRefresh();
    }
}
