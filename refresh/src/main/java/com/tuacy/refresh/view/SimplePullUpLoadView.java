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
	private TextView    mTextClickLoad;
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
		mTextClickLoad = findViewById(R.id.text_pull_up_load_click);
	}

	@Override
	public void updateLoadState(LoadState state) {
		if (mState == state) {
			return;
		}
		switch (state) {
			case INITIAL_STATE:
				break;
			case LOADING_STATE:
				break;
			case PULL_LOAD_STATE:
				break;
			case RELEASE_LOAD_STATE:
				break;
			case LOAD_COMPLETE_STATE:
				break;
		}
		mState = state;
	}
}
