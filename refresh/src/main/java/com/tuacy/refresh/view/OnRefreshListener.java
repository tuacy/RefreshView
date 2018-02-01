package com.tuacy.refresh.view;

/**
 * 上拉加载和下拉刷新监听类
 */
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
