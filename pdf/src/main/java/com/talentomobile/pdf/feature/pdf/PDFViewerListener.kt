package com.talentomobile.pdf.feature.pdf

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.dkv.design.pdf.PdfDownloader
import com.talentomobile.pdf.R
import com.talentomobile.pdf.databinding.PdfViewerBinding
import java.io.File

class PDFViewer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    interface PDFViewerStatusListener {
        fun onStartDownload()
        fun onPageChanged(position: Int, total: Int)
        fun onProgressDownload(currentStatus: Int)
        fun onSuccessDownLoad(path: String)
        fun onFail(error: Throwable)
        fun unsupportedDevice()
    }

    private var orientation: Direction = Direction.HORIZONTAL
    private var isPdfAnimation: Boolean = false

    init {
        getAttrs(attrs, defStyleAttr)
    }

    private var binding =
        PdfViewerBinding.inflate(LayoutInflater.from(context), this, true)

    @SuppressLint("CustomViewStyleable")
    private fun getAttrs(attrs: AttributeSet?, defStyle: Int) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.PDFViewer, defStyle, 0)
        setTypeArray(typedArray)
    }

    private fun setTypeArray(typedArray: TypedArray) {
        val ori =
            typedArray.getInt(R.styleable.PDFViewer_pdf_direction, Direction.HORIZONTAL.ori)
        orientation = Direction.values().first { it.ori == ori }
        isPdfAnimation = typedArray.getBoolean(R.styleable.PDFViewer_pdf_animation, false)
        typedArray.recycle()
    }

    var pdfRendererCore: PdfCore? = null
    private lateinit var statusListener: PDFViewerStatusListener
    private val pageTotalCount get() = pdfRendererCore?.getPDFPagePage() ?: 0

    fun initializePDFDownloader(url: String, statusListener: PDFViewerStatusListener) {
        this.statusListener = statusListener
        val cacheFile = File(context.cacheDir, "pdfcache.pdf")
        PdfDownloader(cacheFile, url, statusListener)
    }

    fun initStatusListener(statusListener: PDFViewerStatusListener): PDFViewer {
        this.statusListener = statusListener
        return this
    }

    fun fileInit(path: String) {
        pdfRendererCore = PdfCore(context, File(path))

        val rv = binding.rvPdfViewer.apply {
            layoutManager = LinearLayoutManager(this.context).apply {
                orientation =
                    if (this@PDFViewer.orientation.ori == Direction.HORIZONTAL.ori) {
                        LinearLayoutManager.HORIZONTAL
                    } else {
                        LinearLayoutManager.VERTICAL
                    }
            }
            if (pdfRendererCore != null) {
                adapter = PdfAdapter(pdfRendererCore!!, isPdfAnimation)
            }
            addOnScrollListener(scrollListener)
        }

        rv.let(PagerSnapHelper()::attachToRecyclerView)
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            (recyclerView.layoutManager as LinearLayoutManager).run {
                var foundPosition = findFirstCompletelyVisibleItemPosition()
                if (foundPosition != RecyclerView.NO_POSITION) {
                    statusListener.onPageChanged(foundPosition, pageTotalCount)
                    return@run
                }
                foundPosition = findFirstVisibleItemPosition()
                if (foundPosition != RecyclerView.NO_POSITION) {
                    statusListener.onPageChanged(foundPosition, pageTotalCount)
                    return@run
                }
            }
        }
    }
}
