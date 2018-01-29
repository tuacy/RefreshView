package com.tuacy.refresh.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.tuacy.refresh.R;

public class SimplePullUpLoadView extends LinearLayout implements IDropDownRefreshView {

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
	}
}
