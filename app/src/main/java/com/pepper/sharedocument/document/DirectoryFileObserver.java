package com.pepper.sharedocument.document;

import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;

/**
 * Created by prasanth.mathavan on 15,April,2020
 */
public class DirectoryFileObserver extends FileObserver {

    private String rootPath = "";
    private final String TAG = DirectoryFileObserver.class.getSimpleName();
    private Handler handler = new Handler();
    private DocumentReceivedListener mDocumentReceivedListener;
    private File file;
    private static final int mask = (FileObserver.CREATE |
            FileObserver.DELETE |
            FileObserver.DELETE_SELF |
            FileObserver.MODIFY |
            FileObserver.MOVED_FROM |
            FileObserver.MOVED_TO |
            FileObserver.MOVE_SELF);

    DirectoryFileObserver(String path, DocumentReceivedListener documentReceivedListener) {
        super(path, mask);
        rootPath = path;
        mDocumentReceivedListener = documentReceivedListener;
        Log.d(TAG, "DirectoryFileObserver: " + path);
    }

    private int i = 0;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (i == 2)
                mDocumentReceivedListener.onReceived(file);
        }
    };

    @Override
    public void onEvent(int event, @Nullable String path) {
        Log.d(TAG, "Path : " + path);
        file = new File(rootPath + File.separator + path);
        switch (event) {
            case FileObserver.CREATE:
                i = 1;
                Log.d(TAG, "CREATE:" + file.getAbsolutePath());
                handler.postDelayed(runnable, 1000);
                break;
            case FileObserver.DELETE:
                Log.d(TAG, "DELETE:" + file.getAbsolutePath());
                break;
            case FileObserver.DELETE_SELF:
                Log.d(TAG, "DELETE_SELF:" + file.getAbsolutePath());
                break;
            case FileObserver.MODIFY:
                if (i == 1) {
                    mDocumentReceivedListener.onReceiving(file);
                }
                handler.removeCallbacksAndMessages(null);
                Log.d("handler", "removeCallbacksAndMessages");
                handler.postDelayed(runnable, 1 * 1000);
                Log.d("handler", "postDelayed to 5 secs");
                i = 2;
                Log.d(TAG, "MODIFY:" + file.getAbsolutePath());
                break;
            case FileObserver.MOVED_FROM:
                Log.d(TAG, "MOVED_FROM:" + file.getAbsolutePath());
                break;
            case FileObserver.MOVED_TO:
                Log.d(TAG, "MOVED_TO:" + file.getAbsolutePath());
                break;
            case FileObserver.MOVE_SELF:
                Log.d(TAG, "MOVE_SELF:" + file.getAbsolutePath());
                break;
            default:
                // just ignore
                break;
        }
    }
}
