package com.tuacy.refresh.view;


/**
 * 下拉刷新View必须实现的方法
 */
public interface IDropDownRefreshView {

	enum RefreshState {
		/**
		 * 初始状态
		 */
		INITIAL_STATE(0),
		/**
		 * 下拉刷新
		 */
		PULL_REFRESH_STATE(1),
		/**
		 * 释放刷新
		 */
		RELEASE_REFRESH_STATE(2),
		/**
		 * 正在刷新
		 */
		REFRESHING_STATE(3),
		/**
		 * 刷新完成
		 */
		REFRESH_COMPLETE_STATE(4);

		private int mState;

		RefreshState(int state) {
			mState = state;
		}

		public int state() {
			return mState;
		}
	}

	void updateRefreshState(RefreshState state);

}
