package org.cracktech.docscan.crop

import android.widget.ImageView
import org.cracktech.docscan.view.PaperRectangle



class ICropView {
    interface Proxy {
        fun getPaper(): ImageView
        fun getPaperRect(): PaperRectangle
        fun getCroppedPaper(): ImageView
    }
}