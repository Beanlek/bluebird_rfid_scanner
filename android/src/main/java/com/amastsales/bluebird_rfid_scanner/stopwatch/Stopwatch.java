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

import android.util.Log;

import java.util.ArrayList;

public class Stopwatch {
    
    private static final String TAG = Stopwatch.class.getSimpleName();
    
    private static final boolean D = false;
    
    private GetTime mTime;
    
    private long mStartTime;
    
    private long mStopTime;
    
    private long mPauseOffset;
    
    private ArrayList<Long> mLaps = new ArrayList<>();
        
    public enum State { 
        PAUSED, 
        RUNNING
    };
    
    private State mState;

    
    public interface GetTime {
        public long now();
    }
    
    private GetTime SystemTime = new GetTime() {
        @Override
        public long now() { 
            if (D) Log.d(TAG, "now");
            return System.currentTimeMillis(); 
        }
    };   
    
    public Stopwatch() {
        if (D) Log.d(TAG, "Stopwatch");
        mTime = SystemTime;
        reset();
    }
    
    public Stopwatch(GetTime time) {
        if (D) Log.d(TAG, "Stopwatch arg");
        mTime = time;
        reset();
    }
    
    public void start() {
        if (D) Log.d(TAG, "start");
        if ( mState == State.PAUSED ) {
            mPauseOffset = getElapsedTime();
            mStopTime = 0;
            mStartTime = mTime.now();
            mState = State.RUNNING;
        }
    }

    public void pause() {
        if (D) Log.d(TAG, "pause");
        if ( mState == State.RUNNING ) {
            mStopTime = mTime.now();
            mState = State.PAUSED;
        }
    }

    public void reset() {
        if (D) Log.d(TAG, "reset");
        mState = State.PAUSED;
        mStartTime = 0;
        mStopTime = 0;
        mPauseOffset = 0;
        mLaps.clear();
    }
    
    public void lap() {
        if (D) Log.d(TAG, "lap");
        mLaps.add(getElapsedTime());
    }
    
    public long getElapsedTime() {
        if (D) Log.d(TAG, "getElapsedTime");
        if ( mState == State.PAUSED ) {
            return (mStopTime - mStartTime) + mPauseOffset;
        } else {
            return (mTime.now() - mStartTime) + mPauseOffset;
        }
    }
    
    public ArrayList<Long> getLaps() {
        if (D) Log.d(TAG, "getLaps");
        return mLaps;
    }
    
    public boolean isRunning() {
        if (D) Log.d(TAG, "isRunning");
        return (mState == State.RUNNING);
    }

}
