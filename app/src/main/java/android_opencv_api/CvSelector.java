package android_opencv_api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

//触屏选区

public class CvSelector extends View {
    private static final String TAG = "Selector.View";
    private Bitmap mBitmap;
    private Canvas canv;

    //    声明Paint对象
    private Paint mPaint;
    private int StrokeWidth = 2;
    private Rect rect = new Rect(0,0,0,0);//手动绘制矩形

    public CvSelector(Context context)
    {
        super(context);
    }

    public CvSelector(Context context, AttributeSet attrs){
        super(context, attrs);
        mPaint = new Paint();
        //设置无锯齿
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(StrokeWidth);
        mPaint.setStyle(Paint.Style.FILL); //填充
        mPaint.setColor(Color.argb(125,144,238,144)); //浅绿
        //mBitmap = Bitmap.createBitmap(ScreenContent.displayWidth, ScreenContent.displayHeight, Bitmap.Config.ARGB_8888);
        //canv = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawARGB(50,255,227,0);
        canvas.drawRect(rect, mPaint);
        //canv.drawRect(rect, mPaint);
        //Log.d(TAG,"&********************drawing********************&");
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                rect.right+=StrokeWidth;
                rect.bottom+=StrokeWidth;
                invalidate(rect);
                rect.left = x;
                rect.top = y;
                rect.right =rect.left;
                rect.bottom = rect.top;

                //开始设置选区//起点坐标
                CvHelper.setSelectObj(CvHelper.SELECTION_ENABLE, rect.left, rect.top);

            case MotionEvent.ACTION_MOVE:
                Rect old = new Rect(rect.left,rect.top,rect.right+StrokeWidth,rect.bottom+StrokeWidth);
                rect.right = x;
                rect.bottom = y;
                old.union(x,y);
                invalidate(old);
                break;
            case MotionEvent.ACTION_UP:
                invalidate(rect);

                //选区设置结束//终端坐标
                CvHelper.setSelectObj(CvHelper.SELECTION_DISABLE, rect.right, rect.bottom);

                rect.setEmpty();

                break;
            default:
                break;
        }
        return  true;//处理了触摸信息，消息不再传递
    }

}
