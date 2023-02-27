package org.cracktech.docscan.scan

import android.view.Display
import android.view.SurfaceView
import org.cracktech.docscan.view.PaperRectangle

interface IScanView {
    interface Proxy {
        fun exit()
        fun getCurrentDisplay(): Display?
        fun getSurfaceView(): SurfaceView
        fun getPaperRect(): PaperRectangle
    }
}