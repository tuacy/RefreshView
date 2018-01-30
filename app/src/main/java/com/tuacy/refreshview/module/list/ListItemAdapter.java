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

	private List<String> mDataList;
	private Context      mContext;

	public ListItemAdapter(Context context) {
		this(context, null);
	}

	public ListItemAdapter(Context context, List<String> list) {
		mContext = context;
		mDataList = list;
	}

	public void setData(List<String> list) {
		mDataList = list;
		notifyDataSetChanged();
	}

	public void appendData(List<String> list) {
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
	public String getItem(int position) {
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
		holder.mTextItemName.setText(getItem(position));
		return convertView;
	}

	static class ViewHolder {

		TextView mTextItemName;

		ViewHolder(View itemView) {
			mTextItemName = itemView.findViewById(R.id.text_item_list_refresh);
		}
	}
}
