package com.dkv.design.pdf

import com.talentomobile.pdf.feature.pdf.PDFViewer
import com.talentomobile.pdf.feature.pdf.PdfScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class PdfDownloader(
    private val file: File,
    private val downLoadUrl: String,
    private val statusListener: PDFViewer.PDFViewerStatusListener
) : CoroutineScope by PdfScope() {

    companion object {
        const val BUFFER_SIZE = 8192
    }

    init {
        download()
    }

    private fun download() = launch(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            statusListener.onStartDownload()
        }

        kotlin.runCatching {
            if (file.exists()) {
                file.delete()
            }

            val url = URL(downLoadUrl)

            val connection: HttpsURLConnection = url.openConnection() as HttpsURLConnection
            connection.requestMethod = "GET"
            connection.addRequestProperty("Content-Type", "application/pdf")
            connection.instanceFollowRedirects = true
            connection.setChunkedStreamingMode(0)
            connection.connect()
            val totalLength = connection.contentLength

            BufferedInputStream(connection.inputStream, BUFFER_SIZE).use { input ->
                file.outputStream().use { output ->
                    var downloaded = 0

                    do {
                        val data = ByteArray(BUFFER_SIZE)
                        val count = input.read(data)
                        if (count == -1) {
                            break
                        }

                        if (totalLength > 0) {
                            downloaded += count
                            withContext(Dispatchers.Main) {
                                (downloaded.toFloat() / totalLength.toFloat() * 100F)
                                    .toInt()
                                    .let(statusListener::onProgressDownload)
                            }
                        }

                        output.write(data, 0, count)
                    } while (true)
                }
            }
        }.onFailure {
            withContext(Dispatchers.Main) {
                statusListener.onFail(it)
            }

            cancel()
        }

        withContext(Dispatchers.Main) {
            statusListener.onSuccessDownLoad(file.absolutePath)
        }
    }
}
