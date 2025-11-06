package com.amastsales.bluebird_rfid_scanner;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amastsales.bluebird_rfid_scanner.control.ListItem;
import com.amastsales.bluebird_rfid_scanner.control.ListNoView;
import com.amastsales.bluebird_rfid_scanner.control.TagListAdapter;
import com.amastsales.bluebird_rfid_scanner.stopwatch.StopwatchService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;
import co.kr.bluebird.sled.SelectionCriterias;
import io.flutter.embedding.engine.plugins.FlutterPlugin;

public class BluebirdRfidScannerHelper {
    // -- -- DEBUG
    private static final String TAG = BluebirdRfidScannerHelper.class.getSimpleName();
    private static final boolean D = Constants.HELPER_D;


    // -- -- VAR
    int mEncodingMode = 0;
    int mRssiLimitVal = -100;
    private int mLocateValue;
    private int ret = -100;
    private int mSoundId;
    private int mSuccessCnt = 0;
    private double mOldTotalCount = 0;
    private double mOldSec = 0;
    private float mSoundVolume;
    private boolean mSoundPlay = true;
    private boolean mSoundFileLoadState;
    private boolean mLocate;
    private boolean mIgnorePC = true;
    private boolean mMask = false;
    private boolean mIsTurbo = true;
    private boolean mTagFilter = true;
    private boolean mInventory = false;
    private boolean mTriggerActivity = false;
    String mTagStrSKU;
    private String mLocateEPC;
    private String retString = null;
    private String mMessage;
    private String mCountText;
    private String mSpeedCountText;
    private String mAvrSpeedCountTest;
    private String mTimerText;
    private String mSuccess;
    private String mFail;
    private String mConnectState;
    private String modelIDStr;
    private CopyOnWriteArrayList<Constants.EPCItem> mInditexTagINFOList;
    private CopyOnWriteArrayList<String> mInditexTagList;
    private TagListAdapter mAdapter;
    private ListNoView mRfidList;
    private TimerTask mLocateTimerTask;
    private Timer mClearLocateTimer;
    private StopwatchService mStopwatchSvc;
    private SoundPool mSoundPool;
    private Reader mReader;
    private Context mContext;
    private FlutterPlugin.FlutterPluginBinding mBinding;
    private Activity mActivity;
    private Handler mOptionHandler;
    private SoundTask mSoundTask;
    private final ConnectivityHandler mConnectivityHandler = new ConnectivityHandler();



    // -- -- CONSTRUCTORS
    private static BluebirdRfidScannerHelper instance;
    public BluebirdRfidScannerHelper(Context context, FlutterPlugin.FlutterPluginBinding binding) {
        mContext = context;
        mBinding = binding;
    }
    public BluebirdRfidScannerHelper(Context context, Activity activity) {
        mContext = context;
        mActivity = activity;
    }

    public static BluebirdRfidScannerHelper getInstance(Context context, FlutterPlugin.FlutterPluginBinding binding) {
        Log.d(TAG, "getInstance");
        if (instance == null) {
            instance = new BluebirdRfidScannerHelper(context, binding);
        }
        return instance;
    }
    public static BluebirdRfidScannerHelper getInstance(Context context, Activity activity) {
        Log.d(TAG, "getInstance");
        if (instance == null) {
            instance = new BluebirdRfidScannerHelper(context, activity);
        } else {
            instance.mActivity = activity;
        }
        return instance;
    }


    // -- -- FUNC
    // -- -- -- PUBLIC (PLUGIN FUNCTIONS)
    public void initReader() {
        if (D) Log.d(TAG, "initReader");

        boolean openResult = false;
        boolean isConnected = false;

        mReader = Reader.getReader(mContext, mConnectivityHandler);
        if (mReader != null)
            openResult = mReader.SD_Open();
        if (openResult == SDConsts.SD_OPEN_SUCCESS) {
            Log.i(TAG, "Reader opened");
            modelIDStr = (mReader.SD_GetModel() == SDConsts.MODEL.RFR900) ? "RFR900" : "RFR901";
            if (mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED)
                isConnected = true;
        } else if (openResult == SDConsts.RF_OPEN_FAIL)
            if (D) Log.e(TAG, "Reader open failed");

        mAdapter = new TagListAdapter(mContext);
        mRfidList = new ListNoView(mContext);

        updateConnectState(isConnected);

        createSoundPool();
    }

    public String getConnectState() {
        fGetConnectState();

        return mConnectState;

    }
    public boolean connect() {
        fConnect();

        boolean connected = false;

        for (int i = 0; i < 10; i++) {
            fGetConnectState();

            if (Objects.equals(mConnectState, "Connected"))
                connected = true;
            if (Objects.equals(mConnectState, "Disconnected"))
                connected = false;

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                if (D) Log.e(TAG, "connect() interrupted");
            }

        }


        return connected;

    }

    public boolean disconnect() {
        ret = mReader.SD_Disconnect();
        if (D) Log.d(TAG, "disconnect result = " + ret);
        if (ret == SDConsts.SDConnectState.DISCONNECTED || ret == SDConsts.SDConnectState.ALREADY_DISCONNECTED ||
                ret == SDConsts.SDConnectState.ACCESS_TIMEOUT) {
            updateConnectState(false);
        }

        return Objects.equals(getConnectState(), "Disconnected");
    }

    public void clearActivity() {
        if (D) Log.d(TAG, "clearActivity");

        mActivity = null;
    }
    
    public boolean getTriggerActivity() {
        if (D) Log.d(TAG, "getTriggerActivity");
        if (D) Log.d("getTriggerActivity", String.valueOf(mTriggerActivity));

        return mTriggerActivity;
    }

    public List<ListItem> performInventory() {
        if (D) Log.d(TAG, "performInventory");

        CopyOnWriteArrayList<ListItem> currentListItem = (CopyOnWriteArrayList<ListItem>) mAdapter.getItemList();

        return currentListItem;

    }

    public void clearInventory() {
        if (D) Log.d(TAG, "clearInventory");

        mAdapter.removeAllItem();
    }



    // -- -- -- PRIVATE
    private static class ConnectivityHandler extends Handler {
        public ConnectivityHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (instance != null) {
                instance.handleMessage(msg);
            }
        }
    }

    private void handleMessage(Message m) {
        if (D) Log.d(TAG, "mDefaultHandler");
        if (D) Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");

        switch (m.what) {
            case SDConsts.Msg.SDMsg:
                if (m.arg1 == SDConsts.SDCmdMsg.SLED_WAKEUP) {
                    mMessage = " " + "SLED_WAKEUP " + m.arg2;
                    // closeDialog();

                    if (m.arg2 == SDConsts.SDResult.SUCCESS) {
                        int ret = mReader.SD_Connect();
                        mMessage = " " + "SD_Connect " + ret;

                        if (ret == SDConsts.SDResult.SUCCESS || ret == SDConsts.SDResult.ALREADY_CONNECTED) {
                            updateConnectState(true);
                        }
                    }
                    else {
                        if (D) Log.d(TAG, "Wakeup failed!");
                        // Toast.makeText(mContext, "Wakeup failed!", Toast.LENGTH_SHORT).show();
                    }
                }
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                    mMessage = " " + "SLED_UNKNOWN_DISCONNECTED";
                    updateConnectState(false);
                }
                //+Always be display Battery
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED) {

                    // // //+smart batter -critical temper
                    if(m.arg2 == SDConsts.SDCommonResult.SMARTBATT_CRITICAL_TEMPERATURE) {
                        if (D) Log.d(TAG, "The battery temperature is 58 degrees or higher.");
                        // Utils.createAlertDialog(mContext, getString(R.string.smart_critical_temper_str));
                    }
                    // // //smart batter -critical temper+

                    Activity activity = mActivity;
                    if (activity != null) {
                        if (mOptionHandler != null) {
                            Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
                            mOptionHandler.obtainMessage(BluebirdRfidScannerPlugin.MSG_BATT_NOTI, m.arg1, m.arg2).sendToTarget();
                        }
                    }
                }
                
                //Always be display Battery+
                //+Hotswap feature
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_HOTSWAP_STATE_CHANGED) {
                    if (m.arg2 == SDConsts.SDHotswapState.HOTSWAP_STATE) {
                        if (D) Log.d(TAG, "HOTSWAP STATE CHANGED = HOTSWAP_STATE");
                        // Toast.makeText(mContext, "HOTSWAP STATE CHANGED = HOTSWAP_STATE", Toast.LENGTH_SHORT).show();
                        
                    } else if (m.arg2 == SDConsts.SDHotswapState.NORMAL_STATE) {
                        if (D) Log.d(TAG, "HOTSWAP STATE CHANGED = NORMAL_STATE");
                        // Toast.makeText(mContext, "HOTSWAP STATE CHANGED = NORMAL_STATE", Toast.LENGTH_SHORT).show();
                    }
                    // FragmentTransaction ft = getFragmentManager().beginTransaction();
                    // ft.detach(mFragment).attach(mFragment).commit();
                }
                //Hotswap feature+

                // -- inventory
                //
                switch (m.arg1) {
                    //+Hotswap feature
                    // case SDConsts.SDCmdMsg.SLED_HOTSWAP_STATE_CHANGED:
                    //     if (m.arg2 == SDConsts.SDHotswapState.HOTSWAP_STATE)
                    //         Toast.makeText(mContext, "HOTSWAP STATE CHANGED = HOTSWAP_STATE", Toast.LENGTH_SHORT).show();
                    //     else if (m.arg2 == SDConsts.SDHotswapState.NORMAL_STATE)
                    //         Toast.makeText(mContext, "HOTSWAP STATE CHANGED = NORMAL_STATE", Toast.LENGTH_SHORT).show();
                    //     FragmentTransaction ft = getFragmentManager().beginTransaction();
                    //     ft.detach(mFragment).attach(mFragment).commit();
                    //     break;
                    //Hotswap feature+

                    case SDConsts.SDCmdMsg.TRIGGER_PRESSED:
                        mTriggerActivity = true;
                        startInventory(Constants.InventoryType.NORAML);
                        break;

                    case SDConsts.SDCmdMsg.SLED_INVENTORY_STATE_CHANGED:
                        mInventory = false;
                        // enableControl(!mInventory);
                        pauseStopwatch();
                        // In case of low battery on inventory, reason value is LOW_BATTERY
                        Toast.makeText(mContext, "Inventory Stopped reason : " + m.arg2, Toast.LENGTH_SHORT).show();

                        mAdapter.addItem(-1, "Inventory Stopped reason : " + m.arg2, Integer.toString(m.arg2), !mIgnorePC, mTagFilter);
                        break;

                    case SDConsts.SDCmdMsg.TRIGGER_RELEASED:
                        mTriggerActivity = false;
                        if (mReader.RF_StopInventory() == SDConsts.SDResult.SUCCESS) {
                            mInventory = false;
                            // saveFile();//20231011 change save routine
                            // enableControl(!mInventory);
                        }
                        pauseStopwatch();
                        break;

                    case SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED:
                        //This message contain DETACHED event.
                        if (mInventory) {
                            pauseStopwatch();
                            mInventory = false;
                        }
                        // enableControl(false);
                        if (mOptionHandler != null)
                            mOptionHandler.obtainMessage(BluebirdRfidScannerPlugin.MSG_OPTION_DISCONNECTED).sendToTarget();
                        break;

                    case SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED:
                        //Toast.makeText(mContext, "Battery state = " + m.arg2, Toast.LENGTH_SHORT).show();
                        if (D) Log.d(TAG, "Battery state = " + m.arg2);
                        // mBatteryText.setText("" + m.arg2 + "%");

                        //+smart batter -critical temper
                        if(m.arg2 == SDConsts.SDCommonResult.SMARTBATT_CRITICAL_TEMPERATURE) {
                            if (mInventory) {
                                pauseStopwatch();
                                mInventory = false;
                                // enableControl(!mInventory);
                            }
                            // Utils.createAlertDialog(mContext, getString(R.string.smart_critical_temper_str));
                        }
                        //smart batter -critical temper+

                        //+Always be display Battery
                        Activity activity = mActivity;
                        if (activity != null) {
                            if (mOptionHandler != null) {
                                Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
                                mOptionHandler.obtainMessage(BluebirdRfidScannerPlugin.MSG_BATT_NOTI, m.arg1, m.arg2).sendToTarget();
                            }
                        }
                        //Always be display Battery+
                        break;
                }

                break;

            case SDConsts.Msg.RFMsg:
                switch (m.arg1) {
                    //+RF_PerformInventoryCustom
                    // case SDConsts.RFCmdMsg.INVENTORY_CUSTOM_READ:
                    //     if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                    //         String data = (String) m.obj;
                    //         if (data != null)
                    //             processReadDataCustom(data);
                    //     }
                    //     break;
                    //RF_PerformInventoryCustom+
                    case SDConsts.RFCmdMsg.INVENTORY:
                    case SDConsts.RFCmdMsg.READ:
                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                            if (m.obj != null && m.obj instanceof String) {
                                String data = (String) m.obj;
                                if (data != null)
                                    processReadData(data);
                            }
                        }
                        break;
                    ////<-[20250402]Add Bulk encoding
                    //case SDConsts.RFCmdMsg.WRITE_BULK_ENCODING_INVENTORY:
                    //    if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                    //        if (m.obj != null && m.obj instanceof String) {
                    //            String data = (String) m.obj;
                    //            if (data != null){
                    //                String[] i = data.split(";");
                    //                String targetPcEPC = i[0];
                    //                String last8EPC = targetPcEPC.substring(targetPcEPC.length() - 8, targetPcEPC.length());
                    //                String targetEPC = targetPcEPC.substring(4);
                    //                if(mInditexTagList.contains(last8EPC)){//check inditex tag
                    //                    processEncodingReadData(data);
                    //                    int idx = mInditexTagList.indexOf(last8EPC);//To find matching EPC's index
                    //                    EPCItem eItem = mInditexTagINFOList.get(idx);//To find matching access pw
                    //                    String epcToWrite = generateNewEpc(targetEPC);
                    //
                    //                    CopyOnWriteArrayList<String> addedTagList = (CopyOnWriteArrayList<String>) mAdapter.getTagList();
                    //                    int seqID = addedTagList.indexOf(targetPcEPC);
                    //
                    //                    Log.d(TAG, "WRITE_BULK_ENCODING_INVENTORY::SUCCESS::seqID = " + seqID + " ,targetEPC = " + targetEPC + " ,last8EPC = " + last8EPC);
                    //                    Log.d(TAG, "WRITE_BULK_ENCODING_INVENTORY::SUCCESS::idx(INDITEX List) = " + idx +" ,newAccessPW = " + eItem.mAccessPW + " ,newEPC = " + epcToWrite);
                    //
                    //                    switch (mEncodingMode){
                    //                        case Constants.EncodeMode.MASS:
                    //                            mReader.RF_SetEncodeInformation(seqID, (targetEPC.length()/2), targetEPC,
                    //                                    eItem.mAccessPW, SDConsts.RFMemType.EPC, 2, (epcToWrite.length()/4), epcToWrite);
                    //                            break;
                    //                        case Constants.EncodeMode.PRIVATE:
                    //                            mReader.RF_SetEncodeInformation(seqID, (targetEPC.length()/2), targetEPC,
                    //                                    eItem.mAccessPW, SDConsts.RFMemType.RESERVED, 2, 3, (Constants.BB_PW + Constants.PRIVATE_SUFFIX_PW));
                    //                            break;
                    //                        case Constants.EncodeMode.REVIVE:
                    //                            mReader.RF_SetEncodeInformation(seqID, (targetEPC.length()/2), targetEPC,
                    //                                    Constants.BB_PW, SDConsts.RFMemType.RESERVED, 2, 3, (eItem.mAccessPW + Constants.REVIVE_SUFFIX_PW));
                    //                            break;
                    //                    }
                    //                }
                    //            }
                    //        }
                    //    }
                    //    break;
                    case SDConsts.RFCmdMsg.WRITE_BULK_ENCODING_REPORT:
                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                            try{
                                if (m.obj != null && m.obj instanceof String) {
                                    String strID = (String) m.obj;
                                    int seqID = Integer.parseInt(strID);
                                    CopyOnWriteArrayList<String> addedTagList = (CopyOnWriteArrayList<String>) mAdapter.getTagList();
                                    String targetPcEPC = addedTagList.get(seqID);
                                    String targetEPC = targetPcEPC.substring(4);

                                    String last8EPC = targetPcEPC.substring(targetPcEPC.length() - 8, targetPcEPC.length());
                                    int idx = mInditexTagList.indexOf(last8EPC);//To find matching EPC's index
                                    Constants.EPCItem eItem = mInditexTagINFOList.get(idx);//To find matching access pw
                                    String epcToWrite = generateNewEpc(targetEPC);

                                    Log.d(TAG, "WRITE_BULK_ENCODING_REPORT::SUCCESS::seqID = " + seqID + " ,targetEPC(seqID) = " + targetEPC + " ,last8EPC = " + last8EPC);
                                    Log.d(TAG, "WRITE_BULK_ENCODING_REPORT::SUCCESS::idx(INDITEX List) = " + idx +" ,newAccessPW = " + eItem.mAccessPW + " ,newEPC = " + epcToWrite);

                                    mSuccessCnt++;

                                    if(mSuccessCnt >= mInditexTagList.size()) {
                                        Log.d(TAG, "WRITE_BULK_ENCODING_REPORT::SUCCESS::All counted,Stop Encoding");
                                        stopEncodingInventory();
                                    }
                                    updateSuccessText();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        } else {
                            try{
                                int errorCode = m.arg2;
                                if (m.obj != null && m.obj instanceof String) {
                                    String strID = (String) m.obj;
                                    int seqID = Integer.parseInt(strID);
                                    CopyOnWriteArrayList<String> addedTagList = (CopyOnWriteArrayList<String>) mAdapter.getTagList();
                                    String targetPcEPC = addedTagList.get(seqID);
                                    String targetEPC = targetPcEPC.substring(4);

                                    String last8EPC = targetPcEPC.substring(targetPcEPC.length() - 8, targetPcEPC.length());
                                    int idx = mInditexTagList.indexOf(last8EPC);//To find matching EPC's index
                                    Constants.EPCItem eItem = mInditexTagINFOList.get(idx);//To find matching access pw
                                    String epcToWrite = generateNewEpc(targetEPC);

                                    Log.d(TAG, "WRITE_BULK_ENCODING_REPORT::FAIL::errorCode = " + errorCode + " ,seqID = " + seqID + " ,targetEPC(seqID) = " + targetEPC + " ,last8EPC = " + last8EPC);
                                    Log.d(TAG, "WRITE_BULK_ENCODING_REPORT::FAIL::idx(INDITEX List idx) = " + idx +" ,newAccessPW = " + eItem.mAccessPW + " ,newEPC = " + epcToWrite);

                                    switch (mEncodingMode){
                                        case Constants.EncodeMode.MASS:
                                            mReader.RF_SetEncodeInformation(seqID, (targetEPC.length()/2), targetEPC,
                                                    eItem.mAccessPW, SDConsts.RFMemType.EPC, 2, (epcToWrite.length()/4), epcToWrite);
                                            break;
                                        case Constants.EncodeMode.PRIVATE:
                                            mReader.RF_SetEncodeInformation(seqID, (targetEPC.length()/2), targetEPC,
                                                    eItem.mAccessPW, SDConsts.RFMemType.RESERVED, 2, 3, (Constants.BB_PW + Constants.PRIVATE_SUFFIX_PW));
                                            break;
                                        case Constants.EncodeMode.REVIVE:
                                            mReader.RF_SetEncodeInformation(seqID, (targetEPC.length()/2), targetEPC,
                                                    eItem.mAccessPW, SDConsts.RFMemType.RESERVED, 2, 3, (Constants.BB_PW + Constants.REVIVE_SUFFIX_PW));
                                            break;
                                    }
                                }
                            }catch (Exception e1){
                                e1.printStackTrace();
                            }
                        }
                        break;
                    //[20250402]Add Bulk encoding->

                    case SDConsts.RFCmdMsg.LOCATE:
                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                            if (m.obj != null && m.obj instanceof Integer) {
                                processLocateData((int) m.obj);
                            }
                        }
                        break;
                }
                break;
        }
    }

    private void processLocateData(int data) {
        startLocateTimer();
        mLocateValue = data;
        //mTagLocateProgress.setProgress(data);
        if (mSoundTask == null) {
            mSoundTask = new SoundTask();
            mSoundTask.execute();
        } else {
            if (mSoundTask.getStatus() == AsyncTask.Status.FINISHED) {
                mSoundTask.cancel(true);
                mSoundTask = null;
                mSoundTask = new SoundTask();
                mSoundTask.execute();
            }
        }
    }

    private String generateNewEpc(String oldEpc)
    {
        String newEpc = "";
        int position = 8;
        int length = 4;
        newEpc = oldEpc.substring(0, position) + mTagStrSKU + oldEpc.substring(position + length);
        return newEpc;
    }

    private void startLocateTimer() {
        stopLocateTimer();

        mLocateTimerTask = new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                locateTimeout();
            }
        };
        mClearLocateTimer = new Timer();
        mClearLocateTimer.schedule(mLocateTimerTask, 500);
    }

    private void stopLocateTimer() {
        if (mClearLocateTimer != null) {
            mClearLocateTimer.cancel();
            mClearLocateTimer = null;
        }
    }

    private void locateTimeout() {
        // mTagLocateProgress.setProgress(0);
    }

    private void processReadData(String data) {
        //updateCountText();
        StringBuilder tagSb = new StringBuilder();
        tagSb.setLength(0);
        String info = "";
        String epcDecode = "";
        String pha = "";
        String freq = "";

        String originalData = data;
        if (originalData.contains(";")) {
            if (D) Log.d(TAG, "full tag = " + data);
            //full tag example(with optional value)
            //1) RF_PerformInventory => "3000123456783333444455556666;rssi:-54.8"
            //2) RF_PerformInventoryWithLocating => "3000123456783333444455556666;loc:64"
            //3) RF_PerformInventoryWithEPCDecoder => "3000123456783333444455556666;rssi:-54.2;ed:(01)02432042280962(21) 3735552"
//            int infoTagPoint = data.indexOf(';');
//            info = data.substring(infoTagPoint, data.length());
//            int infoPoint = info.indexOf(':') + 1;
//            info = info.substring(infoPoint, info.length());
//            if (D) Log.d(TAG, "info tag = " + info);
//            data = data.substring(0, infoTagPoint);
//            if (D) Log.d(TAG, "data tag = " + data);
            data = "";
            String[] splitData = originalData.split(";");
            Activity activity = mActivity;
            String prefix = "";

            for (String dt : splitData) {
                if(dt.startsWith("rssi:")) {
                    int type = -1;
                    String[] splitInfo = dt.split(":");
                    for(String str : splitInfo) {
                        if(str.equals("rssi")) {
                            type = 0;
                        } else if(str.equals("pha")) {
                            type = 1;
                        } else if(str.equals("freq")) {
                            type = 2;
                        } else if(type != -1) {
                            switch (type) {
                                case 0: {
                                    if (activity != null)
                                        prefix = ""; //""RSSI :";
                                    info = prefix + str.replace("rssi:", "");
                                    break;
                                }
                                case 1: {
                                    pha = "PHA : " + str;
                                    break;
                                }
                                case 2: {
                                    freq = "Freq : " + str;
                                    break;
                                }
                            }
                        }
                    }
                    if (D) Log.d(TAG, "rssi tag = " + info);
                } /*else if (dt.startsWith("pha:")) {
                    if (activity != null)
                        prefix = activity.getString(R.string.rssi_str);
                    pha = prefix + dt.replace("pha:", "");
                    if (D) Log.d(TAG, "rssi tag = " + info);
                } */else if (dt.startsWith("loc:")) {
                    if (activity != null)
                        prefix = "LOC :";
                    info = prefix + dt.replace("loc:", "");
                    if (D) Log.d(TAG, "loc tag = " + info);
                } else if (dt.startsWith("epcdc:")) {
                    if (activity != null)
                        prefix = "EPC :";
                    epcDecode = prefix + dt.replace("epcdc:", "");
                    if (D) Log.d(TAG, "epc_decode tag = " + info);
                } else if (TextUtils.isEmpty(data)) {
                    data = dt;
                    if (D) Log.d(TAG, "data tag = " + data);
                }
            }
        }
        //addItem(int img, String upText, String downText, String pha, String frequency, String epcDecode, boolean hasPC, boolean filter) {
        mAdapter.addItem(-1, data, info, pha, freq, epcDecode, !mIgnorePC, mTagFilter);
        // if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        //     if (mFileManager != null && mFile)
        //         mFileManager.writeToFile(data);
        // } else {
        //     if (mUriWrite != null && mFile)
        //         mUriWrite.writeToFile(data);
        // }

        mRfidList.setSelection(mRfidList.getAdapter().getCount() - 1);
        if (!mInventory) {
            updateCountText();
            updateSpeedCountText();
            updateAvrSpeedCountText();
        }

        if (mSoundTask == null) {
            mSoundTask = new SoundTask();
            mSoundTask.execute();
        } else {
            if (mSoundTask.getStatus() == AsyncTask.Status.FINISHED) {
                mSoundTask.cancel(true);
                mSoundTask = null;
                mSoundTask = new SoundTask();
                mSoundTask.execute();
            }
        }
    }

    private void processEncodingReadData(String data) {
        StringBuilder tagSb = new StringBuilder();
        tagSb.setLength(0);
        String info = "";
        String epcDecode = "";
        String pha = "";
        String freq = "";
        String antId = "";
        String originalData = data;
        if (originalData.contains(";")) {
            if (D) Log.d(TAG, "full tag = " + data);
            data = "";
            String[] splitData = originalData.split(";");
            Activity activity = mActivity;
            String prefix = "";
            for (String dt : splitData) {
                if(dt.startsWith("rssi:")) {
                    int type = -1;
                    String[] splitInfo = dt.split(":");
                    for(String str : splitInfo) {
                        if(str.equals("rssi")) type = 0;
                        else if(str.equals("pha")) type = 1;
                        else if(str.equals("freq")) type = 2;
                        else if(str.equals("antid"))type = 3;
                        else if(type != -1) {
                            switch (type) {
                                case 0:
                                    if (activity != null)
                                        prefix = "RSSI :";
                                    info = prefix + str.replace("rssi:", "");
                                    break;
                                case 1: pha = "PHA : " + str;
                                    break;
                                case 2: freq = "Freq : " + str;
                                    break;
                                case 3: antId = "AntId : " + str;
                                    break;
                            }
                        }
                    }
                    if (D) Log.d(TAG, "rssi tag = " + info);
                } else if (dt.startsWith("loc:")) {
                    if (activity != null)
                        prefix = "LOC :";
                    info = prefix + dt.replace("loc:", "");
                    if (D) Log.d(TAG, "loc tag = " + info);
                } else if (dt.startsWith("epcdc:")) {
                    if (activity != null)
                        prefix = "EPC :";
                    epcDecode = prefix + dt.replace("epcdc:", "");
                    if (D) Log.d(TAG, "epc_decode tag = " + info);
                } else if (TextUtils.isEmpty(data)) {
                    data = dt;
                    if (D) Log.d(TAG, "data tag = " + data);
                }
            }
        }

        mAdapter.addItem(-1, data, info, pha, freq, epcDecode, 0, mStopwatchSvc.getFormattedElapsedTime(), !mIgnorePC, mTagFilter);
        mRfidList.setSelection(mRfidList.getAdapter().getCount() - 1);
        if (!mInventory) {
            updateCountText();
            updateSpeedCountText();
            updateAvrSpeedCountText();
        }

        if (mSoundTask == null) {
            mSoundTask = new SoundTask();
            mSoundTask.execute();
        } else {
            if (mSoundTask.getStatus() == AsyncTask.Status.FINISHED) {
                mSoundTask.cancel(true);
                mSoundTask = null;
                mSoundTask = new SoundTask();
                mSoundTask.execute();
            }
        }
    }

    private void startInventory(int type) {
        if (!mInventory) {
            clearAll();
//            openFile();//20231011 change save routine

            int ret = 0;
            if (mLocate) {
                //+force optimize config for find tag - RF Mode:1,Session:S0,Toggole:On,Singulation:5
//                mReader.RF_SetRFMode(1);
//                mReader.RF_SetSession(SDConsts.RFSession.SESSION_S0);
//                mReader.RF_SetToggle(SDConsts.RFToggle.ON);
//                mReader.RF_SetSingulationControl(5, SDConsts.RFSingulation.MIN_SINGULATION, SDConsts.RFSingulation.MAX_SINGULATION);
                //force optimize config for find tag - RF Mode:1,Session:S0,Toggole:On,Singulation:5+
                ret = mReader.RF_PerformInventoryForLocating(mLocateEPC);
            } else {
                //+force optimize config for unique tag - RF Mode:1,Session:S1,Toggole:Off,Singulation:10
//                mReader.RF_SetRFMode(1);
//                mReader.RF_SetSession(SDConsts.RFSession.SESSION_S1);
//                mReader.RF_SetToggle(SDConsts.RFToggle.OFF);
//                mReader.RF_SetSingulationControl(10, SDConsts.RFSingulation.MIN_SINGULATION, SDConsts.RFSingulation.MAX_SINGULATION);
                //force optimize config for unique tag - RF Mode:1,Session:S1,Toggole:Off,Singulation:10+
//                ret = mReader.RF_PerformInventory(mIsTurbo, mMask, mIgnorePC, false);
                //<-[20250424]Add other inventory api for test
                switch (type){
                    case Constants.InventoryType.NORAML:
                        ret = mReader.RF_PerformInventory(mIsTurbo, mMask, mIgnorePC);
                        break;
                    case Constants.InventoryType.RSSI_TO_LOCATE:
                        ret = mReader.RF_PerformInventoryWithLocating(mIsTurbo, mMask, mIgnorePC);
                        break;
                    case Constants.InventoryType.CUSTOM:
                        mReader.RF_PerformInventoryCustom(SelectionCriterias.SCMemType.TID, 0, 2, "00000000", mMask);
                        break;
                    case Constants.InventoryType.RSSI_LIMIT:
                        ret = mReader.RF_PerformInventoryWithRssiLimitation(mIsTurbo, mMask, mIgnorePC, mRssiLimitVal);
                        break;
                    case Constants.InventoryType.WITH_PAHSE_FREQ:
                        ret = mReader.RF_PerformInventoryWithPhaseFreq(mIsTurbo, mMask, mIgnorePC);
                        break;
                }
                //[20250424]Add other inventory api for test->
//                ret = mReader.RF_PerformInventory(mIsTurbo, mMask, mIgnorePC);
                //+additional inventory feature
                //Check the API below to use inventory with limitation.
//                ret = mReader.RF_PerformInventoryWithRssiLimitation(mIsTurbo, mMask, mIgnorePC, mRssiLimitVal);

                //Check the API below to use inventory with information other than EPC
                //ret = mReader.RF_PerformInventoryCustom(SDConsts.RFMemType.TID, 0 ,2, "00000000", mMask);

                //Check the API below to use inventory with locating
                //ret = mReader.RF_PerformInventoryWithLocating(mIsTurbo, mMask, mIgnorePC);

                //Check the API below to use inventory with phase freq
//                ret = mReader.RF_PerformInventoryWithPhaseFreq(mIsTurbo, mMask, mIgnorePC);

                //Check the API below to use inventory custom
                //ret = mReader.RF_PerformInventoryCustom(SelectionCriterias.SCMemType.TID, 0, 4, "00000000",mMask);
                //additional inventory feature+
            }
            if (ret == SDConsts.RFResult.SUCCESS) {
                startStopwatch();
                mInventory = true;
                // enableControl(!mInventory);
            } else if (ret == SDConsts.RFResult.MODE_ERROR)
                Toast.makeText(mContext, "Start Inventory failed, Please check RFR MODE", Toast.LENGTH_SHORT).show();
            else if (ret == SDConsts.RFResult.LOW_BATTERY)
                Toast.makeText(mContext, "Start Inventory failed, LOW_BATTERY", Toast.LENGTH_SHORT).show();
            else if (D) Log.d(TAG, "Start Inventory failed");
        }
    }

    private void stopEncodingInventory(){
        mReader.RF_RemoveSelection();
        int ret = mReader.RF_StopInventoryEncoding();
        if (ret == SDConsts.RFResult.SUCCESS || ret == SDConsts.RFResult.NOT_INVENTORY_STATE) {
            mInventory = false;
            // saveFile();//20231011 change save routine
            // enableControl(!mInventory);
            pauseStopwatch();
        } else if (ret == SDConsts.RFResult.STOP_FAILED_TRY_AGAIN)
            Toast.makeText(mContext, "Stop Inventory failed", Toast.LENGTH_SHORT).show();

    }

    private void clearAll() {
        if (!mInventory) {
            if (mAdapter != null)
                mAdapter.removeAllItem();

            updateCountText();

            stopStopwatch();

            mOldTotalCount = 0;

            mOldSec = 0;

            //<-[20250402]Add Bulk encoding
            mSuccessCnt = 0;
            updateSuccessText();
            //[20250402]Add Bulk encoding->

            updateSpeedCountText();

            updateAvrSpeedCountText();

            Activity activity = mActivity;
            if (activity != null)
                mSpeedCountText = "0" + "cnt/sec";
        }
    }

    private void updateConnectState(boolean b) {
        if (b) {
            mConnectState = "Connected";
            if (mOptionHandler != null)
                mOptionHandler.obtainMessage(BluebirdRfidScannerPlugin.MSG_OPTION_CONNECTED).sendToTarget();
        }
        else {
            mConnectState = "Disconnected";
            if (mOptionHandler != null)
                mOptionHandler.obtainMessage(BluebirdRfidScannerPlugin.MSG_OPTION_DISCONNECTED).sendToTarget();
        }
    }

    private void updateSuccessText() {
        Log.d(TAG, "updateSuccessText");

        String text = Integer.toString(mSuccessCnt);
        mSuccess = text;

        if (mAdapter != null)
            text = Integer.toString(  mAdapter.getCount() - mSuccessCnt);

        mFail = text;
    }

    private void updateCountText() {
        if (D) Log.d(TAG, "updateCountText");
        String text = "0";

        if (mAdapter != null)
         text = Integer.toString(mAdapter.getCount());

        mCountText = text;
    }

    private void startStopwatch() {
        if (D) Log.d(TAG, "startStopwatch");

        if (mStopwatchSvc != null && !mStopwatchSvc.isRunning())
            mStopwatchSvc.start();

        // mProgressBar.setVisibility(View.VISIBLE);
    }

    private void pauseStopwatch() {
        if (D) Log.d(TAG, "pauseStopwatch");

        if (mStopwatchSvc != null && mStopwatchSvc.isRunning())
            mStopwatchSvc.pause();

        updateCountText();

        updateTimerText();

        updateSpeedCountText();

        updateAvrSpeedCountText();

        // mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void stopStopwatch() {
        if (D) Log.d(TAG, "stopStopwatch");

        if (mStopwatchSvc != null && mStopwatchSvc.isRunning())
            mStopwatchSvc.pause();

        if (mStopwatchSvc != null)
            mStopwatchSvc.reset();

        updateTimerText();

        updateAvrSpeedCountText();

        // mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void updateTimerText() {
        if (D) Log.d(TAG, "updateTimerText");
        if (mStopwatchSvc != null)
            mTimerText = (mStopwatchSvc.getFormattedElapsedTime());
    }

    private void updateSpeedCountText() {
        if (D) Log.d(TAG, "updateSpeedCountText");
        String speedStr = "";
        double value = 0;
        double totalCount = 0;
        double sec = 0;
        if (mStopwatchSvc != null) {
            sec = ((double) ((int) (mStopwatchSvc.getElapsedTime() / 100))) / 10;

            if (!mTagFilter)
                totalCount = mAdapter.getTotalCount();
            else {
                totalCount = mAdapter.getTotalCount();
                for (int i = 0; i < mAdapter.getCount(); i++)
                    totalCount += mAdapter.getItemDupCount(i);
            }
            if (totalCount > 0 && sec - mOldSec >= 1) {
                value = (double) ((int) (((totalCount - mOldTotalCount) / (sec - mOldSec)) * 10)) / 10;

                mOldTotalCount = totalCount;

                mOldSec = sec;
                Activity activity = mActivity;
                if (activity != null)
                    speedStr = Double.toString(value) + "cnt/sec";
                mSpeedCountText = speedStr;
            }
        }
    }

    private void updateAvrSpeedCountText() {
        if (D) Log.d(TAG, "updateAvrSpeedCountText");
        String speedStr = "";
        double value = 0;
        int totalCount = 0;
        double sec = 0;
        if (mStopwatchSvc != null) {
            sec = ((double) ((int) (mStopwatchSvc.getElapsedTime() / 100))) / 10;

            if (!mTagFilter)
                totalCount = mAdapter.getTotalCount();
            else {
                totalCount = mAdapter.getTotalCount();
                for (int i = 0; i < mAdapter.getCount(); i++)
                    totalCount += mAdapter.getItemDupCount(i);
            }
            if (totalCount > 0 && sec >= 1)
                value = (double) ((int) (((double) totalCount / sec) * 10)) / 10;

            Activity activity = mActivity;
            if (activity != null)
                speedStr = Double.toString(value) + "cnt/sec";
            mAvrSpeedCountTest = speedStr;
        }
    }

    private void createSoundPool() {
        boolean b = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            b = createNewSoundPool();
        else
            b = createOldSoundPool();
        if (b) {
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            float actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mSoundVolume = actVolume / maxVolume;
            SoundLoadListener();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean createNewSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        mSoundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(5).build();
        if (mSoundPool != null)
            return true;
        return false;
    }

    @SuppressWarnings("deprecation")
    private boolean createOldSoundPool() {
        mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        if (mSoundPool != null)
            return true;
        return false;
    }

    private void SoundLoadListener() {
        if (mSoundPool != null) {
            mSoundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
                mSoundFileLoadState = true;
            });
            mSoundId = loadBeepFromFlutterAssets("assets/raw/beep.mp3");
        }
    }

    private int loadBeepFromFlutterAssets(String assetPath) {
        try {
            String lookupKey = mBinding
                .getFlutterAssets()
                .getAssetFilePathByName(assetPath);

            AssetFileDescriptor afd = mContext.getAssets().openFd(lookupKey);
            return mSoundPool.load(afd, 1);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void fConnect() {
        retString = "SD_Wakeup ";
        ret = mReader.SD_Wakeup();

        if (ret == SDConsts.SDResult.SUCCESS) {
            Activity activity = mActivity;
            if (activity != null) {
                if (D) Log.d(TAG, "Connecting... ");
                // createDialog(LOADING_DIALOG, activity.getString(R.string.connecting_str));
            }
        }

        if (D) Log.d(TAG, "wakeup result = " + ret);
    }

    private void fGetConnectState() {
        retString = "SD_GetConnectState ";
        ret = mReader.SD_GetConnectState();

        if (ret == SDConsts.SDConnectState.CONNECTED) {
            if (D) Log.d(TAG, "connected");
            updateConnectState(true);
        }
        else if (ret == SDConsts.SDConnectState.DISCONNECTED) {
            if (D) Log.d(TAG, "disconnected");
            updateConnectState(false);
        }
        else {
            if (D) Log.d(TAG, "other state");
            updateConnectState(false);
        }

        if (D) Log.d(TAG, "connect state = " + ret);
    }

    // -- PRIVATE CLASS
    //

    private class SoundTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            //if (mLocate)
            //    mTagLocateProgress.setProgress(mLocateValue);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            if (mSoundPlay) {
                try {
                    if (mSoundFileLoadState) {
                        if (mSoundPool != null) {
                            mSoundPool.play(mSoundId, mSoundVolume, mSoundVolume, 0, 0, (48000.0f / 44100.0f));
                            try {
                                Thread.sleep(25);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (java.lang.NullPointerException e) {
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}
