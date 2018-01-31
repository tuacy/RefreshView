package com.tuacy.refreshview.module.list;


public class ListItemBean {

	private String mMainTitle;
	private String mSubTitle;

	public ListItemBean(String mainTitle, String subTitle) {
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
