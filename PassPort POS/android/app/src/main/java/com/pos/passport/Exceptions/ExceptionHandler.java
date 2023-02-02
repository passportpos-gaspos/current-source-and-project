package com.pos.passport.Exceptions;

import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Kareem on 10/8/2016.
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Context myContext;
    private final Class<?> myActivityClass;

    public ExceptionHandler(Context context, Class<?> c) {

        myContext = context;
        myActivityClass = c;
    }
    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        System.err.println(stackTrace);// You can use LogCat too
        //Intent intent = new Intent(myContext, myActivityClass);
        String s = stackTrace.toString();
        Log.d("exception", exception.getMessage());
        //you can use this String to know what caused the exception and in which Activity
        /*intent.putExtra("uncaughtException",
                "Exception is: " + stackTrace.toString());
        intent.putExtra("stacktrace", s);
        myContext.startActivity(intent);*/
        Intent intent = myContext.getPackageManager().getLaunchIntentForPackage("com.pos.cumulus");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        myContext.startActivity(intent);

        //for restarting the Activity
        Process.killProcess(Process.myPid());
        System.exit(0);
    }
}
