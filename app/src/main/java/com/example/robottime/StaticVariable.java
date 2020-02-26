package com.example.robottime;

public class StaticVariable {

    /**
     * @ 页面跳转标志
     */

    //跳转页面时模式类型标志
    public static final String CONNECT_TYPE = "conType";
    //跳转页面时蓝牙连接标志
    public static final int CONNECT_TYPE_BLE = 100;
    //跳转页面时OTG连接标志
    public static final int CONNECT_TYPE_OTG = 101;
    //跳转页面时模拟器连接标志
    public static final int CONNECT_TYPE_VIRTUAL = 102;

    /**
     * @ 页面返回标志
     */

    //蓝牙页面返回标志
    public static final int ACTIVITYBLE_MSG_ID = 120;
    //USB设备页面返回标志
    public static final int ACTIVITYUSB_MSG_ID = 121;
    //功能选择页面返回标志
    public static final int ACTIVITYFUN_MSG_ID = 122;
    //视觉跟踪页面返回标志
    public static final int ACTIVITYCV_MSG_ID = 123;
    //按键页面返回标志
    public static final int ACTIVITYKEYBOARD_MSG_ID = 124;
    //语音控制界面返回标志
    public static final int ACTIVITYVOICE_MSG_ID = 125;

    /**
     * @ 按键标志
     */

    //语音开启标志
    public static final int BTN_CLICK_VOICE = 140;
    //开始识别标志
    public static final int REC_THREAD_START = 141;
    //识别结束标志
    public static final int REC_THREAD_END = 142;

    /**
     * @ 文件操作
     */

    //模型文件解压开始
    public static final int MODELRES_UZIP_START = 160;
    //模型文件解压完成
    public static final int MODELRES_UZIP_END = 161;
    //模型文件解压失败
    public static final int MODELRES_UZIP_FAILED = 162;
    //模型文件搜索开始
    public static final int MODELRES_SEARCH_START = 163;
    //模型文件搜索完成
    public static final int MODELRES_SEARCH_END = 164;

    /**
     * @ 当前连接类型
     */
    public static int CURRENT_CONNECT_TYPE;

    /**
     * @ 当前连接设备名称
     */
    public static String CURRENT_CONNECT_DEVICE;
}
