import 'dart:async';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:docscan/docscan.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String? _imagePath;

  @override
  void initState() {
    super.initState();
  }

  Future<void> getImageFromCamera() async {
    if (Platform.isAndroid) {
      PermissionStatus status = await Permission.camera.request();
      if (status.isPermanentlyDenied) {
        openAppSettings();
      }
      // print('status: $status');
      bool isCameraGranted = await Permission.camera.request().isGranted;
      print('isCameraGranted: $isCameraGranted');
      if (!isCameraGranted) {
        isCameraGranted =
            await Permission.camera.request() == PermissionStatus.granted;
      }
      if (!isCameraGranted) {
        // Have not permission to camera
        return;
      }
    }
    try {
      List<String>? imgPaths = await DocScan.scanDocument();
      print('success: $imgPaths');
      if (!mounted) return;
      if (imgPaths == null || imgPaths.isEmpty) {
        return;
      }
      setState(() {
        _imagePath = imgPaths[0];
      });
    } catch (e) {
      print(e);
    }
  }

  Future<void> getImageFromGallery() async {
    String imagePath = join((await getApplicationSupportDirectory()).path,
        "${(DateTime.now().millisecondsSinceEpoch / 1000).round()}.jpeg");

    try {
      bool success = await DocScan.detectEdgeFromGallery(
        imagePath,
        androidCropTitle: 'Crop',
        androidCropBlackWhiteTitle: 'Black White',
        androidCropReset: 'Reset',
      );
      print("success: $success");
    } catch (e) {
      print(e);
    }
    if (!mounted) return;

    setState(() {
      _imagePath = imagePath;
    });
  }
  // ok

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          centerTitle: true,
          backgroundColor: Colors.amberAccent,
          title: const Text(
            'DocScan',
            style: TextStyle(color: Colors.black),
          ),
        ),
        body: SingleChildScrollView(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Center(
                    child: ElevatedButton(
                      onPressed: getImageFromCamera,
                      child: Text('Scan Document'),
                      style: ElevatedButton.styleFrom(
                        foregroundColor: Colors.black,
                        backgroundColor: Colors.amberAccent,
                      ),
                    ),
                  ),
                  // SizedBox(width: 20),
                  // Center(
                  //   child: ElevatedButton(
                  //     onPressed: getImageFromGallery,
                  //     child: Text('Gallery'),
                  //     style: ElevatedButton.styleFrom(
                  //       foregroundColor: Colors.black,
                  //       backgroundColor: Colors.amberAccent,
                  //     ),
                  //   ),
                  // ),
                ],
              ),
              SizedBox(height: 20),
              Text('Cropped image path:'),
              Padding(
                padding: const EdgeInsets.only(top: 0, left: 0, right: 0),
                child: Text(
                  _imagePath.toString(),
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 14),
                ),
              ),
              Padding(
                padding:
                    const EdgeInsets.symmetric(horizontal: 8.0, vertical: 0.0),
                child: Card(
                  color: Colors.blueGrey,
                  elevation: 2,
                  child: Visibility(
                    visible: _imagePath != null,
                    child: Padding(
                      padding: const EdgeInsets.all(8.0),
                      child: Image.file(
                        File(_imagePath ?? ''),
                      ),
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
