package com.tuacy.refresh.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.widget.ListViewCompat;
import android.util.AttributeSet;
import android.util.Log;
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
 * 1. RefreshLayout里面会限制三个子View:下拉刷新view、内容view、 加载更多view
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
	/**
	 * 没有更多数据
	 */
	private boolean                     mNoMoreData;

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
		//添加上拉加载对应的View
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
		//变量初始化
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
	 * 1. 刷新view的宽度直接用父layout的宽度。
	 * 2. 内容view正常测量。
	 * 3. 加载view的宽度直接用父layout的宽度。
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (getChildCount() < 2) {
			throw new IllegalArgumentException("*** in layout xml you should set content view ***");
		}
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		for (int index = 0; index < getChildCount(); index++) {
			final View child = getChildAt(index);
			if (index == 0 || index == 2) {
				final LayoutParams lp = child.getLayoutParams();
				final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
				final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(), lp.height);
				child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
			} else if (index == 1) {
				measureChild(child, widthMeasureSpec, heightMeasureSpec);
			}
		}
		mContentView = getChildAt(1);
		setMeasuredDimension(mContentView.getMeasuredWidth() + getPaddingLeft() + getPaddingRight(),
							 mContentView.getMeasuredHeight() + getPaddingTop() + getPaddingBottom());
	}

	/**
	 * 1. 刷新view显示在屏幕之上
	 * 2. 内容view正常显示
	 * 3. 加载view显示在屏幕之下
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				if (i == 0) {
					child.layout(0, 0 - child.getMeasuredHeight(), child.getMeasuredWidth(), 0);
				} else if (i == 1) {
					//content view
					child.layout(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + child.getMeasuredWidth(),
								 getPaddingTop() + child.getMeasuredHeight());
				} else {
					child.layout(0, getMeasuredHeight(), child.getMeasuredWidth(), getMeasuredHeight() + child.getMeasuredHeight());
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
		if (mRefreshing || mLoading || mReadyRefreshing || mReadyLoading) {
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
				if ((mDropDownRefreshEnable || mPullUpLoadEnable) && getScrollY() == 0 && !mInControl &&
					shouldResetMotionEventChild(yDiff)) {
					Log.d(TAG, "child reset");
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
				final int yDiff = (int) (currentY - mPreviousY);
				if (Math.abs(yDiff) > mTouchSlop) {
					final int yDiffAdapter = (int) (yDiff / mOffsetRadio);
					if (shouldResetMotionEventSelf(yDiffAdapter)) {
						scrollTo(0, 0);
						event.setAction(MotionEvent.ACTION_DOWN);
						dispatchTouchEvent(event);
						mInControl = false;
					} else {
						if (canOffset(yDiffAdapter)) {
							offset(yDiffAdapter);
						}
						mInControl = true;
					}
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
					IDropDownRefreshView refreshViewInterface = (IDropDownRefreshView) mDropDownRefreshView;
					if (Math.abs(scrollOffset) > refreshViewInterface.getReleaseRefreshDistance()) {
						//释放刷新(达到了刷新的条件)
						mScroller.startScroll(0, scrollOffset, 0,
											  Math.abs(Math.abs(scrollOffset) - refreshViewInterface.getReleaseRefreshDistance()));
						mReadyRefreshing = true;
						invalidate();
					} else {
						//下拉刷新(还没达到刷新的条件)
						mScroller.startScroll(0, scrollOffset, 0, -scrollOffset);
						invalidate();
					}
				}
				if (scrollOffset > 0 && mPullUpLoadEnable && mPullUpLoadView != null) {
					IPullUpLoadView pullUpLoadInterface = (IPullUpLoadView) mPullUpLoadView;
					//上拉加载
					if (mNoMoreData) {
						mScroller.startScroll(0, scrollOffset, 0, -scrollOffset);
						invalidate();
					} else {
						if (Math.abs(scrollOffset) > pullUpLoadInterface.getReleaseLoadDistance()) {
							//释放加载更多(达到了加载更多的条件)
							mScroller.startScroll(0, scrollOffset, 0,
												  -Math.abs(Math.abs(scrollOffset) - pullUpLoadInterface.getReleaseLoadDistance()));
							mReadyLoading = true;
							invalidate();
						} else {
							//上拉加载更多(还没达到加载更多的条件)
							mScroller.startScroll(0, scrollOffset, 0, -scrollOffset);
							invalidate();
						}
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
		dealScrollDistance();
	}

	@Override
	public void scrollBy(int x, int y) {
		super.scrollBy(x, y);
		dealScrollDistance();
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(0, mScroller.getCurrY());
			invalidate();
		}
	}

	private void dealScrollDistance() {
		int scrollY = getScrollY();
		if (scrollY == 0) {
			if (mRefreshing) {
				mRefreshing = false;
			}
			if (mLoading) {
				if (mNoMoreData && mPullUpLoadView != null) {
					IPullUpLoadView pullUpLoadViewInterface = (IPullUpLoadView) mPullUpLoadView;
					pullUpLoadViewInterface.updateLoadState(IPullUpLoadView.LoadState.LOAD_NO_MORE_DATA_STATE);
				}
				mLoading = false;
			}
		} else if (scrollY < 0) {
			if (mDropDownRefreshEnable && mDropDownRefreshView != null) {
				IDropDownRefreshView refreshViewInterface = (IDropDownRefreshView) mDropDownRefreshView;
				if (!mRefreshing) {
					if (Math.abs(scrollY) >= refreshViewInterface.getReleaseRefreshDistance()) {
						refreshViewInterface.updateRefreshState(IDropDownRefreshView.RefreshState.RELEASE_REFRESH_STATE);
					} else {
						refreshViewInterface.updateRefreshState(IDropDownRefreshView.RefreshState.PULL_REFRESH_STATE);
					}
					if (Math.abs(scrollY) == refreshViewInterface.getReleaseRefreshDistance() && mReadyRefreshing) {
						mReadyRefreshing = false;
						mRefreshing = true;
						refreshViewInterface.updateRefreshState(IDropDownRefreshView.RefreshState.REFRESHING_STATE);
						mOnRefreshListener.onDropDownRefresh(false);
					}
				}
				refreshViewInterface.updateDropDownDistance(Math.abs(scrollY));
			}
		} else {
			//load
			if (mPullUpLoadEnable && mPullUpLoadView != null && !mLoading) {
				IPullUpLoadView pullUpLoadInterface = (IPullUpLoadView) mPullUpLoadView;
				if (!mNoMoreData) {
					if (Math.abs(scrollY) >= pullUpLoadInterface.getReleaseLoadDistance()) {
						pullUpLoadInterface.updateLoadState(IPullUpLoadView.LoadState.RELEASE_LOAD_STATE);
					} else {
						pullUpLoadInterface.updateLoadState(IPullUpLoadView.LoadState.PULL_LOAD_STATE);
					}
					if (Math.abs(scrollY) == pullUpLoadInterface.getReleaseLoadDistance() && mReadyLoading) {
						mReadyLoading = false;
						mLoading = true;
						pullUpLoadInterface.updateLoadState(IPullUpLoadView.LoadState.LOADING_STATE);
						mOnRefreshListener.onPullUpLoad(false);
					}
				}
				pullUpLoadInterface.updatePullUpDistance(Math.abs(scrollY));
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

	/**
	 * 添加下拉刷新view，child index = 0
	 *
	 * @param view 下拉刷新view
	 */
	private void addDropDownRefreshView(View view) {
		if (mDropDownRefreshView != null) {
			removeView(mDropDownRefreshView);
			mDropDownRefreshView = view;
		}
		addView(mDropDownRefreshView, 0);
	}

	/**
	 * 添加上拉加载view，child index = 2
	 *
	 * @param view 上拉加载view
	 */
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
	private boolean canChildScrollUp() {
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

	private boolean canOffset(int deltaY) {
		if (deltaY >= 0 && !mDropDownRefreshEnable && getScrollY() == 0) {
			return false;
		}

		if (deltaY <= 0 && !mPullUpLoadEnable && getScrollY() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * 这里要判断是否支持上拉下拉刷新
	 *
	 * @param deltaY offset distance
	 */
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
	private boolean shouldResetMotionEventSelf(float yDiff) {

		if (!mDropDownRefreshEnable && getScrollY() == 0 && yDiff >= 0) {
			return true;
		}

		if (!mPullUpLoadEnable) {

		}

		if (yDiff < 0) {
			if (mDropDownRefreshEnable && getScrollY() < 0 && Math.abs(getScrollY()) <= Math.abs(yDiff)) {
				return true;
			}
		} else {
			if (mPullUpLoadEnable && getScrollY() > 0 && Math.abs(getScrollY()) <= Math.abs(yDiff)) {
				return true;
			}
		}
		return false;
	}

	private boolean shouldResetMotionEventChild(float yDiff) {
		Log.d(TAG, "yDiff = " + yDiff);
		//不允许上拉和下拉
		if (yDiff <= 0) {
			Log.d(TAG, "canChildScrollUp = " + canChildScrollUp());
			//上滑动
			return !canChildScrollUp();
		} else {
			//下滑动
			Log.d(TAG, "canChildScrollDown = " + canChildScrollDown());
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
	public void setEnableRefresh(boolean enable) {
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

	/**
	 * 设置刷新完成
	 */
	public void setRefreshComplete() {
		if (mDropDownRefreshEnable && mRefreshing) {
			IDropDownRefreshView refreshViewInterface = (IDropDownRefreshView) mDropDownRefreshView;
			refreshViewInterface.updateRefreshState(IDropDownRefreshView.RefreshState.REFRESH_COMPLETE_STATE);
			mScroller.startScroll(0, getScrollY(), 0, -getScrollY());
			invalidate();
		}
	}

	public void setLoadComplete() {
		setLoadComplete(false);
	}

	/**
	 * 设置加载完成
	 *
	 * @param noMoreData 是否还有更多数据
	 */
	public void setLoadComplete(boolean noMoreData) {
		if (mPullUpLoadEnable) {
			mNoMoreData = noMoreData;
			if (mLoading) {
				IPullUpLoadView pullUpLoadViewInterface = (IPullUpLoadView) mPullUpLoadView;
				pullUpLoadViewInterface.updateLoadState(IPullUpLoadView.LoadState.LOAD_COMPLETE_STATE);
				mScroller.startScroll(0, getScrollY(), 0, -getScrollY());
				invalidate();
			} else {
				IPullUpLoadView pullUpLoadViewInterface = (IPullUpLoadView) mPullUpLoadView;
				pullUpLoadViewInterface.updateLoadState(IPullUpLoadView.LoadState.LOAD_NO_MORE_DATA_STATE);
			}
		}
	}

	/**
	 * 设置是否还有更多数据
	 *
	 * @param noMoreData 是否有更多数据
	 */
	public void setNoMoreData(boolean noMoreData) {
		mNoMoreData = noMoreData;
	}

}
