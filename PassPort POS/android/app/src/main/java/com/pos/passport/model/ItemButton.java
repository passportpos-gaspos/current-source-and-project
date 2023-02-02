
package com.pos.passport.model;

import android.graphics.Bitmap;

public class ItemButton {
    public static final int TYPE_FOLDER = 1;
    public static final int TYPE_PRODUCT = 2;
    public static final int TYPE_DEPARTMENT = 3;
    public static final int TYPE_TENDER = 4;
    public static final int TYPE_MULTI = 5;
    public static final int TYPE_APPLINK = 6;
    public static final int TYPE_TEXT = 7;

    public int id;
    public int type;
    public int parent = 0;
    public int productID;
    public String folderName;
    public Bitmap image;
    public int order;
    public String link;
    public String price="";
    public int departID;
    public boolean deleted;
    public String startdate;
    public String enddate;
    public int trackable;
    public int reorderLevel;
    public String saleprice;
    public int quantity=0;
}
