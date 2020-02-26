//https://blog.csdn.net/xyzz609/article/details/51916222
//对话框主机对象
//显示在列表左侧
package com.example.robottime.msg;

public class Slave {
    private String bName;
    private int bIcon;

    public Slave() {
    }

    public Slave(String bName, int bIcon) {
        this.bName = bName;
        this.bIcon = bIcon;
    }

    public String getbName() {
        return bName;
    }

    public int getbIcon() {
        return bIcon;
    }

    public void setbName(String bName) {
        this.bName = bName;
    }

    public void setbIcon(int bIcon) {
        this.bIcon = bIcon;
    }
}