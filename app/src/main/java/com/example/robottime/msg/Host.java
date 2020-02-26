//https://blog.csdn.net/xyzz609/article/details/51916222
//对话框从机对象
//显示在列表右侧

package com.example.robottime.msg;

public class Host {
    private int aIcon;
    private String aName;

    public Host() {
    }

    public Host(int aIcon, String aName) {
        this.aIcon = aIcon;
        this.aName = aName;
    }

    public int getaIcon() {
        return aIcon;
    }

    public String getaName() {
        return aName;
    }

    public void setaIcon(int aIcon) {
        this.aIcon = aIcon;
    }

    public void setaName(String aName) {
        this.aName = aName;
    }
}

