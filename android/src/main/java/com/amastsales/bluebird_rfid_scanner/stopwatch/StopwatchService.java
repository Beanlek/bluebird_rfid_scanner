/*
 * Copyright (C) 2015 - 2017 Bluebird Inc, All rights reserved.
 * 
 * http://www.bluebirdcorp.com/
 * 
 * Author : Bogon Jun
 * 
 * Date : 8.7.2015
 */

package com.amastsales.bluebird_rfid_scanner.stopwatch;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class StopwatchService extends Service {
    
    private static final String TAG = StopwatchService.class.getSimpleName();
    
    private static final boolean D = false;
    
    public static final int TICK_WHAT = 2;
    
    private final long mFrequency = 100;
    
    private Stopwatch mStopwatch;
    
    private LocalBinder mBinder = new LocalBinder();
    
    private Handler mHandler;
    
    public void setHandler(Handler h) {
        mHandler = h;
    }
    
    public class LocalBinder extends Binder {
        public StopwatchService getService(Handler h) {
            if (D) Log.d(TAG, "getService");
            StopwatchService.this.setHandler(h);
            return StopwatchService.this;
        }
    }
       
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (D) Log.d(TAG, "onCreate");
        mStopwatch = new Stopwatch();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (D) Log.d(TAG, "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }
        
    public void start() {
        if (D) Log.d(TAG, "start");
        mStopwatch.start();
        mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), mFrequency);
    }
    
    public void pause() {
        if (D) Log.d(TAG, "pause");
        mStopwatch.pause();
        mHandler.removeMessages(TICK_WHAT);
    }
    
    public void lap() {
        if (D) Log.d(TAG, "lap");
        mStopwatch.lap();
    }
    
    public void reset() {
        if (D) Log.d(TAG, "reset");
        mStopwatch.reset();
    }
    
    public void update() {
        if (D) Log.d(TAG, "update");
        mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), mFrequency);
    }
    
    public long getElapsedTime() {
        if (D) Log.d(TAG, "getElapsedTime = " + mStopwatch.getElapsedTime());
        return mStopwatch.getElapsedTime();
    }
    
    public String getFormattedElapsedTime() {
        if (D) Log.d(TAG, "getFormattedElapsedTime");
        return formatElapsedTime(getElapsedTime());
    }
    
    public boolean isRunning() {
        if (D) Log.d(TAG, "isRunning");
        return mStopwatch.isRunning();
    }

    private String formatElapsedTime(long now) {
        if (D) Log.d(TAG, "formatElapsedTime");
        long hours = 0, minutes = 0, seconds = 0, tenths = 0;
        StringBuilder sb = new StringBuilder();
        
        if (now < 1000) {
            tenths = now / 100;
        } else if (now < 60000) {
            seconds = now / 1000;
            now -= seconds * 1000;
            tenths = (now / 100);
        } else if (now < 3600000) {
            now -= hours * 3600000;
            minutes = now / 60000;
            now -= minutes * 60000;
            seconds = now / 1000;
            now -= seconds * 1000;
            tenths = (now / 100);
        } else {
            hours = now / 3600000;
            now -= hours * 3600000;
            minutes = now / 60000;
            now -= minutes * 60000;
            seconds = now / 1000;
            now -= seconds * 1000;
            tenths = (now / 100);
        }

        if (hours > 0) {
            sb.append(formatDigits(hours)).append(":")
                .append(formatDigits(minutes)).append(":")
                .append(formatDigits(seconds)).append(".")
                .append(tenths);
        } else {
            sb.append(formatDigits(hours)).append(":")
                .append(formatDigits(minutes)).append(":")
                .append(formatDigits(seconds)).append(".")
                .append(tenths);
        }
        return sb.toString();
    }
    
    private String formatDigits(long num) {
        if (D) Log.d(TAG, "formatDigits");
        return (num < 10) ? "0" + num : new Long(num).toString();
    }
}
