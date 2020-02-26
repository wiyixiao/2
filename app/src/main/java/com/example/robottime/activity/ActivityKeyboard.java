/*
    键盘控制、消息发送接收页java
    2019/1/12
 */

package com.example.robottime.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robottime.R;
import com.example.robottime.StaticVariable;
import com.example.robottime.msg.Host;
import com.example.robottime.msg.Slave;
import com.example.robottime.menu.MenuListContentModel;
import com.example.robottime.utils.CommonUtil;
import com.example.robottime.utils.ScreenContent;
import com.example.robottime.utils.DataFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

public class ActivityKeyboard extends AppCompatActivity implements View.OnClickListener{
    private static final int SECONDACTIVITY_MSG_ID = 23; //页面返回识别ID

    private static final int MENU_CONFIRM = 17;
    private static final int INTERVAL_MIN = 100;
    private static final int INTERVAL_MAX = 500;

    private TextView deviceInfoBox;

    private GridLayout keyTwo;
    private GridLayout keyBasic;
    private GridLayout keyExtend;
    private GridLayout keyFn;
    private GridLayout cmdBasic;

    private CheckBox sendDisplayCheckBox;
    private CheckBox keySetBtn;
    private CheckBox cmdBtn;
    private CheckBox hexBtn;

    private AlertDialog.Builder keySetBuilder;
    private AlertDialog.Builder intervalSetBuilder;

    private SharedPreferences cfg;
    private SharedPreferences.Editor editor;

    private EditText name_et;
    private EditText short_press_value_et;
    private EditText long_press_value_et;
    private EditText release_value_et;
    private EditText interval_value_et;

    private EditText editText;
    private static int viewOffset;

    private boolean KEY_TWO_MODE = false;
    private boolean KEY_BASIC_MODE = false;
    private boolean KEY_EXTEND_MODE = true;
    private boolean KEY_FN_MODE = false;

    private static int KEY_TWO_COUNT = 2; //基本控制按键数量2个
    private static int KEY_BASIC_COUNT = 2 * 4; //基本控制键数量5个
    private static int KEY_EXTEND_COUNT = 12;  //扩展键位12个
    private static int KEY_FN_COUNT = 16; //高级键位16个
    private static int KEY_TWO_AREA = 1; //两键区
    private static int KEY_BASIC_AREA = 2; //基本按键区
    private static int KEY_FN_AREA = 3; //高级按键区
    private static int KEY_EXTEND_AREA = 4; //扩展按键区

    private Thread longPressThread; //长按线程
    private int send_interval = 200; //长按发送间隔时间
    private String long_press_value =""; //存储长按时的键值
    private boolean long_press_flag = false; //长按标志
    private boolean key_setting_mode = false; //进入按键设置模式标志

    private static final int LONG_PRESS_COMPLETE = 6;

    private String[] two_name = {"前进","后退"};
    private String[] name = {"前进","左转","停止","右转","后退"};

    //定义屏幕宽高
    private int key_area_width = ScreenContent.displayWidth;
    private int key_area_height = (int)(ScreenContent.displayHeight * 0.4);
    private int key_width;
    private int key_height;
    private android.view.ViewGroup.LayoutParams glp;
    
    //右侧边栏listview
    private DrawerLayout mDrawerLayout;
    private List<MenuListContentModel> mList = new ArrayList<MenuListContentModel>();
    private ListView menuListView;

    //右侧边栏开启按钮
    private ImageView imgMenuBtn;
    //二维码等信息显示顶部按钮
    private ImageView infoMenuBtn;
    //主界面清屏按钮
    private ImageView screenClearBtn;

    //消息框数据
    private ListView listContent;
    private static final int TYPE_HOST = 0;
    private static final int TYPE_SLAVE = 1;
    private ArrayList<Object> mData = null;
    private adapter.MsgAdapter myAdapter = null;

    //广播
    private MyBroadCastReceiverTalk receiverTalk;
    public static final String ALLTALKS = "rev_data";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_keyboard);
        mData = new ArrayList<Object>();

        getCameraPermission();

        initView();
        menuItemInit();
        initData();
    }

    //右侧边栏数据初始化
    private void menuItemInit(){
        //##########################################
        //此选项取消
        //mList.add(new MenuListContentModel(R.mipmap.set_items,setString(R.string.key_value_set),0)); //按键设置/保存
        //此选项取消
        //mList.add(new MenuListContentModel(R.mipmap.set_items,setString(R.string.action_settings_1),0)); //发送间隔设置
        //##########################################
        //禁止侧边栏手动滑出
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        //添加侧边栏选项
        mList.add(new MenuListContentModel(R.mipmap.set_items,setString(R.string.action_settings_0),0)); //两键位
        mList.add(new MenuListContentModel(R.mipmap.set_items,setString(R.string.action_settings_1),1)); //基本键位
        mList.add(new MenuListContentModel(R.mipmap.set_items,setString(R.string.action_settings_2),2)); //扩展键位
        mList.add(new MenuListContentModel(R.mipmap.set_items,setString(R.string.action_settings_3),3)); //高级键位
        mList.add(new MenuListContentModel(R.mipmap.set_items,setString(R.string.action_settings_4),4)); //帮助
        //mList.add(new MenuListContentModel(R.mipmap.set_items,setString(R.string.action_settings_5),5)); //雕刻界面//待完善


        menuListView = (ListView) findViewById(R.id.right_listview);
        adapter.MenuListContentAdapter menuAdapter = new adapter.MenuListContentAdapter(this,mList);
        menuListView.setAdapter(menuAdapter);

        //右侧边栏每项点击事件
        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                clearScreen();//清除显示区域
                switch (position){
                    case 0:
                        showTwoArea();      //设置两键位
                        break;
                    case 1:                 //设置基本键位
                        showBasicArea();
                        break;
                    case 2:                 //设置扩展键位
                        showExtendArea();
                        break;
                    case 3:                 //设置高级键位
                        showFnArea();
                        break;
                    case 4:
                        if(key_setting_mode){
                            CommonUtil.toastTextShow(ActivityKeyboard.this, "请退出按键设置");
                        }else{
                            msgPirnt(setString(R.string.help_info));
                        }
                        break;
                    case 5:
                        //雕刻界面跳转
                        //Intent intent_grbl = new Intent(ActivityKeyboard.this,ActivityGrbl.class);
                        //startActivityForResult(intent_grbl, SECONDACTIVITY_MSG_ID);
                        CommonUtil.toastTextShow(ActivityKeyboard.this, R.string.help_function_adding);
                        break;
                }
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

    }


    //*********广播************
    @Override
    public void onStart() {

        super.onStart();
        //创建
        if (receiverTalk == null) {
            receiverTalk = new MyBroadCastReceiverTalk();
            registerReceiver(receiverTalk, new IntentFilter(ALLTALKS));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销
        if (receiverTalk != null) {
            unregisterReceiver(receiverTalk);
            receiverTalk = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SECONDACTIVITY_MSG_ID:
                if(resultCode == RESULT_OK){
                    //boolean res = data.getBooleanExtra("state",true);
                    //设置屏幕锁定
                    //CommonUtil.toastTextShow(ActivityKeyboard.this, "页面返回成功");
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //返回后设置竖屏
                }
                break;
            default:
                break;
        }
    }

    private class MyBroadCastReceiverTalk extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //这里直接设置接收广播后操作
            listViewAdd(TYPE_HOST,intent.getStringExtra("dt"));
        }
    }

    //初始化界面控件
    private void initView(){
        deviceInfoBox = (TextView) findViewById(R.id.device_info_box);
        listContent = (ListView)findViewById((R.id.list_content));
        keyTwo = (GridLayout)findViewById(R.id.key_two); //两键模式
        keyBasic = (GridLayout) findViewById(R.id.key_basic); //基本键位 / 5 / 前进 / 后退 / 左转/ 右转 /停止
        keyExtend = (GridLayout) findViewById(R.id.key_extend); //扩展键位 /12 / 0 -11
        keyFn = (GridLayout) findViewById(R.id.key_fn); //高级键位 / 16/ 0 - 15
        cmdBasic = (GridLayout) findViewById(R.id.cmd_basic); //命令行 /EditText + Button /输入 + 发送
        editText = (EditText) findViewById(R.id.input_box);
        //################################
        //显示发送取消
        //sendDisplayCheckBox = (CheckBox) findViewById(R.id.display_btn);
        //改为按键设置保存
        keySetBtn = (CheckBox)findViewById(R.id.keyset_btn);
        //################################
        cmdBtn = (CheckBox) findViewById(R.id.cmd_btn);
        //hex发送复选框
        hexBtn = (CheckBox) findViewById(R.id.hex_btn);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        imgMenuBtn = (ImageView) findViewById(R.id.right_menu);
        infoMenuBtn = (ImageView)findViewById(R.id.info_menu);
        screenClearBtn = (ImageView) findViewById(R.id.screen_cls_btn);

        findViewById(R.id.send_btn).setOnClickListener(this);
        cmdBtn.setOnClickListener(this);
        hexBtn.setOnClickListener(this);
        keySetBtn.setOnClickListener(this);
        imgMenuBtn.setOnClickListener(this);
        infoMenuBtn.setOnClickListener(this);
        screenClearBtn.setOnClickListener(this);

        editText.setInputType(TYPE_TEXT_FLAG_MULTI_LINE);
        //editText.setSingleLine(false);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId){
                    case EditorInfo.IME_NULL:
                        //System.out.println("null for default_content: " + v.getText() );
                        break;
                    case EditorInfo.IME_ACTION_SEND:
                        //System.out.println("action send for email_content: "  + v.getText());
                        String cmd = editText.getText().toString();
                        dataSend(cmd);
                        break;
                    case EditorInfo.IME_ACTION_DONE:
                        //System.out.println("action done for number_content: "  + v.getText());
                        break;
                }
                return true;
            }
        });

        //连接信息显示
        deviceInfoBox.setText(StaticVariable.CURRENT_CONNECT_DEVICE);
        //############################################
        //取消默认显示连接状态
        //deviceInfoBox.setText("设备状态：已连接");
        //############################################
    }

    //初始化控件数据
    private void initData(){
        //检测配置文件是否存在
        boolean tmp = fileIsExists("/data/data/com.example.robottime/shared_prefs/configuration.xml");

        Context ctx = ActivityKeyboard.this;
        cfg = ctx.getSharedPreferences("configuration", MODE_PRIVATE);
        //存入数据
        editor = cfg.edit();
        //editor.commit();

        //初始化两键及基本按键宽高
        key_width = (int)(key_area_width / 2);
        key_height = (int)(key_area_height / 3);

        //添加两键
        for(int i=0;i<KEY_TWO_COUNT;i++)
        {
            Button button = new Button(this);
            button.setBackgroundResource(R.drawable.view_radius);
            button.setText(cfg.getString("key_two_" + i,"功能" + i));
            button.setId(i);
            button.setOnLongClickListener(new keyBtnLongClickListener(button, KEY_BASIC_AREA));
            button.setOnTouchListener(new keyBtnTouchListener(button, KEY_TWO_AREA));
            keyTwo.addView(button);

            glp = button.getLayoutParams();
            glp.width = key_width;
            glp.height = key_height;
            button.setLayoutParams(glp);
        }


        //初始化基本键位宽高
        key_width = (int)(key_area_width / 3);
        key_height = (int)(key_area_height / 3);

        //添加基本键位
        for(int i=0,j=0;i<KEY_BASIC_COUNT;i++){
            Button button = new Button(this);
            if(i==0 || i==2 || i==6){
                button.setVisibility(View.GONE);
                keyBasic.addView(button);
            }else{
                //#################################
                //取消设置默认按键名
                //button.setText(name[j]);
                //#################################
                button.setBackgroundResource(R.drawable.view_radius);
                button.setText(cfg.getString("key_basic_" + j,"功能" + j));
                button.setId(j);
                button.setOnLongClickListener(new keyBtnLongClickListener(button, KEY_BASIC_AREA));
                button.setOnTouchListener(new keyBtnTouchListener(button, KEY_BASIC_AREA));
                keyBasic.addView(button);

                glp = button.getLayoutParams();
                glp.width = key_width;
                glp.height = key_height;
                button.setLayoutParams(glp);
                j++;
            }
        }

        //初始化扩展按键宽高
        key_width = (int)(key_area_width / 3);

        //添加扩展键位-12键
        for(int i=0;i<KEY_EXTEND_COUNT;i++){
            Button button = new Button(this);
            button.setBackgroundResource(R.drawable.view_radius);
            button.setText(cfg.getString("key_extend_" + i,"功能" + i));
            button.setId(i);
            button.setOnLongClickListener(new keyBtnLongClickListener(button, KEY_EXTEND_AREA));
            button.setOnTouchListener(new keyBtnTouchListener(button, KEY_EXTEND_AREA));
            keyExtend.addView(button);

            glp = button.getLayoutParams();
            glp.width = key_width;
            glp.height = key_height;
            button.setLayoutParams(glp);
        }

        //初始化高级按键宽高
        key_width = (int)(key_area_width / 4);
        key_height = (int)(key_area_height / 4);

        //添加高级键位-16键
        for(int i=0;i<KEY_FN_COUNT;i++){
            Button button = new Button(this);
            button.setBackgroundResource(R.drawable.view_radius);
            button.setText(cfg.getString("key_fn_" + i,"功能" + i));
            button.setId(i);
            button.setOnLongClickListener(new keyBtnLongClickListener(button, KEY_FN_AREA));
            button.setOnTouchListener(new keyBtnTouchListener(button, KEY_FN_AREA));
            keyFn.addView(button);

            glp = button.getLayoutParams();
            glp.width = key_width;
            glp.height = key_height;
            button.setLayoutParams(glp);
        }

        //初始化发送间隔
        send_interval = cfg.getInt("interval",send_interval); //没有存储值则返回默认值
    }

    //按键长按事件
    private class keyBtnLongClickListener implements View.OnLongClickListener{
        private Button btn;
        private int area;

        private keyBtnLongClickListener(Button btn, int area){
            this.btn = btn;
            this.area = area;
        }

        @Override
        public boolean onLongClick(View v){
            long_press_flag = true;
            //建立长按事件线程
            longPressThread = new Thread(){
                @Override
                public void run(){
                    super.run();
                    while (long_press_flag){
                        try{
                            Message msg = Message.obtain();
                            //长按执行操作
                            switch (area){
                                case 1:
                                    long_press_value=cfg.getString("tlv"+btn.getId(),"");
                                    break;
                                case 2:
                                    long_press_value=cfg.getString("blv"+btn.getId(),"");
                                    break;
                                case 3:
                                    long_press_value=cfg.getString("flv"+btn.getId(),"");
                                    break;
                                case 4:
                                    long_press_value=cfg.getString("elv"+btn.getId(),"");
                                    break;
                            }
                            //长按发送数据使用handler传递数据，更新页面控件
                            msg.what = LONG_PRESS_COMPLETE;
                            handler.sendMessage(msg);
                            Thread.sleep(send_interval); //一定时间间隔发送数据
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            };
            longPressThread.start();
            return true;
        }

    }

    //对话框EditText对象获取
    private void getEtDialog(View v){
        name_et = (EditText) v.findViewById(R.id.key_name);
        short_press_value_et = (EditText) v.findViewById(R.id.edit_short_value);
        long_press_value_et = (EditText) v.findViewById(R.id.edit_long_value);
        release_value_et = (EditText) v.findViewById(R.id.edit_release_value);
        interval_value_et = (EditText) v.findViewById(R.id.edit_frequency_value);
    }

    //按键按下及释放事件
    private class keyBtnTouchListener implements View.OnTouchListener{
        private Button btn;
        private int area;

        private keyBtnTouchListener(Button btn, int area){
            this.btn = btn;
            this.area =area;
        }

        @Override
        public boolean onTouch(View v,MotionEvent event){
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                //设置按键背景
                this.btn.setBackgroundResource(R.drawable.view_radius_gradual);
                if(key_setting_mode){
                    //进入按键功能设置模式
                    keySetBuilder = new AlertDialog.Builder(ActivityKeyboard.this);
                    keySetBuilder.setTitle("按键功能设置");
                    View keySetview = GridLayout.inflate(ActivityKeyboard.this, R.layout.dialog_key_fn, null);

                    //获取弹出对话框中的输入框对象
                    getEtDialog(keySetview);
                    //初始化对象值
                    String srf=""; //short read flag
                    String lrf=""; //long read flag
                    String rrf=""; //release read flag
                    switch (area){
                        case 1:  //两键位数据
                            srf="tsv"+btn.getId();
                            lrf="tlv"+btn.getId();
                            rrf="trv"+btn.getId();
                            break;
                        case 2:  //基本键位数据
                            srf="bsv"+btn.getId();
                            lrf="blv"+btn.getId();
                            rrf="brv"+btn.getId();
                            //#################################
                            //取消基本按键名字不可编辑
                            //name_et.setEnabled(false); //设置基本控制键名称不可编辑
                            //#################################
                            break;
                        case 3: //高级键位数据
                            srf="fsv"+btn.getId();
                            lrf="flv"+btn.getId();
                            rrf="frv"+btn.getId();
                            break;
                        case 4: //扩展键位数据
                            srf="esv"+btn.getId();
                            lrf="elv"+btn.getId();
                            rrf="erv"+btn.getId();
                            break;
                    }
                    //根据存储数据设置弹出对话框对象内容
                    name_et.setText(btn.getText());
                    short_press_value_et.setText(cfg.getString(srf,"")); //short value
                    long_press_value_et.setText(cfg.getString(lrf,"")); //long value
                    release_value_et.setText(cfg.getString(rrf,"")); //release value

                    keySetBuilder.setView(keySetview);
                    keySetBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String swf=""; //short write flag
                            String lwf=""; //long write flag
                            String rwf=""; //release write flag
                            //判断弹出框项名称是否为空，为空设置默认值
                            if(name_et.getText().toString().equals("")){ //当名称输入为空时设置默认值
                                if(area == 1){
                                    btn.setText(two_name[btn.getId()]);
                                }
                                else if(area == 2)
                                {
                                    btn.setText(name[btn.getId()]);
                                }else{
                                    btn.setText("功能" + btn.getId());
                                }
                            }else{
                                btn.setText(name_et.getText());
                            }
                            switch (area){
                                //sv--short value
                                //lv-long value
                                //rv--release value
                                case 1:     //two -- t
                                    swf="tsv"+btn.getId();
                                    lwf="tlv"+btn.getId();
                                    rwf="trv"+btn.getId();
                                    editor.putString("key_two_"+btn.getId(),btn.getText().toString());
                                    break;
                                case 2:     //basic -- b
                                    swf="bsv"+btn.getId();
                                    lwf="blv"+btn.getId();
                                    rwf="brv"+btn.getId();
                                    editor.putString("key_basic_"+btn.getId(),btn.getText().toString());
                                    break;
                                case 3:     //fn -- f
                                    //存储各个控件值
                                    swf="fsv"+btn.getId();
                                    lwf="flv"+btn.getId();
                                    rwf="frv"+btn.getId();
                                    editor.putString("key_fn_"+btn.getId(),btn.getText().toString());
                                    break;
                                case 4:     //expand -- e
                                    swf="esv"+btn.getId();
                                    lwf="elv"+btn.getId();
                                    rwf="erv"+btn.getId();
                                    editor.putString("key_extend_"+btn.getId(),btn.getText().toString());
                                    break;

                            }
                            //存储对话框每项输入值
                            editor.putString(swf,short_press_value_et.getText().toString());
                            editor.putString(lwf,long_press_value_et.getText().toString());
                            editor.putString(rwf,release_value_et.getText().toString());
                            editor.commit();
                        }
                    });
                    keySetBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //
                        }
                    });
                    keySetBuilder.show();
                }else{
                    //短按执行操作
                    if(area == 1){
                        dataSend(cfg.getString("tsv"+btn.getId(),""));
                    }
                    else if(area == 2){
                        dataSend(cfg.getString("bsv"+btn.getId(),""));
                    }else if(area == 3){
                        dataSend(cfg.getString("fsv"+btn.getId(),""));
                    }else if(area == 4){
                        dataSend(cfg.getString("esv"+btn.getId(),""));
                    }

                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                long_press_flag = false;
                //设置按键背景
                this.btn.setBackgroundResource(R.drawable.view_radius);
                if(!key_setting_mode) {
                    if(area == 1){
                        dataSend(cfg.getString("trv"+btn.getId(),""));
                    }
                    else if(area == 2){
                        dataSend(cfg.getString("brv"+btn.getId(),""));
                    }else if(area == 3){
                        dataSend(cfg.getString("frv"+btn.getId(),""));
                    }else if(area == 4){
                        dataSend(cfg.getString("erv"+btn.getId(),""));
                    }
                }
            }
            return false;
        }

    }

    //设置按键区域显示或隐藏
    private void showTwoArea()
    {
        KEY_TWO_MODE = true;
        KEY_BASIC_MODE = false;
        KEY_EXTEND_MODE = false;
        KEY_FN_MODE = false;
        keyTwo.setVisibility(View.VISIBLE);
        keyBasic.setVisibility(View.GONE);
        keyExtend.setVisibility(View.GONE);
        keyFn.setVisibility(View.GONE);
        cmdBasic.setVisibility(View.GONE);
    }

    private void showBasicArea(){
        KEY_TWO_MODE = false;
        KEY_BASIC_MODE = true;
        KEY_EXTEND_MODE = false;
        KEY_FN_MODE = false;
        keyTwo.setVisibility(View.GONE);
        keyBasic.setVisibility(View.VISIBLE);
        keyExtend.setVisibility(View.GONE);
        keyFn.setVisibility(View.GONE);
        cmdBasic.setVisibility(View.GONE);
    }

    private void showExtendArea(){
        KEY_TWO_MODE = false;
        KEY_BASIC_MODE = false;
        KEY_EXTEND_MODE = true;
        KEY_FN_MODE = false;
        keyTwo.setVisibility(View.GONE);
        keyBasic.setVisibility(View.GONE);
        keyExtend.setVisibility(View.VISIBLE);
        keyFn.setVisibility(View.GONE);
        cmdBasic.setVisibility(View.GONE);
    }

    private void showFnArea(){
        KEY_TWO_MODE = false;
        KEY_BASIC_MODE = false;
        KEY_EXTEND_MODE = false;
        KEY_FN_MODE = true;
        keyTwo.setVisibility(View.GONE);
        keyBasic.setVisibility(View.GONE);
        keyExtend.setVisibility(View.GONE);
        keyFn.setVisibility(View.VISIBLE);
        cmdBasic.setVisibility(View.GONE);
    }

    private void showCmdArea(){
        keyTwo.setVisibility(View.GONE);
        keyBasic.setVisibility(View.GONE);
        keyExtend.setVisibility(View.GONE);
        keyFn.setVisibility(View.GONE);
        cmdBasic.setVisibility(View.VISIBLE);

    }

    private void keySet()
    {
        //清除提示信息
        clearScreen();
        if(keySetBtn.isChecked()) { //进入按键设置模式
            key_setting_mode = true;
            keySetBtn.setText(R.string.key_value_save);
            msgPirnt(setString(R.string.setting_begin_str));
            //Toast.makeText(this,"请设置按键功能",Toast.LENGTH_SHORT).show();
        }else { //退出按键设置模式
            key_setting_mode = false;
            keySetBtn.setText(R.string.key_value_set);
            msgPirnt(setString(R.string.setting_end_str));
            //Toast.makeText(this,"按键功能设置完成",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v){
        int id = v.getId();
        switch (id){
            case R.id.cmd_btn:
                if(cmdBtn.isChecked()){
                    showCmdArea();
                }else{
                    if(KEY_TWO_MODE){
                        showTwoArea();
                    }else if(KEY_BASIC_MODE) {
                        showBasicArea();
                    }else if(KEY_EXTEND_MODE){
                        showExtendArea();
                    }else if(KEY_FN_MODE){
                        showFnArea();
                    }

                }
                break;
            case R.id.keyset_btn:
                keySet();
                break;
            case R.id.send_btn:
                String cmd = editText.getText().toString();
                dataSend(cmd);
                break;
            case R.id.right_menu:
                mDrawerLayout.openDrawer(Gravity.RIGHT);
                break;
            case R.id.info_menu:
                break;
            case R.id.screen_cls_btn:
                if (key_setting_mode){ //按键设置模式，禁用清屏
                    Toast.makeText(this,setString(R.string.cls_not_complete),Toast.LENGTH_SHORT).show();
                }else{
                    clearScreen();
                    msgPirnt(setString(R.string.cls_complete));
                    //Toast.makeText(this,setString(R.string.cls_complete),Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    //按键发送
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case LONG_PRESS_COMPLETE:   //长按发送数据
                    dataSend(long_press_value);
                    break;
            }
            return false;
        }
    });

    //发送数据
    public void dataSend(String str){
        if(!str.equals("")) {  //输入框内容不为空，发送，并将数据显示在发送区
            //判断是否以hex形式发送数据
            if(hexBtn.isChecked())
            {
                //判断模式，按键模式 | 命令行模式
                //hex形式，以FF作为开始及结尾的标志
                try
                {
                    int len = str.length();
                    str = len % 2 == 0 ? str : str.substring(0, len - 1);

                    if(str.length() == 0)
                    {
                        CommonUtil.toastTextShow(ActivityKeyboard.this, "数据格式错误");
                        return;
                    }

                    if(cmdBtn.isChecked())
                    {
                        //命令行模式
                        switch (StaticVariable.CURRENT_CONNECT_TYPE)
                        {
                            case StaticVariable.CONNECT_TYPE_BLE:
                                ActivityBleDevice.sendDataBytes(DataFormat.hexToByteArray(DataFormat.MODE_CMD(), str));
                                break;
                            case StaticVariable.CONNECT_TYPE_OTG:
                            case StaticVariable.CONNECT_TYPE_VIRTUAL:
                                ActivityUsbDevice.sendDataBytes(DataFormat.hexToByteArray(DataFormat.MODE_CMD(), str));
                                break;
                        }
                    }
                    else
                    {
                        //按键模式
                        switch (StaticVariable.CURRENT_CONNECT_TYPE)
                        {
                            case StaticVariable.CONNECT_TYPE_BLE:
                                ActivityBleDevice.sendDataBytes(DataFormat.hexToByteArray(DataFormat.MODE_KEY(), str));
                                break;
                            case StaticVariable.CONNECT_TYPE_OTG:
                            case StaticVariable.CONNECT_TYPE_VIRTUAL:
                                ActivityUsbDevice.sendDataBytes(DataFormat.hexToByteArray(DataFormat.MODE_KEY(), str));
                                break;
                        }
                    }
                }
                catch (NumberFormatException ex)
                {
                    CommonUtil.toastTextShow(ActivityKeyboard.this, "数据格式错误");
                    return;
                }

            }
            else
            {
                str += "\r\n";
                switch (StaticVariable.CURRENT_CONNECT_TYPE)
                {
                    case StaticVariable.CONNECT_TYPE_BLE:
                        ActivityBleDevice.senData(str); //发送数据//非hex形式下默认以/r/n作为结束标志
                        break;
                    case StaticVariable.CONNECT_TYPE_OTG:
                    case StaticVariable.CONNECT_TYPE_VIRTUAL:
                        ActivityUsbDevice.sendData(str);
                        break;
                }
            }
            //###########################################
            //显示发送取消
            /*
            if(sendDisplayCheckBox.isChecked()){
                listViewAdd(TYPE_SLAVE,str);

            }else{
                //不显示
            }
            */
            //###########################################
            listViewAdd(TYPE_SLAVE,str);
            editText.setText(""); //清空输入框
        }else{
            //Toast.makeText(this,"请输入有效字符",Toast.LENGTH_SHORT).show();
        }
    }

    //消息添加
    private void listViewAdd(int type, String cmd){
        switch (type){
            case TYPE_SLAVE:
                mData.add(new Slave(cmd,R.mipmap.slave_phone));
                break;
            case TYPE_HOST:
                mData.add(new Host(R.mipmap.host_ble,cmd));
                break;
        }
        myAdapter = new adapter.MsgAdapter(ActivityKeyboard.this,mData);
        listContent.setAdapter(myAdapter);
        listContent.setSelection(listContent.getBottom());

        listViewClear(500);
    }

    //消息清空
    private void listViewClear(int max_items){
        if(mData.size() > max_items){
            mData.removeAll(mData);
            myAdapter.notifyDataSetChanged();
            listContent.setAdapter(myAdapter);
        }
    }

    //判断按键配置文件是否存在
    public boolean fileIsExists(String strFile){
        try{
            File f = new File(strFile);
            if(!f.exists()) return false;
        }catch (Exception e){
            return false;
        }
        return true;
    }

    //清除屏幕
    private void clearScreen(){
        listViewClear(0);
    }

    //消息框添加系统提示
    private void msgPirnt(String str){
        listViewAdd(TYPE_SLAVE,str);
    }

    //根据id获取文本数据
    private String setString(int id){
        return getResources().getString(id);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(ActivityKeyboard.this,ActivityMain.class);
        intent.putExtra("state",true);
        setResult(RESULT_OK,intent);
        finish();
    }

    private void getCameraPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1001);
            }
        }else {

        }
    }

}
