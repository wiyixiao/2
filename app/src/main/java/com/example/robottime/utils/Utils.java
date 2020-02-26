package com.example.robottime.utils;

import android.content.Context;

public class Utils {

	private static Context context;

	public static boolean DEBUG = false;

	public static void init(Context context) {
		Utils.context = context;
	}

	public static Context getApplication() {
		if (context == null)
			throw new NullPointerException("You must call the 'Utils.init()' to initialized! ");
		return context;
	}
}
