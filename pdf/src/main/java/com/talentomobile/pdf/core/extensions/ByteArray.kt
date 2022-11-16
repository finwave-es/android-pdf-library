package com.talentomobile.pdf.core.extensions

import android.content.Context
import com.talentomobile.pdf.core.Constants
import java.io.File
import java.io.FileOutputStream

fun ByteArray.savePDF(context: Context): File? {

    return runCatching {
        val outFile = File(context.cacheDir, Constants.pdf)
        val output = FileOutputStream(outFile)
        output.write(this)
        output.close()
        outFile
    }
        .getOrNull()
}
