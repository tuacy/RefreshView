package com.tuacy.refreshview.module.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tuacy.refreshview.R;

import java.util.ArrayList;
import java.util.List;

public class ListItemAdapter extends BaseAdapter {

	private List<ListItemBean> mDataList;
	private Context            mContext;

	public ListItemAdapter(Context context) {
		this(context, null);
	}

	public ListItemAdapter(Context context, List<ListItemBean> list) {
		mContext = context;
		mDataList = list;
	}

	public void setData(List<ListItemBean> list) {
		mDataList = list;
		notifyDataSetChanged();
	}

	public void appendData(List<ListItemBean> list) {
		if (mDataList == null) {
			mDataList = new ArrayList<>();
		}
		mDataList.addAll(list);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mDataList == null ? 0 : mDataList.size();
	}

	@Override
	public ListItemBean getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_refresh, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mTextMainTitle.setText(getItem(position).getMainTitle());
		holder.mTextMainTitle.setBackgroundResource(position % 2 == 0 ? R.drawable.bg_device_even : R.drawable.bg_device_odd);
		holder.mTextSubTitle.setText(getItem(position).getSubTitle());
		return convertView;
	}

	static class ViewHolder {

		TextView mTextMainTitle;
		TextView mTextSubTitle;

		ViewHolder(View itemView) {
			mTextMainTitle = itemView.findViewById(R.id.text_item_list_main_title);
			mTextSubTitle = itemView.findViewById(R.id.text_item_list_sub_title);
		}
	}
}
