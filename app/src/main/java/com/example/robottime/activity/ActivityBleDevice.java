package com.example.robottime.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.robottime.R;
import com.example.robottime.StaticVariable;
import com.example.robottime.utils.CommonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

import adapter.BlueToothDeviceAdapter;

public class ActivityBleDevice extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ActivityBleDevice";

    private static final int PRIVATE_CODE = 1315;//开启GPS权限
    private static final int SECONDACTIVITY_MSG_ID = 22; //页面返回值识别ID
    private static final int BLUETOOTH_MSG_ID = 2; //开启蓝牙权限

    private final int BUFFER_SIZE = 1024;

    private BluetoothAdapter bTAdatper = BluetoothAdapter.getDefaultAdapter();
    private BlueToothDeviceAdapter adapter;
    private BluetoothSocket socket;

    private static InputStream is;
    private static OutputStream os;

    private ConnectThread connectThread; //设备连接线程
    private boolean ble_connect_state = false;  //蓝牙设备连接标志
    private boolean ble_open_state = false; //蓝牙开启标志

    private Handler mMsgHander;

    private SwipeRefreshLayout bleSwipeRefreshLayout;
    private ProgressDialog progressDialog;
    private ListView listView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ble_device);

        initView();
        initReceiver();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        try {

        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        try
        {
            refreshDevice();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    @Override
    public void onBackPressed(){
        closeBlueTooth();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消搜索
        if (bTAdatper != null && bTAdatper.isDiscovering()) {
            bTAdatper.cancelDiscovery();
        }
        //注销BroadcastReceiver，防止资源泄露
        unregisterReceiver(mReceiver);
    }

    private void initView()
    {
        bleSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.bleSwipeRefreshLayout);
        listView = (ListView)findViewById(R.id.ble_list);

        adapter = new BlueToothDeviceAdapter(getApplicationContext(), R.layout.item_bledevice_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(bTAdatper.isDiscovering()){
                    bTAdatper.cancelDiscovery();
                }
                BluetoothDevice device = (BluetoothDevice) adapter.getItem(position);
                //连接设备
                connectDevice(device);
            }
        });

        setSwipe();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case PRIVATE_CODE:
                getBluetoothPermission();
                break;
            case BLUETOOTH_MSG_ID:
                if (resultCode == RESULT_OK) {
                    //Toast.makeText(this, "允许本地蓝牙被附近的其它蓝牙设备发现", Toast.LENGTH_SHORT).show();
                    //开始扫描设备
                    startScanBlueth();
                } else if (resultCode == RESULT_CANCELED) {
                    CommonUtil.toastTextShow(ActivityBleDevice.this, R.string.open_failed_info);
                    finish();
                }
                break;
            case SECONDACTIVITY_MSG_ID:
                if(resultCode == RESULT_OK){
                    ble_connect_state = data.getBooleanExtra("state",true);
                    //刷新设备列表
                    refreshDevice();
                }
                break;
            default:
                break;
        }
    }

    private void setSwipe() {
        /*
         * 设置下拉刷新球颜色
         */
        bleSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,R.color.colorBackGround,R.color.colorDarkGreen);
        /*
         * 设置下拉刷新的监听
         */
        bleSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新需执行的操作
                refreshDevice();
            }
        });
    }

    //获取蓝牙权限
    private void getBluetoothPermission(){
        //弹出对话框提示用户是否打开
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent,BLUETOOTH_MSG_ID);
    }

    //注册广播
    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        //增加绑定状态的变更动作
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //避免重复添加已经绑定过的设备
                if ((device.getBondState() != BluetoothDevice.BOND_BONDED) && adapter.getPosition(device) == -1) {
                    adapter.add(device);
                    adapter.notifyDataSetChanged();
                }
            }else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                //更新蓝牙设备的绑定状态
                if(device.getBondState() == BluetoothDevice.BOND_BONDING){
                    CommonUtil.toastTextShow(ActivityBleDevice.this, R.string.matching_begin);
                }else if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                    CommonUtil.toastTextShow(ActivityBleDevice.this, R.string.matching_complete);
                    //配对成功，刷新设备列表
                    refreshDevice();
                    //停止搜索，更新设备列表信息
                    if(bTAdatper.isDiscovering()){
                        bTAdatper.cancelDiscovery();
                    }
                }else if(device.getBondState() == BluetoothDevice.BOND_NONE){
                    CommonUtil.toastTextShow(ActivityBleDevice.this, R.string.matching_cancel);
                    refreshDevice();
                }

            }else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, CommonUtil.getRString(ActivityBleDevice.this, R.string.scan_begin));
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, CommonUtil.getRString(ActivityBleDevice.this, R.string.scan_complete));
                progressDialog.dismiss();
            }

        }
    };

    //开启蓝牙
    private void openBlueTooth(){
        if(bTAdatper == null){
            CommonUtil.toastTextShow(ActivityBleDevice.this, R.string.no_fct);
            //关闭下拉刷新样式
            stopSwipeRefresh();
            return;
        }
        if(!bTAdatper.isEnabled()){
            //print(R.string.device_opening);
            getBluetoothPermission();
        }
        else{
            //蓝牙开启，刷新设备列表
            ble_open_state=true;
            startScanBlueth();
        }
    }

    //关闭蓝牙
    private void closeBlueTooth(){

        if(bTAdatper == null)
            return;

        if(bTAdatper.isEnabled()) {
            //print(R.string.device_close);
            bTAdatper.disable();
            //清除设备列表
            adapter.clear();
        }
    }

    //搜索设备
    private void startScanBlueth(){
        //判断是否在搜索，如果正在搜索，停止搜索
        if(bTAdatper.isDiscovering()){
            bTAdatper.cancelDiscovery();
        }

        //关闭下拉刷新样式
        stopSwipeRefresh();

        //开始搜索
        getBoundedDevices();
        bTAdatper.startDiscovery();

        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("搜索中...");

        //点击取消搜索
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface arg0) {
                if (bTAdatper != null && bTAdatper.isDiscovering()) {
                    bTAdatper.cancelDiscovery();
                }
            }});
        progressDialog.show();
    }

    //获取已配对设备
    private void getBoundedDevices(){
        //获取已经配对过的设备
        Set<BluetoothDevice> pairedDevices = bTAdatper.getBondedDevices();
        //将其添加到设备列表中
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapter.add(device);
            }
        }
    }

    //连接设备
    private void connectDevice(BluetoothDevice device) {
        /*取消配对
            if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                Method removeMethod = BluetoothDevice.class.getMethod("removeBond", (Class[]) null);
                removeMethod.invoke(device, (Object[]) null);
            }
        */
        try{
            //连接之前先配对
            if(device.getBondState() == BluetoothDevice.BOND_NONE){
                Method creMethod = BluetoothDevice.class.getMethod("createBond");
                creMethod.invoke(device);
            }else if(device.getBondState() == BluetoothDevice.BOND_BONDED && !ble_connect_state){
                try{
                    bTAdatper.cancelDiscovery();
                    //获得一个socket，安卓4.2以前蓝牙使用fun-1，获得socket，4.2后为fun-2

                    //fun-1
                    //配对完成，开始连接
                    //固定UUID，蓝牙串口服务，SerialPortServiceClass_UUID = ‘{00001101-0000-1000-8000-00805F9B34FB}’
                    //final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
                    //UUID uuid = UUID.fromString(SPP_UUID);
                    //socket = device.createInsecureRfcommSocketToServiceRecord(uuid);

                    //fun-2
                    //安卓系统4.2以后的蓝牙通信端口为 1 ，但是默认为 -1，通过反射修改
                    socket =(BluetoothSocket) device.getClass()
                            .getDeclaredMethod("createRfcommSocket",new Class[]{int.class})
                            .invoke(device,1);

                    //##################################################
                    /*
                    device_info += "已连接:\b" +
                                   device.getName() + "\b--\b" +
                                   device.getAddress() + "\b--\b" +
                                   device.getBondState();
                    */
                    //##################################################
                    //只显示连接名称
                    StaticVariable.CURRENT_CONNECT_DEVICE = "设备状态-已连接:\b" +
                            device.getName();

                    //启动连接线程
                    show(R.string.connecting);

                    connectThread = new ActivityBleDevice.ConnectThread(socket, true);
                    connectThread.start();
                }catch (Exception e){
                    //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }else{
                //Toast.makeText(this,"设备已连接,请重新扫描以切换设备",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "");
            }

        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    //断开连接
    private void disConnect() {
        if (ble_connect_state) {
            try {
                //重新扫描设备前关闭socket通信
                socket.close();
                ble_connect_state = false;
            } catch (Exception e) {
                //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, CommonUtil.getRString(ActivityBleDevice.this, R.string.no_devices));
        }
    }

    //刷新设备列表
    private void refreshDevice(){
        if (ble_connect_state || ble_open_state){
            disConnect();
            adapter.clear();
            startScanBlueth();
        }
        else{
            openBlueTooth();
        }
    }

    //连接线程
    private class ConnectThread extends Thread {

        private BluetoothSocket socket;
        private boolean activeConnect;

        private ConnectThread(BluetoothSocket socket, boolean connect) {
            this.socket = socket;
            this.activeConnect = connect;
        }

        @Override
        public void run() {
            try {
                //如果是自动连接 则调用连接方法
                if (activeConnect) {
                    socket.connect();
                }

                ble_connect_state = true;
                progressDialog.dismiss();
                ChangePage(ActivityBleDevice.this,ActivityFunPanel.class); //跳转至功能选择界面

                is = socket.getInputStream(); //获取输入流
                os = socket.getOutputStream(); //获取输出流

                while (true) {
                    try{
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytes;
                        int ch;

                        bytes = 0;
                        while((ch=is.read()) != '\n'){
                            if(ch!=-1){
                                buffer[bytes]=(byte)ch;
                                bytes++;
                            }
                        }
                        //buffer[bytes]=(byte)'\n';
                        //bytes++;

                        String rev_str = new String(buffer);

                        /*
                        Message msg = new Message();
                        msg.obj=new String(buffer);
                        mMsgHander = ActivityKeyboard.mMsgHander;
                        mMsgHander.sendMessage(msg);
                        */

                        Intent intent = new Intent(ActivityKeyboard.ALLTALKS);
                        intent.putExtra("dt",rev_str);
                        sendBroadcast(intent);

                    }catch (Exception e){
                        Looper.prepare();
                        CommonUtil.toastTextShow(ActivityBleDevice.this, R.string.connect_lost);
                        Looper.loop();
                        break;
                    }

                }


            } catch (IOException e) {
                e.printStackTrace();
                Looper.prepare();
                CommonUtil.toastTextShow(ActivityBleDevice.this, R.string.connect_error);
                progressDialog.dismiss();
                Looper.loop();
            }
        }
    }

    //发送数据
    public static void senData(String message){
        try{

            if(os != null){
                os.write(message.getBytes("GBK"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void sendDataBytes(byte[] message)
    {
        try
        {
            //hex发送以FF开头，以FF结尾
            if(os != null)
            {
                os.write(message);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //连接成功后跳转页面
    private void ChangePage(Context curPage, Class tarPage){
        Intent intent = new Intent(curPage,tarPage);
        startActivityForResult(intent, SECONDACTIVITY_MSG_ID);
    }

    private void show(int id){
        progressDialog.setMessage(getResources().getString(id));
        progressDialog.show();
    }

    private void stopSwipeRefresh()
    {
        //关闭下拉刷新球
        if (bleSwipeRefreshLayout.isRefreshing()) {
            bleSwipeRefreshLayout.setRefreshing(false);
        }
    }

}
