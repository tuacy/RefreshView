package com.tuacy.refresh.view;


public interface OnRefreshListener {

	/**
	 * 下拉刷新
	 *
	 * @param auto 自动or手动
	 */
	void onDropDownRefresh(boolean auto);

	/**
	 * 上拉加载
	 *
	 * @param auto 自动or手动
	 */
	void onPullUpLoad(boolean auto);

}
