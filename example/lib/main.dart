import 'dart:async';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_gallery_saver/image_gallery_saver.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:docscan/docscan.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:image_picker/image_picker.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String? _imagePath;
  bool _isImageSaved = false; // Track if the image is saved

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
      bool isCameraGranted = await Permission.camera.request().isGranted;
      print('isCameraGranted: $isCameraGranted');
      if (!isCameraGranted) {
        isCameraGranted =
            await Permission.camera.request() == PermissionStatus.granted;
      }
      if (!isCameraGranted) {
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

  Future<void> captureImageFromDeviceCamera() async {
    if (Platform.isAndroid) {
      PermissionStatus status = await Permission.camera.request();
      if (status.isPermanentlyDenied) {
        openAppSettings();
      }
      bool isCameraGranted = await Permission.camera.request().isGranted;
      print('isCameraGranted: $isCameraGranted');
      if (!isCameraGranted) {
        isCameraGranted =
            await Permission.camera.request() == PermissionStatus.granted;
      }
      if (!isCameraGranted) {
        return;
      }
    }

    try {
      final imageFile =
          await ImagePicker().pickImage(source: ImageSource.camera);
      if (imageFile == null) {
        return;
      }
      setState(() {
        _imagePath = imageFile.path;
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

  Future<void> saveImageToGallery() async {
    if (_imagePath == null) {
      return;
    }

    try {
      final result = await ImageGallerySaver.saveFile(_imagePath!);
      if (result['isSuccess']) {
        setState(() {
          _isImageSaved = true;
        });
        Fluttertoast.showToast(msg: 'Image saved to gallery');
      } else {
        Fluttertoast.showToast(msg: 'Failed to save image to gallery');
      }
    } catch (e) {
      print(e);
      Fluttertoast.showToast(msg: 'An error occurred while saving the image');
    }
  }

  void discardImage() {
    setState(() {
      _imagePath = null;

      _isImageSaved = false;
    });
  }

  Future<void> getImageFromDeviceCamera() async {
    String imagePath = join(
      (await getApplicationSupportDirectory()).path,
      "${(DateTime.now().millisecondsSinceEpoch / 1000).round()}.jpeg",
    );

    try {
      bool success = await DocScan.detectEdgeFromDeviceCamera(
        imagePath,
        androidCropTitle: 'Crop',
        androidCropBlackWhiteTitle: 'Black White',
        androidCropReset: 'Reset',
      );

      print("success: $success");

      if (success) {
        setState(() {
          _imagePath = imagePath;
        });
      } else {}
    } catch (e) {
      print(e);
    }
    if (!mounted) return;
  }

  // ok

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(
          centerTitle: true,
          backgroundColor: Colors.amberAccent,
          title: const Text(
            'DocScan New',
            style: TextStyle(color: Colors.black),
          ),
        ),
        body: SingleChildScrollView(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Visibility(
                visible: _imagePath != null,
                child: Column(
                  children: [
                    Text('Cropped image path:'),
                    Padding(
                      padding: const EdgeInsets.only(top: 0, left: 0, right: 0),
                      child: Text(
                        _imagePath.toString(),
                        textAlign: TextAlign.center,
                        style: TextStyle(fontSize: 14),
                      ),
                    ),
                  ],
                ),
              ),
              Card(
                elevation: 2,
                color: Colors.blueGrey,
                shadowColor: Colors.black,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.all(
                    Radius.circular(10),
                  ),
                ),
                child: Visibility(
                  visible: _imagePath != null,
                  child: Stack(
                    children: [
                      Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Image.file(
                          File(_imagePath ?? ''),
                        ),
                      ),
                      Positioned(
                          top: 10,
                          right: 10,
                          child: IconButton(
                            icon: Icon(
                              Icons.close,
                              color: Colors.black,
                            ),
                            onPressed: discardImage,
                          ))
                    ],
                  ),
                ),
              ),
              SizedBox(height: 10),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  ElevatedButton(
                    onPressed: getImageFromCamera,
                    child: Text(
                      'Scan Document',
                    ),
                    style: ElevatedButton.styleFrom(
                      fixedSize: Size(150, 150),
                      elevation: 2,
                      foregroundColor: Colors.black,
                      backgroundColor: Colors.amberAccent,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.all(
                          Radius.circular(10),
                        ),
                      ),
                    ),
                  ),
                  SizedBox(width: 10),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
