package com.pos.passport.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.activity.InventoryActivity;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.ItemButton;
import com.pos.passport.util.Utils;

public class ButtonAdapter extends CursorAdapter {
	public Context context;
	private ProductDatabase mDb;

	public ButtonAdapter(Cursor cursor, Activity context) {
        super(context, cursor);
        this.context = context;
        mDb = ProductDatabase.getInstance(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor c) {
				
        final ImageView imageView = (ImageView) view.findViewById(R.id.button_image);

        TextView order = ((TextView) view.findViewById(R.id.button_order));

		ItemButton itemButton = new ItemButton();
		
		itemButton.type = c.getInt(c.getColumnIndex("type"));
		itemButton.order = c.getInt(c.getColumnIndex("orderBy"));
		itemButton.parent = c.getInt(c.getColumnIndex("parent"));
		itemButton.productID = c.getInt(c.getColumnIndex("productID"));
		itemButton.departID = c.getInt(c.getColumnIndex("departID"));
		itemButton.folderName = c.getString(c.getColumnIndex("folderName"));
		itemButton.link = c.getString(c.getColumnIndex("link"));

        final byte[] imageBlob = c.getBlob(c.getColumnIndexOrThrow("image"));
        
        if (imageBlob != null) {
			itemButton.image = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
        }
		
		if (itemButton.image != null) {
			imageView.setImageBitmap(itemButton.image);
		} else {
			imageView.setImageBitmap(null);
		}
		
        if (itemButton.type == -1) {
        	((TextView) view.findViewById(R.id.button_text)).setText(R.string.txt_back);
        	imageView.setBackgroundResource(R.drawable.button_red_selector);
        	//imageView.setImageResource(R.drawable.back);
        }
        
        if (itemButton.type == ItemButton.TYPE_PRODUCT) {
        	Cursor product = mDb.getProdById(itemButton.productID);
        	if(product != null) {
        		((TextView) view.findViewById(R.id.button_text)).setText(product.getString(product.getColumnIndex("name")));
        		product.close();
        	} else {
        		((TextView) view.findViewById(R.id.button_text)).setText(R.string.txt_error_cap);
        	}
        	imageView.setBackgroundResource(R.drawable.button_gray_selector);

        }
        
        if (itemButton.type == ItemButton.TYPE_FOLDER) {
        	((TextView) view.findViewById(R.id.button_text)).setText(itemButton.folderName);
        	imageView.setBackgroundResource(R.drawable.button_yellow_selector);
        }
        
        if (itemButton.type == ItemButton.TYPE_TEXT) {
        	((TextView) view.findViewById(R.id.button_text)).setText(itemButton.folderName);
        	imageView.setBackgroundResource(R.drawable.button_yellow_selector);
        }
        
        if (itemButton.type == ItemButton.TYPE_APPLINK) {
        	if(Utils.isAppInstalled(context, itemButton.folderName)) {
        		Drawable icon = null;

        		String name = "No Name";
        		try {
        			ApplicationInfo app = context.getPackageManager().getApplicationInfo(itemButton.folderName, PackageManager.GET_META_DATA);
					icon = app.loadIcon(context.getPackageManager());
					name = app.loadLabel(context.getPackageManager()).toString();
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		if (itemButton.image == null && icon != null) {
        			imageView.setImageDrawable(icon);
        			imageView.setScaleType(ImageView.ScaleType.CENTER);
        		} else {
        			imageView.setAdjustViewBounds(true);
        		}
        		((TextView) view.findViewById(R.id.button_text)).setText(name);
        	} else {
        		if (itemButton.folderName.toLowerCase().contains("vt.mercurypay.com")) {
        			((TextView) view.findViewById(R.id.button_text)).setText("Mercury VT");
				} else if(itemButton.folderName.toLowerCase().contains("http")) {
					((TextView) view.findViewById(R.id.button_text)).setText(itemButton.folderName.toLowerCase());
				} else {
					((TextView) view.findViewById(R.id.button_text)).setText("Not Installed");
				}
        	}
        	if(itemButton.folderName.toLowerCase().equals("com.izettle.android") || itemButton.folderName.toLowerCase().equals("com.paypal.here") || itemButton.folderName.toLowerCase().equals("com.squareup") || itemButton.folderName.toLowerCase().equals("com.intuit.intuitgopayment") || itemButton.folderName.toLowerCase().equals("ban.card.payanywhere") || itemButton.folderName.toLowerCase().contains("vt.mercurypay.com")) {
	        	imageView.setBackgroundResource(R.drawable.button_green_selector);
			} else {
	        	imageView.setBackgroundResource(R.drawable.button_yellow_selector);
			}
        }
        
        if (itemButton.type == ItemButton.TYPE_MULTI) {
        	((TextView) view.findViewById(R.id.button_text)).setText(itemButton.folderName);
        	imageView.setBackgroundResource(R.drawable.button_gray_selector);
        }

        if (itemButton.type == ItemButton.TYPE_DEPARTMENT) {
        	String department = mDb.getCatById(itemButton.departID);

        	((TextView) view.findViewById(R.id.button_text)).setText(department);
        	imageView.setBackgroundResource(R.drawable.button_blue_selector);
        }
        
        if (itemButton.type == ItemButton.TYPE_TENDER) {
        	((TextView) view.findViewById(R.id.button_text)).setText(itemButton.folderName);
        	//imageView.setBackgroundResource(R.drawable.button_green_selector);
        	if(itemButton.folderName.equals("Cash"))
        		//imageView.setImageResource(R.drawable.bill);
        	if(itemButton.folderName.equals("Credit Card"))
        		//imageView.setImageResource(R.drawable.credit_card);
        	if(itemButton.folderName.equals("Check")){}
        		//imageView.setImageResource(R.drawable.check);
        }
        
        if(order != null && itemButton.order > -1)
        	order.setText(""+ itemButton.order);
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
        if (context instanceof InventoryActivity) {
        	view = inflater.inflate(R.layout.button_edit, parent, false);
        } else {
        	view = inflater.inflate(R.layout.button_view, parent, false);
        }
        return view;
	}
}