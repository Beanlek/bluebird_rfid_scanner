import 'package:flutter_test/flutter_test.dart';
import 'package:bluebird_rfid_scanner/bluebird_rfid_scanner.dart';
import 'package:bluebird_rfid_scanner/bluebird_rfid_scanner_platform_interface.dart';
import 'package:bluebird_rfid_scanner/bluebird_rfid_scanner_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockBluebirdRfidScannerPlatform
    with MockPlatformInterfaceMixin
    implements BluebirdRfidScannerPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final BluebirdRfidScannerPlatform initialPlatform = BluebirdRfidScannerPlatform.instance;

  test('$MethodChannelBluebirdRfidScanner is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelBluebirdRfidScanner>());
  });

  test('getPlatformVersion', () async {
    BluebirdRfidScanner bluebirdRfidScannerPlugin = BluebirdRfidScanner();
    MockBluebirdRfidScannerPlatform fakePlatform = MockBluebirdRfidScannerPlatform();
    BluebirdRfidScannerPlatform.instance = fakePlatform;

    expect(await bluebirdRfidScannerPlugin.getPlatformVersion(), '42');
  });
}
