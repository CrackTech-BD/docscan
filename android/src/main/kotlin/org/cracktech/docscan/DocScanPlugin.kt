package org.cracktech.docscan

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import org.cracktech.docscan.crop.CropActivity
import org.cracktech.docscan.processor.processPicture
import org.cracktech.docscan.scan.ScanActivity
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


class DocScanPlugin : FlutterPlugin, ActivityAware {
    private var handler: EdgeDetectionHandler? = null



    override fun onAttachedToEngine(binding: FlutterPluginBinding) {
        handler = EdgeDetectionHandler()

        if (!OpenCVLoader.initDebug()) {
        }
        val channel = MethodChannel(
            binding.binaryMessenger, "scan_document"
        )
        channel.setMethodCallHandler(handler)
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {}

    override fun onAttachedToActivity(activityPluginBinding: ActivityPluginBinding) {
        handler?.setActivityPluginBinding(activityPluginBinding)
    }

    override fun onDetachedFromActivityForConfigChanges() {}
    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}
    override fun onDetachedFromActivity() {}
}

class EdgeDetectionHandler : MethodCallHandler, PluginRegistry.ActivityResultListener {
    private var activityPluginBinding: ActivityPluginBinding? = null
    private var result: Result? = null
    private var methodCall: MethodCall? = null

    lateinit var currentPhotoPath: String

    companion object {
        public const val INITIAL_BUNDLE = "initial_bundle"
        public const val FROM_GALLERY = "from_gallery"
        public const val FROM_DEVICE_CAMERA = "from_device_camera"
        public const val SAVE_TO = "save_to"
        public const val CAN_USE_GALLERY = "can_use_gallery"
        public const val SCAN_TITLE = "scan_title"
        public const val CROP_TITLE = "crop_title"
        public const val CROP_BLACK_WHITE_TITLE = "crop_black_white_title"
        public const val CROP_RESET_TITLE = "crop_reset_title"
    }

    fun setActivityPluginBinding(activityPluginBinding: ActivityPluginBinding) {
        activityPluginBinding.addActivityResultListener(this)
        this.activityPluginBinding = activityPluginBinding
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when {
            getActivity() == null -> {
                result.error(
                    "no_activity",
                    "DocScan plugin requires a foreground activity.",
                    null
                )
                return
            }
            call.method.equals("edge_detect") -> {
                openCameraActivity(call, result)
            }
            call.method.equals("edge_detect_device_camera") -> {
                openDeviceCameraActivity(call, result)
            }
            call.method.equals("edge_detect_gallery") -> {
                openGalleryActivity(call, result)
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    private fun getActivity(): Activity? {
        return activityPluginBinding?.activity
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                finishWithSuccess(true)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                finishWithSuccess(false)
            }
            return true
        }else if(requestCode == 102){
            if (resultCode == Activity.RESULT_OK) {
                var bitmap =  data?.extras?.get("data") as Bitmap

                val pic = Mat()
                Utils.bitmapToMat(bitmap, pic)

                val bundle = Bundle();

                bundle.putString(SAVE_TO, "")
                bundle.putString(CROP_TITLE, "Crop")
                bundle.putString(
                    CROP_BLACK_WHITE_TITLE,
                    "Crop Black"
                )
                bundle.putString(CROP_RESET_TITLE, "Reset")
                bundle.putBoolean(FROM_GALLERY, true)

                SourceManager.corners = processPicture(pic)
                //Imgproc.cvtColor(pic, pic, Imgproc.COLOR_RGB2BGRA)
                SourceManager.pic = pic

                val cropIntent = Intent(getActivity()?.applicationContext, CropActivity::class.java);
                cropIntent.putExtra(EdgeDetectionHandler.INITIAL_BUNDLE, bundle)
                getActivity()?.startActivity(cropIntent)


                //inishWithSuccess(true)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //finishWithSuccess(false)
            }
            return true


        }
        return false
    }

    private fun openCameraActivity(call: MethodCall, result: Result) {
        if (!setPendingMethodCallAndResult(call, result)) {
            finishWithAlreadyActiveError()
            return
        }

        val initialIntent =
            Intent(Intent(getActivity()?.applicationContext, ScanActivity::class.java))
        val bundle = Bundle();
        bundle.putString(SAVE_TO, call.argument<String>(SAVE_TO) as String)
        bundle.putString(SCAN_TITLE, call.argument<String>(SCAN_TITLE) as String)
        bundle.putString(CROP_TITLE, call.argument<String>(CROP_TITLE) as String)
        bundle.putString(
            CROP_BLACK_WHITE_TITLE,
            call.argument<String>(CROP_BLACK_WHITE_TITLE) as String
        )
        bundle.putString(CROP_RESET_TITLE, call.argument<String>(CROP_RESET_TITLE) as String)
        bundle.putBoolean(CAN_USE_GALLERY, call.argument<Boolean>(CAN_USE_GALLERY) as Boolean)
        initialIntent.putExtra(INITIAL_BUNDLE, bundle)
        getActivity()?.startActivityForResult(initialIntent, REQUEST_CODE)
    }




    // ...

    private fun openDeviceCameraActivity(call: MethodCall, result: Result) {


        if (!setPendingMethodCallAndResult(call, result)) {
            finishWithAlreadyActiveError()
            return
        }

        val initialIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)



        // Launch the camera app
        val activity = getActivity()
        activity?.packageManager?.let { pm ->
            if (initialIntent.resolveActivity(pm) != null) {

                activity.startActivityForResult(initialIntent, 102)
            } else {
                finishWithError("camera_not_available", "Device camera app is not available.")
            }
        } ?: finishWithError("no_activity", "No foreground activity available.")
    }


    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        val image = File.createTempFile(
            imageFileName,  // prefix
            ".jpg",  // suffix
            storageDir // directory
        )

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = "file:" + image.absolutePath
        return image
    }



//    private fun createImageFile(): File {
//        // Create an image file name
//        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        return File.createTempFile(
//            "JPEG_${timeStamp}_", /* prefix */
//            ".jpg", /* suffix */
//            storageDir /* directory */
//        ).apply {
//            // Save a file: path for use with ACTION_VIEW intents
//            currentPhotoPath = absolutePath
//        }
//    }

// ...


    private fun openGalleryActivity(call: MethodCall, result: Result) {
        if (!setPendingMethodCallAndResult(call, result)) {
            finishWithAlreadyActiveError()
            return
        }
        val initialIntent =
            Intent(Intent(getActivity()?.applicationContext, ScanActivity::class.java))
        val bundle = Bundle();
        bundle.putString(SAVE_TO, call.argument<String>(SAVE_TO) as String)
        bundle.putString(CROP_TITLE, call.argument<String>(CROP_TITLE) as String)
        bundle.putString(
            CROP_BLACK_WHITE_TITLE,
            call.argument<String>(CROP_BLACK_WHITE_TITLE) as String
        )
        bundle.putString(CROP_RESET_TITLE, call.argument<String>(CROP_RESET_TITLE) as String)
        bundle.putBoolean(FROM_GALLERY, call.argument<Boolean>(FROM_GALLERY) as Boolean)
        initialIntent.putExtra(INITIAL_BUNDLE, bundle)
        getActivity()?.startActivityForResult(initialIntent, REQUEST_CODE)
    }



    private fun setPendingMethodCallAndResult(
        methodCall: MethodCall,
        result: Result
    ): Boolean {
        if (this.result != null) {
            return false
        }
        this.methodCall = methodCall
        this.result = result
        return true
    }

    private fun finishWithAlreadyActiveError() {
        finishWithError("already_active", "Edge detection is already active")
    }

    private fun finishWithError(errorCode: String, errorMessage: String) {
        result?.error(errorCode, errorMessage, null)
        clearMethodCallAndResult()
    }

    private fun finishWithSuccess(res: Boolean) {
        result?.success(res)
        clearMethodCallAndResult()
    }

    private fun clearMethodCallAndResult() {
        methodCall = null
        result = null
    }
}
