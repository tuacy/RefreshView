package com.pilot.common.utils;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

public class JumpUtils {

	public static void toSettingActivity(Context context) {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		Uri uri = Uri.fromParts("package", context.getPackageName(), null);
		intent.setData(uri);
		context.startActivity(intent);
	}
}
