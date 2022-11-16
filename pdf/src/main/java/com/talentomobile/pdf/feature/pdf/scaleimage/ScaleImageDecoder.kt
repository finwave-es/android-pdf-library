package com.talentomobile.pdf.feature.pdf.scaleimage

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import androidx.annotation.Keep
import java.io.InputStream

/**
 * Default implementation of [ImageDecoder]
 * using Android's [android.graphics.BitmapFactory],. This works well in most circumstances
 * and has reasonable performance, however it has some problems with grayscale, indexed and
 * CMYK images.
 */
class ScaleImageDecoder(bitmapConfig: Bitmap.Config?) : ImageDecoder {
    private var bitmapConfig: Bitmap.Config? = null

    @Keep
    constructor() : this(null) {
    }

    init {
        val globalBitmapConfig: Bitmap.Config? = ScaleImageView().getPreferredBitmapConfig()
        if (bitmapConfig != null) {
            this.bitmapConfig = bitmapConfig
        } else if (globalBitmapConfig != null) {
            this.bitmapConfig = globalBitmapConfig
        } else {
            this.bitmapConfig = Bitmap.Config.RGB_565
        }
    }

    override fun decode(context: Context?, uri: Uri): Bitmap {
        val uriString = uri.toString()
        val options = BitmapFactory.Options()
        val bitmap: Bitmap
        options.inPreferredConfig = bitmapConfig
        if (uriString.startsWith(RESOURCE_PREFIX)) {
            val res: Resources
            val packageName = uri.authority
            if (context != null) {
                res = if (context.packageName == packageName) {
                    context.resources
                } else {
                    val pm = context.packageManager
                    pm.getResourcesForApplication(packageName!!)
                }
                var id = 0
                val segments = uri.pathSegments
                val size = segments.size
                if (size == 2 && segments[0] == "drawable") {
                    val resName = segments[1]
                    id = res.getIdentifier(resName, "drawable", packageName)
                } else if (size == 1 && TextUtils.isDigitsOnly(segments[0])) {
                    try {
                        id = segments[0].toInt()
                    } catch (ignored: NumberFormatException) {
                    }
                }
                bitmap = BitmapFactory.decodeResource(context.resources, id, options)
            } else {
                throw RuntimeException("Image region decoder returned null bitmap - image format may not be supported")
            }
        } else if (uriString.startsWith(ASSET_PREFIX)) {
            val assetName = uriString.substring(ASSET_PREFIX.length)
            bitmap = BitmapFactory.decodeStream(context?.assets?.open(assetName), null, options)!!
        } else if (uriString.startsWith(FILE_PREFIX)) {
            bitmap = BitmapFactory.decodeFile(uriString.substring(FILE_PREFIX.length), options)
        } else {
            var inputStream: InputStream? = null
            try {
                val contentResolver = context?.contentResolver
                inputStream = contentResolver?.openInputStream(uri)
                bitmap = BitmapFactory.decodeStream(inputStream, null, options)!!
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close()
                    } catch (e: Exception) { /* Ignore */
                    }
                }
            }
        }
        return bitmap
    }

    companion object {
        private const val FILE_PREFIX = "file://"
        private const val ASSET_PREFIX = FILE_PREFIX + "/android_asset/"
        private const val RESOURCE_PREFIX = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
    }
}
