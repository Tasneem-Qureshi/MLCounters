package com.mlcounters.app.detection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateManager @Inject constructor() {

    // Cache structure: Resolution -> (HeroId -> OpenCV Grayscale Mat)
    private val templateCache = ConcurrentHashMap<Int, ConcurrentHashMap<Int, Mat>>()

    // Hero name map for fast lookup during detection
    private val heroNameCache = ConcurrentHashMap<Int, String>()

    /**
     * Loads hero portrait templates from the assets/portraits/ directory.
     * Looks for resolution-specific folder (e.g. assets/portraits/48/)
     * or scales from base assets/portraits/.
     * Templates are converted to grayscale and preprocessed with Gaussian blur.
     */
    @Synchronized
    fun loadTemplates(context: Context, resolution: Int) {
        val validResolution = if (resolution in SUPPORTED_RESOLUTIONS) resolution else selectResolution(1920, 1080)
        Log.d(TAG, "Loading templates for target resolution: ${validResolution}px...")

        val resMap = templateCache.getOrPut(validResolution) { ConcurrentHashMap() }

        try {
            val assetManager = context.assets
            // 1. Try resolution-specific directory first: portraits/{resolution}
            val resPath = "portraits/$validResolution"
            var files = try {
                assetManager.list(resPath) ?: emptyArray()
            } catch (e: IOException) {
                emptyArray()
            }

            var basePath = resPath
            if (files.isEmpty()) {
                // 2. Fall back to root portraits/ directory
                basePath = "portraits"
                files = try {
                    assetManager.list(basePath) ?: emptyArray()
                } catch (e: IOException) {
                    emptyArray()
                }
            }

            Log.d(TAG, "Found ${files.size} template files in assets/$basePath")

            var loadedCount = 0
            for (filename in files) {
                if (!filename.endsWith(".png", ignoreCase = true) &&
                    !filename.endsWith(".jpg", ignoreCase = true) &&
                    !filename.endsWith(".webp", ignoreCase = true)
                ) {
                    continue
                }

                val heroId = extractHeroIdFromFilename(filename) ?: continue
                val heroName = extractHeroNameFromFilename(filename)
                if (heroName.isNotEmpty()) {
                    heroNameCache[heroId] = heroName
                }

                val assetFilePath = "$basePath/$filename"
                try {
                    assetManager.open(assetFilePath).use { inputStream ->
                        val mat = processBitmapToMat(inputStream, validResolution)
                        if (mat != null) {
                            // Release existing Mat if replacing
                            resMap[heroId]?.release()
                            resMap[heroId] = mat
                            loadedCount++
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load template $assetFilePath: ${e.message}")
                }
            }

            // If no physical assets exist in assets yet, generate synthetic test templates
            // to ensure detection algorithms function reliably in development & unit tests
            if (loadedCount == 0 && resMap.isEmpty()) {
                Log.i(TAG, "No portrait assets found on disk; generating fallback templates for standard hero pool.")
                generateFallbackTemplates(validResolution, resMap)
            }

            Log.i(TAG, "Loaded ${resMap.size} hero templates for ${validResolution}px.")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading templates for resolution $validResolution: ${e.message}", e)
        }
    }

    /**
     * Converts input stream to an OpenCV Grayscale Mat at target resolution.
     */
    private fun processBitmapToMat(inputStream: InputStream, targetResolution: Int): Mat? {
        val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null

        val scaledBitmap = if (originalBitmap.width != targetResolution || originalBitmap.height != targetResolution) {
            Bitmap.createScaledBitmap(originalBitmap, targetResolution, targetResolution, true)
        } else {
            originalBitmap
        }

        val rgbaMat = Mat()
        Utils.bitmapToMat(scaledBitmap, rgbaMat)

        val grayMat = Mat()
        Imgproc.cvtColor(rgbaMat, grayMat, Imgproc.COLOR_RGBA2GRAY)
        rgbaMat.release()

        // Apply light Gaussian blur for noise reduction at small resolutions (especially 35px/48px)
        val blurredMat = Mat()
        val kernelSize = if (targetResolution <= RESOLUTION_35) Size(3.0, 3.0) else Size(3.0, 3.0)
        Imgproc.GaussianBlur(grayMat, blurredMat, kernelSize, 0.0)
        grayMat.release()

        return blurredMat
    }

    /**
     * Retrieves a single hero portrait template at the specified resolution.
     */
    fun getTemplate(heroId: Int, resolution: Int): Mat? {
        val resMap = templateCache[resolution] ?: return null
        return resMap[heroId]
    }

    /**
     * Retrieves all loaded hero portrait templates for a given resolution.
     */
    fun getAllTemplates(resolution: Int): Map<Int, Mat> {
        val resMap = templateCache[resolution]
        return if (resMap != null) {
            HashMap(resMap)
        } else {
            emptyMap()
        }
    }

    /**
     * Auto-selects best template resolution based on screen dimensions.
     * Takes minOf(screenWidth, screenHeight) to identify display class:
     * - < 720p: 35px
     * - 720p - 1080p: 48px
     * - 1080p - 1440p: 64px
     * - > 1440p: 96px
     */
    fun selectResolution(screenWidth: Int, screenHeight: Int): Int {
        val minDim = minOf(screenWidth, screenHeight)
        return when {
            minDim < 720 -> RESOLUTION_35
            minDim in 720..1080 -> RESOLUTION_48
            minDim in 1081..1440 -> RESOLUTION_64
            else -> RESOLUTION_96
        }
    }

    /**
     * Handles devices with lowered rendering resolutions (e.g. Samsung Game Booster,
     * Battery Saver, or Android Resolution Switcher).
     * Compares system display metrics against current window render bounds.
     */
    fun detectActualRenderResolution(context: Context): Pair<Int, Int> {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            ?: return Pair(1920, 1080)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val bounds = metrics.bounds
            Pair(bounds.width(), bounds.height())
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }
    }

    /**
     * Extracts Hero ID from filenames such as "1.png", "hero_12.png", "12_48px.png", "tigreal_6.png".
     */
    private fun extractHeroIdFromFilename(filename: String): Int? {
        val nameWithoutExt = filename.substringBeforeLast(".")
        val digits = Regex("\\d+").find(nameWithoutExt)?.value ?: return null
        return digits.toIntOrNull()
    }

    private fun extractHeroNameFromFilename(filename: String): String {
        val nameWithoutExt = filename.substringBeforeLast(".")
        val textOnly = nameWithoutExt.replace(Regex("[_\\-\\d]+"), " ").trim()
        return textOnly.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    /**
     * Generates fallback templates if physical portrait assets are not bundled yet.
     * Creates distinct, contrastive grayscale patterns for common MLBB heroes.
     */
    private fun generateFallbackTemplates(
        resolution: Int,
        destMap: ConcurrentHashMap<Int, Mat>
    ) {
        val sampleHeroes = listOf(
            1 to "Miya", 2 to "Balmond", 3 to "Saber", 4 to "Alice", 5 to "Nana",
            6 to "Tigreal", 7 to "Alucard", 8 to "Karina", 9 to "Akai", 10 to "Franco",
            17 to "Fanny", 24 to "Chou", 27 to "Ruby", 38 to "Karrie", 44 to "Lancelot",
            45 to "Diggie", 50 to "Lesley", 53 to "Gusion", 56 to "Uranus", 75 to "Khufra",
            78 to "Esmeralda", 80 to "Ling", 81 to "Dyrroth", 85 to "Baxia", 87 to "Wanwan"
        )

        for ((id, name) in sampleHeroes) {
            heroNameCache[id] = name
            val bitmap = Bitmap.createBitmap(resolution, resolution, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb((id * 37) % 256, (id * 73) % 256, (id * 109) % 256)
            }
            canvas.drawCircle(resolution / 2f, resolution / 2f, resolution / 2.2f, paint)

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = (resolution * 0.35f)
                textAlign = Paint.Align.CENTER
            }
            val textBounds = Rect()
            val label = id.toString()
            textPaint.getTextBounds(label, 0, label.length, textBounds)
            canvas.drawText(label, resolution / 2f, (resolution / 2f) + (textBounds.height() / 2f), textPaint)

            val rgbaMat = Mat()
            Utils.bitmapToMat(bitmap, rgbaMat)

            val grayMat = Mat()
            Imgproc.cvtColor(rgbaMat, grayMat, Imgproc.COLOR_RGBA2GRAY)
            rgbaMat.release()

            val blurredMat = Mat()
            Imgproc.GaussianBlur(grayMat, blurredMat, Size(3.0, 3.0), 0.0)
            grayMat.release()

            destMap[id] = blurredMat
        }
    }

    fun getHeroName(heroId: Int): String {
        return heroNameCache[heroId] ?: "Hero #$heroId"
    }

    /**
     * Clears all in-memory Mat objects and releases native OpenCV memory.
     */
    @Synchronized
    fun clearCache() {
        Log.d(TAG, "Clearing all cached OpenCV Mat templates...")
        for ((res, map) in templateCache) {
            for ((id, mat) in map) {
                try {
                    mat.release()
                } catch (e: Exception) {
                    Log.w(TAG, "Error releasing Mat for hero $id at $res px: ${e.message}")
                }
            }
            map.clear()
        }
        templateCache.clear()
        Log.d(TAG, "Template cache cleared successfully.")
    }

    companion object {
        private const val TAG = "TemplateManager"

        const val RESOLUTION_35 = 35
        const val RESOLUTION_48 = 48
        const val RESOLUTION_64 = 64
        const val RESOLUTION_96 = 96

        val SUPPORTED_RESOLUTIONS = listOf(
            RESOLUTION_35,
            RESOLUTION_48,
            RESOLUTION_64,
            RESOLUTION_96
        )
    }
}
