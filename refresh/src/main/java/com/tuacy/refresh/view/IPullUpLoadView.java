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
		LOAD_COMPLETE_STATE(4);

		private int mState;

		LoadState(int state) {
			mState = state;
		}

		public int state() {
			return mState;
		}
	}

	void updateLoadState(LoadState state);

}
