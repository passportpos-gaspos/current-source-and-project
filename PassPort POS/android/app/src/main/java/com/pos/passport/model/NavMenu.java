package com.pos.passport.model;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 10/29/15.
 */
public class NavMenu {
    private @IdRes int id;
    private @DrawableRes int icon;
    private @StringRes int name;
    private List<NavMenu> subMenus;

    public NavMenu() {
        subMenus = new ArrayList<>();
    }

    public NavMenu(@IdRes int id, @DrawableRes int icon, @StringRes int name) {
        this();
        this.id = id;
        this.icon = icon;
        this.name = name;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public List<NavMenu> getSubMenus() {
        return subMenus;
    }

    public void setSubMenus(List<NavMenu> subMenus) {
        this.subMenus = subMenus;
    }
}
