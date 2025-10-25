import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'bluebird_rfid_scanner_platform_interface.dart';

/// An implementation of [BluebirdRfidScannerPlatform] that uses method channels.
class MethodChannelBluebirdRfidScanner extends BluebirdRfidScannerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('bluebird_rfid_scanner');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  // init scanner service
  @override
  Future<String?> initReader() async {
    final result = await methodChannel.invokeMethod<String>('initReader');
    return result;
  }

  // connect to device
  @override
  Future<bool?> connect() async {
    final result = await methodChannel.invokeMethod<bool>('connect');
    return result;
  }

  // disconnect to device
  @override
  Future<bool?> disconnect() async {
    final result = await methodChannel.invokeMethod<bool>('disconnect');
    return result;
  }

  // get device connection status
  @override
  Future<String?> getConnectState() async {
    final result = await methodChannel.invokeMethod<String>('getConnectState');
    return result;
  }


  // scan rfids
  static const ePerformInventory = EventChannel('performInventory');
  @override
  Stream<List<Map<Object?, Object?>>?> performInventory() {
    debugPrint('Flutter: performInventory');
    return ePerformInventory.receiveBroadcastStream().map((event) => List.from(event));
  }

  // clear rfids
  @override
  Future<String?> clearInventory() async {
    final result = await methodChannel.invokeMethod<String>('clearInventory');
    return result;
  }

  // set scanner mode

  // set power level

  // set scanner name
}
