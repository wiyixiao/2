package com.example.robottime.activity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.robottime.R;
import com.example.robottime.StaticVariable;
import com.example.robottime.usb_serial.driver.UsbSerialDriver;
import com.example.robottime.usb_serial.driver.UsbSerialPort;
import com.example.robottime.usb_serial.driver.UsbSerialProber;
import com.example.robottime.utils.CommonUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adapter.UsbDeviceAdapter;
import adapter.UsbSerialAdapter;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

import static com.example.robottime.utils.SystemUtil.RootCommand;

public class ActivityUsbDevice extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "ActivityUsbDevice";

    //usb
    private static final String ACTION_USB_PERMISSION = "com.example.robottime.USB_PERMISSION";
    private static UsbManager usbManager;
    private PendingIntent pendingIntent;
    private List<UsbSerialPort> mEntries = new ArrayList<UsbSerialPort>();
    private List<UsbDevice> usbDevices = new ArrayList<UsbDevice>();
    private UsbDevice usbDevice;    //USB设备
    private UsbSerialPort mSerialPort; //USB串口设备

    private ListView lvUsbDevice;
    private ListView lvSerialDevice;
    private UsbDeviceAdapter usbDeviceAdapter;
    private UsbSerialAdapter usbSerialAdapter;

    //serial
    private SerialPortFinder serialPortFinder;
    private static SerialPort serialPort;
    protected static InputStream mInputStream;
    protected static OutputStream mOutputStream;
    private ReadThread mReadThread;

    private Spinner portSpinner, baudSpinner;
    private ArrayAdapter<String> portAdapter, baudAdapter;

    private static final String[] baudArray = new String[]{
			"300","600","1200","1800","2400","4800","9600",
			"19200","38400","57600","115200"
    };

    private SwipeRefreshLayout usbSwipeRefreshLayout;
    private ProgressDialog progressDialog;

    private LinearLayout otgLayout;
    private LinearLayout vdbgLayout;

    private Button usbconBtn;

    private TextView tvRefresh;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_usb_device);

        initView();
        initConnectType();

    }

    @Override
    public void onStart()
    {
        super.onStart();
        closeDevice();
        refreshDevice();
    }

    @Override
    public void onBackPressed(){
        closeDevice();
        finish();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //取消广播
        if(StaticVariable.CURRENT_CONNECT_TYPE == StaticVariable.CONNECT_TYPE_OTG)
            unregisterReceiver(mUsbReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.usbcon_btn:
                openVitrualDevice();
                break;
        }
    }

    private void initView()
    {
        otgLayout = (LinearLayout)findViewById(R.id.otg_layout);
        vdbgLayout = (LinearLayout)findViewById(R.id.virtualdbg_layout);

        usbSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.usbSwipeRefreshLayout);
        usbconBtn = (Button)findViewById(R.id.usbcon_btn);
        tvRefresh = (TextView)findViewById(R.id.refresh_info);

        //端口号下拉框
        portSpinner = findViewById(R.id.port_spinner);

        //波特率下拉框
        baudAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, baudArray);
        baudAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        baudSpinner = findViewById(R.id.baud_spinner);
        baudSpinner.setAdapter(baudAdapter);

        int defaultBaudPos = baudAdapter.getPosition("9600");
        baudSpinner.setSelection(defaultBaudPos);

        usbconBtn.setOnClickListener(this);

        setSwipe();

    }

    private void initConnectType()
    {
        switch (StaticVariable.CURRENT_CONNECT_TYPE)
        {
            case StaticVariable.CONNECT_TYPE_OTG:
                otgLayout.setVisibility(View.VISIBLE);
                vdbgLayout.setVisibility(View.GONE);
                usbconBtn.setVisibility(View.GONE);

                lvUsbDevice = (ListView)findViewById(R.id.lv_usb_device);
                lvSerialDevice = (ListView)findViewById(R.id.lv_serial_device);

                //获取usb设备管理器
                usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                IntentFilter usbFilter = new IntentFilter(ACTION_USB_PERMISSION);
                usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED); //usb 插入广播
                usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED); //usb 拔出广播
                registerReceiver(mUsbReceiver, usbFilter);

                usbDeviceAdapter = new UsbDeviceAdapter(this);

                usbSerialAdapter = new UsbSerialAdapter(this){
                    @SuppressLint("InflateParams")
                    @Override
                    public View getView(int position, View converView, ViewGroup parent)
                    {
                        if(converView == null)
                        {
                            final LayoutInflater inflater =
                                    (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            assert inflater != null;
                            converView = inflater.inflate(R.layout.item_otgdevice_list, null);
                            final TextView deviceInfo = (TextView) converView.findViewById(R.id.otg_info);
                            final TextView driverInfo = (TextView)converView.findViewById(R.id.otg_driver);

                            final UsbSerialPort port = mEntries.get(position);
                            final UsbSerialDriver driver = port.getDriver();
                            final UsbDevice device = driver.getDevice();

                            deviceInfo.setText(String.format("%s - VPID(%4X:%4X)",
                                    device.getProductName() ,
                                    device.getVendorId(),
                                    device.getProductId()));
                            driverInfo.setText(driver.getClass().getSimpleName());

                        }

                        return converView;
                    }
                };

                break;
            case StaticVariable.CONNECT_TYPE_VIRTUAL:
                otgLayout.setVisibility(View.GONE);
                vdbgLayout.setVisibility(View.VISIBLE);

                //请求root权限
                String apkRoot="chmod 777 "+getPackageCodePath();
                RootCommand(apkRoot);
                break;
        }
    }

    private void setSwipe() {
        /*
         * 设置下拉刷新球颜色
         */
        usbSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,R.color.colorBackGround,R.color.colorDarkGreen);
        /*
         * 设置下拉刷新的监听
         */
        usbSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新需执行的操作
                refreshDevice();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void initUsb()
    {
        try {
            usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            assert usbManager != null;
            HashMap<String, UsbDevice> map = usbManager.getDeviceList();

            usbDevices.clear();
            usbDevices.addAll(map.values());
            usbDeviceAdapter.setList(usbDevices);
            lvUsbDevice.setAdapter(usbDeviceAdapter);

            if(usbDevices.size() > 0)
            {
                lvUsbDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    usbDevice = usbDevices.get(position);
                    if(usbDevice != null)
                    {
                        CommonUtil.toastTextShow(ActivityUsbDevice.this, "打开串口设备");
                    }
                    }
                });
            }

            new AsyncTask<Void, Void, List<UsbSerialPort>>() {
                @Override
                protected List<UsbSerialPort> doInBackground(Void... params) {
                    Log.d(TAG, "Refreshing device list ...");

                    final List<UsbSerialDriver> drivers =
                            UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);

                    final List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();
                    for (final UsbSerialDriver driver : drivers) {
                        final List<UsbSerialPort> ports = driver.getPorts();
                        Log.d(TAG, String.format("+ %s: %s port%s",
                                driver, Integer.valueOf(ports.size()), ports.size() == 1 ? "" : "s"));
                        result.addAll(ports);
                    }

                    return result;
                }

                @Override
                protected void onPostExecute(List<UsbSerialPort> result) {
                    mEntries.clear();
                    mEntries.addAll(result);
//                    usbSerialAdapter.notifyDataSetChanged();
                    usbSerialAdapter.setList(mEntries);
                    lvSerialDevice.setAdapter(usbSerialAdapter);

                    if(mEntries.size() > 0)
                    {
                        lvSerialDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if (position >= mEntries.size()) {
                                    Log.w(TAG, "Illegal position.");
                                    return;
                                }

                                mSerialPort = mEntries.get(position);
                                UsbDevice device = mSerialPort.getDriver().getDevice();
                                if (!usbManager.hasPermission(device)) {
                                    usbManager.requestPermission(device, pendingIntent);
                                }
                                else
                                {
                                    CommonUtil.toastTextShow(ActivityUsbDevice.this, "打开串口设备");
                                }

                            }
                        });
                    }
                }

            }.execute((Void) null);

        } catch (Exception e) {
            e.printStackTrace();
            CommonUtil.toastTextShow(ActivityUsbDevice.this, "设备获取异常");
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            switch (action) {
                case ACTION_USB_PERMISSION:
                    synchronized (this) {
                        UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (usbDevice != null) {
                                //读取usbDevice里的内容
                                CommonUtil.toastTextShow(ActivityUsbDevice.this, "权限获取成功");
                            }
                        } else {
                            CommonUtil.toastTextShow(ActivityUsbDevice.this, "权限获取失败");
                        }
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    CommonUtil.toastTextShow(ActivityUsbDevice.this, "设备断开连接");
                    refreshDevice();
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    //当设备插入时执行具体操作
                    CommonUtil.toastTextShow(ActivityUsbDevice.this, "设备接入");
                    refreshDevice();
                    break;
            }
        }
    };

    private void searchOtgDevice()
    {
        stopSwipeRefresh();

        initUsb();
    }

    private void searchVirtualDevice()
    {
        //获取端口列表
        String[] portArray = new SerialPortFinder().getAllDevicesPath();

        portAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, portArray);
        portAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        portSpinner.setAdapter(portAdapter);

        //关闭下拉刷新球
        stopSwipeRefresh();

        if(portArray.length == 0)
        {
            CommonUtil.toastTextShow(ActivityUsbDevice.this, "访问系统文件失败");
        }
        else
        {
            usbconBtn.setEnabled(true);
        }
    }

    private void openOtgDevice()
    {

    }

    private void openVitrualDevice()
    {
        try
        {
            //模拟器挂载PC串口设备
            //emulator @Nexus_5_API_21 -qemu -serial COM2
            try {

                if(serialPort != null)
                {
                    CommonUtil.toastTextShow(ActivityUsbDevice.this, "设备已连接");
                    return;
                }

                String port = portSpinner.getSelectedItem().toString();

                serialPort = new SerialPort(new File(port),
                        Integer.parseInt(baudSpinner.getSelectedItem().toString()),
                        0);

                mInputStream = serialPort.getInputStream();
                mOutputStream = serialPort.getOutputStream();

                //mReadThread = new ReadThread();
                //mReadThread.start();

                CommonUtil.toastTextShow(ActivityUsbDevice.this, "连接成功");

                //跳转至功能选择页面
                Intent intent = new Intent(ActivityUsbDevice.this, ActivityFunPanel.class);
                StaticVariable.CURRENT_CONNECT_DEVICE = "设备状态-已连接:\b" + port;
                startActivityForResult(intent, StaticVariable.ACTIVITYFUN_MSG_ID);

            } catch (SecurityException e) {
                CommonUtil.toastTextShow(ActivityUsbDevice.this, "连接异常");
                e.printStackTrace();
            } catch (IOException e) {
                CommonUtil.toastTextShow(ActivityUsbDevice.this, "连接失败");
                e.printStackTrace();
            }

        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
    }

    //模拟器串口接收线程
    private class ReadThread extends Thread
    {
        @Override
        public void run()
        {
            super.run();

            while(!isInterrupted())
            {
                int size;
                Log.i("test", "接收线程已经开启");
                try
                {

                    byte[] buffer = new byte[64];

                    if (mInputStream == null || !serialPort.isOpen)
                        return;

                    size = mInputStream.read(buffer); //接收大于8字节数据模拟器卡死

                    if (size > 0)
                    {
                        onDataReceived(buffer, size);
                    }

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable(){
            public void run(){
                String recinfo = new String(buffer, 0, size);
                Log.i("test", "接收到串口信息======" + recinfo);
            }
        });
    }


    //刷新设备
    private void refreshDevice()
    {
        switch (StaticVariable.CURRENT_CONNECT_TYPE)
        {
            case StaticVariable.CONNECT_TYPE_OTG:
                searchOtgDevice();
                break;
            case StaticVariable.CONNECT_TYPE_VIRTUAL:
                searchVirtualDevice();
                break;
        }
    }


    //关闭设备连接
    private void closeDevice()
    {
        switch (StaticVariable.CURRENT_CONNECT_TYPE)
        {
            case StaticVariable.CONNECT_TYPE_OTG:
                if(usbManager != null)
                {
                    usbManager = null;
                }
                break;
            case StaticVariable.CONNECT_TYPE_VIRTUAL:
                if(serialPort != null)
                {
                    serialPort.closeSerial();
                    serialPort = null;
                    mInputStream = null;
                    mOutputStream = null;
                }
                break;
        }
    }

    public static void sendData(String data){

        try
        {
            switch (StaticVariable.CURRENT_CONNECT_TYPE)
            {
                case StaticVariable.CONNECT_TYPE_OTG:
                    break;
                case StaticVariable.CONNECT_TYPE_VIRTUAL:
                    if(serialPort.isOpen)
                    {
                        mOutputStream.write(data.getBytes());
                    }
                    break;
            }
        }
        catch (IOException e)
        {
            Log.i(TAG, "串口发送失败");
            e.printStackTrace();
        }
    }

    public static void sendDataBytes(byte[] message)
    {
        try
        {
            //hex发送以FF开头，以FF结尾
            switch (StaticVariable.CURRENT_CONNECT_TYPE)
            {
                case StaticVariable.CONNECT_TYPE_OTG:
                    break;
                case StaticVariable.CONNECT_TYPE_VIRTUAL:
                    if(serialPort.isOpen)
                    {
                        mOutputStream.write(message);
                    }
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void stopSwipeRefresh()
    {
        //关闭下拉刷新球
        if (usbSwipeRefreshLayout.isRefreshing()) {
            usbSwipeRefreshLayout.setRefreshing(false);
        }
    }
}
