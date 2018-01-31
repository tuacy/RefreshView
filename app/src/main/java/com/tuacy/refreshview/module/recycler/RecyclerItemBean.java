package com.tuacy.refreshview.module.recycler;


public class RecyclerItemBean {

	private String mMainTitle;
	private String mSubTitle;

	public RecyclerItemBean(String mainTitle, String subTitle) {
		mMainTitle = mainTitle;
		mSubTitle = subTitle;
	}

	public String getMainTitle() {
		return mMainTitle;
	}

	public void setMainTitle(String mainTitle) {
		mMainTitle = mainTitle;
	}

	public String getSubTitle() {
		return mSubTitle;
	}

	public void setSubTitle(String subTitle) {
		mSubTitle = subTitle;
	}
}
