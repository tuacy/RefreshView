package com.tuacy.refresh.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tuacy.refresh.R;

public class SimplePullUpLoadView extends LinearLayout implements IPullUpLoadView {

	private ProgressBar mProgressBar;
	private TextView    mTextLoadHint;
	private LoadState   mState;

	public SimplePullUpLoadView(Context context) {
		this(context, null);
	}

	public SimplePullUpLoadView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SimplePullUpLoadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initData();
	}

	private void initData() {
		LayoutInflater.from(getContext()).inflate(R.layout.view_pull_up_load, this, true);
		mProgressBar = findViewById(R.id.progress_pull_up_load_progress);
		mTextLoadHint = findViewById(R.id.text_pull_up_load_hint);
	}

	@Override
	public int getReleaseLoadDistance() {
		return getMeasuredHeight();
	}

	@Override
	public void updateLoadState(LoadState state) {
		if (mState == state) {
			return;
		}
		switch (state) {
			case INITIAL_STATE:
				mProgressBar.setVisibility(GONE);
				mTextLoadHint.setVisibility(VISIBLE);
				mTextLoadHint.setText(R.string.refresh_pull_up_load_more);
				break;
			case PULL_LOAD_STATE:
				mProgressBar.setVisibility(GONE);
				mTextLoadHint.setVisibility(VISIBLE);
				mTextLoadHint.setText(R.string.refresh_pull_up_load_more);
				break;
			case RELEASE_LOAD_STATE:
				mProgressBar.setVisibility(GONE);
				mTextLoadHint.setVisibility(VISIBLE);
				mTextLoadHint.setText(R.string.refresh_view_release_load_more);
				break;
			case LOADING_STATE:
				mProgressBar.setVisibility(VISIBLE);
				mTextLoadHint.setVisibility(GONE);
				mTextLoadHint.setText(R.string.refresh_pull_up_load_more);
				break;
			case LOAD_COMPLETE_STATE:
				mProgressBar.setVisibility(GONE);
				mTextLoadHint.setVisibility(VISIBLE);
				mTextLoadHint.setText(R.string.refresh_view_load_complete);
				break;
			case LOAD_NO_MORE_DATA_STATE:
				mProgressBar.setVisibility(GONE);
				mTextLoadHint.setVisibility(VISIBLE);
				mTextLoadHint.setText(R.string.refresh_view_no_more_data);
				break;
		}
		mState = state;
	}

	@Override
	public void updatePullUpDistance(int distance) {

	}
}
