import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:bluebird_rfid_scanner/bluebird_rfid_scanner.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _bluebirdRfidScannerPlugin = BluebirdRfidScanner();
  bool _loading = false;

  String _connectStatus = 'Unknown';
  bool _connected = false;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      await _bluebirdRfidScannerPlugin.initReader();
      
      platformVersion = await _bluebirdRfidScannerPlugin.getPlatformVersion() ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Stack(
          children: [
            Column(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Column(
                    children: [
                      // -- current connection state
                      Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Row(
                          children: [
                            const Text('Current Connect State: '),
                            Icon(Icons.circle, color: _connected ? Colors.green : Colors.grey),
                            Text(_connectStatus),
                          ],
                        ),
                      ),
                  
                      // -- row of buttons
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceAround,
                        children: [
                          Container(
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(12),
                              color: Colors.amber
                            ),
                            child: TextButton(
                              style: TextButton.styleFrom(visualDensity: VisualDensity.compact, padding: const EdgeInsets.all(0)),
                              onPressed: () async {
                                debugPrint('connect');
                  
                                setState(() => _loading = true);
                                
                                await _bluebirdRfidScannerPlugin.connect().then((res) => setState(() {
                                  _connected = res ?? false;
                                }));
                                await _bluebirdRfidScannerPlugin.getConnectState().then((res) => setState(() {
                                  _connectStatus = res ?? 'Null';
                                }));
                  
                                setState(() => _loading = false);
                              },
                              child: const Text('connect')
                            ),
                          ),
                          Container(
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(12),
                              color: Colors.amber
                            ),
                            child: TextButton(
                              style: TextButton.styleFrom(visualDensity: VisualDensity.compact, padding: const EdgeInsets.all(0)),
                              onPressed: () async {
                                debugPrint('disconnect');
                  
                                setState(() => _loading = true);
                                
                                await _bluebirdRfidScannerPlugin.disconnect().then((res) => setState(() {
                                  _connected = res is bool && res ? false : true;
                                }));
                                await _bluebirdRfidScannerPlugin.getConnectState().then((res) => setState(() {
                                  _connectStatus = res ?? 'Null';
                                }));
                  
                                setState(() => _loading = false);
                              },
                              child: const Text('disconnect')
                            ),
                          ),
                          Container(
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(12),
                              color: Colors.amber
                            ),
                            child: TextButton(
                              style: TextButton.styleFrom(visualDensity: VisualDensity.compact, padding: const EdgeInsets.all(0)),
                              onPressed: () async {
                                debugPrint('getConnectState');
                                
                                setState(() => _loading = true);
                  
                                await _bluebirdRfidScannerPlugin.getConnectState().then((res) => setState(() {
                                  _connectStatus = res ?? 'Null';
                                }));
                  
                                setState(() => _loading = false);
                              },
                              child: const Text('getConnectState')
                            ),
                          ),
                          Container(
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(12),
                              color: Colors.red
                            ),
                            child: TextButton(
                              style: TextButton.styleFrom(visualDensity: VisualDensity.compact, padding: const EdgeInsets.all(0)),
                              onPressed: () async {
                                debugPrint('clearInventory');
                                
                                setState(() => _loading = true);
                  
                                await _bluebirdRfidScannerPlugin.clearInventory();
                  
                                setState(() => _loading = false);
                              },
                              child: const Text('clearInventory')
                            ),
                          ),
                  
                        ],
                      )
                    ],
                  ),
                ),

                // -- scan rfid(s)
                Expanded(
                  flex: 2,
                  child: StreamBuilder<List<Map<Object?, Object?>>?>(
                    stream: _bluebirdRfidScannerPlugin.performInventory().map((event) {
                      debugPrint('Received event: $event');
                      return event;
                    }),
                    builder: (context, snapshot) {
                      
                      // Connection states
                      if (snapshot.connectionState == ConnectionState.waiting) {
                        return const Center(child: CircularProgressIndicator());
                      }

                      // Error handling
                      if (snapshot.hasError) {
                        return Center(
                          child: Text(
                            'Error: ${snapshot.error}',
                            style: const TextStyle(color: Colors.red),
                          ),
                        );
                      }

                      // Data handling
                      final tags = snapshot.data ?? [];

                      if (tags.isEmpty) {
                        return const Center(child: Text('No RFID tags detected yet.'));
                      }

                      // Display live list of RFID tags
                      return ListView.builder(
                        itemCount: tags.length,
                        itemBuilder: (context, index) {
                          final tag = tags[index];
                          return ListTile(
                            leading: const Icon(Icons.nfc),
                            title: Text(tag['epc']?.toString() ?? 'Unknown Tag'),
                            subtitle: Text(tag['info']?.toString() ?? ''),
                            trailing: Text('#${index + 1}'),
                          );
                        },
                      );
                    }
                  )
                ),
                
                
                Expanded(
                  child: Center(
                    child: Text('Running on: $_platformVersion\n'),
                  ),
                ),

              ],
            ),

            // // loading screen
            if(_loading)
              Container(
                color: Colors.white.withOpacity(.5),
                child: Center(
                  child: CircularProgressIndicator(
                    backgroundColor: Colors.black.withOpacity(.1),
                  ),
                ),
              )
          ],
        ),
      ),
    );
  }
}
