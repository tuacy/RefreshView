package com.tuacy.refreshview.module.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.tuacy.refresh.view.OnRefreshListener;
import com.tuacy.refresh.view.RefreshLayout;
import com.tuacy.refreshview.R;
import com.tuacy.refreshview.app.base.MobileBaseActivity;

import java.util.ArrayList;
import java.util.List;

public class ListRefreshActivity extends MobileBaseActivity {

	public static void startUp(Context context) {
		context.startActivity(new Intent(context, ListRefreshActivity.class));
	}

	private RefreshLayout   mRefreshLayout;
	private ListView        mListRefresh;
	private ListItemAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_refresh);
		initView();
		initEvent();
		initData();
	}

	private void initView() {
		mRefreshLayout = findViewById(R.id.refresh_list_view);
		mListRefresh = findViewById(R.id.list_refresh);
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
	}

	private void initData() {
		mAdapter = new ListItemAdapter(mContext, initAdapterData());
		mListRefresh.setAdapter(mAdapter);
	}

	private List<String> initAdapterData() {
		List<String> list = new ArrayList<>();
		for (int index = 0; index < 20; index++) {
			list.add("item = " + index);
		}
		return list;
	}

	private List<String> appendAdapterData() {
		List<String> list = new ArrayList<>();
		for (int index = 0; index < 5; index++) {
			list.add("append = " + index);
		}
		return list;
	}

}
