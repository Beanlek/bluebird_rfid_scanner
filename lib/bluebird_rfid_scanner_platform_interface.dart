import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'bluebird_rfid_scanner_method_channel.dart';

abstract class BluebirdRfidScannerPlatform extends PlatformInterface {
  /// Constructs a BluebirdRfidScannerPlatform.
  BluebirdRfidScannerPlatform() : super(token: _token);

  static final Object _token = Object();

  static BluebirdRfidScannerPlatform _instance = MethodChannelBluebirdRfidScanner();

  /// The default instance of [BluebirdRfidScannerPlatform] to use.
  ///
  /// Defaults to [MethodChannelBluebirdRfidScanner].
  static BluebirdRfidScannerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [BluebirdRfidScannerPlatform] when
  /// they register themselves.
  static set instance(BluebirdRfidScannerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  // init scanner service
  Future<String?> initReader() {
    throw UnimplementedError('initReader() has not been implemented.');
  }

  // connect to device
  Future<bool?> connect() {
    throw UnimplementedError('connect() has not been implemented.');
  }

  // disconnect to device
  Future<bool?> disconnect() {
    throw UnimplementedError('disconnect() has not been implemented.');
  }

  // get device connection status
  Future<String?> getConnectState() {
    throw UnimplementedError('getConnectState() has not been implemented.');
  }


  // scan rfids
  Stream<List<Map<Object?, Object?>>?> performInventory() {
    throw UnimplementedError('performInventory() has not been implemented.');
  }

  // clear rfids
  Future<String?> clearInventory() {
    throw UnimplementedError('clearInventory() has not been implemented.');
  }

  // set scanner mode

  // set power level

  // set scanner name
}
