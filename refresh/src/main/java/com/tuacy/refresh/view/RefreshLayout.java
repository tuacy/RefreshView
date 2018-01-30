package com.tuacy.refresh.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.widget.ListViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.ListView;
import android.widget.OverScroller;

import com.tuacy.refresh.R;

/**
 * 下拉刷新，上拉加载控件
 */

public class RefreshLayout extends ViewGroup {

	private static final String TAG = "RefreshLayout";

	/**
	 * 对应下拉刷新的View
	 */
	private View                        mDropDownRefreshView;
	/**
	 * 对应内容View,可以是任何View
	 */
	private View                        mContentView;
	/**
	 * 对应上拉加载的View
	 */
	private View                        mPullUpLoadView;
	/**
	 * 是否可以下拉刷新
	 */
	private boolean                     mDropDownRefreshEnable;
	/**
	 * 是否可以上拉加载
	 */
	private boolean                     mPullUpLoadEnable;
	/**
	 * Scroller帮助类
	 */
	private OverScroller                mScroller;
	/**
	 * 滑动速率
	 */
	private VelocityTracker             mVelocityTracker;
	/**
	 * 最大滑动速度
	 */
	private int                         mMaximumVelocity;
	/**
	 * 最小滑动速度
	 */
	private int                         mMinimumVelocity;
	/**
	 * 滑动距离
	 */
	private int                         mTouchSlop;
	/**
	 * 一些控件需要自己控制可不可以下拉和上拉
	 */
	private OnChildScrollEnableCallback mChildScrollEnableCallback;
	/**
	 * 刷新中
	 */
	private boolean                     mRefreshing;
	/**
	 * 加载中
	 */
	private boolean                     mLoading;
	/**
	 * 阻尼系数
	 */
	private float                       mOffsetRadio;
	/**
	 * 上拉下拉刷新监听
	 */
	private OnRefreshListener           mOnRefreshListener;
	/**
	 * 准备下拉刷新
	 */
	private boolean                     mReadyRefreshing;
	/**
	 * 准备上拉加载
	 */
	private boolean                     mReadyLoading;
	/**
	 * 是否self控制范围之内
	 */
	private boolean                     mInControl;

	public RefreshLayout(Context context) {
		this(context, null);
	}

	public RefreshLayout(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RefreshLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initAttrs(attrs, defStyleAttr);
		initData();
	}

	private void initAttrs(AttributeSet attrs, int defStyleAttr) {
		TypedArray typeArray = getContext().obtainStyledAttributes(attrs, R.styleable.RefreshLayout, defStyleAttr, 0);
		mDropDownRefreshEnable = typeArray.getBoolean(R.styleable.RefreshLayout_refresh_drop_down_refresh_enable, true);
		mPullUpLoadEnable = typeArray.getBoolean(R.styleable.RefreshLayout_refresh_pull_up_load_enable, true);
		mOffsetRadio = typeArray.getFloat(R.styleable.RefreshLayout_refresh_pull_offset_radio, 1.8f);
		typeArray.recycle();
	}

	private void initData() {
		//添加下拉加载对应的View
		mDropDownRefreshView = new SimpleDropDownRefreshView(getContext());
		addDropDownRefreshView(mDropDownRefreshView);
		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				//添加上拉刷新对应的View
				mPullUpLoadView = new SimplePullUpLoadView(getContext());
				addPullUpLoadView(mPullUpLoadView);
				// 移除视图树监听器
				getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});
		mScroller = new OverScroller(getContext(), new LinearInterpolator());
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		mMaximumVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
		mMinimumVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
		mRefreshing = false;
		mLoading = false;
		mReadyRefreshing = false;
		mReadyLoading = false;
	}

	/**
	 * 这里高度，我们直接用Content View 的高度
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		for (int index = 0; index < getChildCount(); index++) {
			final View child = getChildAt(index);
			final LayoutParams lp = child.getLayoutParams();
			if (index == 0 || index == 2) {
				final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
				final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(), lp.height);
				child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
			} else if (index == 1) {
				measureChild(child, widthMeasureSpec, heightMeasureSpec);
			}
		}
		if (getChildCount() > 1) {
			mContentView = getChildAt(1);
			setMeasuredDimension(mContentView.getMeasuredWidth() + getPaddingLeft() + getPaddingRight(),
								 mContentView.getMeasuredHeight() + getPaddingTop() + getPaddingBottom());
		} else {
			setMeasuredDimension(0, 0);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				if (i == 0) {
					child.layout(l, t - child.getMeasuredHeight(), r, t);
				} else if (i == 1) {
					//content view
					child.layout(l + getPaddingLeft(), t + getPaddingTop(), l + getPaddingLeft() + child.getMeasuredWidth(),
								 t + getPaddingTop() + child.getMeasuredHeight());
				} else {
					child.layout(l, b, l + child.getMeasuredWidth(), b + child.getMeasuredHeight());
				}
			}
		}
	}

	private float mInitialDownY;
	private float mInitialDownX;
	private float mPreviousY;
	private float mPreviousDispatchY;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mRefreshing || mLoading) {
			return false;
		}
		final int action = ev.getAction();
		final float currentX = ev.getRawX();
		final float currentY = ev.getRawY();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mInitialDownX = currentX;
				mInitialDownY = currentY;
				mPreviousY = mInitialDownY;
				mPreviousDispatchY = mInitialDownY;
				break;
			case MotionEvent.ACTION_MOVE:
				final float yDiff = currentY - mPreviousDispatchY;
				if ((mDropDownRefreshEnable || mPullUpLoadEnable) && getScrollY() == 0 && !mInControl && isResetMotionEventContent(yDiff)) {
					ev.setAction(MotionEvent.ACTION_CANCEL);
					MotionEvent eventDown = MotionEvent.obtain(ev);
					dispatchTouchEvent(ev);
					eventDown.setAction(MotionEvent.ACTION_DOWN);
					return onTouchEvent(eventDown);
				}
				mPreviousDispatchY = currentY;
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				break;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (!mDropDownRefreshEnable && !mPullUpLoadEnable) {
			return false;
		}
		if (mOnRefreshListener == null) {
			return false;
		}
		if (mRefreshing || mLoading) {
			return false;
		}
		boolean intercept = false;
		final int action = ev.getAction();
		final float currentX = ev.getRawX();
		final float currentY = ev.getRawY();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mInitialDownX = ev.getRawX();
				mInitialDownY = ev.getRawY();
				mPreviousY = mInitialDownY;
				break;
			case MotionEvent.ACTION_MOVE:
				if ((Math.abs(currentY - mInitialDownY) > mTouchSlop) &&
					(Math.abs(currentY - mInitialDownY) > Math.abs(currentX - mInitialDownX))) {
					if (currentY - mInitialDownY > 0) {
						// 下滑
						intercept = !canChildScrollDown();
					} else {
						// 上滑
						intercept = !canChildScrollUp();
					}
					if (intercept) {
						initVelocityTracker();
						mVelocityTracker.addMovement(ev);
					}
				}
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				recycleVelocityTracker();
				break;
		}
		return intercept;
	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mRefreshing || mLoading) {
			return false;
		}
		if (!mDropDownRefreshEnable && !mPullUpLoadEnable) {
			return false;
		}
		if (mOnRefreshListener == null) {
			return false;
		}
		initVelocityTracker();
		mVelocityTracker.addMovement(event);
		final int action = event.getAction();
		final float currentY = event.getRawY();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mInitialDownY = event.getRawY();
				mPreviousY = mInitialDownY;
				performClick();
				mInControl = true;
				return true;
			case MotionEvent.ACTION_MOVE:
				final float yDiff = currentY - mPreviousY;
				if (isResetMotionEventSelf(yDiff / mOffsetRadio)) {
					scrollTo(0, 0);
					event.setAction(MotionEvent.ACTION_DOWN);
					dispatchTouchEvent(event);
					mInControl = false;
				} else {
					offset((int) (yDiff / mOffsetRadio));
					mInControl = true;
				}
				mPreviousY = currentY;
				break;
			case MotionEvent.ACTION_CANCEL:
				recycleVelocityTracker();
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
				}
				mInControl = false;
				break;
			case MotionEvent.ACTION_UP:
				mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int velocityY = (int) mVelocityTracker.getYVelocity();
				if (Math.abs(velocityY) > mMinimumVelocity) {
					fling(-velocityY);
				}
				int scrollOffset = getScrollY();
				if (scrollOffset < 0 && mDropDownRefreshEnable && mDropDownRefreshView != null) {
					//下拉刷新
					if (Math.abs(scrollOffset) > mDropDownRefreshView.getMeasuredHeight()) {
						//释放刷新(达到了刷新的条件)
						mScroller.startScroll(0, scrollOffset, 0,
											  Math.abs(Math.abs(scrollOffset) - mDropDownRefreshView.getMeasuredHeight()));
						mReadyRefreshing = true;
						invalidate();
					} else {
						//下拉刷新(还没达到刷新的条件)
						mScroller.startScroll(0, scrollOffset, 0, -scrollOffset);
						invalidate();
					}
				}
				if (scrollOffset > 0 && mPullUpLoadEnable && mPullUpLoadView != null) {
					//上拉加载
					if (Math.abs(scrollOffset) > mPullUpLoadView.getMeasuredHeight()) {
						//释放加载更多(达到了加载更多的条件)
						mScroller.startScroll(0, scrollOffset, 0, -Math.abs(Math.abs(scrollOffset) - mPullUpLoadView.getMeasuredHeight()));
						mReadyLoading = true;
						invalidate();
					} else {
						//上拉加载更多(还没达到加载更多的条件)
						mScroller.startScroll(0, scrollOffset, 0, -scrollOffset);
						invalidate();
					}
				}
				mInControl = false;
				recycleVelocityTracker();
				break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
		updateState();
	}

	@Override
	public void scrollBy(int x, int y) {
		super.scrollBy(x, y);
		updateState();
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(0, mScroller.getCurrY());
			invalidate();
		}
	}

	private void updateState() {
		float scrollY = getScrollY();
		if (scrollY == 0) {
			if (mRefreshing) {
				mRefreshing = false;
			}
			if (mLoading) {
				mLoading = false;
			}
		} else if (scrollY < 0) {
			//refresh
			if (mDropDownRefreshEnable && mDropDownRefreshView != null && !mRefreshing) {
				IDropDownRefreshView refreshViewInterface = (IDropDownRefreshView) mDropDownRefreshView;
				if (Math.abs(scrollY) >= mDropDownRefreshView.getMeasuredHeight()) {
					refreshViewInterface.updateRefreshState(IDropDownRefreshView.RefreshState.RELEASE_REFRESH_STATE);
				} else {
					refreshViewInterface.updateRefreshState(IDropDownRefreshView.RefreshState.PULL_REFRESH_STATE);
				}
				if (Math.abs(scrollY) == mDropDownRefreshView.getMeasuredHeight() && mReadyRefreshing) {
					mReadyRefreshing = false;
					mRefreshing = true;
					refreshViewInterface.updateRefreshState(IDropDownRefreshView.RefreshState.REFRESHING_STATE);
					mOnRefreshListener.onDropDownRefresh(false);
				}
			}
		} else {
			//load
			if (mPullUpLoadEnable && mPullUpLoadView != null && !mLoading) {
				IPullUpLoadView pullUpLoadInterface = (IPullUpLoadView) mPullUpLoadView;
				if (Math.abs(scrollY) >= mPullUpLoadView.getMeasuredHeight()) {
					pullUpLoadInterface.updateLoadState(IPullUpLoadView.LoadState.RELEASE_LOAD_STATE);
				} else {
					pullUpLoadInterface.updateLoadState(IPullUpLoadView.LoadState.PULL_LOAD_STATE);
				}
				if (Math.abs(scrollY) == mDropDownRefreshView.getMeasuredHeight() && mReadyLoading) {
					mReadyLoading = false;
					mLoading = true;
					pullUpLoadInterface.updateLoadState(IPullUpLoadView.LoadState.LOADING_STATE);
					mOnRefreshListener.onPullUpLoad(false);
				}
			}
		}
	}

	private void initVelocityTracker() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
	}

	private void recycleVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	private void addDropDownRefreshView(View view) {
		if (mDropDownRefreshView != null) {
			removeView(mDropDownRefreshView);
			mDropDownRefreshView = view;
		}
		addView(mDropDownRefreshView, 0);
	}


	private void addPullUpLoadView(View view) {
		if (mPullUpLoadView != null) {
			removeView(mPullUpLoadView);
			mPullUpLoadView = view;
		}
		addView(mPullUpLoadView, 2);
	}

	/**
	 * 判断content view 是否可以下拉
	 *
	 * @return 是否可以下拉
	 */
	public boolean canChildScrollDown() {
		if (mContentView == null) {
			return true;
		}
		if (mChildScrollEnableCallback != null) {
			return mChildScrollEnableCallback.canChildDropDown(this, mContentView);
		}
		if (mContentView instanceof ListView) {
			return ListViewCompat.canScrollList((ListView) mContentView, -1);
		}
		return mContentView.canScrollVertically(-1);
	}

	/**
	 * 判断content view 是否可以上拉
	 *
	 * @return 是否可以上拉
	 */
	public boolean canChildScrollUp() {
		if (mContentView == null) {
			return true;
		}
		if (mChildScrollEnableCallback != null) {
			return mChildScrollEnableCallback.canChildPullUp(this, mContentView);
		}
		if (mContentView instanceof ListView) {
			return ListViewCompat.canScrollList((ListView) mContentView, 1);
		}
		return mContentView.canScrollVertically(1);
	}

	private void offset(int deltaY) {
		scrollBy(0, -deltaY);
	}

	private void fling(int velocityY) {
		//		mScroller.fling(0, getScrollY(), 0, velocityY, 0, 0, 0, mTopViewHeight);
		//		invalidate();
	}

	/**
	 * 是否需要干扰事件的分发流程
	 *
	 * @param yDiff 偏移距离
	 * @return 是否是需要干扰
	 */
	private boolean isResetMotionEventSelf(float yDiff) {
		if (yDiff < 0) {
			if (mDropDownRefreshEnable && getScrollY() < 0 && Math.abs(getScrollY()) < Math.abs(yDiff)) {
				return true;
			}
		} else {
			if (mPullUpLoadEnable && getScrollY() > 0 && Math.abs(getScrollY()) < Math.abs(yDiff)) {
				return true;
			}
		}
		return false;
	}

	private boolean isResetMotionEventContent(float yDiff) {
		if (yDiff <= 0) {
			//上滑动
			return !canChildScrollUp();
		} else {
			//下滑动
			return !canChildScrollDown();
		}
	}

	public void setOnChildScrollEnableCallback(OnChildScrollEnableCallback callback) {
		mChildScrollEnableCallback = callback;
	}

	public void setOnRefreshListener(OnRefreshListener listener) {
		mOnRefreshListener = listener;
	}

	/**
	 * 设置是否可以下拉刷新
	 *
	 * @param enable 是否可以下拉刷新
	 */
	public void setEnableRefres(boolean enable) {
		mDropDownRefreshEnable = enable;
	}

	/**
	 * 设置是否可以上拉加载更多
	 *
	 * @param enable 是否可以上拉加载更多
	 */
	public void setEnableLoad(boolean enable) {
		mPullUpLoadEnable = enable;
	}

	public void setRefreshComplete() {
		if (mRefreshing) {
			IDropDownRefreshView refreshViewInterface = (IDropDownRefreshView) mDropDownRefreshView;
			refreshViewInterface.updateRefreshState(IDropDownRefreshView.RefreshState.REFRESH_COMPLETE_STATE);
			mScroller.startScroll(0, getScrollY(), 0, -getScrollY());
			invalidate();
		}
	}

	public void setLoadComplete() {
		if (mLoading) {
			IPullUpLoadView pullUpLoadViewInterface = (IPullUpLoadView) mPullUpLoadView;
			pullUpLoadViewInterface.updateLoadState(IPullUpLoadView.LoadState.LOAD_COMPLETE_STATE);
			mScroller.startScroll(0, getScrollY(), 0, -getScrollY());
			invalidate();
		}
	}
}
