package com.talentomobile.pdf.core.extensions

import android.content.Context
import com.talentomobile.pdf.core.Constants
import java.io.File
import java.io.FileOutputStream

fun ByteArray.savePDF(context: Context): File? {
    val outFile: File
    return try {
        outFile = File(context.cacheDir, Constants.pdf)
        val output = FileOutputStream(outFile)
        output.write(this)
        output.close()
        outFile
    } catch (e: Exception) {
        null
    }
}
