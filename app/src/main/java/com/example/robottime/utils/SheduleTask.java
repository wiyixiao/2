package com.example.robottime.utils;

/**
 * https://www.cnblogs.com/happyxiaoyu02/p/6818948.html
 */

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SheduleTask {

    private final String tag = getClass().getSimpleName();

    private Context mcontext;
//	private Map<Integer,Integer> weekValues=new HashMap<Integer,Integer>(7);

    public SheduleTask(Context mcontext){
        this.mcontext=mcontext;
//		initWeekDay();
    }

	/*private void initWeekDay(){
		weekValues.put(1, Calendar.MONDAY);
		weekValues.put(2, Calendar.TUESDAY);
		weekValues.put(3, Calendar.WEDNESDAY);
		weekValues.put(4, Calendar.THURSDAY);
		weekValues.put(5, Calendar.FRIDAY);
		weekValues.put(6, Calendar.SATURDAY);
		weekValues.put(7, Calendar.SUNDAY);
	}*/
    /**
     * 格式化时间格式
     * @param date
     */
    private void formatSheduleTimer(String date){
        int week=0,hour=0,minutes=0;
        String[] str=date.split(":");

        if(str==null||str.length==0){
            Log.e(tag, "time data error!");
            return;
        }else{
            try{
                week=Integer.valueOf(str[0]);
                hour=Integer.valueOf(str[1]);
                minutes=Integer.valueOf(str[2]);
            }catch(NumberFormatException e){
                Log.e(tag, "time formate erroe!");
            }
        }
        Log.e(tag, "time= "+week+":"+hour+":"+minutes);
    }
    /**
     * 距离此时后多少秒开始计划任务
     * @param intent
     * @param requestCode
     * @param delaySecond
     */
    public void startSheduleDelayTime(Intent intent,int requestCode,int delaySecond){
        // We want the alarm to go off some seconds from now.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, delaySecond);
        PendingIntent sender = PendingIntent.getBroadcast(mcontext,requestCode, intent, 0);
        // Schedule the alarm!
        AlarmManager am = (AlarmManager)mcontext.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }
    /**
     * 某月某日一次计划任务
     * @param intent
     * @param requestCode
     * @param month
     * @param day
     * @param hour
     * @param minuts
     */
    public void startSheduleSpecificData(Intent intent,int requestCode,int month,int day,int hour,int minuts){
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minuts);
        calendar.set(Calendar.SECOND, 0);

        PendingIntent sender = PendingIntent.getBroadcast(mcontext,requestCode, intent, 0);
        // Schedule the alarm!
        AlarmManager am = (AlarmManager)mcontext.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }
    /**
     * 以天为单位进行循环计划任务
     * @param intent
     * @param requestCode
     * @param hour
     * @param minuts
     */
    public void startSheduleEveryday(Intent intent,int requestCode,int hour,int minuts){
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minuts);
        calendar.set(Calendar.SECOND, 0);

        PendingIntent sender = PendingIntent.getBroadcast(mcontext,requestCode, intent, 0);
        AlarmManager am = (AlarmManager)mcontext.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, sender);
    }
    /**
     * 以周为单位进行循环计划任务
     * @param intent
     * @param requestCode
     * @param week
     * @param hour
     * @param minuts
     */
    public void startSheduleWeekday(Intent intent,int requestCode,int week,int hour,int minuts){

        Calendar calSet=Calendar.getInstance();
        calSet.set(Calendar.DAY_OF_WEEK, week);
        calSet.set(Calendar.HOUR_OF_DAY, hour);
        calSet.set(Calendar.MINUTE, minuts);
        calSet.set(Calendar.SECOND, 0);
        //calSet.set(Calendar.MILLISECOND, 0);

        PendingIntent sender = PendingIntent.getBroadcast(mcontext,requestCode, intent, 0);
        AlarmManager am = (AlarmManager)mcontext.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP,
                calSet.getTimeInMillis(), 60 * 60 * 1000, sender);
    }
    /**
     * 取消计划任务
     * @param intent
     * @param requestCode
     */
    public void cancelShedule(Intent intent,int requestCode){
        PendingIntent sender = PendingIntent.getBroadcast(mcontext,requestCode, intent, 0);
        AlarmManager am = (AlarmManager)mcontext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }


    /**
     * @return true if clock is set to 24-hour mode
     */
    public boolean get24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }

}