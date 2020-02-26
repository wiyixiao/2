/*
    流转换Stream
    参考链接：http://download.csdn.net/detail/qq_26787115/9463814
 */

package com.example.robottime.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class CommonUtil {
    // 对外发放
    public static String toStream(InputStream in) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 长度
        int length = 0;
        byte[] buffer = new byte[1024];
        // -1代表读完了
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        // 读完关闭
        in.close();
        out.close();
        // 我们把返回的数据转换成String
        return out.toString();
    }

    public static void toastTextShow(Context context, int id)
    {
        Toast.makeText(context,context.getResources().getString(id),Toast.LENGTH_SHORT).show();
    }

    public static void toastTextShow(Context context, String msg)
    {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    //获取R.string
    public static String getRString(Context context, int id)
    {
        return context.getResources().getString(id);
    }

    //获取请求requestID
    public static String getRequestId()
    {
        return UUID.randomUUID().toString().replace("-","");
    }

    //获取版本号
    public static long getAppVersionCode(Context context) {
        long appVersionCode = 0;
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                appVersionCode = packageInfo.getLongVersionCode();
            } else {
                appVersionCode = packageInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("", e.getMessage());
        }
        return appVersionCode;
    }

    /**
     * 获取当前app version name
     */
    public static String getAppVersionName(Context context) {
        String appVersionName = "";
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            appVersionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("", e.getMessage());
        }
        return appVersionName;
    }

    //去除ActionBar
    public static void removeActionBar(Activity activity)
    {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    //设置ActionBar
    public static void displayActionBar(Activity activity)
    {
        activity.requestWindowFeature(Window.FEATURE_ACTION_BAR);
    }

}
