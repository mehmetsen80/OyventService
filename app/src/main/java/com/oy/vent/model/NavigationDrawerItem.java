package com.oy.vent.model;

/**
 * Created by mehmet on 11/8/2014.
 */
public class NavigationDrawerItem {

    private String title;
    private int icon;

    public NavigationDrawerItem(){}

    public NavigationDrawerItem(String title, int icon){
        this.title = title;
        this.icon = icon;
    }

    public String getTitle(){
        return this.title;
    }

    public int getIcon(){
        return this.icon;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setIcon(int icon){
        this.icon = icon;
    }
}
