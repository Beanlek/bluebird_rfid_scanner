
package com.amastsales.bluebird_rfid_scanner.control;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amastsales.bluebird_rfid_scanner.Constants;

import java.util.concurrent.CopyOnWriteArrayList;

public class TagListAdapter {

    private static final String TAG = TagListAdapter.class.getSimpleName();
    
    private static final boolean D = Constants.TAG_LIST_ADAPTER_D;
    
    private static final int MAX_LIST_COUNT = 50000;
    
    private int mListCycleCount = 0;
       
    private CopyOnWriteArrayList<ListItem> mItemList;
    
    private CopyOnWriteArrayList<String> mTagList;
    
    private Context mContext;

    public TagListAdapter(Context ctx) {
        super();
        if (D) Log.d(TAG, "TagListAdapter");
        mContext = ctx;

        mItemList = new CopyOnWriteArrayList<>();
        mTagList = new CopyOnWriteArrayList<>();
    }


    public int getCount() {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, "getCount");
        return mItemList.size();
    }

    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, "getItem");
        return mItemList.get(arg0);
    }

    public int getItemDupCount(int arg0) {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, "getItemDupCount");
        return mItemList.get(arg0).mDupCount;
    }

    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, "getItemId");
        return arg0;
    }
	
    //<-[20250402]Add Bulk encoding
    public Object getTagList() {
        if (D) Log.d(TAG, "getTagList");
        return mTagList;
    }

    public Object getItemList() {
        if (D) Log.d(TAG, "getItemList");
        return mItemList;
    }
    
    public void addItem(int img, String upText, String downText, String pha, String frequency, String epcDecode, int isRead, String time,
                        boolean hasPC, boolean filter) {
        if (D) Log.d(TAG, "addItem " + filter);

        if (filter) {
            if (mTagList.contains(upText)) {
                if (D) Log.d(TAG, "count++ " + true);

                int idx = mTagList.indexOf(upText);
                mItemList.get(idx).mDupCount = (mItemList.get(idx).mDupCount) + 1;

                return;
            }

            if (mItemList.size() == MAX_LIST_COUNT) {
                mTagList.clear();
                mItemList.clear();

                mListCycleCount++;
            }

            ListItem item = new ListItem();
            item.mIv = img;
            item.mUt = upText;
            item.mDt = downText;
            item.mHasPc = hasPC;
            item.mDupCount = 1;
            item.mPha = pha;
            item.mFrequency = frequency;
            item.mEpcDecode = epcDecode;
            mTagList.add(upText);
            mItemList.add(item);

        } else {
            if (mItemList.size() == MAX_LIST_COUNT) {
                mTagList.clear();
                mItemList.clear();

                mListCycleCount++;
            }
            ListItem item = new ListItem();
            item.mIv = img;
            item.mUt = upText;
            item.mDt = downText;
            item.mHasPc = hasPC;
            item.mDupCount = 1;
            item.mPha = pha;
            item.mFrequency = frequency;
            item.mEpcDecode = epcDecode;
            mItemList.add(item);

        }
    }
    //[20250402]Add Bulk encoding->

    public void addItem(int img, String upText, String downText, boolean hasPC, boolean filter) {
        addItem(img, upText, downText, "", "", "", hasPC, filter);
    }

    public void addItem(int img, String upText, String downText, String pha, String frequency, String epcDecode, boolean hasPC, boolean filter) {
        if (D) Log.d(TAG, "addItem " + filter);

        if (filter) {
            if (mTagList.contains(upText)) {
                if (D) Log.d(TAG, "count++ " + filter);

                int idx = mTagList.indexOf(upText);
                mItemList.get(idx).mDupCount = (mItemList.get(idx).mDupCount) + 1;

                return;
            }
            if (mItemList.size() == MAX_LIST_COUNT) {
                mTagList.clear();
                mItemList.clear();

                mListCycleCount++;
            }
            ListItem item = new ListItem();
            item.mIv = img;
            item.mUt = upText; // + Long.toString(mItemList.size() + 1);
            item.mDt = downText;
            item.mHasPc = hasPC;
            item.mDupCount = 1;
            item.mPha = pha;
            item.mFrequency = frequency;
            item.mEpcDecode = epcDecode;
            mTagList.add(upText);
            mItemList.add(item);

        } else {
            if (mItemList.size() == MAX_LIST_COUNT) {
                mTagList.clear();
                mItemList.clear();

                mListCycleCount++;
            }
            ListItem item = new ListItem();
            item.mIv = img;
            item.mUt = upText; // + Long.toString(mItemList.size() + 1);
            item.mDt = downText;
            item.mHasPc = hasPC;
            item.mDupCount = 1;
            item.mPha = pha;
            item.mFrequency = frequency;
            item.mEpcDecode = epcDecode;
            mItemList.add(item);

        }
    }

    public void removeAllItem() {
        if (D) Log.d(TAG, "removeAllItem");

        if (mItemList != null)
            mItemList.clear();
        if (mTagList != null)
            mTagList.clear();

        mListCycleCount = 0;

    }
    
    public int getTotalCount() {
        if (D) Log.d(TAG, "getTotalCount");
        return (mListCycleCount * MAX_LIST_COUNT) + mItemList.size();
    }
}