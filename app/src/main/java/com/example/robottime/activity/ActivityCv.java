package com.example.robottime.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import android_opencv_api.CvSelector;
import com.example.robottime.R;
import com.example.robottime.StaticVariable;
import com.example.robottime.utils.CommonUtil;
import android_opencv_api.CvHelper;
import com.example.robottime.utils.DataFormat;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class ActivityCv extends AppCompatActivity implements View.OnClickListener,CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "CvTrackingActivity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba;

    //checkbox
    private CheckBox cap_cbox;
    private CheckBox cv_set_cbox;
    //textview
    private TextView ver_text;
    //radiobtn
    private RadioButton color_mode_btn;
    private RadioButton face_mode_btn;
    //TextView
    private static TextView pos_textview;
    //bar
    private SeekBar vmin_bar;
    private SeekBar vmax_bar;
    private SeekBar smin_bar;
    private SeekBar scale_bar;
    //linearlayout panel
    private LinearLayout cv_control_panel;
    private LinearLayout color_bar_panel;
    private LinearLayout cv_set_panel;
    private LinearLayout cv_selector_panel;
    //spinner
    private Spinner dpi_spinner;
    private ArrayAdapter<String> dpi_adapter;
    private String[] dpiArray = new String[]{"1280x720", "800x480", "640x480", "320x240"};
    //selector
    private CvSelector selector;

    //Battery
    private TextView battery_info;
    private BattryBroadCastReceiverTalk mBatInfoReceiver;
    private int BatteryN; //电池电量
    private int BatteryV; //电池电压
    private double BatteryT; //电池温度
    private String BatteryStatus; //电池状态
    private String BatteryTemp; //电池使用状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_cv_tracking);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                    != PackageManager.PERMISSION_GRANTED){
//                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1001);
//            }
//        }else {
//
//        }

        //绘制矩形
        //selector = new Selector(this);
        //ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //添加当前自定义View进主布局文件
        //selector = new Selector(this);
        //addContentView(selector, params);

        //LayoutInflater inflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View view=inflater.inflate(R.layout.layout_cv_tracking, null);

        //注册广播，获取电池信息
        //registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        //已获取相机权限//初始化界面控件
        initView();

    }

    //电池状态广播
    @Override
    public void onStart() {

        super.onStart();
        //创建
        if (mBatInfoReceiver == null) {
            mBatInfoReceiver = new BattryBroadCastReceiverTalk();
            registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //关闭摄像头
        disableCamera();
        //注销
        if (mBatInfoReceiver != null) {
            unregisterReceiver(mBatInfoReceiver);
            mBatInfoReceiver = null;
        }
    }

    private class BattryBroadCastReceiverTalk extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            /*
             * 如果捕捉到的action是ACTION_BATTERY_CHANGED， 就运行onBatteryInfoReceiver()
             */
            if (Intent.ACTION_BATTERY_CHANGED.equals(action))
            {
                BatteryN = intent.getIntExtra("level", 0);	  //目前电量
                BatteryV = intent.getIntExtra("voltage", 0);  //电池电压
                BatteryT = intent.getIntExtra("temperature", 0);  //电池温度
                battery_info.setTextColor(getResources().getColor(R.color.colorDarkGreen));

                switch (intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN))
                {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        BatteryStatus = "充电状态";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        BatteryStatus = "放电状态";
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        BatteryStatus = "未充电";
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        BatteryStatus = "充满电";
                        break;
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        BatteryStatus = "未知状态";
                        break;
                }

                switch (intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN))
                {
                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        BatteryTemp = "未知错误";
                        break;
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        BatteryTemp = "状态良好";
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        BatteryTemp = "电池没有电";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        BatteryTemp = "电池电压过高";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        BatteryTemp =  "电池过热";
                        battery_info.setTextColor(getResources().getColor(R.color.colorRed));
                        break;
                }
                battery_info.setText("电量：" + BatteryN + "%" + "\r\n" +
                                     "电压：" + BatteryV + "mV" + "\r\n" +
                                     "温度：" + (BatteryT*0.1) + "℃" + "\r\n" +
                                     "状态：" + BatteryStatus + "--" + BatteryTemp
                                    );
                String data = String.format("电量：%d",BatteryN);
            }
        }
    };

    //控件初始化
    private void initView()
    {
        //checkbox
        cap_cbox = findViewById(R.id.cap_cbox);
        cv_set_cbox = findViewById(R.id.cv_set_cbox);
        //radiobtn
        color_mode_btn = findViewById(R.id.color_mode_btn);
        face_mode_btn = findViewById(R.id.face_mode_btn);
        //TextView
        pos_textview = findViewById(R.id.pos_textview);
        battery_info = findViewById(R.id.battery_info);
        //bar
        vmin_bar = findViewById(R.id.vmin_bar);
        vmax_bar = findViewById(R.id.vmax_bar);
        smin_bar = findViewById(R.id.smin_bar);
        scale_bar = findViewById(R.id.scale_bar);
        //textview
        ver_text = findViewById(R.id.ver_text);
        String version = "OpenCv: " + CvHelper.getVersion();
        ver_text.setText(version); //显示opencv版本号
        //
        cv_control_panel = findViewById(R.id.cv_control_panel);
        color_bar_panel = findViewById(R.id.color_bar_panel);
        cv_set_panel = findViewById(R.id.cv_set_panel);
        cv_selector_panel = findViewById(R.id.cv_selector_panel);
        //spinner//分辨率下拉框
        dpi_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dpiArray);
        dpi_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dpi_spinner = findViewById(R.id.dpi_spinner);
        dpi_spinner.setAdapter(dpi_adapter);

        //cameraview
        mOpenCvCameraView = findViewById(R.id.cameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.enableFpsMeter();  //显示fps信息

        //selector
        selector = new CvSelector(this);

        //注册事件
        cv_set_cbox.setOnClickListener(this);
        cap_cbox.setOnClickListener(this);
        color_mode_btn.setOnClickListener(this);
        face_mode_btn.setOnClickListener(this);

        vmin_bar.setOnSeekBarChangeListener(osbcl);
        vmax_bar.setOnSeekBarChangeListener(osbcl);
        smin_bar.setOnSeekBarChangeListener(osbcl);
        scale_bar.setOnSeekBarChangeListener(osbcl);

        vmin_bar.setProgress(CvHelper.defaultBarsPro[0]);
        vmax_bar.setProgress(CvHelper.defaultBarsPro[1]);
        smin_bar.setProgress(CvHelper.defaultBarsPro[2]);
        scale_bar.setProgress(CvHelper.defaultBarsPro[3]);

        dpi_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            private String positions;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                positions = dpi_adapter.getItem(position);

                TextView tv = (TextView)view;
                tv.setTextColor(getResources().getColor(R.color.colorWhite));    //设置颜色
                tv.setTextSize(16.0f);    //设置大小
                tv.setGravity(android.view.Gravity.CENTER_HORIZONTAL);   //设置居中

                if (positions.equals(dpiArray[0])) {
                    CommonUtil.toastTextShow(ActivityCv.this,R.string.max_dpi);
                }else if(positions.equals(dpiArray[1])){
                    CommonUtil.toastTextShow(ActivityCv.this,R.string.midu_dpi);
                } else if (positions.equals(dpiArray[2])) {
                    CommonUtil.toastTextShow(ActivityCv.this,R.string.midd_dpi);
                } else if (positions.equals(dpiArray[3])) {
                    CommonUtil.toastTextShow(ActivityCv.this,R.string.min_dpi);
                }

                try
                {
                    //停止相机
                    disableCamera();
                    //设置参数
                    int w = Integer.parseInt(positions.split("x")[0]);
                    int h = Integer.parseInt(positions.split("x")[1]);
                    mOpenCvCameraView.setMaxFrameSize(w, h);
                    //开启相机
                    enableCamera();

                }
                catch (Exception ex)
                {
                    ;
                }

                parent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setVisibility(View.VISIBLE);
            }
        });
    }

    // 设置拖动条改变监听器
    SeekBar.OnSeekBarChangeListener osbcl = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            int pro = seekBar.getProgress();
            switch (seekBar.getId())
            {
                case R.id.vmin_bar:
                case R.id.vmax_bar:
                case R.id.smin_bar:
                {
                    CvHelper.setHsv(
                        vmin_bar.getProgress(),
                        vmax_bar.getProgress(),
                        smin_bar.getProgress()
                    );
                }
                    break;
                case R.id.scale_bar:
                {
                    CvHelper.setScale(scale_bar.getProgress() / 10.0);
                }
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //start
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //stop
        }

    };

    @Override
    public void onClick(View v){
        int id = v.getId();
        switch (id){
            case R.id.cv_set_cbox:
                if(cv_set_cbox.isChecked())
                {
                    //进入设置
                    cv_selector_panel.setVisibility(View.INVISIBLE);
                    //cv_set_panel.setVisibility(View.VISIBLE);
                    //显示参数调节面板
                    cv_control_panel.setVisibility(View.VISIBLE);
                }
                else
                {
                    //取消设置
                    cv_selector_panel.setVisibility(View.VISIBLE);
                    //cv_set_panel.setVisibility(View.INVISIBLE);
                    //隐藏参数调节面板
                    cv_control_panel.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.cap_cbox:
                if(cap_cbox.isChecked())
                {
                    enableCamera();
                    //ActivityMain.sendDataBytes(DataFormat.posToBytes(1280, 10, 10000));
                }
                else
                {
                    disableCamera();
                    //ActivityMain.sendDataBytes(DataFormat.posToBytes(3333, 6666, 9999));
                }
                break;
            case R.id.color_mode_btn:
                //颜色跟踪模式
                color_mode_btn.setChecked(true);
                face_mode_btn.setChecked(false);

                if(color_mode_btn.isChecked())
                    CvHelper.trackColor();
                break;
            case R.id.face_mode_btn:
                //人脸跟踪模式
                color_mode_btn.setChecked(false);
                face_mode_btn.setChecked(true);

                if(face_mode_btn.isChecked())
                    CvHelper.faceDetect();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        disableCamera();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(ActivityCv.this,ActivityKeyboard.class);
        //intent.putExtra("state",true);
        setResult(RESULT_OK,intent);
        finish();
    }

    public void disableCamera() {
        if (mOpenCvCameraView != null)
        {
            //停止跟踪
            CvHelper.disableTrack();
            mOpenCvCameraView.disableView();
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            cap_cbox.setChecked(false);
        }
    }

    public void enableCamera()
    {
        mOpenCvCameraView.enableView();
        cap_cbox.setChecked(true);

        CvHelper.size.device_width = mOpenCvCameraView.getWidth();
        CvHelper.size.device_height = mOpenCvCameraView.getHeight();

        if(color_mode_btn.isChecked())
            CvHelper.trackColor();
        else if(face_mode_btn.isChecked())
            CvHelper.faceDetect();
    }

    public void onCameraViewStarted(int width, int height) {

    }

    public void onCameraViewStopped() {

    }

    private static Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what)
            {
                case 0:
                    ShowCenterPoint();
                    break;
            }

            return false;
        }
    });

    private static void ShowCenterPoint()
    {
        CvHelper.getObjInfo();
        int x = CvHelper.obj.cx;
        int y = CvHelper.obj.cy;
        int d = CvHelper.obj.cd;

        String pos = "( " + x + " , " + y + " , " + d + " )";

        //发送数据至串口
        switch (StaticVariable.CURRENT_CONNECT_TYPE)
        {
            case StaticVariable.CONNECT_TYPE_BLE:
                ActivityBleDevice.sendDataBytes(DataFormat.posToBytes(x, y, d));
                break;
            case StaticVariable.CONNECT_TYPE_OTG:
            case StaticVariable.CONNECT_TYPE_VIRTUAL:
                ActivityUsbDevice.sendDataBytes(DataFormat.posToBytes(x, y, d));
                break;
        }

        //更新界面显示
        pos_textview.setText(pos);
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Message msg = Message.obtain();

        CvHelper.size.frame_width = mRgba.width();
        CvHelper.size.frame_height = mRgba.height();

        if(CvHelper.revDataHandler(mRgba.getNativeObjAddr()) == 0)
        {
            msg.what = 0;
        }

        handler.sendMessage(msg);

        return mRgba;
    }

}
