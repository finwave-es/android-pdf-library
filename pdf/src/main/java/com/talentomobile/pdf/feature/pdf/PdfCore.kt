package com.talentomobile.pdf.feature.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfCore(private val context: Context, pdfFile: File) : CoroutineScope by PdfScope() {

    companion object {
        const val cachePath = "pdf_cache"
    }

    private var pdfRenderer: PdfRenderer? = null

    init {
        initCache()
        openPdfFile(pdfFile)
    }

    fun clear() = try {
        pdfRenderer?.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    fun getPDFPagePage() = pdfRenderer?.pageCount ?: 0

    private fun initCache() = with(File(context.cacheDir, cachePath)) {
        if (exists()) deleteRecursively()
        mkdirs()
    }

    private fun getBitmapFromCache(page: Int): Bitmap? {
        val loadPath = File(File(context.cacheDir, cachePath), page.toString())
        if (!loadPath.exists()) return null

        return try {
            BitmapFactory.decodeFile(loadPath.absolutePath)
        } catch (e: Exception) {
            null
        }
    }

    private fun writeBitmapToCache(pagePosition: Int, bitmap: Bitmap) {
        val outputStream = File(File(context.cacheDir, cachePath), pagePosition.toString())
            .apply { createNewFile() }
            .let(::FileOutputStream)

        with(outputStream) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
            flush()
            close()
        }
    }

    private fun openPdfFile(pdfFile: File) {
        runCatching { ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY) }
            .onSuccess { pdfRenderer = PdfRenderer(it) }
            .onFailure { it.printStackTrace() }
    }

    fun renderPage(page: Int, ready: ((bitmap: Bitmap?, currentPage: Int) -> Unit)? = null) {
        if (page >= getPDFPagePage()) return
        buildBitmap(page) {
            launch {
                synchronized(this@PdfCore) {
                    ready?.invoke(it, page)
                }
            }
        }
    }

    private fun buildBitmap(no: Int, onBitmap: (Bitmap?) -> Unit) = launch {
        var bitmap = getBitmapFromCache(no)
        bitmap?.let {
            onBitmap(bitmap)
            return@launch
        }
        withContext(Dispatchers.IO) {
            try {
                pdfRenderer?.let {
                    val page = it.openPage(no)
                    with(page) {
                        bitmap =
                            Bitmap.createBitmap(width * 2, height * 2, Bitmap.Config.ARGB_8888)
                        bitmap ?: return@withContext
                        render(bitmap!!, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        close()
                        writeBitmapToCache(no, bitmap!!)
                        onBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                cancel()
            }
        }
    }
}
