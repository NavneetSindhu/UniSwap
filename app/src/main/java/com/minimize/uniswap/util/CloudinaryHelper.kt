package com.minimize.uniswap.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloudinary Direct Media Uploader.
 * Uploads local device images (Uri) directly to Cloudinary using standard Unsigned Upload Presets
 * without requiring any backend signature.
 */
@Singleton
class CloudinaryHelper @Inject constructor() {

    // Default configuration (Replace with your Cloudinary Cloud Name & Upload Preset)
    companion object {
        // Replace with your Cloudinary Cloud Name (e.g. "dxyz12345")
        var CLOUD_NAME: String = "cvforvpw"
        
        // Replace with your Cloudinary Unsigned Upload Preset name (e.g. "uniswap_items")
        var UPLOAD_PRESET: String = "uniswap_items"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Uploads an image from an Android Uri to Cloudinary and returns the permanent HTTPS secure URL.
     * Segregates files by folder (e.g. "uniswap/users/{userId}/items").
     */
    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        folder: String? = null,
        tags: String? = null,
        cloudName: String = CLOUD_NAME,
        uploadPreset: String = UPLOAD_PRESET
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = if (imageUri.scheme == "file") {
                java.io.File(imageUri.path ?: "").inputStream()
            } else {
                context.contentResolver.openInputStream(imageUri)
            } ?: return@withContext Result.failure(Exception("Unable to open image stream"))

            val byteArray = inputStream.use { input ->
                val buffer = ByteArrayOutputStream()
                val data = ByteArray(8192)
                var nRead: Int
                while (input.read(data, 0, data.size).also { nRead = it } != -1) {
                    buffer.write(data, 0, nRead)
                }
                buffer.toByteArray()
            }

            val requestBody = byteArray.toRequestBody("image/webp".toMediaTypeOrNull())

            val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "upload_${System.currentTimeMillis()}.webp", requestBody)
                .addFormDataPart("upload_preset", uploadPreset)

            if (!folder.isNullOrBlank()) {
                builder.addFormDataPart("folder", folder)
            }

            if (!tags.isNullOrBlank()) {
                builder.addFormDataPart("tags", tags)
            }

            val multipartBody = builder.build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                .post(multipartBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val secureUrl = json.optString("secure_url")
                if (secureUrl.isNotBlank()) {
                    Result.success(secureUrl)
                } else {
                    Result.failure(Exception("Cloudinary response missing secure_url: $responseBody"))
                }
            } else {
                val errorMsg = try {
                    JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
                } catch (e: Exception) {
                    responseBody
                }
                Result.failure(Exception("Cloudinary upload failed ($response): $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
