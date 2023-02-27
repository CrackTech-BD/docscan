package com.zuhabul.docscan


import com.zuhabul.docscan.processor.Corners
import org.opencv.core.Mat

class SourceManager {
    companion object {
        var pic: Mat? = null
        var corners: Corners? = null
    }
}