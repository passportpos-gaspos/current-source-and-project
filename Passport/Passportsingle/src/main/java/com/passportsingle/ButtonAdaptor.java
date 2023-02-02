package com.passportsingle;

import com.passportsingle.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ButtonAdaptor extends CursorAdapter {
		
	public Context context;

	public ButtonAdaptor(Cursor cursor, Activity context) {
        super(context, cursor);
        this.context = context;
	}

	@Override
	public void bindView(View view, Context context, Cursor c) {
				
        final ImageView imageView = (ImageView) view.findViewById(R.id.button_image);

        TextView order = ((TextView) view.findViewById(R.id.button_order));

		Button button = new Button();	
		
		button.type = c.getInt(c.getColumnIndex("type"));
		button.order = c.getInt(c.getColumnIndex("orderBy"));
		button.parent = c.getInt(c.getColumnIndex("parent"));
		button.productID = c.getInt(c.getColumnIndex("productID"));
		button.departID = c.getInt(c.getColumnIndex("departID"));
		button.folderName = c.getString(c.getColumnIndex("folderName"));
		button.link = c.getString(c.getColumnIndex("link"));

        final byte[] imageBlob = c.getBlob(c.getColumnIndexOrThrow("image"));
        
        if(imageBlob != null)
        {       	
        	Bitmap image = BitmapFactory.decodeByteArray(imageBlob, 
        	        0,imageBlob.length);
        	button.image = image;
        }
		
		if(button.image != null)
		{
			imageView.setImageBitmap(button.image);
			
		}else{
			imageView.setImageBitmap(null);
		}
		
        if(button.type == -1)
        {
        	((TextView) view.findViewById(R.id.button_text)).setText("Back");
        	imageView.setBackgroundResource(R.drawable.button_red_selector);
        	imageView.setImageResource(R.drawable.back);
        }
        
        if(button.type == Button.TYPE_PRODUCT)
        {
        	Cursor product = ProductDatabase.getProdByName(""+button.productID);
        	if(product != null)
        	{
        		((TextView) view.findViewById(R.id.button_text)).setText(product.getString(product.getColumnIndex("name")));
        		product.close();
        	}else{
        		((TextView) view.findViewById(R.id.button_text)).setText("ERROR");
        	}
        	imageView.setBackgroundResource(R.drawable.button_gray_selector);

        }
        
        if(button.type == Button.TYPE_FOLDER)
        {
        	((TextView) view.findViewById(R.id.button_text)).setText(button.folderName);
        	imageView.setBackgroundResource(R.drawable.button_yellow_selector);
        }
        
        if(button.type == Button.TYPE_TEXT)
        {
        	((TextView) view.findViewById(R.id.button_text)).setText(button.folderName);
        	imageView.setBackgroundResource(R.drawable.button_yellow_selector);
        }
        
        if(button.type == Button.TYPE_APPLINK)
        {
        	if(isAppInstalled(button.folderName))
        	{
        		Drawable icon = null;
        		
        		
        		String name = "No Name";
        		try {
        			ApplicationInfo app = context.getPackageManager().getApplicationInfo(button.folderName, PackageManager.GET_META_DATA);
					icon = app.loadIcon(context.getPackageManager());
					name = app.loadLabel(context.getPackageManager()).toString();
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		if(icon != null)
        			imageView.setImageDrawable(icon);
        		
        		imageView.setScaleType(ImageView.ScaleType.CENTER);
        		((TextView) view.findViewById(R.id.button_text)).setText(name);
        	}
        	/*else
        	{
        		if(button.folderName.toLowerCase().equals("com.squareup") || button.folderName.toLowerCase().equals("com.intuit.intuitgopayment") || button.folderName.toLowerCase().equals("ban.card.payanywhere") || button.folderName.toLowerCase().contains("vt.mercurypay.com"))
    			{
        			((TextView) view.findViewById(R.id.button_text)).setText(button.folderName);	
				}else{
					((TextView) view.findViewById(R.id.button_text)).setText("Not Installed");
				}
        	}
        	if(button.folderName.toLowerCase().equals("com.squareup") || button.folderName.toLowerCase().equals("com.intuit.intuitgopayment") || button.folderName.toLowerCase().equals("ban.card.payanywhere") || button.folderName.toLowerCase().contains("vt.mercurypay.com"))
			{
	        	imageView.setBackgroundResource(R.drawable.button_green_selector);
			}else{
	        	imageView.setBackgroundResource(R.drawable.button_yellow_selector);
			} */
        	
        	if(button.folderName.equals("Upgrade")){
        		
        		((TextView) view.findViewById(R.id.button_text)).setText(R.string.upgrade);
        		imageView.setBackgroundResource(R.drawable.posicon);
        	}
        }
        
        if(button.type == Button.TYPE_MULTI)
        {
        	((TextView) view.findViewById(R.id.button_text)).setText(button.folderName);
        	imageView.setBackgroundResource(R.drawable.button_gray_selector);
        }

        if(button.type == Button.TYPE_DEPARTMENT)
        {
        	String department = ProductDatabase.getCatById(button.departID);

        	((TextView) view.findViewById(R.id.button_text)).setText(department);
        	imageView.setBackgroundResource(R.drawable.button_blue_selector);
        }
        
        if(button.type == Button.TYPE_TENDER)
        {
        	((TextView) view.findViewById(R.id.button_text)).setText(button.folderName);
        	imageView.setBackgroundResource(R.drawable.button_green_selector);
        	if(button.folderName.equals("Cash"))
        		imageView.setImageResource(R.drawable.bill);
        	if(button.folderName.equals("Credit Card"))
        		imageView.setImageResource(R.drawable.credit_card);
        	if(button.folderName.equals("Check"))
        		imageView.setImageResource(R.drawable.check);
        }
        
        if(order != null && button.order > -1)
        	order.setText(""+button.order);
        else if(order != null)
        	order.setText("");

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    	int size = (int) Math.ceil(context.getResources().getDimension(R.dimen.button_space));
    	//size = (int) (size / metrics.density);
        imageView.setPadding(size,size,size,size);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view;
        if(context instanceof InventoryFragment)
        {
        	view = inflater.inflate(R.layout.button_edit, parent, false);
        }
        else
        {
        	view = inflater.inflate(R.layout.button_view, parent, false);
        }
        return view;
	}
	
	protected boolean isAppInstalled(String packageName) {
        Intent mIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (mIntent != null) {
            return true;
        }
        else {
            return false;
        }
    }
}