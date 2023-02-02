package com.pos.passport.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.pos.passport.R;

import org.json.JSONObject;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by karim on 9/17/15.
 */
public class Utils {
    public static DecimalFormat twoDecimalFormat = new DecimalFormat("#.00");

    public static boolean hasInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }
    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {

            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                //Read byte from input stream

                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;

                //Write byte from output stream
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    public static Rect locateView(View v)
    {
        int[] loc_int = new int[2];
        if (v == null) return null;
        try
        {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe)
        {
            //Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }

    public static StringBuilder inputStreamToString(InputStream is) {
        String rLine = "";
        StringBuilder answer = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        try {
            while ((rLine = rd.readLine()) != null) {
                answer.append(rLine);
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }
        return answer;
    }

    public static  boolean isAppInstalled(Context context, String packageName) {
        Intent mIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (mIntent != null) {
            return true;
        }
        else {
            return false;
        }
    }

    public static String convert_2_unicode(String str){

        String string = "";
        try {
            // Convert from Unicode to UTF-8

            byte[] utf8 = str.getBytes("UTF-8");

            // Convert from UTF-8 to Unicode
            string = new String(utf8, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }

        return string;
    }

    public static void alertBox(Context context, String title, String mesage) {
        new AlertDialog.Builder(context)
            .setMessage(mesage)
            .setTitle(title)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();
    }
    public static void alertBoxchange(Context context, String title, String mesage)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //new AlertDialog.Builder(context)
        builder.setMessage(mesage);
        builder.setTitle(title);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Double width = metrics.widthPixels * .5;
        Double height = Double.parseDouble("" + metrics.heightPixels);

       // Window win = builder.create().getWindow();
        //win.setLayout(width.intValue(), height.intValue());
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(builder.create().getWindow().getAttributes());
        lp.width = width.intValue();
        lp.height = height.intValue();

        builder.create().getWindow().setAttributes(lp);
        builder.show();



    }
    public static Dialog CustomBox(Context context,  @StringRes int titleId, @StringRes int messageId)
    {
        final int result= 0;
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog);
        // dialog.setTitle("Custom Dialog");
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        TextView title_txt = (TextView) dialog.findViewById(R.id.title_txt);
        TextView msg_txt = (TextView) dialog.findViewById(R.id.msg_txt);
        //TextView btn_ok = (TextView) dialog.findViewById(R.id.btn_ok);
        title_txt.setText(titleId);
        msg_txt.setText(messageId);
        /*btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                dialog.cancel();
            }
        });*/
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Double width = metrics.widthPixels * .58;
        //Double height = metrics.heightPixels*.7;
        Double height = Double.parseDouble("" + metrics.heightPixels);
        Window win = dialog.getWindow();
        win.setLayout(width.intValue(), height.intValue());

        dialog.show();
        dialog.getWindow().setBackgroundDrawable( new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return  dialog;
    }
    public static void alertBox(Context context, String title, @StringRes int messageId) {
        AlertDialog alertDialog =new AlertDialog.Builder(context)
            .setMessage(messageId)
            .setTitle(title)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();
        ViewGroup view = (ViewGroup) alertDialog.getWindow().getDecorView();
        Utils.setTypeFace(Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf"), view);

    }

    public static void alertBox(Context context, @StringRes int titleId, String message) {
        new AlertDialog.Builder(context)
            .setMessage(message)
            .setTitle(titleId)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();
    }

    public static void alertBox(Context context, @StringRes int titleId, @StringRes int messageId) {
        new AlertDialog.Builder(context)
            .setMessage(messageId)
            .setTitle(titleId)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();
    }

    private static boolean makedirs(String dir) {
        File tempdir = new File(dir);
        if (!tempdir.exists())
            tempdir.mkdirs();

        if (tempdir.isDirectory()) {
            File[] files = tempdir.listFiles();
            for (File file : files) {
                if (!file.delete()) {
                    System.out.println("Failed to delete " + file);
                }
            }
        }
        return (tempdir.isDirectory());
    }

    public static boolean hasSmallerSide(Context context, int dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        int dpWidth = Math.round(width / (metrics.densityDpi / 160f));
        int dpHeight = Math.round(height / (metrics.densityDpi / 160f));

        return (dpWidth < dp || dpHeight < dp);
    }

    public static String join(ArrayList<String> coll, String delimiter) {
        if (coll == null)
            return "";
        if (coll.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();
        for (String x : coll)
            sb.append(x + delimiter);
        sb.delete(sb.length() - delimiter.length(), sb.length());
        return sb.toString();
    }

    public static String joinParameters(List<RestAgent.Parameter> params, boolean encoded) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (RestAgent.Parameter param : params) {
            sb.append(encoded ? param.toEncodedString() : param.toString());
            sb.append("&");
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    public static String postData(String url, String name, JSONObject data) {
        String result = null ;

        List<RestAgent.Parameter> params = new ArrayList<>();
        params.add(new RestAgent.Parameter(name, data.toString()));
        RestAgent agent = new RestAgent(url, RestAgent.POST, params);
        try {
            String jsonResult = agent.send();
            JSONObject jsonObject = new JSONObject(jsonResult);
            result = jsonObject.getString("result");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }
    public static String GetData(String url, String name, JSONObject data) {
        String result = null ;

       // List<RestAgent.Parameter> params = new ArrayList<>();
       // params.add(new RestAgent.Parameter(name, data.toString()));
        RestAgent agent = new RestAgent(url, RestAgent.POST, data.toString());
        try {
            String jsonResult = agent.sendNew();
            JSONObject jsonObject = new JSONObject(jsonResult);
            Log.e("Tag",""+jsonResult);
            result = jsonObject.toString();//.getString("result");
        } catch (Exception ex) {
            Log.e("Tag",ex.getMessage());
            //ex.printStackTrace();
        }

        return result;
    }

    public static String SendPrinterOrCardData(String url,JSONObject data,String method) {
        String result = null ;
        // List<RestAgent.Parameter> params = new ArrayList<>();
        // params.add(new RestAgent.Parameter(name, data.toString()));
        RestAgent agent = new RestAgent(url, method, data.toString());
        try {
            String jsonResult = agent.sendNew();
            JSONObject jsonObject = new JSONObject(jsonResult);
            result = jsonObject.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static String GetPrinterOrCardData(String url,JSONObject data,String method) {
        String result = null ;
        // List<RestAgent.Parameter> params = new ArrayList<>();
        // params.add(new RestAgent.Parameter(name, data.toString()));
        RestAgent agent = new RestAgent(url, method, data.toString());
        try {
            String jsonResult = agent.sendNew();
            //JSONObject jsonObject = new JSONObject(jsonResult);
            result = jsonResult;//jsonObject.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static String DeletePrinterOrCardData(String url,JSONObject data) {
        String result = null ;
        // List<RestAgent.Parameter> params = new ArrayList<>();
        // params.add(new RestAgent.Parameter(name, data.toString()));
        RestAgent agent = new RestAgent(url, RestAgent.POST, data.toString());
        try {
            String jsonResult = agent.sendNew();
            JSONObject jsonObject = new JSONObject(jsonResult);
            result = jsonObject.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static String GetDataOrder(String url, String name, JSONObject data) {
        String result = null ;

        // List<RestAgent.Parameter> params = new ArrayList<>();
        // params.add(new RestAgent.Parameter(name, data.toString()));
        RestAgent agent = new RestAgent(url, RestAgent.GET, data.toString());
        try {
            String jsonResult = agent.sendNewOrder();
            //JSONObject jsonObject = new JSONObject(jsonResult);
            //result = jsonObject.toString();//.getString("result");
            result = jsonResult.toString();//.getString("result");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static String GetUpdateStatus(String url, String name, JSONObject data) {
        String result = null ;

        // List<RestAgent.Parameter> params = new ArrayList<>();
        // params.add(new RestAgent.Parameter(name, data.toString()));
        RestAgent agent = new RestAgent(url, RestAgent.PUT, data.toString());
        try {
            String jsonResult = agent.sendNew();
            //JSONObject jsonObject = new JSONObject(jsonResult);
            //result = jsonObject.toString();//.getString("result");
            result = jsonResult.toString();//.getString("result");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static JSONObject postData(String url, String name, String data) {
        JSONObject jsonObject = null;

        List<RestAgent.Parameter> params = new ArrayList<>();
        params.add(new RestAgent.Parameter(name, data));
        //RestAgent agent = new RestAgent(url, RestAgent.POST, params);
        RestAgent agent = new RestAgent(url, RestAgent.POST, data);
        try {
            String jsonResult = agent.sendSubmit();
            jsonObject = new JSONObject(jsonResult);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return jsonObject;
    }

    public static String formatCurrency(BigDecimal amount) {
        return DecimalFormat.getCurrencyInstance().format(amount);
        //return DecimalFormat.getCurrencyInstance().format(amount.divide(Consts.HUNDRED));
    }

    public static String formatDiscount(BigDecimal amount)
    {
        return DecimalFormat.getCurrencyInstance().format(amount.multiply(Consts.MINUS_ONE) );
        //return DecimalFormat.getCurrencyInstance().format(amount.divide(Consts.HUNDRED).multiply(Consts.MINUS_ONE) );
    }
    public static String formatDiscountCart(BigDecimal amount)
    {
        return DecimalFormat.getCurrencyInstance().format(amount);
        //return DecimalFormat.getCurrencyInstance().format(amount.divide(Consts.HUNDRED).multiply(Consts.MINUS_ONE) );
    }
    public static String formatTotal(String amount){
        return twoDecimalFormat.format(new BigDecimal(amount));
    }

    public static BigDecimal formatTotal(BigDecimal amount){
        return new BigDecimal(twoDecimalFormat.format(amount));
    }

    public static String formatCartTotal(BigDecimal total){
        return (new DecimalFormat("#0.00")).format(total);
    }

    public static String getJsonStringFromResource(Context context, @RawRes int resId) {
        InputStream inputStream = context.getResources().openRawResource(resId);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteArrayOutputStream.toString();
    }

    public static void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void setTypeFace(Typeface typeFace, ViewGroup parent){
        for (int i = 0; i < parent.getChildCount(); i++) {
            View v = parent.getChildAt(i);
            if (v instanceof ViewGroup) {
                setTypeFace(typeFace, (ViewGroup) v);
            } else if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setTypeface(typeFace);
                //For making the font anti-aliased.
                tv.setPaintFlags(tv.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
            }
        }
    }

    public static int dpToPixel(Context context, float dp) {
        return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
    }

    public static String getNodeValue(String tagName, NodeList list) {
        String value = "";
        if (list != null && list.getLength() > 0) {
            NodeList subList = list.item(0).getChildNodes();

            if (subList != null && subList.getLength() > 0) {
                value = subList.item(0).getNodeValue();
            }
        }

        return value;
    }

    public static boolean validateEmail(@NonNull String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean checkTimeSpan(long then, int hours) {
        Calendar t = Calendar.getInstance();
        t.setTimeInMillis(then);

        Calendar now = Calendar.getInstance();

        t.add(Calendar.HOUR, hours);

        return now.getTimeInMillis() - t.getTimeInMillis() > 0;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static void setToolbarTitleFont(Context context, Toolbar toolbar){
        try
        {

            Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            TextView titleTextView = (TextView) f.get(toolbar);

            if(PrefUtils.getCashierInfo(context) != null)
            {
                //Log.e("if con","if con side "+context.getResources().getString(R.string.app_name) + " | " + PrefUtils.getCashierInfo(context).name);
                String sourceString = "<b>" + context.getResources().getString(R.string.app_name) + "</b> " + " | " + PrefUtils.getCashierInfo(context).name;
                //titleTextView.setText(context.getResources().getString(R.string.app_name) + " | " + PrefUtils.getCashierInfo(context).name);
                titleTextView.setText(Html.fromHtml(sourceString));
            }else
            {
                //Log.e("else con","admin side else"+context.getResources().getString(R.string.app_name) + " | " + "Administrator");
                //titleTextView.setText(context.getResources().getString(R.string.app_name) + " | " + "Administrator");
                String sourceString = "<b>" + context.getResources().getString(R.string.app_name) + "</b> " + " | " + "Administrator";
                titleTextView.setText(Html.fromHtml(sourceString));
            }
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);

            titleTextView.setTypeface(Typeface.createFromAsset(
                    context.getAssets(),
                    "fonts/Ubuntu-Light.ttf"));
        }catch (Exception e){
            e.printStackTrace();
        }
        /*TextView toolbarTitle = null;
        for (int i = 0; i < toolbar.getChildCount(); ++i) {
            View child = toolbar.getChildAt(i);

            // assuming that the title is the first instance of TextView
            // you can also check if the title string matches
            if (child instanceof TextView) {
                toolbarTitle = (TextView)child;
                break;
            }
        }*/
    }

    public static Map timeDifferenceInHours(long then){
        long now = Calendar.getInstance().getTimeInMillis();
        Calendar offTime = Calendar.getInstance();
        offTime.setTimeInMillis(then);
        long diff = now - offTime.getTimeInMillis();
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
       // long hours = diff/(60 * 60 * 1000);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff-TimeUnit.HOURS.toMillis(diff));
        long seconds = TimeUnit.MILLISECONDS.toMinutes(diff-(TimeUnit.HOURS.toMillis(diff)+TimeUnit.MINUTES.toMillis(diff)));
        Map<String,Long> map = new HashMap<>();
        map.put("hours", hours);
        map.put("minutes", minutes);
        map.put("seconds", seconds);
        return map;
    }

    public static void disableEnableControls(boolean enable, ViewGroup vg){
        for (int i = 0; i < vg.getChildCount(); i++){
            View child = vg.getChildAt(i);
            child.setEnabled(enable);
            if (child instanceof ViewGroup){
                disableEnableControls(enable, (ViewGroup)child);
            }
        }
    }
    public static Bitmap changeImageColor(Bitmap sourceBitmap, int color) {
        Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0,
                sourceBitmap.getWidth() - 1, sourceBitmap.getHeight() - 1);
        Paint p = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 1);
        p.setColorFilter(filter);

        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, p);
        return resultBitmap;
    }


    public static Drawable covertBitmapToDrawable(Context context, Bitmap bitmap) {
        Drawable d = new BitmapDrawable(context.getResources(), bitmap);
        return d;
    }

    public static Bitmap convertDrawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static int getColor(Context context, int res){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           return context.getResources().getColor(res, context.getTheme());
        }else {
            return context.getResources().getColor(res);
        }
    }

    public static String getVersionName(Context context){
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        }catch (Exception e){
            e.printStackTrace();
            return "1.0";
        }
    }


    public static Bitmap convertBase64_2_Bitmap(String encodedImage){
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        return  BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    public static ArrayList<Byte> printImage(Bitmap bmp) {
        ArrayList<Byte> list = new ArrayList<Byte>();
        // Set the Image Resource Id as argument
        int[][] pixels = getPixelsArray(bmp);
        // Get Byte Array
        list = getPrintImageBytes(pixels);
        return list;
    }

    public static int[][] getPixelsArray(Bitmap bmp) {
        int[][] result = new int[bmp.getWidth()][bmp.getHeight()];
        for (int row = 0; row < bmp.getWidth(); row++) {
            for (int col = 0; col < bmp.getHeight(); col++) {
                result[row][col] = bmp.getPixel(row, col);
            }
        }
        return result;
    }

    public static ArrayList<Byte> getPrintImageBytes(int[][] pixels) {
        final char ESC_CHAR = 0x1B;
        final byte[] LINE_FEED = new byte[]{0x0A};
        final byte[] SELECT_BIT_IMAGE_MODE = {0x1B, 0x2A, 33};
        final byte[] SET_LINE_SPACE_24 = new byte[]{ESC_CHAR, 0x33, 24};
        final byte[] SET_LINE_SPACE_30 = new byte[]{ESC_CHAR, 0x33, 30};

        ArrayList<Byte> list = new ArrayList<Byte>();

        for (byte b : SET_LINE_SPACE_24) {
            list.add(Byte.valueOf((byte) b));
        }

        for (int y = 0; y < pixels.length; y += 24) {
            // Set Image Mode
            for (byte b : SELECT_BIT_IMAGE_MODE) {
                list.add(Byte.valueOf((byte) b));
            }

            // Set Pixel Length
            byte[] nLnH = new byte[] {
                    (byte) (0x00ff & pixels[y].length),
                    (byte) ((0xff00 & pixels[y].length) >> 8) };

            for (byte b : nLnH) {
                list.add(Byte.valueOf((byte) b));
            }

            // Set Horizontal pixel
            for (int x = 0; x < pixels[y].length; x++) {
                byte[] sliceArray = recollectSlice(y, x, pixels);
                for (byte b : sliceArray) {
                    list.add(Byte.valueOf((byte) b));
                }
            }

            // Go to next line
            for (byte b : LINE_FEED) {
                list.add(Byte.valueOf((byte) b));
            }

        }

        for (byte b : SET_LINE_SPACE_30) {
            list.add(Byte.valueOf((byte) b));
        }

        return list;
    }

    public static byte[] recollectSlice(int y, int x, int[][] img) {
        byte[] slices = new byte[] { 0, 0, 0 };
        for (int yy = y, i = 0; yy < y + 24 && i < 3; yy += 8, i++) {
            byte slice = 0;
            for (int b = 0; b < 8; b++) {
                int yyy = yy + b;
                if (yyy >= img.length) {
                    continue;
                }
                int col = img[yyy][x];
                boolean v = shouldPrintColor(col);
                slice |= (byte) ((v ? 1 : 0) << (7 - b));
            }
            slices[i] = slice;
        }
        return slices;
    }

    public static boolean shouldPrintColor(int col) {
        final int threshold = 127;
        int a, r, g, b, luminance;
        a = (col >> 24) & 0xff;
        if (a != 0xff) {// Ignore transparencies
            return false;
        }
        r = (col >> 16) & 0xff;
        g = (col >> 8) & 0xff;
        b = col & 0xff;
        luminance = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        return luminance < threshold;
    }
    public static int ResourceSize(Context mcontext)
    {
        return mcontext.getResources().getInteger(R.integer.popuptype);
    }
    public static  int getRandomColor() {
        Random random = new Random();
        int RGB = 0xff + 1;
        int colors;
        int a = 256;
        int r1 = (int) Math.floor(Math.random() * RGB);
        int r2 = (int) Math.floor(Math.random() * RGB);
        int r3 = (int) Math.floor(Math.random() * RGB);
        colors = Color.rgb(r1, r2, r3);

        return colors;
    }
}
