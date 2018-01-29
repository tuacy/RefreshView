package com.tuacy.refresh.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 下拉刷新，上拉加载控件
 */

public class RefreshView extends LinearLayout {

	public RefreshView(Context context) {
		this(context, null);
	}

	public RefreshView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RefreshView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
}
