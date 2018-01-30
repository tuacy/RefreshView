package com.tuacy.refresh.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tuacy.refresh.R;

public class SimpleDropDownRefreshView extends LinearLayout implements IDropDownRefreshView {

	private ImageView       mImageDropDown;
	private ProgressBar     mProgressRefreshing;
	private ImageView       mImageRefreshOk;
	private TextView        mTextRefreshHint;
	private TextView        mTextRefreshTime;
	private RefreshState    mRefreshState;
	private RotateAnimation mRotationRefresh;
	private RotateAnimation mRotationRelease;

	public SimpleDropDownRefreshView(Context context) {
		this(context, null);
	}

	public SimpleDropDownRefreshView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SimpleDropDownRefreshView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initData();
	}

	private void initData() {
		mRotationRefresh = new RotateAnimation(-180f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mRotationRefresh.setDuration(300);
		mRotationRefresh.setFillAfter(true);
		mRotationRelease = new RotateAnimation(0f, -180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mRotationRelease.setDuration(300);
		mRotationRelease.setFillAfter(true);
		LayoutInflater.from(getContext()).inflate(R.layout.view_drop_down_refresh, this, true);
		mImageDropDown = findViewById(R.id.image_drop_down_refresh_arrow);
		mProgressRefreshing = findViewById(R.id.progress_drop_down_refreshing);
		mImageRefreshOk = findViewById(R.id.image_drop_down_refresh_ok);
		mTextRefreshHint = findViewById(R.id.text_drop_down_refresh_hint);
		mTextRefreshTime = findViewById(R.id.text_drop_down_refresh_time);
		updateRefreshState(RefreshState.INITIAL_STATE);
	}

	@Override
	public void updateRefreshState(RefreshState state) {
		if (mRefreshState == state) {
			return;
		}
		switch (state) {
			case INITIAL_STATE:
				mImageDropDown.setVisibility(VISIBLE);
				mImageDropDown.clearAnimation();
				mImageDropDown.startAnimation(mRotationRefresh);
				mProgressRefreshing.setVisibility(GONE);
				mImageRefreshOk.setVisibility(GONE);
				mTextRefreshHint.setText(R.string.refresh_view_drop_down_refresh);
				break;
			case PULL_REFRESH_STATE:
				mImageDropDown.setVisibility(VISIBLE);
				mImageDropDown.clearAnimation();
				mImageDropDown.startAnimation(mRotationRefresh);
				mProgressRefreshing.setVisibility(GONE);
				mImageRefreshOk.setVisibility(GONE);
				mTextRefreshHint.setText(R.string.refresh_view_drop_down_refresh);
				break;
			case RELEASE_REFRESH_STATE:
				mImageDropDown.setVisibility(VISIBLE);
				mImageDropDown.clearAnimation();
				mImageDropDown.startAnimation(mRotationRelease);
				mProgressRefreshing.setVisibility(GONE);
				mImageRefreshOk.setVisibility(GONE);
				mTextRefreshHint.setText(R.string.refresh_view_release_refresh);
				break;
			case REFRESHING_STATE:
				mProgressRefreshing.setVisibility(VISIBLE);
				mImageDropDown.clearAnimation();
				mImageDropDown.setVisibility(GONE);
				mImageRefreshOk.setVisibility(GONE);
				mTextRefreshHint.setText(R.string.refresh_view_refreshing);
				break;
			case REFRESH_COMPLETE_STATE:
				mImageDropDown.clearAnimation();
				mImageDropDown.setVisibility(GONE);
				mProgressRefreshing.setVisibility(GONE);
				mImageRefreshOk.setVisibility(VISIBLE);
				mTextRefreshHint.setText(R.string.refresh_view_load_complete);
				break;
		}
		mRefreshState = state;
	}


}
