package com.passportsingle;

import android.graphics.Bitmap;

public class Button {
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
	public int departID;
}
