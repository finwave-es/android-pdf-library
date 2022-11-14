package com.davidups.pdf

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.davidups.pdf.databinding.ActivityMainBinding
import com.talentomobile.pdf.feature.pdf.PDFViewer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        showPDF()
    }

    private fun showPDF() {
        val url = "https://icseindia.org/document/sample.pdf"
        binding.pdf.initStatusListener(getPdfStatusListener())
        binding.pdf.initializePDFDownloader(url, getPdfStatusListener())
    }

    private fun getPdfStatusListener() =
        object : PDFViewer.PDFViewerStatusListener {
            override fun onStartDownload() {
                showSpinner(true)
            }

            override fun onPageChanged(position: Int, total: Int) {}

            override fun onProgressDownload(currentStatus: Int) {}

            override fun onSuccessDownLoad(path: String) {
                binding.pdf.fileInit(path)
                showSpinner(false)
            }

            override fun onFail(error: Throwable) {
                handleError("error")
                showSpinner(false)
            }

            override fun unsupportedDevice() {}
        }

    private fun showSpinner(show: Boolean) {
        if (show) {
            binding.spinner.visibility = View.VISIBLE
        } else {
            binding.spinner.visibility = View.GONE
        }
    }

    private fun handleError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.pdf.pdfRendererCore?.clear()
    }
}