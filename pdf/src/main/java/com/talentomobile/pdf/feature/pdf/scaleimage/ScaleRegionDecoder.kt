package com.talentomobile.pdf.feature.pdf.scaleimage

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.text.TextUtils
import androidx.annotation.Keep
import java.io.InputStream
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Default implementation of [ImageRegionDecoder]
 * using Android's [android.graphics.BitmapRegionDecoder]. This
 * works well in most circumstances and has reasonable performance due to the cached decoder instance,
 * however it has some problems with grayscale, indexed and CMYK images.
 *
 *
 * A [ReadWriteLock] is used to delegate responsibility for multi threading behaviour to the
 * [BitmapRegionDecoder] instance on SDK &gt;= 21, whilst allowing this class to block until no
 * tiles are being loaded before recycling the decoder. In practice, [BitmapRegionDecoder] is
 * synchronized internally so this has no real impact on performance.
 */
class ScaleRegionDecoder(bitmapConfig: Bitmap.Config?) : ImageRegionDecoder {
    private var decoder: BitmapRegionDecoder? = null
    private val decoderLock: ReadWriteLock = ReentrantReadWriteLock(true)
    private var bitmapConfig: Bitmap.Config? = null

    @Keep
    constructor() : this(null) {
    }

    init {
        val globalBitmapConfig = ScaleImageView.preferredBitmapConfig
        if (bitmapConfig != null) {
            this.bitmapConfig = bitmapConfig
        } else if (globalBitmapConfig != null) {
            this.bitmapConfig = globalBitmapConfig
        } else {
            this.bitmapConfig = Bitmap.Config.RGB_565
        }
    }

    override fun init(context: Context?, uri: Uri): Point {
        val uriString = uri.toString()
        if (uriString.startsWith(RESOURCE_PREFIX) && context != null) {
            val res: Resources?
            val packageName = uri.authority
            res = if (context.packageName == packageName) {
                context.resources
            } else {
                val pm = context.packageManager
                pm?.getResourcesForApplication(packageName!!)
            }
            var id = 0
            val segments = uri.pathSegments
            val size = segments.size
            if (size == 2 && segments[0] == "drawable") {
                val resName = segments[1]
                id = res?.getIdentifier(resName, "drawable", packageName) ?: 0
            } else if (size == 1 && TextUtils.isDigitsOnly(segments[0])) {
                runCatching {
                    id = segments[0].toInt()
                }.getOrNull()
            }
            decoder = BitmapRegionDecoder.newInstance(context.resources.openRawResource(id), false)
        } else if (uriString.startsWith(ASSET_PREFIX) && context != null) {
            val assetName = uriString.substring(ASSET_PREFIX.length)
            decoder = BitmapRegionDecoder.newInstance(
                context.assets.open(
                    assetName,
                    AssetManager.ACCESS_RANDOM
                ),
                false
            )
        } else if (uriString.startsWith(FILE_PREFIX)) {
            decoder =
                BitmapRegionDecoder.newInstance(uriString.substring(FILE_PREFIX.length), false)
        } else {
            var inputStream: InputStream? = null
            runCatching {
                val contentResolver = context?.contentResolver
                inputStream = contentResolver?.openInputStream(uri)
                if (inputStream == null) {
                    throw Exception("Content resolver returned null stream. Unable to initialise with uri.")
                } else {
                    decoder = BitmapRegionDecoder.newInstance(inputStream!!, false)
                }
            }.map {
                if (inputStream != null) {
                    runCatching {
                        inputStream!!.close()
                    }.getOrNull()
                }
            }
        }
        return Point(decoder!!.width, decoder!!.height)
    }

    override fun decodeRegion(sRect: Rect, sampleSize: Int): Bitmap {
        decodeLock.lock()
        return try {
            if (decoder != null && !decoder!!.isRecycled) {
                val options = BitmapFactory.Options()
                options.inSampleSize = sampleSize
                options.inPreferredConfig = bitmapConfig
                val bitmap = decoder!!.decodeRegion(sRect, options)
                    ?: throw RuntimeException("Image decoder returned null bitmap - image format may not be supported")
                bitmap
            } else {
                throw IllegalStateException("Cannot decode region after decoder has been recycled")
            }
        } finally {
            decodeLock.unlock()
        }
    }

    @get:Synchronized
    override val isReady: Boolean
        get() = decoder != null && !decoder!!.isRecycled

    @Synchronized
    override fun recycle() {
        decoderLock.writeLock().lock()
        decoder = try {
            decoder!!.recycle()
            null
        } finally {
            decoderLock.writeLock().unlock()
        }
    }

    /**
     * Before SDK 21, BitmapRegionDecoder was not synchronized internally. Any attempt to decode
     * regions from multiple threads with one decoder instance causes a segfault. For old versions
     * use the write lock to enforce single threaded decoding.
     */
    private val decodeLock: Lock
        get() = decoderLock.readLock()

    companion object {
        private const val FILE_PREFIX = "file://"
        private const val ASSET_PREFIX = FILE_PREFIX + "/android_asset/"
        private const val RESOURCE_PREFIX = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
    }
}
