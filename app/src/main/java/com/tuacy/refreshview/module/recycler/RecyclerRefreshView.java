package com.tuacy.refreshview.module.recycler;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tuacy.refresh.view.IDropDownRefreshView;
import com.tuacy.refreshview.R;

public class RecyclerRefreshView extends FrameLayout implements IDropDownRefreshView {

	private ImageView    mImageDropDown;
	private ImageView    mImageRefreshing;
	private LinearLayout mLayoutRefreshing;
	private LinearLayout mLayoutRefreshComplete;

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
	}

	@Override
	public int getReleaseRefreshDistance() {
		return getMeasuredHeight();
	}

	@Override
	public void updateRefreshState(RefreshState state) {
		switch (state) {
			case INITIAL_STATE:
				break;
			case PULL_REFRESH_STATE:
				break;
			case RELEASE_REFRESH_STATE:
				break;
			case REFRESHING_STATE:
				break;
			case REFRESH_COMPLETE_STATE:
				break;
		}
	}

	@Override
	public void updateDropDownDistance(int distance) {

	}
}
