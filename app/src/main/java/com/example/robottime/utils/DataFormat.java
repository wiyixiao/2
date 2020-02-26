package com.example.robottime.utils;

import android.util.Log;
import android.widget.TableRow;

public class DataFormat {

    private static final String TAG = "DataFormat.java";
    private static final byte DATA_HEAD = (byte) 0xf0; //数据头
    private static final byte DATA_TAIL = (byte) 0xff; //数据尾
    private static final byte MODE_TRACK = (byte) 0x01; //视觉跟踪模式
    private static final byte MODE_KEY = (byte) 0x02; //按键模式
    private static final byte MODE_CMD = (byte) 0x03; //命令行模式

    public static byte MODE_KEY(){return MODE_KEY;}
    public static byte MODE_CMD(){return MODE_CMD;}

    //十进制转二进制字符串
    public static String decTobinStr(int dec)
    {
        return Integer.toBinaryString(dec);
    }

    //十六进制转对应字符串
    public static String hexToString(int hex)
    {
        return Integer.toHexString(hex);
    }

    //十进制转bcd码
    public static String DecimaltoBcd(int dec){
        String b_num="";
        String str = String.valueOf(dec);
        for (int i = 0; i < str.length(); i++) {

            String b = Integer.toBinaryString(Integer.parseInt(str.valueOf(str.charAt(i))));

            int b_len=4-b.length();

            for(int j=0;j<b_len;j++){
                b="0"+b;
            }
            b_num += b;
        }

        return b_num;
    }

    //bcd码转十进制
    public static String bcd2Str(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
            temp.append((byte) (bytes[i] & 0x0f));
        }
        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp
                .toString().substring(1) : temp.toString();
    }

    //byte[]数组转二进制字符串
    public static String byteArrToBinStr(byte[] b) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            result.append(Long.toString(b[i] & 0xff, 2) + ",");
        }
        return result.toString().substring(0, result.length() - 1);
    }

    //二进制字符串转byte[]数组
    static byte[] string2bytes(String input) {
        StringBuilder in = new StringBuilder(input);
        // 注：这里in.length() 不可在for循环内调用，因为长度在变化
        int remainder = in.length() % 8;
        if (remainder > 0)
            for (int i = 0; i < 8 - remainder; i++)
                //in.append("0");
                in.insert(8+i, "0");
        byte[] bts = new byte[in.length() / 8];

        // Step 8 Apply compression
        for (int i = 0; i < bts.length; i++)
            bts[i] = (byte) Integer.parseInt(in.substring(i * 8, i * 8 + 8), 2);

        return bts;
    }

    //byte[]转hex字符串
    public static String bytes2HexString(byte[] array) {
        StringBuilder builder = new StringBuilder();

        for (byte b : array) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            builder.append(hex);
        }

        return builder.toString().toUpperCase();
    }

    private static byte[] intToHexArray(int dec)
    {
        try
        {
            //转bcd码
            String bcdtmp = DecimaltoBcd(dec);
            String bcd = dec <= 9 ? "0000" + bcdtmp : bcdtmp; //小于8位左边补零

            byte[] datatmp = {0, 0, 0}; //3字节
            byte j = 0;
            for(byte b : string2bytes(bcd))
            {
                datatmp[j] = (byte)(b & 0XFF);
                j++;
            }

            if(dec >=0 && dec < 100)
                datatmp[2] = 0x00; //x1
            if(dec >= 100 && dec < 1000)
                datatmp[2] = 0x01; //x10
            else if(dec >= 1000)
                datatmp[2] = 0x02; //x100

            //Log.d(TAG, "**********************"+dec+","+bcd+","+datatmp[0] + "," + datatmp[1] + "," + datatmp[2]);

            return datatmp;
        }
        catch (Exception ex)
        {
            Log.d(TAG, "error from intToHexArray");
        }

        return null;
    }

    //int to bytes[]
    private static void setBits(byte[] data, int offset, int val)
    {
        try
        {
            byte[] tmp = intToHexArray(val);

            data[offset] = tmp[0];
            data[offset+1] = tmp[1];
            data[offset+2] = tmp[2];

            //data[offset] = (byte)((val >> 8) & 0xFF);
            //data[offset+1] = (byte)(val & 0xFF);
        }
        catch (Exception ex)
        {
            ;
        }
    }

    //发送位置信息
    public static byte[] posToBytes(int x, int y, int d)
    {
        /**
         * 开始结束标志2字节
         * 模式标志1字节
         * 位置信息x,y,r各2字节
         * 共：1+1+9+1 = 9
         */
        byte[] data = new byte[12];

        data[0] = DATA_HEAD;
        data[1] = MODE_TRACK;
        setBits(data, 2, x);
        setBits(data, 5, y);
        setBits(data, 8, d);
        data[11] = DATA_TAIL;

        //Log.d(TAG, bytes2HexString(data));

        return  data;
    }

    //hex字符串转byte[]数组
    public static byte[] hexToByteArray(byte mode, String hex)
    {
        hex = hex.replaceAll("\\s*", ""); //去除空格等字符
        int len = hex.length(); //字符串长度

        int bts_len = (len + (len / 2) + 3); //总字节数，增加3字节
        byte[] data = new byte[bts_len];
        data[0] = DATA_HEAD;
        data[1] = mode; //模式标志

        for (int i = 0, j=2; i < len; i += 2,j+=3)
        {
            try
            {
                int tmp = (int) ((Character.digit(hex.charAt(i), 16) << 4)
                        + Character.digit(hex.charAt(i + 1), 16));

                byte[] datahex = intToHexArray(tmp);

                                      //0 2 4
                data[j] = datahex[0]; //0 3 5
                data[j+1] = datahex[1]; //3 6 8
                data[j+2] = datahex[2]; //4 7 9
            }
            catch (StringIndexOutOfBoundsException e)
            {
                e.printStackTrace();
            }
            catch (NullPointerException ex)
            {
                ex.printStackTrace();
            }

        }
        data[bts_len - 1] = DATA_TAIL;

        //输出查看
        //Log.d(TAG, bytes2HexString(data));

        return data;
    }
}
