package com.tuacy.refresh.view;

/**
 * 上拉加载View必须实现的方法
 */
public interface IPullUpLoadView {

	enum LoadState {
		/**
		 * 初始状态
		 */
		INITIAL_STATE(0),
		/**
		 * 上拉加载
		 */
		PULL_LOAD_STATE(1),
		/**
		 * 释放加载
		 */
		RELEASE_LOAD_STATE(2),
		/**
		 * 正在加载
		 */
		LOADING_STATE(3),
		/**
		 * 加载完成
		 */
		LOAD_COMPLETE_STATE(4),
		/**
		 * 没有更多数据
		 */
		LOAD_NO_MORE_DATA_STATE(5);

		private int mState;

		LoadState(int state) {
			mState = state;
		}

		public int state() {
			return mState;
		}
	}

	/**
	 * 达到了这个距离进入加载状态
	 *
	 * @return 加载状态距离
	 */
	int getReleaseLoadDistance();

	/**
	 * 更新加载状态
	 *
	 * @param state 状态
	 */
	void updateLoadState(LoadState state);

	/**
	 * 在加载更多view显示的时候，更新显示的距离
	 *
	 * @param distance 距离
	 */
	void updatePullUpDistance(int distance);

}
