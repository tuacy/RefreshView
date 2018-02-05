package com.tuacy.refreshview.module.recycler;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tuacy.refresh.view.IDropDownRefreshView;
import com.tuacy.refreshview.R;

import static com.tuacy.refresh.view.IDropDownRefreshView.RefreshState.PULL_REFRESH_STATE;

public class RecyclerRefreshView extends FrameLayout implements IDropDownRefreshView {

	private ImageView       mImageDropDown;
	private ImageView       mImageRefreshing;
	private LinearLayout    mLayoutRefreshing;
	private LinearLayout    mLayoutRefreshComplete;
	private RotateAnimation mRotationRefresh;
	private RefreshState mCurrentState;

	public RecyclerRefreshView(@NonNull Context context) {
		this(context, null);
	}

	public RecyclerRefreshView(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RecyclerRefreshView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.layout_recycler_refresh, this, true);
		mImageDropDown = findViewById(R.id.image_drop_down_flag);
		mImageRefreshing = findViewById(R.id.image_loading_flag);
		mLayoutRefreshing = findViewById(R.id.linear_loading);
		mLayoutRefreshComplete = findViewById(R.id.linear_complete);
		mRotationRefresh = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mRotationRefresh.setDuration(300);
		mRotationRefresh.setRepeatCount(-1);
		mRotationRefresh.setFillAfter(true);
	}

	@Override
	public int getReleaseRefreshDistance() {
		return getMeasuredHeight();
	}

	@Override
	public void updateRefreshState(RefreshState state) {
		if (mCurrentState == state) {
			return;
		}
		switch (state) {
			case INITIAL_STATE:
				mImageDropDown.setVisibility(GONE);
				mLayoutRefreshing.setVisibility(GONE);
				mLayoutRefreshComplete.setVisibility(GONE);
				mImageRefreshing.clearAnimation();
				break;
			case PULL_REFRESH_STATE:
				mImageDropDown.setVisibility(VISIBLE);
				mLayoutRefreshing.setVisibility(GONE);
				mLayoutRefreshComplete.setVisibility(GONE);
				mImageRefreshing.clearAnimation();
				break;
			case RELEASE_REFRESH_STATE:
				mImageDropDown.setVisibility(VISIBLE);
				mLayoutRefreshing.setVisibility(GONE);
				mLayoutRefreshComplete.setVisibility(GONE);
				mImageDropDown.setScaleX(1.0f);
				mImageDropDown.setScaleY(1.0f);
				mImageRefreshing.clearAnimation();
				break;
			case REFRESHING_STATE:
				mImageDropDown.setVisibility(GONE);
				mLayoutRefreshing.setVisibility(VISIBLE);
				mLayoutRefreshComplete.setVisibility(GONE);
				mImageRefreshing.clearAnimation();
				mImageRefreshing.startAnimation(mRotationRefresh);
				break;
			case REFRESH_COMPLETE_STATE:
				mImageDropDown.setVisibility(GONE);
				mLayoutRefreshing.setVisibility(GONE);
				mLayoutRefreshComplete.setVisibility(VISIBLE);
				mImageRefreshing.clearAnimation();
				break;
		}
		mCurrentState = state;
	}

	@Override
	public void updateDropDownDistance(int distance) {
		if (mCurrentState == PULL_REFRESH_STATE) {
			float scale = distance * 1.0f / getReleaseRefreshDistance();
			Log.d("tuacy", "scale = " + scale);
			mImageDropDown.setScaleX(scale);
			mImageDropDown.setScaleY(scale);
		}
	}
}
