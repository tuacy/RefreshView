package com.tuacy.refresh.view;


import android.view.View;

public interface OnChildScrollEnableCallback {

	/**
	 * 是否可以下拉
	 */
	boolean canChildDropdown(RefreshLayout parent, View target);

	/**
	 * 是否可以上拉
	 */
	boolean canChildPullUp(RefreshLayout parent, View target);

}
