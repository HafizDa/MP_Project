package com.example.mpproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

val SERVER_PATHNAME = "https://avoindata.eduskunta.fi/"

// 04.10.2024 by Arman Yerkeshev 2214297
// This class is responsible for loading images from the server, employing caching mechanism
object ImageLoader {
    suspend fun getImage(urlString: String?): ImageBitmap? {
        if (urlString == null) {
            return null
        }

        // Check if image was already loaded
        // - if it was - decode file
        // - if it wasn't - download it and save to cache
        val filename: String = urlString.substringAfterLast("/")
        val context = PMApplication.appContext
        val file = File(context.filesDir, filename)

        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        } else {
            val bitmap = downloadImage(SERVER_PATHNAME + urlString)
            cacheImage(file, bitmap)
            bitmap?.asImageBitmap()
        }
    }

    private suspend fun downloadImage(urlString: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                BitmapFactory.decodeStream(url.openStream())
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun cacheImage(file: File, bitmap: Bitmap?) {
        if (bitmap == null) {
            return
        }

        try {
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}