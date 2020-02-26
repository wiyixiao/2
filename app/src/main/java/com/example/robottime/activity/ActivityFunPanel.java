package com.example.robottime.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.GridView;

import com.example.robottime.FunPanelItemBean;
import com.example.robottime.R;
import com.example.robottime.StaticVariable;
import com.example.robottime.utils.CommonUtil;

import java.util.ArrayList;

import adapter.FunPanelItemAdapter;
import android_opencv_api.CvHelper;

public class ActivityFunPanel extends AppCompatActivity implements /*View.OnClickListener, */ AdapterView.OnItemClickListener{

    private static final String TAG = "FunPanelActivity";

    private GridView gdPanel;

    private ArrayList<FunPanelItemBean> funPanelItemBeanArrayList;

    private FunPanelItemAdapter funPanelItemAdapter;

    private AlertDialog.Builder infoDisplayBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_fun_panel);

        initData();
        funPanelItemAdapter = new FunPanelItemAdapter(this, funPanelItemBeanArrayList);
        setLayout();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

    }

    @Override
    public void onBackPressed(){
        //do something

        finish();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.gd_fun_panel :
                selectFunction(position);
                break;
            default:
                break;
        }

    }

    private void initData()
    {
        funPanelItemBeanArrayList = new ArrayList<FunPanelItemBean>();
        funPanelItemBeanArrayList.add(new FunPanelItemBean("视觉模式", R.mipmap.icon_cv_64));
        funPanelItemBeanArrayList.add(new FunPanelItemBean("按键模式", R.mipmap.icon_keyboard_64));
        funPanelItemBeanArrayList.add(new FunPanelItemBean("语音模式", R.mipmap.icon_voicecontrol_64));
        funPanelItemBeanArrayList.add(new FunPanelItemBean("扫码下载", R.mipmap.icon_info_64));
    }

    private void setLayout()
    {
        gdPanel = (GridView) findViewById(R.id.gd_fun_panel);
        gdPanel.setVisibility(View.VISIBLE);
        gdPanel.setAdapter(funPanelItemAdapter);
        gdPanel.setOnItemClickListener(this);
        gdPanel.setSelection(0);
    }

    private void selectFunction(int position)
    {
        //this,funPanelItemBeanArrayList.get(position).getIconName()
        switch (position)
        {
            case 0: //视觉模式
                //视觉功能界面跳转
                //检测是否初始化完成
                if(CvHelper.isInit())
                {
                    Intent intentCv = new Intent(ActivityFunPanel.this, ActivityCv.class);
                    startActivityForResult(intentCv, StaticVariable.ACTIVITYCV_MSG_ID);
                }
                else
                {
                    CommonUtil.toastTextShow(ActivityFunPanel.this, R.string.cv_load_cascade_info);
                }
                break;
            case 1: //按键模式
                Intent intentKeyboard = new Intent(ActivityFunPanel.this, ActivityKeyboard.class);
                startActivityForResult(intentKeyboard, StaticVariable.ACTIVITYKEYBOARD_MSG_ID);
                break;
            case 2: //语音模式
                Intent intentVoice = new Intent(ActivityFunPanel.this, ActivityVoice.class);
                startActivityForResult(intentVoice, StaticVariable.ACTIVITYVOICE_MSG_ID);
                break;
            case 3: //扫码下载
                showDownloadQR();
                break;
        }

    }

    private void showDownloadQR()
    {
        infoDisplayBuilder = new AlertDialog.Builder(ActivityFunPanel.this);
        infoDisplayBuilder.setTitle("扫码下载APP");
        View infoView = GridLayout.inflate(ActivityFunPanel.this, R.layout.dialog_info, null);
        infoDisplayBuilder.setView(infoView);
        infoDisplayBuilder.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
            }
        });
        infoDisplayBuilder.show();
    }

}
