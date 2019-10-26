//Author: Shaobo Wang
//shaobow@student.unimelb.edu.au

package com.example.comp90025.util;

import android.util.Log;

public class Logger {

    private static final int LogLevel = 0;
    private static final String TAG = "debug";
    private static final int LogD = 1;
    private static final int LogE = 2;
    private static final int LogW = 3;
    private static final int LogI = 4;

    public enum LogLevel {
        E
    }

    // for finding bug and error
    public static void e(String msg) {
        e(null,msg);
    }
    public static void e(String Tag, String msg){
        if (LogLevel <= LogE) {
            if (Tag == null) {
                Log.e(TAG, msg);
            } else {
                Log.e(Tag, msg);
            }
        }
    }
}


