package com.pos.passport.util;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;


public class DbExportImport
{
    Context mcontext;

    public static final String TAG = DbExportImport.class.getName();

    public static final String PACKAGE_NAME = "com.pos.cumulus";
    public static final String DATABASE_NAME = "passport.db";


/*    private static final File DATA_DIRECTORY_DATABASE =
            new File(Environment.getDataDirectory() +
                    "/data/" + PACKAGE_NAME +
                    "/databases/" + DATABASE_NAME );*/

    public DbExportImport(Context mcontext) {
        this.mcontext = mcontext;
        File direct = new File(Environment.getExternalStorageDirectory() + "/Exam Creator");

        if(!direct.exists())
        {
            if(direct.mkdir())
            {
                //directory is created;
            }

        }
    }

    public void importDB() {
        // TODO Auto-generated method stub

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data  = Environment.getDataDirectory();

            if (sd.canWrite())
            {
                String  currentDBPath= "//data//" + PACKAGE_NAME
                        + "//databases//" + DATABASE_NAME;
                String backupDBPath  = "/BackupFolder/"+DATABASE_NAME;
                File  backupDB= new File(data, currentDBPath);
                File currentDB  = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(mcontext, backupDB.toString(),
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(mcontext, e.toString(), Toast.LENGTH_LONG)
                    .show();
        }
    }
    //exporting database
    public void exportDB() {
        // TODO Auto-generated method stub

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String  currentDBPath= "//data//" + PACKAGE_NAME
                        + "//databases//" + DATABASE_NAME;
                String backupDBPath  = "/BackupFolder/"+DATABASE_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(mcontext, backupDB.toString(),
                        Toast.LENGTH_LONG).show();

            }
        } catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(mcontext, e.toString(), Toast.LENGTH_LONG)
                    .show();

        }
    }
}