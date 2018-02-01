package com.tuacy.refresh.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.widget.ListViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
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
	 * 是否self在处理事件
	 */
	private boolean                     mInSelfControl;
	/**
	 * dispatchTouchEvent()函数是否可以切断事件的传递
	 */
	private boolean                     mDispatchCanCutMotionEvent;
	/**
	 * 没有更多数据
	 */
	private boolean                     mNoMoreData;
	/**
	 * Layout是否初始化完成
	 */
	private boolean                     mLayoutFinished;

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
		mLayoutFinished = false;
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
				mLayoutFinished = true;
				// 移除视图树监听器
				getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});
		//变量初始化
		mScroller = new OverScroller(getContext(), new LinearInterpolator());
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
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
	private float mPreviousTouchY;
	private float mPreviousDispatchY;
	private float mPreviousInterceptY;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mRefreshing || mLoading || mReadyRefreshing || mReadyLoading) {
			return false;
		}
		final int action = ev.getAction();
		final float currentY = ev.getRawY();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mInitialDownY = currentY;
				mPreviousTouchY = mInitialDownY;
				mPreviousDispatchY = mInitialDownY;
				mInSelfControl = false;
				break;
			case MotionEvent.ACTION_MOVE:
				final float yDiff = currentY - mPreviousDispatchY;
				if (!mDispatchCanCutMotionEvent && shouldDispatchCutMotionEvent(yDiff)) {
					//先发送ACTION_CANCEL
					ev.setAction(MotionEvent.ACTION_CANCEL);
					MotionEvent eventDown = MotionEvent.obtain(ev);
					dispatchTouchEvent(ev);
					//再发送ACTION_DOWN，重新开始一个新的事件
					mDispatchCanCutMotionEvent = true;
					eventDown.setAction(MotionEvent.ACTION_DOWN);
					return dispatchTouchEvent(eventDown);
				}
				mPreviousDispatchY = currentY;
				break;
			case MotionEvent.ACTION_CANCEL:
				mInSelfControl = false;
				break;
			case MotionEvent.ACTION_UP:
				mInSelfControl = false;
				mDispatchCanCutMotionEvent = false;
				break;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		//不支持下拉刷新也不支持上拉加载
		if (!mDropDownRefreshEnable && !mPullUpLoadEnable) {
			return false;
		}
		//没有设置listener
		if (mOnRefreshListener == null) {
			return false;
		}
		//正在加载中或者刷新中
		if (mRefreshing || mLoading) {
			return false;
		}
		//想把事件切断，重新一个新的事件
//		if (mDispatchCanCutMotionEvent && ev.getAction() == MotionEvent.ACTION_DOWN) {
//			return true;
//		}
		boolean intercept = false;
		final int action = ev.getAction();
		final float currentY = ev.getRawY();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mInitialDownY = ev.getRawY();
				mPreviousTouchY = mInitialDownY;
				mPreviousInterceptY = mInitialDownY;
				return false;
			case MotionEvent.ACTION_MOVE:
				final float diffY = currentY - mPreviousInterceptY;
//				if ((Math.abs(diffY) >= mTouchSlop)) {
					intercept = eventShouldIntercept(diffY);
//				}
				mPreviousInterceptY = currentY;
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				mInSelfControl = false;
				mDispatchCanCutMotionEvent = false;
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
		//刷新中或者下拉加载中，事件不处理
		if (mRefreshing || mLoading) {
			return false;
		}
		//不支持下拉刷新也不支持上拉加载，事件不处理
		if (!mDropDownRefreshEnable && !mPullUpLoadEnable) {
			return false;
		}
		//没有设置监听，事件不处理
		if (mOnRefreshListener == null) {
			return false;
		}
		final int action = event.getAction();
		final float currentY = event.getRawY();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mInitialDownY = event.getRawY();
				mPreviousTouchY = mInitialDownY;
				performClick();
				return true;
			case MotionEvent.ACTION_MOVE:
				final int yDiff = (int) (currentY - mPreviousTouchY);
				final int yDiffConvert = (int) (yDiff / mOffsetRadio);
				if (mInSelfControl || canBeginOffset(yDiff)) {
					if (shouldTouchCutMotionEvent(yDiffConvert)) {
						//先恢复位置
						scrollTo(0, 0);
						//送出ACTION_CANCEL事件
						MotionEvent touchEvent = MotionEvent.obtain(event);
						touchEvent.setAction(MotionEvent.ACTION_CANCEL);
						onTouchEvent(touchEvent);
						//在送出ACTION_DOWN事件
						MotionEvent dispatchEvent = MotionEvent.obtain(event);
						dispatchEvent.setAction(MotionEvent.ACTION_DOWN);
						dispatchTouchEvent(dispatchEvent);
					} else {
						mInSelfControl = true;
						offset(yDiffConvert);
					}
					mDispatchCanCutMotionEvent = false;
					mPreviousTouchY = currentY;
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
				}
				mInSelfControl = false;
				break;
			case MotionEvent.ACTION_UP:
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
				mInSelfControl = false;
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

	/**
	 * 判断是否可以开始滑动
	 */
	private boolean canBeginOffset(int deltaY) {
		if (Math.abs(deltaY) < mTouchSlop) {
			return false;
		}
		if (mDropDownRefreshEnable || mPullUpLoadEnable) {
			if (!mDropDownRefreshEnable) {
				//只支持上拉加载
				if (getScrollY() == 0) {
					//本来没有offset，还想往下拉，不行
					return !(deltaY > 0);
				}
				return deltaY < 0 || getScrollY() <= 0 && !canChildScrollDown();
			} else if (!mPullUpLoadEnable) {
				//只支持下来刷新
				if (getScrollY() == 0) {
					//本来没有offset，还想往上拉，不行
					return !(deltaY < 0);
				}
				return deltaY > 0 || getScrollY() >= 0 && !canChildScrollUp();
			} else {
				//下拉刷新，上拉加载
				return true;
			}
		}
		return false;
	}

	/**
	 * 这里要判断是否支持上拉下拉刷新
	 *
	 * @param deltaY offset distance
	 */
	private void offset(int deltaY) {
		scrollBy(0, -deltaY);
	}

	/**
	 * 判断事件是否需要拦截
	 *
	 * @param yDiff 偏移距离
	 * @return 是否需要拦截
	 */
	private boolean eventShouldIntercept(float yDiff) {
		if (mDropDownRefreshEnable || mPullUpLoadEnable) {
			if (!mDropDownRefreshEnable) {
				//只允许上拉加载
				//				if (getScrollY() == 0) {
				return yDiff < 0 && !canChildScrollUp();
				//				}
			} else if (!mPullUpLoadEnable) {
				//只允许下拉刷新
				//				if (getScrollY() == 0) {
				return yDiff > 0 && !canChildScrollDown();
				//				}
			} else {
				//上拉刷新，下拉加载
				return (yDiff > 0 && !canChildScrollDown()) || (yDiff < 0 && !canChildScrollUp());
				//				if (yDiff > 0) {
				//					// 下滑
				//					return !canChildScrollDown();
				//				} else {
				//					// 上滑
				//					return !canChildScrollUp();
				//				}
			}

		}
		return false;
	}

	/**
	 * 是否需要干扰事件的分发流程，事件自己的处理的时候，有的时候需要过渡到content view里面去
	 * 1. 比如开始下拉刷新的view显示了，这个时候继续往上滑动需要把事件过渡到content view 里面去
	 *
	 * @param yDiff 偏移距离
	 * @return 是否是需要干扰
	 */
	private boolean shouldTouchCutMotionEvent(float yDiff) {

		if (!mInSelfControl) {
			return false;
		}
		if (mPullUpLoadEnable || mDropDownRefreshEnable) {
			if (yDiff < 0) {
				//下拉刷新view显示的时候，触摸事件继续往上滑动，当滑动的距离大于scroll y的距离的时候，需要把事件过渡到content view里面去了。
				if (mDropDownRefreshEnable && getScrollY() <= 0 && Math.abs(getScrollY()) <= Math.abs(yDiff)) {
					return true;
				}
			} else {
				//上拉加载view显示的是，触摸事件继续往下滑动，当滑动的距离大于scroll y的距离的时候，需要把事件过渡到content view里面去了。
				if (mPullUpLoadEnable && getScrollY() >= 0 && Math.abs(getScrollY()) <= Math.abs(yDiff)) {
					return true;
				}
			}
			return false;
		}
		return false;
	}

	/**
	 * 当在MotionEvent事件先在content view(ListView、RecyclerView)上下滑动的时候，有的时候需要干扰事件的传递。分两种情况:
	 * 1. 当滑动到content view的顶部的时候还要需要往下来的时候。
	 * 2. 当滑动到content view的底部的时候还要继续往上拉的时候。
	 * 这两种情况只能在dispatchTouchEvent函数中处理才有效果
	 *
	 * @param yDiff 偏移距离
	 */
	private boolean shouldDispatchCutMotionEvent(float yDiff) {
		//上拉下拉都不支持
		if (!mDropDownRefreshEnable && !mPullUpLoadEnable) {
			return false;
		}
		/**
		 * 如果当前事件还在自己的控制范围之内
		 */
		if (mInSelfControl) {
			return false;
		}
		//getScrollY() != 0 也不用判断
		if (getScrollY() != 0) {
			return false;
		}
		if (/*Math.abs(yDiff) > mTouchSlop && */(mDropDownRefreshEnable || mPullUpLoadEnable)) {
			if (!mDropDownRefreshEnable) {
				//只允许上拉加载
				return yDiff < 0 && !canChildScrollUp();
			} else if (!mPullUpLoadEnable) {
				//只允许下拉刷新
				return yDiff > 0 && !canChildScrollDown();
			} else {
				//即允许下拉刷新，又允许上拉加载
				return (yDiff < 0 && !canChildScrollUp()) || (yDiff > 0 && !canChildScrollDown());
				//				if (yDiff < 0) {
				//					//上滑动
				//					return !canChildScrollUp();
				//				} else {
				//					//下滑动
				//					return !canChildScrollDown();
				//				}
			}
		}
		return false;
	}

	/**
	 * 自定义是否可以上拉和下拉，会在下拉刷新view,上拉加载view显示的时候调用判断
	 *
	 * @param callback OnChildScrollEnableCallback
	 */
	public void setOnChildScrollEnableCallback(OnChildScrollEnableCallback callback) {
		mChildScrollEnableCallback = callback;
	}

	/**
	 * 设置下拉刷新和上拉加载的监听
	 *
	 * @param listener OnRefreshListener
	 */
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

	/**
	 * 自动刷新(下拉刷新)
	 */
	public void startAutoRefresh() {
		if (!mDropDownRefreshEnable) {
			//不允许下拉刷新，直接返回
			return;
		}
		if (mDropDownRefreshView != null && !mRefreshing) {
			if (!mLayoutFinished) {
				post(new Runnable() {
					@Override
					public void run() {
						IDropDownRefreshView refreshViewInterface = (IDropDownRefreshView) mDropDownRefreshView;
						mScroller.startScroll(0, getScrollY(), 0, getScrollY() - refreshViewInterface.getReleaseRefreshDistance());
						invalidate();
						mReadyRefreshing = true;
					}
				});

			} else {
				IDropDownRefreshView refreshViewInterface = (IDropDownRefreshView) mDropDownRefreshView;
				mScroller.startScroll(0, getScrollY(), 0, getScrollY() - refreshViewInterface.getReleaseRefreshDistance());
				invalidate();
				mReadyRefreshing = true;
			}
		}
	}

}
