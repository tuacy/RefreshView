package com.tuacy.refreshview;

import android.os.Bundle;
import android.view.View;

import com.tuacy.refreshview.app.base.MobileBaseActivity;
import com.tuacy.refreshview.module.list.ListRefreshActivity;
import com.tuacy.refreshview.module.recycler.RecyclerRefreshActivity;
import com.tuacy.refreshview.module.smartrefresh.SmartRefreshTableActivity;
import com.tuacy.refreshview.module.text.TextRefreshActivity;

public class MainActivity extends MobileBaseActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.button_text_view_refresh).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TextRefreshActivity.startUp(mContext);
			}
		});
		findViewById(R.id.button_list_view_refresh).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ListRefreshActivity.startUp(mContext);
			}
		});
		findViewById(R.id.button_recycler_view_refresh).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RecyclerRefreshActivity.startUp(mContext);
			}
		});

		findViewById(R.id.button_table_smart_refresh).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SmartRefreshTableActivity.startUp(mContext);
			}
		});
	}
}
