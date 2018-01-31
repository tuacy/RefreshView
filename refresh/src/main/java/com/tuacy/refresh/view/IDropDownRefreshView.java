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

	/**
	 * 达到了这个距离进入刷新状态
	 *
	 * @return 刷新状态距离
	 */
	int getReleaseRefreshDistance();

	/**
	 * 更新刷新view对应的状态
	 *
	 * @param state 状态
	 */
	void updateRefreshState(RefreshState state);

	/**
	 * 更新下拉的距离
	 *
	 * @param distance 下拉距离
	 */
	void updateDropDownDistance(int distance);

}
