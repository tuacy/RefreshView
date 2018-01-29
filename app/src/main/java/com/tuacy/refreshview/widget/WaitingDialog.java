package com.tuacy.refreshview.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.tuacy.refreshview.R;


public class WaitingDialog extends Dialog {

	public WaitingDialog(Context context) {
		super(context, R.style.NoTitleDialog);
		initView();
	}

	private void initView() {
		View parentView = View.inflate(getContext(), R.layout.dialog_waitting, null);
		setContentView(parentView);

		this.setCanceledOnTouchOutside(false);

		ImageView image = parentView.findViewById(R.id.image_view_wait_anim);

		Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.waiting_dialog);
		LinearInterpolator interpolator = new LinearInterpolator();
		anim.setInterpolator(interpolator);
		image.startAnimation(anim);
	}

	@Override
	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
		return KeyEvent.ACTION_DOWN == event.getAction() && keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
	}
}
