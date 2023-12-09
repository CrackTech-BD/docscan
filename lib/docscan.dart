import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';

class DocScan {
  static const MethodChannel _channel = const MethodChannel('scan_document');

  static bool canUseGallery = true;
  static String androidScanTitle = "Scanning";
  static String androidCropTitle = "Crop";
  static String androidCropBlackWhiteTitle = "Scan";
  static String androidCropReset = "Undo";

  static String imagePath = "";

  static Future<List<String>?> scanDocument({String saveToPath = ""}) async {
    List<String> paths = [];

    Map<Permission, PermissionStatus> statuses = await [
      Permission.camera,
    ].request();
    if (statuses.containsValue(PermissionStatus.denied)) {
      throw Exception("Permission not granted");
    }

    if (saveToPath.isEmpty) {
      await getApplicationSupportDirectory().then((value) {
        imagePath = join(value.path,
            "${(DateTime.now().millisecondsSinceEpoch / 1000).round()}.jpeg");
      });
    } else {
      imagePath = saveToPath;
    }

    if (Platform.isAndroid) {
      bool isSuccess = await _channel.invokeMethod('edge_detect', {
        'save_to': imagePath,
        'can_use_gallery': canUseGallery,
        'scan_title': androidScanTitle,
        'crop_title': androidCropTitle,
        'crop_black_white_title': androidCropBlackWhiteTitle,
        'crop_reset_title': androidCropReset,
      });
      if (isSuccess == true) {
        paths.add(imagePath);
        return paths;
      }
    } else if (Platform.isIOS) {
      final List<dynamic>? pictures =
          await _channel.invokeMethod('edge_detect');
      return pictures?.map((e) => e as String).toList();
    }
    return paths;
  }

  static Future<bool> detectEdgeFromGallery(
    String saveTo, {
    String androidCropTitle = "Crop",
    String androidCropBlackWhiteTitle = "Black White",
    String androidCropReset = "Reset",
  }) async {
    print('aqui1:$saveTo');
    return await _channel.invokeMethod('edge_detect_gallery', {
      'save_to': saveTo,
      'crop_title': androidCropTitle,
      'crop_black_white_title': androidCropBlackWhiteTitle,
      'crop_reset_title': androidCropReset,
      'from_gallery': true,
    });
  }

  static Future<bool> detectEdgeFromDeviceCamera(
    String saveTo, {
    String androidCropTitle = "Crop",
    String androidCropBlackWhiteTitle = "Black White",
    String androidCropReset = "Reset",
  }) async {
    print('aqui1:$saveTo');
    Map<Permission, PermissionStatus> statuses = await [
      Permission.camera,
    ].request();

    if (statuses.containsValue(PermissionStatus.denied)) {
      throw Exception("Permission not granted");
    }

    return await _channel.invokeMethod('edge_detect_device_camera', {
      'save_to': saveTo,
      'crop_title': androidCropTitle,
      'crop_black_white_title': androidCropBlackWhiteTitle,
      'crop_reset_title': androidCropReset,
      'from_device_camera': true,
    });
  }
}
