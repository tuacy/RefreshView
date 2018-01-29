package com.tuacy.refresh.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.tuacy.refresh.R;

public class SimpleDropDownRefreshView extends LinearLayout implements IPullUpLoadView {

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
		LayoutInflater.from(getContext()).inflate(R.layout.view_drop_down_refresh, this, true);
	}
}
