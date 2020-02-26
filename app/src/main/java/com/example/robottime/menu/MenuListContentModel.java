//https://blog.csdn.net/qq_32059827/article/details/53644380

package com.example.robottime.menu;


public class MenuListContentModel {

    private int imageView;
    private String text;
    private int id;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MenuListContentModel() {
    }

    public MenuListContentModel(int imageView, String text, int id) {
        this.imageView = imageView;
        this.text = text;
        this.id = id;
    }

    public int getImageView() {
        return imageView;
    }

    public void setImageView(int imageView) {
        this.imageView = imageView;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
