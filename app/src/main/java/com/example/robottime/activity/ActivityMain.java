/*
 蓝牙设备列表页java
 2019/1/12
 */
package com.example.robottime.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.robottime.R;
import com.example.robottime.CustomDialog;
import com.example.robottime.StaticVariable;
import com.example.robottime.utils.CommonUtil;
import com.example.robottime.utils.RtHttpUtil;
import com.example.robottime.utils.SystemUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ActivityMain extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "ActivityMain";
    private static final int BAIDU_READ_PHONE_STATE = 100;//定位权限请求
    private static final int PRIVATE_CODE = 1315;//开启GPS权限
    static final String[] LOCATIONGPS = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE};

    private ProgressDialog progressDialog;
    private ToggleButton bleButton;
    private TextView app_info;

    private Handler mMsgHander;

    private String device_info = ""; //存储当前所连接设备信息

    //版本更新
    private JSONObject jsonObject;
    private String versionName;
    private int versionCode;
    private String content;
    private String url;
    // 更新
    private static final int UPDATE_YES = 1;
    // 不更新
    private static final int UPDATE_NO = 2;
    // URL错误
    private static final int URL_ERROR = 3;
    // 没有网络
    private static final int IO_ERROR = 4;
    // 数据异常
    private static final int JSON_ERROR = 5;

    private PackageInfo packageInfo;

    //连接模式提示框
    private CustomDialog connModeDialog;

    private RadioButton bleconRBtn;
    private RadioButton otgconRBtn;
    private RadioButton usbconRBtn;

    private TextView conn_cancel;
    private TextView conn_confirm;

    //反馈提示框
    private CustomDialog fadeBackDialog;
    private EditText fadeback_content;
    private boolean isFadeBackComplete = false; //重复判断

    private TextView fadeback_cancel;
    private TextView fadeback_submit;
    private TextView fadeback_status;
    //请求地址
    private static final String fadeback_url = "https://www.wiyixiao4.com/blog/?plugin=gbook";
    private static final int FB_IO_ERROR = 100;
    private static final int FB_SUCCESS = 101;
    private static final int FB_FALL = 102;
    private static final int FB_WORKING = 103;

    // 升级提示框
    private CustomDialog updateDialog;
    private TextView dialog_update_content;
    private TextView dialog_confrim;
    private TextView dialog_cancel;
    //下载进度框
    private ProgressDialog loadProcessDialog;

    //下载文件保存路径
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

//        getGPSPermission();
        initView();
    }

    private void initView(){
        findViewById(R.id.device_conn).setOnClickListener(this);
        app_info = (TextView) findViewById(R.id.app_about);

        //textview滑动
        app_info.setMovementMethod(ScrollingMovementMethod.getInstance());
        //进度加载弹出框
        progressDialog = new ProgressDialog(this);
        //下载进度框
        loadProcessDialog = new ProgressDialog(this);
        loadProcessDialog.setCancelable(false);
    }

    //获取提交参数
    private HashMap<String, String> getFadeBackParams(String content)
    {
        HashMap<String,String> parasMap = new HashMap<>();
        parasMap.put("requestid", CommonUtil.getRequestId()); //获取32位随机数作为requestID
        parasMap.put("act",RtHttpUtil.AddFadeBackMsg); //请求方法
        parasMap.put("nickname", CommonUtil.getRString(ActivityMain.this, R.string.app_name)); //应用名称
        parasMap.put("version", CommonUtil.getAppVersionName(ActivityMain.this)); //版本号
        parasMap.put("content",content); //反馈内容
        parasMap.put("model",SystemUtil.getSystemModel()); //获取手机型号
        parasMap.put("uid","1"); //默认

        return parasMap;
    }

    private void requestHandlerCall(int code, String content)
    {
        isFadeBackComplete = false;
        Message msg = Message.obtain();
        msg.what = code;
        msg.obj = content.toString();
        handler.sendMessage(msg);
    }

    @Override
    protected void onResume ()
    {
        super.onResume();

        showConnModeDialog();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.device_conn: //设备连接
                showConnModeDialog();
                break;
            case R.id.conn_confirm: //连接
                connModeDialog.dismiss();
                //判断模式
                if(bleconRBtn.isChecked())
                {
                    Intent intent = new Intent(ActivityMain.this,ActivityBleDevice.class);
                    StaticVariable.CURRENT_CONNECT_TYPE = StaticVariable.CONNECT_TYPE_BLE;
                    startActivityForResult(intent, StaticVariable.ACTIVITYBLE_MSG_ID);
                }
                else if(otgconRBtn.isChecked())
                {
                    Intent intent = new Intent(ActivityMain.this, ActivityUsbDevice.class);
                    //intent.putExtra(StaticVariable.CONNECT_TYPE, StaticVariable.CONNECT_TYPE_OTG);
                    StaticVariable.CURRENT_CONNECT_TYPE = StaticVariable.CONNECT_TYPE_OTG;
                    startActivityForResult(intent, StaticVariable.ACTIVITYUSB_MSG_ID);
                }
                else if(usbconRBtn.isChecked())
                {
                    Intent intent = new Intent(ActivityMain.this, ActivityUsbDevice.class);
                    //intent.putExtra(StaticVariable.CONNECT_TYPE, StaticVariable.CONNECT_TYPE_VIRTUAL);
                    StaticVariable.CURRENT_CONNECT_TYPE = StaticVariable.CONNECT_TYPE_VIRTUAL;
                    startActivityForResult(intent, StaticVariable.ACTIVITYUSB_MSG_ID);
                }
                break;
            case R.id.conn_cancel: //取消连接
                connModeDialog.dismiss();
                break;
            case R.id.dialog_cancel:    //取消更新，关闭更新弹窗
                updateDialog.dismiss();
                break;
            case R.id.dialog_confrim:   //确定更新，关闭更新弹窗，开始下载文件
                updateDialog.dismiss();
                downloadAPK();
                break;
            case R.id.fadeback_cancel:
                fadeBackDialog.dismiss();
                break;
            case R.id.fadeback_submit:
                String content = fadeback_content.getText().toString();

                if(content.equals(""))
                {
                    CommonUtil.toastTextShow(ActivityMain.this, "内容不能为空");
                }
                else
                {
                    if(isFadeBackComplete)
                    {
                        requestHandlerCall(FB_WORKING,"反馈提交中，请等待完成！");
                    }
                    else
                    {
                        isFadeBackComplete = true;
                        RtHttpUtil http = new RtHttpUtil();
                        fadeback_status.setText("提交中...");
                        http.postData(fadeback_url, getFadeBackParams(content), new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                requestHandlerCall(FB_IO_ERROR,"");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try
                                {
                                    final String result = response.body().string();
                                    if(!TextUtils.isEmpty(result))
                                    {
                                        //解析
                                        JSONObject obj = new JSONObject(result);
                                        int code = obj.getInt("result");
                                        String content = obj.getString("content");

                                        switch (code)
                                        {
                                            case 0:
                                                requestHandlerCall(FB_SUCCESS,content);
                                                break;
                                            case 1:
                                                requestHandlerCall(FB_FALL, content);
                                                break;
                                        }
                                    }
                                }
                                catch (Exception ex)
                                {
                                    Log.d(TAG, ex.getMessage());
                                }
                            }
                        });
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed(){
//        closeBlueTooth();
        finish();
    }

    //获取位置权限
//    private void getGPSPermission(){
//        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//        if(gps){ //开启了定位服务
//            if(Build.VERSION.SDK_INT >= 23){ //判断是否为6.0以上系统，如果是，需动态添加权限
//                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED){ //没有权限，申请权限
//                    ActivityCompat.requestPermissions(this, LOCATIONGPS,
//                            PRIVATE_CODE);
//                }else{
//                    //
//                    Log.d(TAG, "");
//                }
//            }else{
//                //
//                Log.d(TAG, "");
//            }
//        }else{
//            Toast.makeText(this,"应用请求开启定位服务",Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent();
//            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivityForResult(intent,PRIVATE_CODE);
//        }
//    }

    //菜单选项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.item_0:
                getJSON();
                break;
            case R.id.item_1:
                CommonUtil.toastTextShow(ActivityMain.this, "主题设置");
                break;
            case R.id.item_2:
                showFadeBackDialog();
                break;
//            case R.id.item_3:
//                app_info.setVisibility(View.VISIBLE);
//                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //连接模式弹窗
    private void showConnModeDialog()
    {
        if(connModeDialog == null)
        {
            connModeDialog = new CustomDialog(this, 0,0,R.layout.dialog_conn_mode,
                    R.style.Theme_dialog, Gravity.CENTER,0);
        }

        connModeDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });

        //模式按钮
        bleconRBtn = (RadioButton) connModeDialog.findViewById(R.id.blecon_rbtn);
        otgconRBtn = (RadioButton) connModeDialog.findViewById(R.id.otgcon_rbtn);
        usbconRBtn = (RadioButton) connModeDialog.findViewById(R.id.usbcon_rbtn);
        bleconRBtn.setOnClickListener(this);
        otgconRBtn.setOnClickListener(this);
        usbconRBtn.setOnClickListener(this);

        //连接
        conn_cancel = (TextView) connModeDialog.findViewById(R.id.conn_cancel);
        conn_cancel.setOnClickListener(this);
        //取消
        conn_confirm = (TextView) connModeDialog.findViewById(R.id.conn_confirm);
        conn_confirm.setOnClickListener(this);

        connModeDialog.show();

    }

    //反馈弹窗
    private void showFadeBackDialog(){
        fadeBackDialog = new CustomDialog(this, 0, 0, R.layout.dialog_feedback,
                R.style.Theme_dialog, Gravity.CENTER, 0);
        fadeBackDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                //clearScreen();
            }
        });

        //取消反馈
        fadeback_cancel = (TextView) fadeBackDialog
                .findViewById(R.id.fadeback_cancel);
        fadeback_cancel.setOnClickListener(this);
        //提交反馈
        fadeback_submit = (TextView) fadeBackDialog
                .findViewById(R.id.fadeback_submit);
        fadeback_submit.setOnClickListener(this);
        //反馈内容
        fadeback_content = (EditText) fadeBackDialog
                .findViewById(R.id.fadeback_content);
        //状态
        fadeback_status = (TextView) fadeBackDialog
                .findViewById(R.id.fadeback_status);

        fadeBackDialog.show();
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            progressDialog.dismiss();
            switch (msg.what){
                case UPDATE_YES:
                    showUpdateDialog();
                    break;
                case UPDATE_NO:
                    CommonUtil.toastTextShow(ActivityMain.this, R.string.no_update);
                    break;
                case URL_ERROR:
                    CommonUtil.toastTextShow(ActivityMain.this, "地址错误");
                    break;
                case IO_ERROR:
                    CommonUtil.toastTextShow(ActivityMain.this, "服务器未响应，请稍后检查更新");
                    break;
                case JSON_ERROR:
                    CommonUtil.toastTextShow(ActivityMain.this, "解析错误");
                    break;
                case FB_IO_ERROR:
                    fadeback_status.setText("");
                    CommonUtil.toastTextShow(ActivityMain.this, "提交未完成，请稍后重试");
                    break;
                case FB_WORKING:
                    CommonUtil.toastTextShow(ActivityMain.this, (String)msg.obj);
                    break;
                case FB_SUCCESS:
                    fadeback_status.setText("");
                    fadeback_content.setText("");
                    CommonUtil.toastTextShow(ActivityMain.this,(String) msg.obj);
                    break;
                case FB_FALL:
                    fadeback_status.setText("");
                    CommonUtil.toastTextShow(ActivityMain.this, (String) msg.obj);
                    break;
            }

            return false;
        }
    });

    /*
       解析json
       参考链接：https://yq.aliyun.com/articles/46261
    */
    private void getJSON() {
        //显示正在检测更新
        //progressDialog.setCancelable(false);

        progressDialog.setMessage(getResources().getString(R.string.check_update_begin));
        progressDialog.show();

        // 子线程访问，耗时操作
        new Thread() {
            public void run() {

                Message msg = Message.obtain();

                // 开始访问网络的时间
                long startTime = System.currentTimeMillis();

                try {
                    // JSON地址

//                    HttpURLConnection conn = (HttpURLConnection) new URL(
//                            // 模拟器一般有一个预留IP：10.0.2.2
//                            "http://39.96.185.66/update/ble_check_update.json")
//                            .openConnection();

                    //更改更新服务器地址
                    HttpURLConnection conn = (HttpURLConnection) new URL(
                            // 模拟器一般有一个预留IP：10.0.2.2
                            getResources().getString(R.string.update_url))
                            .openConnection();

                    // 请求方式GRT
                    conn.setRequestMethod("GET");
                    // 连接超时
                    conn.setConnectTimeout(5000);
                    // 响应超时
                    conn.setReadTimeout(3000);
                    // 连接
                    conn.connect();
                    // 获取请求码
                    int responseCode = conn.getResponseCode();
                    // 等于200说明请求成功
                    if (responseCode == 200) {
                        // 拿到他的输入流
                        InputStream in = conn.getInputStream();
                        String stream = CommonUtil.toStream(in);

                        Log.i("JSON", stream);
                        jsonObject = new JSONObject(stream);
                        versionName = jsonObject.getString("versionName");
                        versionCode = jsonObject.getInt("versionCode");
                        content = jsonObject.getString("content");
                        url = jsonObject.getString("url");

                        // 版本判断
                        if (versionCode > CommonUtil.getAppVersionCode(ActivityMain.this)) {
                            // 提示更新
                            msg.what = UPDATE_YES;
                        } else {
                            // 不更新，跳转到主页
                            msg.what = UPDATE_NO;
                        }
                    }

                } catch (MalformedURLException e) {
                    // URL错误
                    e.printStackTrace();
                    msg.what = URL_ERROR;
                } catch (IOException e) {
                    // 没有网络
                    e.printStackTrace();
                    msg.what = IO_ERROR;
                } catch (JSONException e) {
                    // 数据错误
                    e.printStackTrace();
                    msg.what = JSON_ERROR;
                } finally {

                    // 网络访问结束的时间
                    long endTime = System.currentTimeMillis();
                    // 计算网络用了多少时间
                    long time = endTime - startTime;

                    try {
                        if (time < 3000) {
                            // 停留三秒钟
                            Thread.sleep(3000 - time);
                        }
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // 全部走完发消息
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

    /*
        更新弹窗
     */
    private void showUpdateDialog(){
        updateDialog = new CustomDialog(this, 0, 0, R.layout.dialog_update,
                R.style.Theme_dialog, Gravity.CENTER, 500);
        //如果他点击其他地方，不安装
        updateDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                //clearScreen();
            }
        });
        // 更新内容
        dialog_update_content = (TextView) updateDialog
                .findViewById(R.id.dialog_update_content);
        //textview滑动
        dialog_update_content.setMovementMethod(ScrollingMovementMethod.getInstance());
        //内容显示
        dialog_update_content.setText((String)content);

        // 确定更新
        dialog_confrim = (TextView) updateDialog
                .findViewById(R.id.dialog_confrim);
        dialog_confrim.setOnClickListener(this);
        // 取消更新
        dialog_cancel = (TextView) updateDialog
                .findViewById(R.id.dialog_cancel);
        dialog_cancel.setOnClickListener(this);
        updateDialog.show();
    }

    //下载文件
    private void downloadAPK() {
        // 判断是否有SD卡
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/robottime_ble_release.apk";
            HttpUtils httpUtils = new HttpUtils();

            //1.网络地址 2.存放地址 3.回调
            loadProcessDialog.setTitle("下载中...");
            loadProcessDialog.show();
            httpUtils.download(url, path, new RequestCallBack<File>() {

                //下载进度
                @Override
                public void onLoading(long total, long current,
                                      boolean isUploading) {
                    // TODO Auto-generated method stub
                    super.onLoading(total, current, isUploading);

                    //显示进度
                    loadProcessDialog.setMessage(100 * current / total + "%");

                }
                //下载完成

                // 成功
                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    //下载完成，关闭进度框
                    loadProcessDialog.dismiss();
                    //Android 6.0以上版本需要临时获取权限
                    /*
                    if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1&&
                            PackageManager.PERMISSION_GRANTED!=checkSelfPermission(
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        requestPermissions(perms,PERMS_REQUEST_CODE);
                    }
                    */

                    // 跳转系统安装页面
                    File file = new File(path);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri data;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//判断版本大于等于7.0
                        // "com.ansen.checkupdate.fileprovider"即是在清单文件中配置的authorities
                        // 通过FileProvider创建一个content类型的Uri
                        data = FileProvider.getUriForFile(ActivityMain.this,
                                "com.example.robottime.activity.ActivityMain.fileprovider", file);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);// 给目标应用一个临时授权
                    } else {
                        data = Uri.fromFile(file);
                    }
                    intent.setDataAndType(data,"application/vnd.android.package-archive");
                    startActivity(intent);

                }

                // 失败
                @Override
                public void onFailure(HttpException error, String msg) {
                    Log.i("error", msg);
                    CommonUtil.toastTextShow(ActivityMain.this, R.string.download_failed);
                    progressDialog.dismiss();
                }
            });
        } else {
            CommonUtil.toastTextShow(ActivityMain.this, R.string.no_sd_card);
        }

    }

}
