//Author: Shaobo Wang
//shaobow@student.unimelb.edu.au

package com.example.comp90025.util;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SDcard {

    public static boolean isExist() {
        return Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment.getExternalStorageState());
    }

    public static String saveToSDCard(Context st) {
        if (isExist()) {
            File sdFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            return sdFile.getAbsolutePath();
        }

        String sdPath = null;

        File sdFile;

        String[] paths = List(st);

        for (String path : paths) {
            File file = new File(path);

            if (file.isDirectory() && file.canWrite()) {
                sdPath = file.getAbsolutePath();
                String time = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                File timeTable = new File(sdPath, "test_" + time);
                if (timeTable.mkdirs()) {
                    timeTable.delete();
                } else {
                    sdPath = null;
                }
            }
        }

        if (sdPath != null) {
            sdFile = new File(sdPath);
            return sdFile.getAbsolutePath();
        }

        return null;
    }

    public static String[] List(Context st) {
        StorageManager storage = (StorageManager) st.getSystemService(Activity.STORAGE_SERVICE);
        try {

            Method getPath = storage.getClass().getMethod("getVolumePaths");
            String[] filePath = null;
            filePath = (String[]) getPath.invoke(storage);

            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}

