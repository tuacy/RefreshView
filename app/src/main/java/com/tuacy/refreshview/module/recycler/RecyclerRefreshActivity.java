package com.tuacy.refreshview.module.recycler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.tuacy.refresh.view.OnRefreshListener;
import com.tuacy.refresh.view.RefreshLayout;
import com.tuacy.refreshview.R;
import com.tuacy.refreshview.app.base.MobileBaseActivity;

public class RecyclerRefreshActivity extends MobileBaseActivity {

	public static void startUp(Context context) {
		context.startActivity(new Intent(context, RecyclerRefreshActivity.class));
	}

	private RefreshLayout mRefreshLayout;
	private RecyclerView  mRecyclerRefresh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recycler_refresh);
		initView();
		initEvent();
		initData();
	}

	private void initView() {
		mRefreshLayout = findViewById(R.id.refresh_recycler_view);
		mRecyclerRefresh = findViewById(R.id.recycler_refresh);
	}

	private void initEvent() {
		mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onDropDownRefresh(boolean auto) {
			}

			@Override
			public void onPullUpLoad(boolean auto) {
			}
		});
	}

	private void initData() {

	}
}
