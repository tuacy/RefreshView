package com.tuacy.refresh.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.widget.ListViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.ListView;
import android.widget.Scroller;

import com.tuacy.refresh.R;

/**
 * 下拉刷新，上拉加载控件
 */

public class RefreshLayout extends ViewGroup {

	/**
	 * 对应下拉刷新的View
	 */
	private View                        mDropDownRefreshView;
	/**
	 * 下拉刷新View高度
	 */
	private int                         mDropDownViewHeight;
	/**
	 * 对应内容View,可以是任何View
	 */
	private View                        mContentView;
	/**
	 * 对应上拉加载的View
	 */
	private View                        mPullUpLoadView;
	/**
	 * 上拉加载View高度
	 */
	private int                         mPullUpViewHeight;
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
	private Scroller                    mScroller;
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
	private float                       mOffsetRadio;

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
		mDropDownRefreshEnable = typeArray.getBoolean(R.styleable.RefreshLayout_refresh_drop_down_refresh_enable, false);
		mPullUpLoadEnable = typeArray.getBoolean(R.styleable.RefreshLayout_refresh_pull_up_load_enable, false);
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
		mScroller = new Scroller(getContext(), new LinearInterpolator());
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		mRefreshing = false;
		mLoading = false;
	}

	/**
	 * 这里高度，我们直接用Content View 的高度
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		if (mDropDownRefreshView != null) {
			mDropDownViewHeight = mDropDownRefreshView.getMeasuredHeight();
		}
		if (mPullUpLoadView != null) {
			mPullUpViewHeight = mPullUpLoadView.getMeasuredHeight();
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
					//drop down refresh view 把drop down view 放到屏幕顶部外面去
					child.layout(0, 0 - child.getMeasuredHeight(), child.getMeasuredWidth(), 0);
				} else if (i == 1) {
					//content view
					child.layout(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + child.getMeasuredWidth(),
								 getPaddingTop() + child.getMeasuredHeight());
				} else {
					//pull up view 把pull up view 放到屏幕顶部外面去
					child.layout(0, getMeasuredHeight(), child.getMeasuredWidth(), getMeasuredHeight() + child.getMeasuredHeight());
				}
			}
		}
	}

	private float mInitialDownY;
	private float mInitialDownX;
	private float mPreviousY;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mInitialDownX = ev.getRawX();
				mInitialDownY = ev.getRawY();
				mPreviousY = mInitialDownY;
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				break;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mRefreshing || mLoading || canChildScrollEnable()) {
			return super.onInterceptTouchEvent(ev);
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
					intercept = true;
				}
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				break;
		}
		return intercept || super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handle = false;
		final int action = event.getAction();
		final float currentY = event.getRawY();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mInitialDownY = event.getRawY();
				mPreviousY = mInitialDownY;
				performClick();
				handle = true;
				break;
			case MotionEvent.ACTION_MOVE:
				final float yDiff = currentY - mPreviousY;
				offset((int) yDiff);
				handle = true;
				mPreviousY = currentY;
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				break;
		}
		return handle || super.onTouchEvent(event);
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
	 * 判断content view 是否可以下拉和上拉
	 *
	 * @return 是否可以下拉和上拉
	 */
	public boolean canChildScrollEnable() {
		if (mContentView == null) {
			return true;
		}
		if (mChildScrollEnableCallback != null) {
			return mChildScrollEnableCallback.canChildDropdown(this, mContentView) ||
				   mChildScrollEnableCallback.canChildPullUp(this, mContentView);
		}
		if (mContentView instanceof ListView) {
			return ListViewCompat.canScrollList((ListView) mContentView, -1) || ListViewCompat.canScrollList((ListView) mContentView, 1);
		}
		return mContentView.canScrollVertically(-1);
	}

	private void offset(int deltaY) {
		if (mDropDownRefreshView != null) {
			mDropDownRefreshView.offsetTopAndBottom(deltaY);
		}
		if (mContentView != null) {
			mContentView.offsetTopAndBottom(deltaY);
		}
		if (mPullUpLoadView != null) {
			mPullUpLoadView.offsetTopAndBottom(deltaY);
		}
	}

	public void setOnChildScrollEnableCallback(OnChildScrollEnableCallback callback) {
		mChildScrollEnableCallback = callback;
	}
}
