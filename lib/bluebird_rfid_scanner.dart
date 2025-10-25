
import 'bluebird_rfid_scanner_platform_interface.dart';

class BluebirdRfidScanner {
  Future<String?> getPlatformVersion() {
    return BluebirdRfidScannerPlatform.instance.getPlatformVersion();
  }

  // init scanner service
  Future<String?> initReader() {
    return BluebirdRfidScannerPlatform.instance.initReader();
  }

  // connect to device
  Future<bool?> connect() {
    return BluebirdRfidScannerPlatform.instance.connect();
  }

  // disconnect to device
  Future<bool?> disconnect() {
    return BluebirdRfidScannerPlatform.instance.disconnect();
  }

  // get device connection status
  Future<String?> getConnectState() {
    return BluebirdRfidScannerPlatform.instance.getConnectState();
  }


  // scan rfids
  Stream<List<Map<Object?, Object?>>?> performInventory() {
    return BluebirdRfidScannerPlatform.instance.performInventory();
  }

  // clear rfids
  Future<String?> clearInventory() {
    return BluebirdRfidScannerPlatform.instance.clearInventory();
  }

  // set scanner mode

  // set power level

  // set scanner name
}
