package com.hh.myocr;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;


public class AutoFocusManager implements Camera.AutoFocusCallback{
    private static final String TAG = AutoFocusManager.class.getSimpleName();
    private static final long AUTO_FOCUS_INTERVAL_MS = 1000L;
    private static final Collection<String> FOCUS_MODES_CALLING_AF;
    static {
        FOCUS_MODES_CALLING_AF = new ArrayList<String>(2);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
    }

    private boolean stopped;
    private boolean focusing;
    private final boolean useAutoFocus;
    private final Camera camera;
    private AsyncTask<?,?,?> outstandingTask;
    private Camera.PreviewCallback mp;
    public AutoFocusManager(Camera camera, Camera.PreviewCallback mp) {
        this.camera = camera;
        String currentFocusMode = camera.getParameters().getFocusMode();
        useAutoFocus = FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
        Log.e(TAG, "Current focus mode '" + currentFocusMode + "'; use auto focus? " + useAutoFocus);
        this.mp = mp;
        start();
    }

    @Override
    public synchronized void onAutoFocus(final boolean success, Camera theCamera) {
//        if(success){
//            camera.takePicture(null, null, myjpegCallback);
//        }

        ;
        focusing = false;
        autoFocusAgainLater();
    }


    private synchronized void autoFocusAgainLater() {
        if (!stopped && outstandingTask == null) {
            AutoFocusTask newTask = new AutoFocusTask();
            try {
                newTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                outstandingTask = newTask;
            } catch (RejectedExecutionException ree) {
                Log.e(TAG, "Could not request auto focus", ree);
            }
        }
    }

    /**
     * 开始自动对焦
     */
    public synchronized void start() {
//        camera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
//            @Override
//            public void onPreviewFrame(byte[] data, Camera camera)
//        });
        if (useAutoFocus) {
            outstandingTask = null;
            if (!stopped && !focusing) {
                try {
                    camera.setOneShotPreviewCallback(mp);
                    camera.autoFocus(this);
                    focusing = true;
                } catch (RuntimeException re) {
                    // Have heard RuntimeException reported in Android 4.0.x+; continue?
                    Log.e(TAG, "Unexpected exception while focusing", re);
                    // Try again later to keep cycle going
                    autoFocusAgainLater();
                }
            }
        }
    }


    private synchronized void cancelOutstandingTask() {
        if (outstandingTask != null) {
            if (outstandingTask.getStatus() != AsyncTask.Status.FINISHED) {
                outstandingTask.cancel(true);
            }
            outstandingTask = null;
        }
    }

    /**
     * 停止自动对焦
     */
    public synchronized void stop() {
        stopped = true;
        if (useAutoFocus) {
            cancelOutstandingTask();
            // Doesn't hurt to call this even if not focusing
            try {
                camera.cancelAutoFocus();
            } catch (RuntimeException re) {
                // Have heard RuntimeException reported in Android 4.0.x+; continue?
                Log.e(TAG, "Unexpected exception while cancelling focusing", re);
            }
        }
    }


    private final class AutoFocusTask extends AsyncTask<Object, Object, Object> {
        @Override
        protected Object doInBackground(Object... voids) {
            try {
                Thread.sleep(AUTO_FOCUS_INTERVAL_MS);
            } catch (InterruptedException e) {
                // continue
            }
            start();
            return null;
        }
    }

}
