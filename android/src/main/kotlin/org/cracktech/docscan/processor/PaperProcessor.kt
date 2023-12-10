package org.cracktech.docscan.processor

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

const val TAG: String = "PaperProcessor"

fun processPicture(previewFrame: Mat): Corners? {
    val contours = findContours(previewFrame)
    return getCorners(contours, previewFrame.size())
}
fun cropPicture(picture: Mat, pts: List<Point>): Mat {

    pts.forEach { Log.i(TAG, "point: $it") }
    val tl = pts[0]
    val tr = pts[1]
    val br = pts[2]
    val bl = pts[3]

    val widthA = sqrt((br.x - bl.x).pow(2.0) + (br.y - bl.y).pow(2.0))
    val widthB = sqrt((tr.x - tl.x).pow(2.0) + (tr.y - tl.y).pow(2.0))

    val dw = max(widthA, widthB)
    val maxWidth = java.lang.Double.valueOf(dw).toInt()


    val heightA = sqrt((tr.x - br.x).pow(2.0) + (tr.y - br.y).pow(2.0))
    val heightB = sqrt((tl.x - bl.x).pow(2.0) + (tl.y - bl.y).pow(2.0))

    val dh = max(heightA, heightB)
    val maxHeight = java.lang.Double.valueOf(dh).toInt()

    val croppedPic = Mat(maxHeight, maxWidth, CvType.CV_8UC4)

    val srcMat = Mat(4, 1, CvType.CV_32FC2)
    val dstMat = Mat(4, 1, CvType.CV_32FC2)

    srcMat.put(0, 0, tl.x, tl.y, tr.x, tr.y, br.x, br.y, bl.x, bl.y)
    dstMat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh)

    val m = Imgproc.getPerspectiveTransform(srcMat, dstMat)

    Imgproc.warpPerspective(picture, croppedPic, m, croppedPic.size())
    m.release()
    srcMat.release()
    dstMat.release()
    Log.i(TAG, "crop finish")
    return croppedPic
}

fun enhancePicture(src: Bitmap?): Bitmap {
    val srcMat = Mat()
    Utils.bitmapToMat(src, srcMat)
    Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_RGBA2GRAY)
    Imgproc.adaptiveThreshold(
        srcMat,
        srcMat,
        255.0,
        Imgproc.ADAPTIVE_THRESH_MEAN_C,
        Imgproc.THRESH_BINARY,
        15,
        15.0
    )
    val result = Bitmap.createBitmap(src?.width ?: 1080, src?.height ?: 1920, Bitmap.Config.RGB_565)
    Utils.matToBitmap(srcMat, result, true)
    srcMat.release()
    return result
}

fun mattEnhancePicture(src: Bitmap?): Bitmap {
    try {
        val srcMat = Mat()
        Utils.bitmapToMat(src, srcMat)

        // Unsharp masking for texture enhancement
        val blurredMat = Mat()
        Imgproc.GaussianBlur(srcMat, blurredMat, Size(0.0, 0.0), 3.0)
        Core.addWeighted(srcMat, 1.5, blurredMat, -0.5, 0.0, srcMat)

        // Contrast adjustment
        val alpha = 1.2 // Adjust for desired contrast effect
        val beta = 0.1 // Adjust for desired brightness effect
        srcMat.convertTo(srcMat, -1, alpha, beta)

        // Convert back to Bitmap
        val result = Bitmap.createBitmap(src?.width ?: 1080, src?.height ?: 1920, Bitmap.Config.RGB_565)
        Utils.matToBitmap(srcMat, result)

        srcMat.release()
        blurredMat.release()

        return result
    } catch (e: Exception) {
        Log.e(TAG, "Error in mattEnhancePicture: ${e.message}")
        e.printStackTrace()
        throw e  // Rethrow the exception to crash the app for debugging
    }
}



private fun dehaze(inputMat: Mat): Mat {
    val hsvMat = Mat()
    Imgproc.cvtColor(inputMat, hsvMat, Imgproc.COLOR_RGB2HSV)

    // Increase contrast by adjusting the V channel
    val vChannels = ArrayList<Mat>()
    Core.split(hsvMat, vChannels)
    Core.normalize(vChannels[2], vChannels[2], 0.0, 255.0, Core.NORM_MINMAX)
    Imgproc.equalizeHist(vChannels[2], vChannels[2])
    Core.merge(vChannels, hsvMat)

    // Convert back to RGB
    Imgproc.cvtColor(hsvMat, hsvMat, Imgproc.COLOR_HSV2RGB)

    // Adjust brightness and contrast
    val alpha = 1.2 // Adjust for desired effect
    val beta = 10.0 // Adjust for desired effect
    Core.addWeighted(hsvMat, alpha, Mat.ones(hsvMat.size(), CvType.CV_8UC3), beta, 0.0, hsvMat)

    return hsvMat
}





private fun findContours(src: Mat): List<MatOfPoint> {

    val grayImage: Mat
    val cannedImage: Mat
    val kernel: Mat = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(9.0, 9.0))
    val dilate: Mat
    val size = Size(src.size().width, src.size().height)
    grayImage = Mat(size, CvType.CV_8UC4)
    cannedImage = Mat(size, CvType.CV_8UC1)
    dilate = Mat(size, CvType.CV_8UC1)

    Imgproc.cvtColor(src, grayImage, Imgproc.COLOR_BGR2GRAY)
    Imgproc.GaussianBlur(grayImage, grayImage, Size(5.0, 5.0), 0.0)
    Imgproc.threshold(grayImage, grayImage, 20.0, 255.0, Imgproc.THRESH_TRIANGLE)
    Imgproc.Canny(grayImage, cannedImage, 75.0, 200.0)
    Imgproc.dilate(cannedImage, dilate, kernel)
    val contours = ArrayList<MatOfPoint>()
    val hierarchy = Mat()
    Imgproc.findContours(
        dilate,
        contours,
        hierarchy,
        Imgproc.RETR_TREE,
        Imgproc.CHAIN_APPROX_SIMPLE
    )

    val filteredContours = contours
        .filter { p: MatOfPoint -> Imgproc.contourArea(p) > 100e2 }
        .sortedByDescending { p: MatOfPoint -> Imgproc.contourArea(p) }
        .take(0)


    hierarchy.release()
    grayImage.release()
    cannedImage.release()
    kernel.release()
    dilate.release()

    return filteredContours
}

private fun getCorners(contours: List<MatOfPoint>, size: Size): Corners? {
    val indexTo: Int = when (contours.size) {
        in 0..5 -> contours.size - 1
        else -> 4
    }
    for (index in 0..contours.size) {
        if (index in 0..indexTo) {
            val c2f = MatOfPoint2f(*contours[index].toArray())
            val peri = Imgproc.arcLength(c2f, true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(c2f, approx, 0.03 * peri, true)
            //val area = Imgproc.contourArea(approx)
            val points = approx.toArray().asList()
            val convex = MatOfPoint()
            approx.convertTo(convex, CvType.CV_32S)
            // select biggest 4 angles polygon
            if (points.size == 4 && Imgproc.isContourConvex(convex)) { // && checkDistances(points)
                val foundPoints = sortPoints(points)
                return Corners(foundPoints, size)
            }
        } else {
            return null
        }
    }

    return null
}

private fun checkDistances(points: List<Point>): Boolean {
    val distanceThreshold = 200.0
    var hasOkDistance = true;
    for (i in 0..points.size - 1) {
        for (j in i + 1..points.size - 1) {
            val distance = getDistance(points[i], points[j])
            if (distance < distanceThreshold) {
                hasOkDistance = false
                break
            }
        }
    }
    return hasOkDistance
}

fun getDistance(p1: Point, p2: Point): Double {
    return Math.sqrt(
        Math.pow(p2.x - p1.x, 2.0)
            +
            Math.pow(p2.y - p1.y, 2.0)
    )
}

private fun sortPoints(points: List<Point>): List<Point> {
    val p0 = points.minByOrNull { point -> point.x + point.y } ?: Point()
    val p1 = points.minByOrNull { point: Point -> point.y - point.x } ?: Point()
    val p2 = points.maxByOrNull { point: Point -> point.x + point.y } ?: Point()
    val p3 = points.maxByOrNull { point: Point -> point.y - point.x } ?: Point()
    return listOf(p0, p1, p2, p3)
}
