package com.example.robottime.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by dell on 2017/12/10.
 */

public class ImageProcessor {
    public static Drawable currentPicture;


    public static Bitmap reversePicture(Drawable picture, int flag){
        Bitmap curImageBitmap=drawableToBitmap(picture);
//        Bitmap reverseBitmap=DealImage.reverseBitmap(curImageBitmap,flag);
        Bitmap reverseBitmap=reverseBitmap(curImageBitmap,flag);
        return reverseBitmap;
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {
        return (((BitmapDrawable)drawable).getBitmap());
    }


    private static Bitmap reverseBitmap(Bitmap bitmap, int flag){
        Matrix matrix=new Matrix();
        if(flag==0)
            matrix.setScale(-1,1);//水平反转
        if(flag==1)
            matrix.setScale(1,-1);//垂直反转
        int width=bitmap.getWidth();
        int height=bitmap.getHeight();
        // 根据原始位图和Matrix创建新图片
        return Bitmap.createBitmap(bitmap, 0, 0, width, height,matrix, true);
    }


    public static Bitmap reliefPicture(Drawable picture){
        Bitmap curImageBitmap=drawableToBitmap(picture);
        Bitmap reliefBitmap=reliefBitmap(curImageBitmap);
        return reliefBitmap;
    }


    /**
     * 浮雕效果
     * @param bitmap
     * @return
     */
    private static Bitmap reliefBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int color = 0, preColor = 0, a, r, g, b;
        int r1, g1, b1;
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int[] oldPx = new int[width * height];
        int[] newPx = new int[width * height];
        bitmap.getPixels(oldPx, 0, width, 0, 0, width, height);
        for (int i = 1; i < oldPx.length; i++) {
            preColor = oldPx[i-1];
            a = Color.alpha(preColor);
            r = Color.red(preColor);
            g = Color.green(preColor);
            b = Color.blue(preColor);

            color = oldPx[i];
            r1 = Color.red(color);
            g1 = Color.green(color);
            b1 = Color.blue(color);

            r = r1 - r + 127;
            g = g1 - g + 127;
            b = b1 - b + 127;

            if (r > 255) {
                r = 255;
            } else if (r < 0){
                r = 0;
            }

            if (g > 255) {
                g = 255;
            } else if (g < 0){
                g = 0;
            }

            if (b > 255) {
                b = 255;
            } else if (b < 0){
                b = 0;
            }
            newPx[i] = Color.argb(a, r, g, b);
        }
        bmp.setPixels(newPx, 0, width, 0, 0, width, height);
        return bmp;
    }



    public static Bitmap sharpenPicture(Drawable picture){
        Bitmap curImageBitmap=drawableToBitmap(picture);
        Bitmap reliefBitmap=sharpenImage(curImageBitmap);
        return reliefBitmap;
    }

    /**
     * 图片锐化（拉普拉斯变换）
     * @param bmp
     * @return
     */
    private static Bitmap sharpenImage(Bitmap bmp)
    {
        long start = System.currentTimeMillis();
        // 拉普拉斯矩阵
        int[] laplacian = new int[] { -1, -1, -1, -1, 9, -1, -1, -1, -1 };

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        int newR = 0;
        int newG = 0;
        int newB = 0;

        float alpha = 0.3F;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 1, length = height - 1; i < length; i++)
        {
            for (int k = 1, len = width - 1; k < len; k++)
            {
                int idx = 0;
                for (int m = -1; m <= 1; m++)
                {
                    for (int n = -1; n <= 1; n++)
                    {
                        int pixColor = pixels[(i + n) * width + k + m];
                        int pixR = Color.red(pixColor);
                        int pixG = Color.green(pixColor);
                        int pixB = Color.blue(pixColor);

                        newR = newR + (int) (pixR * laplacian[idx] * alpha);
                        newG = newG + (int) (pixG * laplacian[idx] * alpha);
                        newB = newB + (int) (pixB * laplacian[idx] * alpha);
                        idx++;
                    }
                }

                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));

                pixels[i * width + k] = Color.argb(255, newR, newG, newB);
                newR = 0;
                newG = 0;
                newB = 0;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        long end = System.currentTimeMillis();
        Log.d("may", "used time="+(end - start));
        return bitmap;
    }




//    private static Bitmap extractBitmap(Bitmap image,int threshold){
//        long start = System.currentTimeMillis();
//
//        int width=image.getWidth();
//        int height=image.getHeight();
//
//        Bitmap outImg =Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
//        outImg.eraseColor(Color.BLACK);//填充颜色
//        // 拉普拉斯矩阵
//        int[] laplacian = new int[] { 0, 1, 0, 1, -4, 1, 0, 1,0 };
//        int newR = 0;
//        int newG = 0;
//        int newB = 0;
//        int[] pixels = new int[width * height];
//        image.getPixels(pixels, 0, width, 0, 0, width, height);
//        for (int i = 1, length = height - 1; i < length; i++)
//        {
//            for (int k = 1, len = width - 1; k < len; k++)
//            {
//                int idx = 0;
//                for (int m = -1; m <= 1; m++)
//                {
//                    for (int n = -1; n <= 1; n++)
//                    {
//                        int pixColor = pixels[(i + n) * width + k + m];
//                        int pixR = Color.red(pixColor);
//                        int pixG = Color.green(pixColor);
//                        int pixB = Color.blue(pixColor);
//
//                        newR +=pixR * laplacian[idx] ;
//                        newG +=pixG * laplacian[idx] ;
//                        newB +=pixB * laplacian[idx] ;
//                        idx++;
//                    }
//                }
//                if(newB>threshold&&newG>threshold&&newR>threshold)
//                    pixels[i * width + k]=Color.BLACK;
//                else
//                    pixels[i * width + k]=Color.WHITE;
//                newR = 0;
//                newG = 0;
//                newB = 0;
//            }
//        }
//        outImg.setPixels(pixels, 0, width, 0, 0, width, height);
//        long end = System.currentTimeMillis();
//        Log.d("may", "used time="+(end - start));
//        return outImg;
//    }



    public static boolean saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "xyImage";
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();

            //把文件插入到系统图库
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }








}
