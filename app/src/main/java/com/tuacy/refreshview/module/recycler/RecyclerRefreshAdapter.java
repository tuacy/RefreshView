package com.tuacy.refreshview.module.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tuacy.refreshview.R;

import java.util.ArrayList;
import java.util.List;

public class RecyclerRefreshAdapter extends RecyclerView.Adapter<RecyclerRefreshAdapter.ItemHolder> {

	private List<RecyclerItemBean> mDataList;

	public RecyclerRefreshAdapter(List<RecyclerItemBean> list) {
		mDataList = list;
	}

	public void setData(List<RecyclerItemBean> list) {
		mDataList = list;
		notifyDataSetChanged();
	}

	public void appendData(List<RecyclerItemBean> list) {
		if (mDataList == null) {
			mDataList = new ArrayList<>();
		}
		mDataList.addAll(list);
		notifyDataSetChanged();
	}

	@Override
	public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_refresh, parent, false));
	}

	@Override
	public void onBindViewHolder(ItemHolder holder, int position) {
		RecyclerItemBean item = mDataList.get(position);
		holder.mTextMain.setBackgroundResource(position % 2 == 0 ? R.drawable.bg_device_odd : R.drawable.bg_device_even);
		holder.mTextMain.setText(item.getMainTitle());
		holder.mTextSub.setText(item.getSubTitle());
	}

	@Override
	public int getItemCount() {
		return mDataList == null ? 0 : mDataList.size();
	}

	static class ItemHolder extends RecyclerView.ViewHolder {

		TextView mTextMain;
		TextView mTextSub;

		ItemHolder(View itemView) {
			super(itemView);
			mTextMain = itemView.findViewById(R.id.text_item_recycler_main_title);
			mTextSub = itemView.findViewById(R.id.text_item_recycler_sub_title);
		}
	}

}
