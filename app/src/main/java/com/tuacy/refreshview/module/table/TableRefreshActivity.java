package com.tuacy.refreshview.module.table;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;


import com.pilot.common.utils.DensityUtils;
import com.tuacy.refresh.view.OnRefreshListener;
import com.tuacy.refresh.view.RefreshLayout;
import com.tuacy.refreshview.R;
import com.tuacy.refreshview.app.base.MobileBaseActivity;

import java.util.ArrayList;
import java.util.List;

public class TableRefreshActivity extends MobileBaseActivity {

	public static void startUp(Context context) {
		context.startActivity(new Intent(context, TableRefreshActivity.class));
	}

	public static final int COLUMN_COUNT = 10;

	private RecyclerView  mRecyclerView;
	private RefreshLayout mRefreshLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_table_refresh);
		mContext = this;
		initView();
		initEvent();
		initData();
	}

	private void initView() {
		mRefreshLayout = findViewById(R.id.refresh_table);
		mRecyclerView = findViewById(R.id.recycler_table);
		TableLayoutManager layoutManager = new TableLayoutManager.Build(mContext).setColumnCount(COLUMN_COUNT)
																				 .setFixColumnCount(1)
																				 .setFixHeader(true)
																				 .setHeadHeight(DensityUtils.dp2px(mContext, 32))
																				 .setRowHeight(DensityUtils.dp2px(mContext, 48))
																				 .build();
		mRecyclerView.setLayoutManager(layoutManager);
		mRecyclerView.addItemDecoration(new TableItemDecoration(mContext));
	}

	private void initEvent() {
		mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onDropDownRefresh(boolean auto) {
				mRefreshLayout.setRefreshComplete();
			}

			@Override
			public void onPullUpLoad(boolean auto) {
				mRefreshLayout.setLoadComplete();
			}
		});
	}

	private void initData() {
		TableAdapter adapter = new TableAdapter(mContext, obtainDataList());
		mRecyclerView.setAdapter(adapter);
		adapter.setOnItemClickListener(new TableAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position) {
				Log.d("tuacy", "position = " + position);
			}
		});
	}

	private List<String> obtainDataList() {
		List<String> dataList = new ArrayList<>();
		for (int column = 0; column < COLUMN_COUNT; column++) {
			if (column == 0) {
				dataList.add("身高/体重");
			} else {
				dataList.add(String.valueOf(152 + (column - 1) * 4));
			}
		}
		for (int row = 0; row < 60; row++) {
			for (int column = 0; column < COLUMN_COUNT; column++) {
				if (column == 0) {
					dataList.add(String.valueOf(19 + row));
				} else {
					dataList.add(String.valueOf(50 + row + (column - 1) * 2));
				}
			}
		}
		return dataList;
	}
}
