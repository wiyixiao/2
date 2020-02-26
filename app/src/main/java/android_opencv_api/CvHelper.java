package android_opencv_api;

import android.util.Log;
import android.widget.TableRow;

public class CvHelper {
    public static final String TAG = "CvHelper";

    public static final int RESULT_FAILED = -1;
    public static final int RESULT_SUCCESS = 0;
    public static final int SELECTION_ENABLE = 0;
    public static final int SELECTION_DISABLE = 1;

    private static boolean isInit;

    public static boolean isInit() {
        return isInit;
    }

    public static void setInit(boolean init) {
        isInit = init;
    }

    public static class ObjInfo
    {
        public int cx;
        public int cy;
        public int cd;
        public int face_num;
    };

    public static class HsvInfo
    {
        public int hmin, hmax;
        public int smin, smax;
        public int vmin, vmax;
    }

    public static class SizeInfo
    {
        public double device_width, device_height;
        public double frame_width, frame_height;
    }

    public static class RatioInfo
    {
        public double rw, rh; //设备与图像宽高比，主要用于选区时尺寸确定
    }

    public static ObjInfo obj = new ObjInfo();
    public static HsvInfo hsv = new HsvInfo();
    public static SizeInfo size = new SizeInfo();

    public static int defaultBarsPro[] = {110, 256, 150, 35}; //vmin,vmax,smin,scale

    //获取opencv版本
    public native static String getVersion();
    //人脸检测
    public native static int faceDetect();
    //颜色检测
    public native static void trackColor();
    //停止检测
    public native static void disableTrack();
    //加载分类器
    public native static int loadCascade(String cascadeFileName);
    //传入图像数据
    public native static int revDataHandler(long addrInputRgbaImage);
    //设置hsv//此处只处理vmin,vmax,smin三个值
    public native static void setHsv(int _vmin, int _vmax, int _smin);
    //设置缩放//scale
    public native static void setScale(double scale);

    /**
     * 设置选区
     * @param enable begin | end
     * @param a x0 | x1
     * @param b y0 | y1
     * @return
     */
    public native static int setSelection(int enable, int a, int b);

    //获取信息
    protected native static int getCx();
    protected native static int getCy();
    protected native static int getCd();
    protected native static int getFaces();

    public static void getObjInfo() {
        obj.cx = getCx();
        obj.cy = getCy();
        obj.cd = getCd();
        obj.face_num = getFaces();
    }

    public static RatioInfo returnRatio()
    {
        RatioInfo ri = new RatioInfo();
        ri.rw = size.frame_width / size.device_width;
        ri.rh = size.frame_height / size.device_height;
        return ri;
    }

    public static void setSelectObj(int enable, int a, int b)
    {
        RatioInfo rif = returnRatio();
        a *= rif.rw;
        b *= rif.rh;

        if(setSelection(enable, a, b) == 0)
        {
            Log.d(TAG,"选区设置成功");
        }
        else
        {
            Log.d(TAG, "选区设置失败");
        }
    }


}
