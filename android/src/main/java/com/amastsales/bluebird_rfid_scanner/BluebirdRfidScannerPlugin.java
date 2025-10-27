package com.amastsales.bluebird_rfid_scanner;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import android.content.Context;
import android.util.Log;

import com.amastsales.bluebird_rfid_scanner.control.ListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** BluebirdRfidScannerPlugin */
public class BluebirdRfidScannerPlugin implements FlutterPlugin, MethodCallHandler,  ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity

  private static final String TAG = BluebirdRfidScannerPlugin.class.getSimpleName();

  private static final boolean D = Constants.MAIN_D;

  public static final int MSG_OPTION_CONNECT_STATE_CHANGED = 0;

  public static final int MSG_OPTION_DISCONNECTED = 0;

  public static final int MSG_OPTION_CONNECTED = 1;

  public static final int MSG_BACK_PRESSED = 2;

  public static final int MSG_BATT_NOTI = 3; // Always be display Battery

  private MethodChannel channel;
  private BluebirdRfidScannerHelper helper;
  private Context context;
  private FlutterPluginBinding binding;
  private Activity activity;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "bluebird_rfid_scanner");
    context = flutterPluginBinding.getApplicationContext();
    binding = flutterPluginBinding;
    channel.setMethodCallHandler(this);

    helper = BluebirdRfidScannerHelper.getInstance(context, binding);

    EventChannel ePerformInventory = new EventChannel(binding.getBinaryMessenger(), EVENT_PerformInventory);
    ePerformInventory.setStreamHandler(new EventChannelHandler(this, EVENT_PerformInventory));

  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();

    helper = BluebirdRfidScannerHelper.getInstance(context, activity);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    activity = null;
    if (helper != null) {
      helper.clearActivity();
    }
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    onAttachedToActivity(binding);
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
    if (helper != null) {
      helper.clearActivity();
    }
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    handleMethods(call, result);
  }

  private void handleMethods(@NonNull MethodCall call, @NonNull Result result) {

    switch (call.method) {
      case CHANNEL_GetPlatformVersion:
        result.success("Android " + android.os.Build.VERSION.RELEASE);
        break;

      case CHANNEL_InitReader:
        if (D) Log.d(TAG, CHANNEL_InitReader);

        try {
          helper.initReader();

          result.success("Reader initialized");

        } catch (Exception e) {
          if (D) Log.d(TAG, CHANNEL_InitReader);
          result.error(TAG, CHANNEL_InitReader, e.toString());

        }

        break;

      case CHANNEL_Connect:
        if (D) Log.d(TAG, CHANNEL_Connect);

        try {
          Thread connectThread = new Thread(() -> {
            final boolean res = helper.connect();

            activity.runOnUiThread(() -> result.success(res));

          });

          connectThread.start();

        } catch (Exception e) {
          if (D) Log.d(TAG, CHANNEL_Connect);
          result.error(TAG, CHANNEL_Connect, e.toString());

        }

        break;

      case CHANNEL_Disconnect:
        if (D) Log.d(TAG, CHANNEL_Disconnect);

        try {
          Thread disconnectThread = new Thread(() -> {
            final boolean res = helper.disconnect();

            activity.runOnUiThread(() -> result.success(res));

          });

          disconnectThread.start();

        } catch (Exception e) {
          if (D) Log.d(TAG, CHANNEL_Disconnect);
          result.error(TAG, CHANNEL_Disconnect, e.toString());

        }

        break;
      case CHANNEL_GetConnectState:
        if (D) Log.d(TAG, CHANNEL_GetConnectState);

        try {
          final String res = helper.getConnectState();

          result.success(res);

        } catch (Exception e) {
          if (D) Log.d(TAG, CHANNEL_GetConnectState);
          result.error(TAG, CHANNEL_GetConnectState, e.toString());

        }

        break;

      case CHANNEL_ClearInventory:
        if (D) Log.d(TAG, CHANNEL_ClearInventory);

        try {
          helper.clearInventory();

          result.success("ListItem cleared");

        } catch (Exception e) {
          if (D) Log.d(TAG, CHANNEL_ClearInventory);
          result.error(TAG, CHANNEL_ClearInventory, e.toString());

        }

        break;

      default:
        result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  // -- normal channels
  private static final String CHANNEL_GetPlatformVersion = "getPlatformVersion";
  private static final String CHANNEL_InitReader = "initReader";
  private static final String CHANNEL_Connect = "connect";
  private static final String CHANNEL_Disconnect = "disconnect";
  private static final String CHANNEL_GetConnectState = "getConnectState";
  private static final String CHANNEL_ClearInventory = "clearInventory";


  // -- event channels
  private static final String EVENT_PerformInventory = "performInventory";


  // -- handlers
  private class EventChannelHandler implements EventChannel.StreamHandler{

    private EventChannel.EventSink eSink = null;
    private BluebirdRfidScannerPlugin eContext;
    private String eChannel;
    private Boolean eTriggerActivity;
    private volatile Boolean isListening = false;


    public EventChannelHandler(BluebirdRfidScannerPlugin context, String channel) {
      if (eContext == null)
        eContext = context;

      eChannel = channel;
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
      eSink = events;
      isListening = true;

      new Thread(() -> {
        while(isListening) {
          switch (eChannel) {
            case EVENT_PerformInventory:
              if (D) Log.d(TAG, EVENT_PerformInventory);
              List<Map<String, Object>> list = new ArrayList<>();

              try {
                List<ListItem> mItemList = helper.performInventory();

                // Log.d(eChannel, String.valueOf(mItemList.size()));

                for (ListItem item : mItemList) {
                  // Log.d(eChannel, item.mUt);
                  Map<String, Object> map = new HashMap<>();
                  map.put("epc", item.mUt); // epc
                  map.put("info", item.mDt); // rssi
                  map.put("dupCount", item.mDupCount);
                  list.add(map);
                }

                if (activity != null && eSink != null) {
                  activity.runOnUiThread(() -> {
                    eSink.success(list);
                  });
                }

                Thread.sleep(1000);

              } catch (Exception e) {
                Log.e(eChannel, "", e);
                eSink.error(eChannel, e.getMessage(), null);
              }

              break;
          }
        }


      }).start();

    }

    @Override
    public void onCancel(Object arguments) {
      isListening = false;
      eSink = null;
    }
  }
}
