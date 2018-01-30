package com.tuacy.refreshview.module.text;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.tuacy.refresh.view.OnRefreshListener;
import com.tuacy.refresh.view.RefreshLayout;
import com.tuacy.refreshview.R;
import com.tuacy.refreshview.app.base.MobileBaseActivity;

public class TextRefreshActivity extends MobileBaseActivity {

	public static void startUp(Context context) {
		context.startActivity(new Intent(context, TextRefreshActivity.class));
	}

	private RefreshLayout mRefreshLayout;
	private TextView      mTextRefresh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text_refresh);
		initView();
		initEvent();
		initData();
	}

	private void initView() {
		mRefreshLayout = findViewById(R.id.refresh_text_view);
		mTextRefresh = findViewById(R.id.text_refresh);
	}

	private void initEvent() {
		mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onDropDownRefresh(boolean auto) {
				getMainHandler().postDelayed(new Runnable() {
					@Override
					public void run() {
						mTextRefresh.setText("done refresh");
						mRefreshLayout.setRefreshComplete();
					}
				}, 2000);
			}

			@Override
			public void onPullUpLoad(boolean auto) {
				getMainHandler().postDelayed(new Runnable() {
					@Override
					public void run() {
						mTextRefresh.setText("done load");
						mRefreshLayout.setLoadComplete();
					}
				}, 2000);
			}
		});
	}

	private void initData() {
	}


}
