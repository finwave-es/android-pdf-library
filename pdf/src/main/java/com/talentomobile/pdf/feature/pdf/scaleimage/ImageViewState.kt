package com.talentomobile.pdf.feature.pdf.scaleimage

import android.graphics.PointF
import java.io.Serializable

/**
 * Wraps the scale, center and orientation of a displayed image for easy restoration on screen rotate.
 */
class ImageViewState(val scale: Float, center: PointF, orientation: Int) :
    Serializable {
    private val centerX: Float
    private val centerY: Float
    val orientation: Int

    init {
        centerX = center.x
        centerY = center.y
        this.orientation = orientation
    }

    val center: PointF
        get() = PointF(centerX, centerY)
}
