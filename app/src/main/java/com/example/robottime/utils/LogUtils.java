package com.example.robottime.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * Log工具，类似android.util.Log。 tag自动产生，
 * 格式:customTagPrefix:className.methodName(L:lineNumber),
 * customTagPrefix为空时只输出：className.methodName(L:lineNumber)。
 * <p/>
 */
public class LogUtils {

	public static String customTagPrefix = "JASON";

	private LogUtils() {
	}

	public static boolean allowD = Utils.DEBUG;
	public static boolean allowE = Utils.DEBUG;
	public static boolean allowI = Utils.DEBUG;
	public static boolean allowV = Utils.DEBUG;
	public static boolean allowW = Utils.DEBUG;
	public static boolean allowWtf = Utils.DEBUG;

	private static String generateTag(StackTraceElement caller) {
		String tag = "%s.%s(L:%d)";
		String callerClazzName = caller.getClassName();
		callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
		tag = String.format(tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
		tag = TextUtils.isEmpty(customTagPrefix) ? tag : customTagPrefix + ":" + tag;
		return tag;
	}

	public static void d(String content) {
		if (!allowD)
			return;
		StackTraceElement caller = getCallerStackTraceElement();
		String tag = generateTag(caller);
		Log.d(tag, content);
	}

	public static void d(String content, Throwable tr) {
		if (!allowD)
			return;
		StackTraceElement caller = getCallerStackTraceElement();
		String tag = generateTag(caller);
		Log.d(tag, content, tr);
	}

	public static void e(String content) {
		if (!allowE)
			return;
		StackTraceElement caller = getCallerStackTraceElement();
		String tag = generateTag(caller);
		Log.e(tag, content);
	}

	public static void e(String content, Throwable tr) {
		if (!allowE)
			return;
		StackTraceElement caller = getCallerStackTraceElement();
		String tag = generateTag(caller);
		Log.e(tag, content, tr);
	}

	public static void i(String content) {
		if (!allowI)
			return;
		StackTraceElement caller = getCallerStackTraceElement();
		String tag = generateTag(caller);
		Log.i(tag, content);
	}

	public static void i(String content, Throwable tr) {
		if (!allowI)
			return;
		StackTraceElement caller = getCallerStackTraceElement();
		String tag = generateTag(caller);
		Log.i(tag, content, tr);
	}

	public static void v(String content) {
		if (!allowV)
			return;
		StackTraceElement caller = getCallerStackTraceElement();
		String tag = generateTag(caller);
		Log.v(tag, content);
	}

	public static void v(String content, Throwable tr) {
		if (!allowV)
			return;
		StackTraceElement caller = getCallerStackTraceElement();
		String tag = generateTag(caller);
		Log.v(tag, content, tr);
	}

	public static void w(String content) {
		if (!allowW)
			return;
		StackTraceElement caller = getCallerStackTraceElement();
		String tag = generateTag(caller);
		Log.w(tag, content);
	}

	public static void w(String content, Throwable tr) {
		if (!allowW)
			return;
		StackTraceElement caller = getCallerStackTraceElement();
		String tag = generateTag(caller);
		Log.w(tag, content, tr);
	}

	public static void w(Throwable tr) {
		if (!allowW)
			return;
		StackTraceElement caller = getCallerStackTraceElement();
		String tag = generateTag(caller);
		Log.w(tag, tr);
	}

	public static void wtf(String content) {
		if (!allowWtf)
			return;
		StackTraceElement caller = getCallerStackTraceElement();
		String tag = generateTag(caller);
		Log.wtf(tag, content);
	}

	public static void wtf(String content, Throwable tr) {
		if (!allowWtf)
			return;
		StackTraceElement caller = getCallerStackTraceElement();
		String tag = generateTag(caller);
		Log.wtf(tag, content, tr);
	}

	public static void wtf(Throwable tr) {
		if (!allowWtf)
			return;
		StackTraceElement caller = getCallerStackTraceElement();
		String tag = generateTag(caller);
		Log.wtf(tag, tr);
	}

	private static StackTraceElement getCallerStackTraceElement() {
		return Thread.currentThread().getStackTrace()[4];
	}
}
