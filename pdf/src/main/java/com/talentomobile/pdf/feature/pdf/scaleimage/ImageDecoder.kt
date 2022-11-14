package com.talentomobile.pdf.feature.pdf.scaleimage

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri

interface ImageDecoder {
    /**
     * Decode an image. The URI can be in one of the following formats:
     * <br></br>
     * File: `file:///scard/picture.jpg`
     * <br></br>
     * Asset: `file:///android_asset/picture.png`
     * <br></br>
     * Resource: `android.resource://com.example.app/drawable/picture`
     *
     * @param context Application context
     * @param uri     URI of the image
     * @return the decoded bitmap
     * @throws Exception if decoding fails.
     */
    @Throws(Exception::class)
    fun decode(context: Context?, uri: Uri): Bitmap
}
