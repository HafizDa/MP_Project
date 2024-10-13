package com.example.mpproject.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.mpproject.PMApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.URL

val SERVER_PATHNAME = "https://avoindata.eduskunta.fi/"

// 12.10.2024 by Hafiz
// This class is responsible for loading images from the server, employing caching mechanism
object ImageLoader {
    fun getImage(urlString: String?): ImageBitmap? {
        if (urlString == null) {
            return null
        }
// 12.10.2024 by Hafiz
        // Check if image was already loaded
        // - if it was - decode file
        // - if it wasn't - download it and save to cache
        val filename: String = urlString.substringAfterLast("/")

        try {
            return BitmapFactory.decodeFile(filename).asImageBitmap()
        } catch (e: Exception) {
            val url = URL(SERVER_PATHNAME + urlString)
            val res: Bitmap? = runBlocking {
                val deferred = async(Dispatchers.IO) {
                    try {
                        return@async BitmapFactory.decodeStream(
                            url.openConnection().getInputStream()
                        )
                    } catch (e: Exception) {
                        return@async null
                    }
                }
                return@runBlocking deferred.await()
            }
            cacheImage(filename, res)
            return res?.asImageBitmap()
        }
    }

    fun cacheImage(filename: String, bitmap: Bitmap?) {
        if (bitmap == null) {
            return
        }

        val context = PMApplication.appContext
        val fileOutputStream: FileOutputStream =
            context.openFileOutput(filename, Context.MODE_PRIVATE)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
    }


    // Add the new loadImage function here
    fun loadImage(filePath: String) {
        try {
            val bitmap = BitmapFactory.decodeFile(filePath)
            if (bitmap == null) {
                Log.e("BitmapFactory", "Unable to decode file: $filePath")
            } else {
                // Use the bitmap
            }
        } catch (e: FileNotFoundException) {
            Log.e("BitmapFactory", "File not found: $filePath", e)
        }
    }
}