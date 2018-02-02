package com.tuacy.refreshview.module.recycler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tuacy.refresh.view.OnRefreshListener;
import com.tuacy.refresh.view.RefreshLayout;
import com.tuacy.refreshview.R;
import com.tuacy.refreshview.app.base.MobileBaseActivity;

import java.util.ArrayList;
import java.util.List;

public class RecyclerRefreshActivity extends MobileBaseActivity {

	public static void startUp(Context context) {
		context.startActivity(new Intent(context, RecyclerRefreshActivity.class));
	}

	private RefreshLayout          mRefreshLayout;
	private RecyclerView           mRecyclerRefresh;
	private RecyclerRefreshAdapter mAdapter;

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
		mRecyclerRefresh.setLayoutManager(new LinearLayoutManager(mContext));
	}

	private void initEvent() {
		mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onDropDownRefresh(boolean auto) {
				getMainHandler().postDelayed(new Runnable() {
					@Override
					public void run() {
						mAdapter.setData(initAdapterData());
						mRefreshLayout.setRefreshComplete();
					}
				}, 2000);
			}

			@Override
			public void onPullUpLoad(boolean auto) {
				getMainHandler().postDelayed(new Runnable() {
					@Override
					public void run() {
						mAdapter.appendData(appendAdapterData());
						mRefreshLayout.setLoadComplete();
					}
				}, 2000);
			}
		});
		//自定义一个下拉刷新View
		mRefreshLayout.setDropDownRefreshView(new RecyclerRefreshView(mContext));
	}

	private void initData() {
		mAdapter = new RecyclerRefreshAdapter(initAdapterData());
		mRecyclerRefresh.setAdapter(mAdapter);
		//		mRefreshLayout.startAutoRefresh();
	}

	private List<RecyclerItemBean> initAdapterData() {
		List<RecyclerItemBean> list = new ArrayList<>();
		for (int index = 0; index < 20; index++) {
			list.add(new RecyclerItemBean("初始化or刷新", "item = " + index));
		}
		return list;
	}

	private List<RecyclerItemBean> appendAdapterData() {
		List<RecyclerItemBean> list = new ArrayList<>();
		for (int index = 0; index < 5; index++) {
			list.add(new RecyclerItemBean("加载更多", "item = " + index));
		}
		return list;
	}
}
