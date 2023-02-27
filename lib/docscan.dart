import 'dart:async';
import 'dart:io';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';

class DocScan {
  static const MethodChannel _channel = const MethodChannel('scan_document');

  static bool canUseGallery = false;
  static String androidScanTitle = "Scanning";
  static String androidCropTitle = "Crop";
  static String androidCropBlackWhiteTitle = "Scan";
  static String androidCropReset = "Undo";

  static String imagePath = "";

  /// Call this method to scan the object edge in live camera.
  static Future<List<String>?> scanDocument({String saveToPath = ""}) async {
    List<String> paths = [];
    // Request permission for camera

    Map<Permission, PermissionStatus> statuses = await [
      Permission.camera,
    ].request();
    // Check if permission is granted or throw exception
    if (statuses.containsValue(PermissionStatus.denied)) {
      throw Exception("Permission not granted");
    }

    // Generate Random file name
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
}
