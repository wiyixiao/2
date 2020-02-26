package com.example.robottime.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

    private static final String TAG = "FileUtil";

    /**
     * @检测目录是否存在
     * @param strFolder
     * @return
     */
    public static boolean isFolderExists(String strFolder, boolean isCreate) {
        File file = new File(strFolder);

        if(!file.exists())
        {
            if(isCreate)
                file.mkdirs();
            return false;
        }

        return true;
    }

    /**
     * @检测文件夹是否为空
     * @param strFolder
     * @return
     */
    public static boolean isFolderEmpty(String strFolder)
    {
        File file = new File(strFolder);

        if (file.exists() && file.isDirectory()) {
            if(file.list().length > 0) {
                //Not empty, do something here.
                return false;
            }
        }

        return true;
    }


    /**
     * @搜索指定路径内指定后缀名文件
     * @param Path
     * @param Extension
     * @param IsWidthEx
     * @param IsIterative
     * @return
     */
    public static List<String> getFiles(String Path, String Extension,boolean IsIterative,boolean IsWidthEx)
    {
        final List<String> lstFile = new ArrayList<String>();
        File[] files = new File(Path).listFiles();

        for (File f : files) {
            if (f.isFile()) {
                if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension))  //判断扩展名
                    //lstFile.add(f.getPath());
                    if(IsWidthEx)
                        lstFile.add(f.getName());
                    else
                        lstFile.add(getFileNameNoEx(f.getName()));

                if (!IsIterative)
                    break;
            } else if (f.isDirectory() && !f.getPath().contains("/."))  //忽略点文件（隐藏文件/文件夹）
                getFiles(f.getPath(), Extension, IsIterative, IsWidthEx);
        }

        return lstFile;
    }

    /**
     * @检测文件是否存在
     * @param strFile
     * @return
     */
    public static boolean isFileExists(String strFile)
    {
        try
        {
            File f=new File(strFile);
            if(!f.exists())
            {
                return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    /**
     * @创建文件
     * @param strFile
     */
    public static void createFile(String strFile){
        try{
            File f=new File(strFile);

            if(!f.getParentFile().exists()){
                isFolderExists(f.getParentFile().getPath(), true);
            }

            if(!isFileExists(strFile))
            {
                f.createNewFile();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * @获取文件扩展名
     * @param filename
     * @return
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /**
     * @获取不带扩展名文件
     * @param filename
     * @return
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * @InputStreamToByte
     * @param is
     * @return
     * @throws IOException
     */
    public static byte[] InputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        return imgdata;
    }

    /**
     * @获取SD卡路径
     * @return
     */
    public static String getSDCardDirPath() {
        return android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * @判断SD卡是否存在
     * @return
     */
    public static boolean isSDCardExist() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    /**
     * @拷贝文件夹
     * @param srcFolderFullPath
     * @param destFolderFullPath
     * @return
     */
    public static boolean copyFolder(String srcFolderFullPath, String destFolderFullPath) {
        Log.d(TAG, "copyFolder " + "srcFolderFullPath-" + srcFolderFullPath + " destFolderFullPath-" + destFolderFullPath);
        try {
            (new File(destFolderFullPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
            File file = new File(srcFolderFullPath);
            String[] files = file.list();
            File temp = null;
            for (int i = 0; i < files.length; i++) {
                if (srcFolderFullPath.endsWith(File.separator)) {
                    temp = new File(srcFolderFullPath + files[i]);
                } else {
                    temp = new File(srcFolderFullPath + File.separator + files[i]);
                }
                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    copyFile(input, destFolderFullPath + "/" + (temp.getName()).toString());
                }
                if (temp.isDirectory()) { // 如果是子文件夹
                    copyFolder(srcFolderFullPath + "/" + files[i], destFolderFullPath + "/" + files[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "copyFolder " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * @拷贝文件
     * @param ins
     * @param destFileFullPath
     * @return
     */
    public static boolean copyFile(InputStream ins, String destFileFullPath) {
        Log.d(TAG, "copyFile " + "destFileFullPath-" + destFileFullPath);
        FileOutputStream fos = null;
        try {
            File file = new File(destFileFullPath);
            Log.d(TAG, "copyFile " + "开始读入");
            fos = new FileOutputStream(file);
            Log.d(TAG, "copyFile " + "开始写出");
            byte[] buffer = new byte[8192];
            int count = 0;
            Log.d(TAG, "copyFile " + "准备循环了");
            while ((count = ins.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            Log.d(TAG, "copyFile " + "已经创建该文件");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "copyFile " + e.getMessage());
            return false;
        } finally {
            try {
                fos.close();
                ins.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "copyFile " + e.getMessage());
            }
        }
    }

    /**
     * @删除文件夹
     * @param targetFolderFullPath
     */
    public static void deleteFolder(String targetFolderFullPath) {
        Log.d(TAG, "deleteFolder " + "targetFolderFullPath-" + targetFolderFullPath);
        File file = new File(targetFolderFullPath);
        if (!file.exists()) {
            return;
        }
        String[] files = file.list();
        File temp = null;
        for (int i = 0; i < files.length; i++) {
            if (targetFolderFullPath.endsWith(File.separator)) {
                temp = new File(targetFolderFullPath + files[i]);
            } else {
                temp = new File(targetFolderFullPath + File.separator + files[i]);
            }
            if (temp.isFile()) {
                deleteFile(targetFolderFullPath + "/" + (temp.getName()).toString());
            }
            if (temp.isDirectory()) { // 如果是子文件夹
                deleteFolder(targetFolderFullPath + "/" + files[i]);
            }
        }
        file.delete();
    }

    /**
     * @删除文件
     * @param targetFileFullPath
     */
    public static void deleteFile(String targetFileFullPath) {
        Log.d(TAG, "deleteFolder " + "targetFileFullPath-" + targetFileFullPath);
        File file = new File(targetFileFullPath);
        file.delete();
    }

    /**
     * @从assets目录下拷贝文件
     * @param context
     * @param assetsFilePath
     * @param targetFileFullPath
     */
    public static void copyFileFromAssets(Context context, String assetsFilePath, String targetFileFullPath) {
        Log.d(TAG, "copyFileFromAssets ");
        InputStream assestsFileImputStream;
        try {
            assestsFileImputStream = context.getAssets().open(assetsFilePath);
            copyFile(assestsFileImputStream, targetFileFullPath);
        } catch (IOException e) {
            Log.d(TAG, "copyFileFromAssets " + "IOException-" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @从assets目录下拷贝整个文件夹
     * @param context
     * @param rootDirFullPath
     * @param targetDirFullPath
     */
    public static void copyFolderFromAssets(Context context, String rootDirFullPath, String targetDirFullPath) {
        Log.d(TAG, "copyFolderFromAssets " + "rootDirFullPath-" + rootDirFullPath + " targetDirFullPath-" + targetDirFullPath);
        try {
            String[] listFiles = context.getAssets().list(rootDirFullPath);// 遍历该目录下的文件和文件夹
            for (String string : listFiles) {// 看起子目录是文件还是文件夹，这里只好用.做区分了
                Log.d(TAG, "name-" + rootDirFullPath + "/" + string);
                if (isFileByName(string)) {// 文件
                    copyFileFromAssets(context, rootDirFullPath + "/" + string, targetDirFullPath + "/" + string);
                } else {// 文件夹
                    String childRootDirFullPath = rootDirFullPath + "/" + string;
                    String childTargetDirFullPath = targetDirFullPath + "/" + string;
                    new File(childTargetDirFullPath).mkdirs();
                    copyFolderFromAssets(context, childRootDirFullPath, childTargetDirFullPath);
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "copyFolderFromAssets " + "IOException-" + e.getMessage());
            Log.d(TAG, "copyFolderFromAssets " + "IOException-" + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private static boolean isFileByName(String string) {
        if (string.contains(".")) {
            return true;
        }
        return false;
    }

    /**
     * @解压assets压缩包(.zip)资源到SD卡
     * @param context
     * @param assetName
     * @param outputDirectory
     * @param isReWrite
     * @throws IOException
     */
    public static void unZip(Context context, String assetName,
                             String outputDirectory,boolean isReWrite) throws IOException {
        //创建解压目标目录
        File file = new File(outputDirectory);
        //如果目标目录不存在，则创建
        if (!file.exists()) {
            file.mkdirs();
        }
        //打开压缩文件
        InputStream inputStream = context.getAssets().open(assetName);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        //读取一个进入点
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        //使用1Mbuffer
        byte[] buffer = new byte[1024 * 1024];
        //解压时字节计数
        int count = 0;
        //如果进入点为空说明已经遍历完所有压缩包中文件和目录
        while (zipEntry != null) {
            //如果是一个目录
            if (zipEntry.isDirectory()) {
                file = new File(outputDirectory + File.separator + zipEntry.getName());
                //文件需要覆盖或者是文件不存在
                if(isReWrite || !file.exists()){
                    file.mkdir();
                }
            } else {
                //如果是文件
                file = new File(outputDirectory + File.separator
                        + zipEntry.getName());
                //文件需要覆盖或者文件不存在，则解压文件
                if(isReWrite || !file.exists()){
                    file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    while ((count = zipInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, count);
                    }
                    fileOutputStream.close();
                }
            }
            //定位到下一个文件入口
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();
    }

    public static void notifySystemToScan(Activity activity, String filePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(filePath);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        activity.getApplication().sendBroadcast(intent);
    }
}
