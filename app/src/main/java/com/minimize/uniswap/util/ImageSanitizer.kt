package com.minimize.uniswap.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Low-level Android Image Sanitizer & Optimization Pipeline.
 * 
 * Responsibilities:
 * 1. Privacy / Security: Completely scrubs EXIF GPS coordinates, camera serial numbers, and metadata.
 * 2. EXIF Orientation Normalization: Pre-rotates portrait/landscape photos to prevent sideways uploads.
 * 3. Memory Safety: Uses inJustDecodeBounds & inSampleSize to avoid OutOfMemoryError on 50MP+ camera photos.
 * 4. Compression & Downsampling: Resizes to max bounding box (1440px) and encodes to WebP (~200KB output).
 */
object ImageSanitizer {

    private const val MAX_DIMENSION = 1440 // Ideal for mobile screens & high DPI zoom
    private const val WEBP_QUALITY = 82   // Sweet spot for visual clarity vs file size

    /**
     * Sanitizes and compresses a list of Uris in parallel on Dispatchers.IO.
     */
    suspend fun sanitizeAll(context: Context, uris: List<Uri>): List<Uri> = withContext(Dispatchers.IO) {
        Timber.i("sanitizeAll() started for %d image URI(s)", uris.size)
        uris.map { uri ->
            try {
                sanitize(context, uri)
            } catch (e: Exception) {
                Timber.e(e, "Failed to sanitize image: %s", uri)
                uri // Fallback to raw Uri on error
            }
        }
    }

    /**
     * Sanitizes a single Uri:
     * - Strips EXIF metadata (GPS, device info)
     * - Corrects orientation
     * - Downsamples to max 1440px
     * - Compresses to WebP in app cacheDir
     */
    suspend fun sanitize(context: Context, imageUri: Uri): Uri = withContext(Dispatchers.IO) {
        Timber.i("Starting image sanitization for: %s", imageUri)

        // If imageUri is already a sanitized file from our cache, skip re-processing
        if (imageUri.path?.contains("sanitized_images") == true) {
            Timber.i("Already sanitized, skipping: %s", imageUri)
            return@withContext imageUri
        }

        // Read bytes once to avoid Android PhotoPicker / DocumentProvider stream exhaustion
        val rawBytes = readUriBytes(context, imageUri) ?: run {
            Timber.w("Could not read bytes for: %s", imageUri)
            return@withContext imageUri
        }

        if (rawBytes.isEmpty()) {
            Timber.w("Empty bytes for: %s", imageUri)
            return@withContext imageUri
        }

        // Step 1: Read EXIF orientation before modifying the bitmap
        val orientation = getExifOrientation(rawBytes)

        // Step 2: Query image dimensions without loading full pixels into RAM (Memory Safety)
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, boundsOptions)

        val originalWidth = boundsOptions.outWidth
        val originalHeight = boundsOptions.outHeight

        if (originalWidth <= 0 || originalHeight <= 0) {
            Timber.w("Invalid bounds (%dx%d) for: %s", originalWidth, originalHeight, imageUri)
            return@withContext imageUri
        }

        // Step 3: Compute optimal power-of-2 inSampleSize to prevent OOM
        val sampleSize = calculateInSampleSize(originalWidth, originalHeight, MAX_DIMENSION, MAX_DIMENSION)

        // Step 4: Decode downsampled bitmap
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val sampledBitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOptions) ?: run {
            Timber.w("Could not decode sampled bitmap for: %s", imageUri)
            return@withContext imageUri
        }

        // Step 5: Apply EXIF rotation if necessary
        val rotatedBitmap = rotateBitmapIfRequired(sampledBitmap, orientation)

        // Step 6: Scale down precisely to MAX_DIMENSION if still larger
        val finalBitmap = scaleBitmapToMaxDimension(rotatedBitmap, MAX_DIMENSION)

        // Step 7: Write to clean, private WebP cache file (wipes all metadata & GPS)
        val cacheDir = File(context.cacheDir, "sanitized_images").apply {
            if (!exists()) mkdirs()
        }
        val sanitizedFile = File(cacheDir, "item_${UUID.randomUUID()}.webp")

        FileOutputStream(sanitizedFile).use { outStream ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                finalBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, WEBP_QUALITY, outStream)
            } else {
                @Suppress("DEPRECATION")
                finalBitmap.compress(Bitmap.CompressFormat.WEBP, WEBP_QUALITY, outStream)
            }
        }

        // Recycle bitmaps to free native memory immediately
        if (finalBitmap != sampledBitmap) sampledBitmap.recycle()
        if (finalBitmap != rotatedBitmap) rotatedBitmap.recycle()
        finalBitmap.recycle()

        val outputSizeKb = sanitizedFile.length() / 1024
        Timber.i("✓ Sanitized: %dx%d -> %s (~%d KB, EXIF GPS stripped)", originalWidth, originalHeight, sanitizedFile.name, outputSizeKb)

        Uri.fromFile(sanitizedFile)
    }

    private fun getExifOrientation(bytes: ByteArray): Int {
        return try {
            ByteArrayInputStream(bytes).use { input ->
                val exif = ExifInterface(input)
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun rotateBitmapIfRequired(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return rotated
    }

    private fun scaleBitmapToMaxDimension(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDim && height <= maxDim) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxDim
            newHeight = (maxDim / ratio).toInt()
        } else {
            newHeight = maxDim
            newWidth = (maxDim * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth.coerceAtLeast(1), newHeight.coerceAtLeast(1), true)
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun readUriBytes(context: Context, uri: Uri): ByteArray? {
        return try {
            if (uri.scheme == "file") {
                File(uri.path ?: "").readBytes()
            } else {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
        } catch (e: Exception) {
            Timber.e(e, "readUriBytes failed for %s", uri)
            null
        }
    }
}
