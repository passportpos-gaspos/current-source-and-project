package com.pos.passport.model;

/**
 * Created by imd-macmini on 10/25/16.
 */
public class ItemComoSelection
{
    int comboid;
    int itemid;

    public ItemComoSelection(int comboid, int itemid) {
        this.comboid = comboid;
        this.itemid = itemid;
    }

    public int getComboid() {
        return comboid;
    }

    public void setComboid(int comboid) {
        this.comboid = comboid;
    }

    public int getItemid() {
        return itemid;
    }

    public void setItemid(int itemid) {
        this.itemid = itemid;
    }
}
