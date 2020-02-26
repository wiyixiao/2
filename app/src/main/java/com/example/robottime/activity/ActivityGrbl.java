package com.example.robottime.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.robottime.R;
import com.example.robottime.utils.ScreenContent;
import android_opencv_api.CvHelper;
import com.example.robottime.utils.ImageProcessor;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.InputStream;
import static android.graphics.Bitmap.Config.ARGB_8888;

public class ActivityGrbl extends AppCompatActivity implements View.OnClickListener,View.OnTouchListener,CompoundButton.OnCheckedChangeListener {
    //一些命令
    private static final String RELATIVE_MODE="G91"; //相对坐标
    private static final String ABSOLUTE_MODE="G90"; //绝对坐标
    private static final String RESET="ctrl-x"; //grbl复位
    private static final String UNLOCK="$X"; //解除锁定
    private static final String ZERO="G92X0Y0Z0"; //设置零点
    private String RZ="G0X0Y0Z0F"; //归零（加上文本框速度值）//G90; G0X0Y0Z0F500
    private String Y_MOVE="G0Y"; //Y移动 //G91; G0Y1F500; G90
    private String X_MOVE="G0X"; //X移动

    //控制区域
    private GridLayout grbl_btn_layout;
    private GridLayout img_btn_layout;
    private boolean img_select = true; //是否可以选择图片
    private android.view.ViewGroup.LayoutParams glp;

    //按钮
    private Button btn_up;
    private Button btn_left;
    private Button btn_home;
    private Button btn_right;
    private Button btn_down;
    private Button btn_begin;
    private Button btn_unlock;
    private Button btn_zero;

    private Button action_rotate;
    private Button action_togray;
    private Button action_contours;
    private Button action_fill;
    private Button action_point;
    private Button action_color;
    private Button action_track;
    private Button action_set;
    private Button action_save;
    private Button action_create;

    private ToggleButton view_tb; //图片显示区域与命令显示区域切换按钮
    private ImageButton add_ib; //添加图片按钮
    //输入框
    private EditText grbl_cmd_et;
    private EditText step_et;
    private EditText speed_et;
    //文本显示框
    private TextView grbl_msg_tv;
    private ImageView showImage;
    //滑动条
    private SeekBar seb_1;
    private SeekBar seb_2;

    //屏幕及按钮尺寸
    private int screen_width = ScreenContent.displayWidth;
    private int screen_height = (int)(ScreenContent.displayHeight * 0.4);
    private int key_width = (screen_width-80) / 5;
    private int key_height = (screen_height - 140) / 3;
    private int cv_key_height = (screen_height - 140) / 6;

    //阈值
    private int th1_val = 127;
    private int th2_val = 127;
    private Bitmap src_bp;
    private Bitmap res_bp;
    private Mat srcBitmapMat = new Mat();
    private Mat resBitmapMat = new Mat();

    //图像相关
    private int rotate_dir = 1; //转动方向 // 1:左 2:上 3:右 4:下
    private double max_size = 1024;
    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_grbl);

        initViews();

    }

    //滑动条初始化
    public void initSeekBar(){
        seb_1 = (SeekBar)findViewById(R.id.threshold1);
        seb_2 = (SeekBar)findViewById(R.id.threshold2);
    }

    //按钮初始化
    public void initBtn(){
        btn_up = (Button) findViewById(R.id.btn_up);
        btn_left = (Button) findViewById(R.id.btn_left);
        btn_home = (Button) findViewById(R.id.btn_home);
        btn_right = (Button) findViewById(R.id.btn_right);
        btn_down = (Button) findViewById(R.id.btn_down);
        btn_begin = (Button) findViewById(R.id.btn_begin);
        btn_unlock = (Button) findViewById(R.id.btn_unlock);
        btn_zero = (Button) findViewById(R.id.btn_zero);

        action_rotate = (Button)findViewById(R.id.action_rotate);
        action_togray = (Button)findViewById(R.id.action_togray);
        action_save = (Button)findViewById(R.id.action_save);
        action_create = (Button)findViewById(R.id.action_create);
        action_contours = (Button)findViewById(R.id.action_contours);
        action_fill = (Button)findViewById(R.id.action_fill);
        action_point = (Button)findViewById(R.id.action_point);
        action_color = (Button)findViewById(R.id.action_color);
        action_track = (Button)findViewById(R.id.action_track);
        action_set = (Button)findViewById(R.id.action_set);

        view_tb = (ToggleButton)findViewById(R.id.content_switch_tb); //命令显示区域及图片显示区域切换按钮
        add_ib = (ImageButton) findViewById(R.id.img_add_btn); //图片添加按钮

    }

    //设置监听
    public void addListener(){
        btn_up.setOnClickListener(this);
        btn_left.setOnClickListener(this);
        btn_home.setOnClickListener(this);
        btn_right.setOnClickListener(this);
        btn_down.setOnClickListener(this);
        btn_begin.setOnClickListener(this);
        btn_unlock.setOnClickListener(this);
        btn_zero.setOnClickListener(this);

        action_rotate.setOnClickListener(this);
        action_togray.setOnClickListener(this);
        action_save.setOnClickListener(this);
        action_create.setOnClickListener(this);
        action_contours.setOnClickListener(this);
        action_fill.setOnClickListener(this);
        action_point.setOnClickListener(this);
        action_color.setOnClickListener(this);
        action_track.setOnClickListener(this);
        action_set.setOnClickListener(this);

        view_tb.setOnCheckedChangeListener(this);
        add_ib.setOnClickListener(this);

        btn_up.setOnTouchListener(this);
        btn_left.setOnTouchListener(this);
        btn_home.setOnTouchListener(this);
        btn_right.setOnTouchListener(this);
        btn_down.setOnTouchListener(this);
        btn_begin.setOnTouchListener(this);
        btn_unlock.setOnTouchListener(this);
        btn_zero.setOnTouchListener(this);

        action_rotate.setOnTouchListener(this);
        action_togray.setOnTouchListener(this);
        action_save.setOnTouchListener(this);
        action_create.setOnTouchListener(this);
        action_contours.setOnTouchListener(this);
        action_fill.setOnTouchListener(this);
        action_point.setOnTouchListener(this);
        action_color.setOnTouchListener(this);
        action_track.setOnTouchListener(this);
        action_set.setOnTouchListener(this);

        seb_1.setOnSeekBarChangeListener(osbcl);
        seb_2.setOnSeekBarChangeListener(osbcl);
    }

    //根据屏幕宽度设置按钮尺寸
    public void initSize(){
        glp = btn_up.getLayoutParams();
        glp.width = key_width;
        glp.height = key_height;
        btn_up.setLayoutParams(glp);

        glp = btn_left.getLayoutParams();
        glp.width = key_width;
        glp.height = key_height;
        btn_left.setLayoutParams(glp);

        glp = btn_home.getLayoutParams();
        glp.width = key_width;
        glp.height = key_height;
        btn_home.setLayoutParams(glp);

        glp = btn_right.getLayoutParams();
        glp.width = key_width;
        glp.height = key_height;
        btn_right.setLayoutParams(glp);

        glp = btn_down.getLayoutParams();
        glp.width = key_width;
        glp.height = key_height;
        btn_down.setLayoutParams(glp);


        glp = btn_begin.getLayoutParams();
        glp.width = key_width;
        glp.height = key_height;
        btn_begin.setLayoutParams(glp);

        glp = btn_unlock.getLayoutParams();
        glp.width = key_width;
        glp.height = key_height;
        btn_unlock.setLayoutParams(glp);

        glp = btn_zero.getLayoutParams();
        glp.width = key_width;
        glp.height = key_height;
        btn_zero.setLayoutParams(glp);

        glp = action_rotate.getLayoutParams();
        glp.width = key_width;
        glp.height = cv_key_height;
        action_rotate.setLayoutParams(glp);

        glp = action_togray.getLayoutParams();
        glp.width = key_width;
        glp.height = cv_key_height;
        action_togray.setLayoutParams(glp);

        glp = action_save.getLayoutParams();
        glp.width = key_width;
        glp.height = cv_key_height;
        action_save.setLayoutParams(glp);

        glp = action_create.getLayoutParams();
        glp.width = key_width;
        glp.height = cv_key_height;
        action_create.setLayoutParams(glp);

        glp = action_fill.getLayoutParams();
        glp.width = key_width;
        glp.height = cv_key_height;
        action_fill.setLayoutParams(glp);

        glp = action_point.getLayoutParams();
        glp.width = key_width;
        glp.height = cv_key_height;
        action_point.setLayoutParams(glp);

        glp = action_color.getLayoutParams();
        glp.width = key_width;
        glp.height = cv_key_height;
        action_color.setLayoutParams(glp);

        glp = action_track.getLayoutParams();
        glp.width = key_width;
        glp.height = cv_key_height;
        action_track.setLayoutParams(glp);

        glp = action_set.getLayoutParams();
        glp.width = key_width;
        glp.height = cv_key_height;
        action_set.setLayoutParams(glp);
    }

    public void initView(){
        grbl_cmd_et = (EditText)findViewById(R.id.grbl_input_box);
        step_et = (EditText)findViewById(R.id.step_et);
        speed_et = (EditText)findViewById(R.id.speed_et);
        grbl_msg_tv = (TextView)findViewById(R.id.grbl_msg_content);
        grbl_msg_tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        showImage = (ImageView)findViewById(R.id.img_content);
        showImage.setImageResource(R.drawable.laser_logo);
    }

    public void initLayout(){
        grbl_btn_layout = (GridLayout)findViewById(R.id.grbl_btn_group);
        img_btn_layout = (GridLayout) findViewById(R.id.img_set_group);
    }

    public void initViews(){
        initBtn();
        initView();
        initLayout();
        initSeekBar();

        //设置监听
        addListener();
        //设置尺寸
        initSize();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_up:               //Y正方向移动
                dataSend(RELATIVE_MODE);
                dataSend(Y_MOVE+step_et.getText()+"F"+speed_et.getText());
                dataSend(ABSOLUTE_MODE);
                break;
            case R.id.btn_down:             //Y负方向移动
                dataSend(RELATIVE_MODE);
                dataSend(Y_MOVE+"-"+step_et.getText()+"F"+speed_et.getText());
                dataSend(ABSOLUTE_MODE);
                break;
            case R.id.btn_left:             //X负方向移动
                dataSend(RELATIVE_MODE);
                dataSend(X_MOVE+"-"+step_et.getText()+"F"+speed_et.getText());
                dataSend(ABSOLUTE_MODE);
                break;
            case R.id.btn_right:            //X正方向移动
                dataSend(RELATIVE_MODE);
                dataSend(X_MOVE+step_et.getText()+"F"+speed_et.getText());
                dataSend(ABSOLUTE_MODE);
                break;
            case R.id.btn_home:             //归零
                dataSend(ABSOLUTE_MODE);
                dataSend(RZ+speed_et.getText());
                break;
            case R.id.btn_unlock:           //解锁
                dataSend(UNLOCK);
                break;
            case R.id.btn_zero:             //设置零点
                dataSend(ZERO);
                break;
            case R.id.img_add_btn:          //添加图片
                if(img_select) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");//设置类型
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, 1);
                }else{
                    Toast.makeText(this, "请切换至图片设置页面", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_rotate:  //图片旋转
                rotatePicture();
                break;
            case R.id.action_togray: //转灰度
                jni_image2gray();
                break;
            case R.id.action_contours: //获取轮廓
                jni_extract(th1_val, th2_val);
                break;
            case R.id.action_save:      //图片保存
                savePicture();
                break;
            case R.id.action_create:    //生成G代码
                //Toast.makeText(this,"G代码已生成",Toast.LENGTH_SHORT).show();
                Toast.makeText(this,CvHelper.getVersion(),Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onTouch(View v,MotionEvent event){

        if(event.getAction() == MotionEvent.ACTION_UP){
            v.setBackgroundResource(R.drawable.view_radius);
        }
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            v.setBackgroundResource(R.drawable.view_radius_gradual_2);
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //当tb被点击的时候，当前的方法会执行
        //buttonView——代表被点击控件的本身
        //isChecked——代表被点击的控件的状态
        //当点击tb这个控件的时候更好img的src
        if(isChecked){  //显示图片区域
            grbl_msg_tv.setVisibility(View.GONE);
            showImage.setVisibility(View.VISIBLE);
            grbl_btn_layout.setVisibility(View.GONE);
            img_btn_layout.setVisibility(View.VISIBLE);

            img_select = true; //可以选择图片
            view_tb.setBackgroundResource(R.mipmap.on);
        }else { //显示命令区域
            grbl_msg_tv.setVisibility(View.VISIBLE);
            showImage.setVisibility(View.GONE);
            grbl_btn_layout.setVisibility(View.VISIBLE);
            img_btn_layout.setVisibility(View.GONE);

            img_select = false; //不可以选择图片
            view_tb.setBackgroundResource(R.mipmap.off);
        }

    }

    // 设置拖动条改变监听器
    SeekBar.OnSeekBarChangeListener osbcl = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if(seekBar.getId() == R.id.threshold1){
                th1_val = seekBar.getProgress();
            }else if(seekBar.getId() == R.id.threshold2){
                th2_val = seekBar.getProgress();
            }else{
                //
            }
            //通过阈值调整图像
            jni_extract(th1_val, th2_val);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
//            Toast.makeText(getApplicationContext(), "onStartTrackingTouch",
//                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
//            Toast.makeText(getApplicationContext(), "onStopTrackingTouch",
//                    Toast.LENGTH_SHORT).show();
        }

    };

    /***********************************************************************************/
    //页面返回调用
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == 1) {
//                Uri uri = data.getData();
//                showPictureFromFile(uri);
////                String selectedPath=uri.toString();
////                String picturePath=getApplicationContext().getFilesDir().getAbsolutePath();
////                Toast.makeText(this, "文件路径："+selectedPath+"\r\n"+picturePath, Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void showPictureFromFile(Uri mImageCaptureUri){
//        Bitmap photoBmp =getBitmap(mImageCaptureUri);
//        Drawable showDrawable=new BitmapDrawable(this.getResources(),photoBmp);
//        showImage.setImageDrawable(showDrawable);
//        //ImageProcessor.currentPicture=showImage.getDrawable();
//    }
    /************************************************************************************/

    /************************************************************************************/
    //opencv4java转灰度
//    private void convertGray() {
//        Mat src = new Mat();
//        Mat temp = new Mat();
//        Mat dst = new Mat();
//        CommonUtil.bitmapToMat(selectbp, src);
//        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);
//        Log.i("CV", "image type:" + (temp.type() == CvType.CV_8UC3));
//        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2GRAY);
//        CommonUtil.matToBitmap(dst, selectbp);
//        showImage.setImageBitmap(selectbp);
//    }

    /************************************************************************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Log.d("image-tag", "start to decode selected image now...");
                InputStream input = getContentResolver().openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(input, null, options);
                int raw_width = options.outWidth;
                int raw_height = options.outHeight;
                int max = Math.max(raw_width, raw_height);
                int newWidth = raw_width;
                int newHeight = raw_height;
                int inSampleSize = 1;
                if(max > max_size) {
                    newWidth = raw_width / 2;
                    newHeight = raw_height / 2;
                    while((newWidth/inSampleSize) > max_size || (newHeight/inSampleSize) > max_size) {
                        inSampleSize *=2;
                    }
                }

                options.inSampleSize = inSampleSize;
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                src_bp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);

                showImage.setImageBitmap(src_bp);
                res_bp = null;
                jni_extract(th1_val, th2_val);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /************************************************************************************/
    //图片处理。。
    //转灰度图
    private void jni_image2gray(){
        try{
            Utils.bitmapToMat(src_bp,srcBitmapMat);
            if(res_bp==null){
                res_bp = Bitmap.createBitmap(src_bp.getWidth(),src_bp.getHeight(),ARGB_8888);
            }else{
                //image2gray(srcBitmapMat.nativeObj, resBitmapMat.nativeObj);
                Utils.matToBitmap(resBitmapMat,res_bp);
                showImage.setImageDrawable(bitmap2Drawable(res_bp));
            }
        }catch (Exception e){
            Toast.makeText(this, "请选择一张图片", Toast.LENGTH_SHORT).show();
        }
    }

    //提取轮廓
    private void jni_extract(int a, int b){
        try{
            Utils.bitmapToMat(src_bp,srcBitmapMat);
            if(res_bp==null){
                res_bp = Bitmap.createBitmap(src_bp.getWidth(),src_bp.getHeight(),ARGB_8888);
            }else{
                //imageextract(srcBitmapMat.nativeObj, resBitmapMat.nativeObj, a, b);
                Utils.matToBitmap(resBitmapMat,res_bp);
                showImage.setImageDrawable(bitmap2Drawable(res_bp));
            }
        }catch (Exception e){
            //Toast.makeText(this, "请选择一张图片", Toast.LENGTH_SHORT).show();
        }

    }

    //图片旋转
    private void rotatePicture(){
        if(rotate_dir>4){
            rotate_dir=1;
        }
        switch (rotate_dir){
            case 1: reversePicture(0);break;
            case 2: reversePicture(1);break;
            case 3: reversePicture(0);break;
            case 4: reversePicture(1);break;
        }
        rotate_dir++;
    }

    //图片反转
    private void reversePicture(int flag){
        try{
            Drawable curImageDrawable=bitmap2Drawable(src_bp);
            Bitmap reversedBitmap= ImageProcessor.reversePicture(curImageDrawable,flag);
            Drawable reversePicture=new BitmapDrawable(this.getResources(),reversedBitmap);
            showImage.setImageDrawable(reversePicture);
            src_bp = drawable2Bitmap(reversePicture);
        }catch (Exception e){
            Toast.makeText(this, "请选择一张图片", Toast.LENGTH_SHORT).show();
        }

    }

    //图片保存,需要开启存储权限
    private void savePicture(){
        if(res_bp==null && src_bp == null) {
            Toast.makeText(this,"默认背景不可保存",Toast.LENGTH_SHORT).show();
        }else{
            ImageProcessor.saveImageToGallery(this, ImageProcessor.drawableToBitmap(showImage.getDrawable()));
            Toast.makeText(this,"保存成功！",Toast.LENGTH_SHORT).show();
        }
    }


    /************************************************************************************/
    //drawble转换为bitmap
    Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

    //bitmap转换为drawable
    Drawable bitmap2Drawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }

    public Bitmap getBitmap(Uri url) {
        try{
            Bitmap photoBmp;
            if (url !=null) {
                ContentResolver mContentResolver = this.getContentResolver();
                photoBmp = MediaStore.Images.Media.getBitmap(mContentResolver, url);
                return photoBmp;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void showBitmap(Bitmap bitmap){

        if(bitmap != null) {
            showImage.setImageBitmap(bitmap);
            Log.i("ActivityMain", "bitmap load end");
        } else {
            Log.i("ActivityMain", "bitmap null");
        }
    }

    //数据发送
    private void dataSend(String str){
        ActivityBleDevice.senData(str+"\r\n"); //发送数据
        textViewAdd(str);
    }

    private void textViewAdd(String str){
        grbl_msg_tv.append("[root@xy ~]>" +str + "\n");

        int offset=grbl_msg_tv.getLineCount()*grbl_msg_tv.getLineHeight();
        if(offset>grbl_msg_tv.getHeight()){
            grbl_msg_tv.scrollTo(0,offset-grbl_msg_tv.getHeight());
        }
    }
}
