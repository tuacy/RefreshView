package com.pilot.common.utils;


import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckUtils {

	public static boolean strIsNull(String str) {
		if (null != str && !"".equals(str)) {
			return false;
		}
		return true;
	}

	/**
	 * 判断字符串是否为正整数
	 * +1, +2, +3……算作正整数
	 * 0,1.0,2.0,3.0……不算做正整数
	 *
	 * @return true=>是;false=>不是
	 */
	public static boolean strIsPositiveInteger(String str) {
		if (strIsNull(str)) {
			return false;
		}
		Pattern p = Pattern.compile("[1-9]\\d*");
		Pattern p1 = Pattern.compile("\\+[1-9]\\d*");
		Matcher m = p.matcher(str);
		Matcher m1 = p1.matcher(str);
		if (m.matches() || m1.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * 判断字符串是否为负整数
	 * 0,-1.0,-2.0,-3.0……不算做负整数
	 *
	 * @return true=>是;false=>不是
	 */
	public static boolean strIsNegativeInteger(String str) {
		if (strIsNull(str)) {
			return false;
		}
		Pattern p = Pattern.compile("-[1-9]\\d*");
		Matcher m = p.matcher(str);
		if (m.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * 判断字符串是否是整数
	 * +xxx,xxx,-xxx,+0,-0,0都算作整数
	 *
	 * @return true=>是;false=>不是
	 */
	public static boolean strIsInteger(String str) {
		if (strIsNull(str)) {
			return false;
		}
		Pattern p = Pattern.compile("((-|\\+)?[1-9]\\d*)|((-|\\+)?0)");
		Matcher m = p.matcher(str);
		if (m.matches()) {
			return true;
		}
		return false;
	}


	/**
	 * 判断字符串是否为正浮点型（Float or Double）数据
	 * x.xx或+x.xx都算作正浮点数
	 * 0.0不算做正浮点数
	 *
	 * @return true=>是;false=>不是
	 */
	public static boolean strIsPositiveFloat(String str) {
		if (strIsNull(str)) {
			return false;
		}
		if (strIsPositiveInteger(str) || strIsNegativeInteger(str)) {
			return false;
		}
		Pattern p = Pattern.compile("[1-9]\\d*.\\d*|0.\\d*[1-9]\\d*");
		Pattern p1 = Pattern.compile("\\+([1-9]\\d*.\\d*|0.\\d*[1-9]\\d*)");
		Matcher m = p.matcher(str);
		Matcher m1 = p1.matcher(str);
		if (m.matches() || m1.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * 判断字符串是否为负浮点型（Float or Double）数据
	 * -0.0不算做负浮点数
	 *
	 * @return true=>是;false=>不是
	 */
	public static boolean strIsNegativeFloat(String str) {
		if (strIsNull(str)) {
			return false;
		}
		if (strIsPositiveInteger(str) || strIsNegativeInteger(str)) {
			return false;
		}
		Pattern p = Pattern.compile("-([1-9]\\d*.\\d*|0.\\d*[1-9]\\d*)");
		Matcher m = p.matcher(str);
		if (m.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * 判断字符串是否为数字
	 *
	 * @return true=>是;false=>不是
	 */
	public static boolean strIsNumber(String str) {
		if (strIsNull(str)) {
			return false;
		}
		if (strIsInteger(str) || strIsNegativeFloat(str) || strIsPositiveFloat(str)) {
			return true;
		}
		return false;
	}


	/**
	 * 判断当前应用是否运行在前台
	 *
	 * @return true 前台
	 */
	public static boolean isForeground(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(context.getPackageName())) {
				if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
					return false;
				} else {
					return true;
				}
			}
		}
		return true;
	}

	/**
	 * 检查SD卡是否存在
	 */
	public static boolean isExistSDCard() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 检查文件是否存在
	 */
	public static boolean isExistFile(String path) {
		try {
			File f = new File(path);
			if (!f.exists()) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 是否是手机号码
	 *
	 * @return true:是
	 */
	public static boolean isMobile(String mobile) {
		Pattern p = Pattern.compile("^((14[0-9])|(17[0-9])|(13[0-9])|(15[0-9])|(18[0-9]))\\d{8}$");
		Matcher m = p.matcher(mobile);
		return m.matches();
	}

	/**
	 * 是否是固定电话
	 *
	 * @return true：是
	 */
	public static boolean isLandlineTelePhone(String phone) {
		String str = "^((\\d{3,4}\\-)|)\\d{7,8}(|([-\\u8f6c]{1}\\d{1,5}))$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(phone);
		return m.matches();
	}

	/**
	 * 是否是邮政编码
	 *
	 * @return true:是
	 */
	public static boolean isZipCode(String zipCode) {
		String str = "^[1-9]\\d{5}$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(zipCode);
		return m.matches();
	}

	/**
	 * 是否是邮箱
	 *
	 * @return true:是
	 */
	public static boolean isEmail(String email) {
		String str
			= "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);
		return m.matches();
	}

	/**
	 * 一般昵称规则验证
	 * 是否由汉字字符数字下划线组成
	 *
	 * @return true:是
	 */
	public static boolean isNickname(String nick) {
		String str = "[\u4e00-\u9fa5_a-zA-Z0-9_]+";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(nick);
		return m.matches();
	}
}
