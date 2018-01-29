package com.tuacy.refreshview.app;

import android.content.Context;

import com.pilot.common.base.application.BaseApplication;
import com.pilot.common.log.TuacyLog;
import com.tuacy.refreshview.BuildConfig;

public class MobileApplication extends BaseApplication {

	@Override
	public void onCreate() {
		super.onCreate();
		TuacyLog.setDebugEnabled(BuildConfig.DEBUG);
	}

	@Override
	protected void initializeApplication() {

	}

	@Override
	protected void deInitializeApplication() {

	}

	@Override
	protected void onAppCrash(Context context, Thread thread, Throwable ex) {

	}
}
