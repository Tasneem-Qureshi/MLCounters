package com.mlcounters.app.detection

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import com.mlcounters.app.data.repository.CounterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * Result data class for a detected hero on screen.
 */
data class DetectionResult(
    val heroId: Int,
    val heroName: String,
    val confidence: Float,
    val location: Point
)

/**
 * Internal candidate representation before Non-Maximum Suppression.
 */
private data class CandidateDetection(
    val heroId: Int,
    val heroName: String,
    val confidence: Float,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

@Singleton
class HeroDetectionEngine @Inject constructor(
    private val templateManager: TemplateManager,
    private val repository: CounterRepository
) {
    private var confidenceThreshold: Float = DEFAULT_CONFIDENCE_THRESHOLD
    private var globalRoi: Rect? = null

    fun setConfidenceThreshold(threshold: Float) {
        confidenceThreshold = threshold.coerceIn(0.1f, 1.0f)
        Log.d(TAG, "Detection confidence threshold set to: $confidenceThreshold")
    }

    fun getConfidenceThreshold(): Float = confidenceThreshold

    fun setROI(rect: Rect?) {
        globalRoi = rect
        Log.d(TAG, "Detection ROI set to: $rect")
    }

    fun getROI(): Rect? = globalRoi

    /**
     * Detects heroes in the provided screen bitmap.
     * Supports optional ROI cropping, multi-scale template matching,
     * parallel execution via coroutines, and Non-Maximum Suppression (NMS).
     */
    suspend fun detectHeroes(
        screenBitmap: Bitmap,
        roi: Rect? = globalRoi
    ): List<DetectionResult> = withContext(Dispatchers.Default) {
        if (screenBitmap.isRecycled || screenBitmap.width <= 0 || screenBitmap.height <= 0) {
            Log.w(TAG, "Invalid or recycled bitmap passed to detectHeroes.")
            return@withContext emptyList()
        }

        val screenWidth = screenBitmap.width
        val screenHeight = screenBitmap.height

        // 1. Determine best template resolution based on screen dimensions
        val primaryResolution = templateManager.selectResolution(screenWidth, screenHeight)

        // 2. Crop ROI if provided and valid
        val effectiveRoi = calculateSafeRoi(screenWidth, screenHeight, roi)
        val roiOffsetX = effectiveRoi?.left ?: 0
        val roiOffsetY = effectiveRoi?.top ?: 0

        // 3. Convert Bitmap to Mat
        val screenRgbaMat = Mat()
        val croppedMat: Mat
        val grayMat = Mat()
        val blurredMat = Mat()

        try {
            Utils.bitmapToMat(screenBitmap, screenRgbaMat)

            if (effectiveRoi != null && (effectiveRoi.width() < screenWidth || effectiveRoi.height() < screenHeight)) {
                val opencvRoi = org.opencv.core.Rect(
                    effectiveRoi.left,
                    effectiveRoi.top,
                    effectiveRoi.width(),
                    effectiveRoi.height()
                )
                croppedMat = Mat(screenRgbaMat, opencvRoi)
            } else {
                croppedMat = screenRgbaMat
            }

            // 4. Optimize: Convert to grayscale once
            Imgproc.cvtColor(croppedMat, grayMat, Imgproc.COLOR_RGBA2GRAY)

            // 5. Apply Gaussian blur preprocessing for noise reduction at small sizes (e.g. 35px)
            val kernelSize = if (primaryResolution <= TemplateManager.RESOLUTION_35) {
                Size(3.0, 3.0)
            } else {
                Size(3.0, 3.0)
            }
            Imgproc.GaussianBlur(grayMat, blurredMat, kernelSize, 0.0)

            // 6. Multi-scale detection: try primary resolution first, and fallback scales if needed
            val resolutionsToTry = mutableListOf(primaryResolution)
            if (primaryResolution == TemplateManager.RESOLUTION_48) {
                resolutionsToTry.add(TemplateManager.RESOLUTION_35)
                resolutionsToTry.add(TemplateManager.RESOLUTION_64)
            } else if (primaryResolution == TemplateManager.RESOLUTION_35) {
                resolutionsToTry.add(TemplateManager.RESOLUTION_48)
            } else if (primaryResolution == TemplateManager.RESOLUTION_64) {
                resolutionsToTry.add(TemplateManager.RESOLUTION_48)
            }

            // 7. Parallel matching using coroutines
            val allCandidates = mutableListOf<CandidateDetection>()

            for (res in resolutionsToTry) {
                val templates = templateManager.getAllTemplates(res)
                if (templates.isEmpty()) continue

                val candidatesForRes = coroutineScope {
                    templates.entries.map { (heroId, templateMat) ->
                        async(Dispatchers.Default) {
                            matchHeroTemplate(
                                heroId = heroId,
                                templateMat = templateMat,
                                searchMat = blurredMat,
                                templateResolution = res,
                                roiOffsetX = roiOffsetX,
                                roiOffsetY = roiOffsetY,
                                threshold = confidenceThreshold
                            )
                        }
                    }.awaitAll().filterNotNull()
                }

                allCandidates.addAll(candidatesForRes)

                // If primary resolution gave high quality candidates, no need to over-search
                if (candidatesForRes.size >= 5 && res == primaryResolution) {
                    break
                }
            }

            // 8. Apply Non-Maximum Suppression (NMS) to eliminate overlapping duplicate detections
            val filteredResults = applyNonMaximumSuppression(allCandidates, iouThreshold = 0.35f)

            // 9. Return sorted by confidence descending (draft enemy team at most 5 heroes)
            val sortedResults = filteredResults.sortedByDescending { it.confidence }
            Log.d(TAG, "Detection completed. Found ${sortedResults.size} heroes.")
            sortedResults
        } catch (e: Exception) {
            Log.e(TAG, "Exception during hero detection: ${e.message}", e)
            emptyList()
        } finally {
            try {
                blurredMat.release()
                grayMat.release()
                if (croppedMat != screenRgbaMat) {
                    croppedMat.release()
                }
                screenRgbaMat.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error cleaning up OpenCV Mat: ${e.message}")
            }
        }
    }

    /**
     * Executes normalized cross-correlation template matching for a single hero portrait.
     */
    private fun matchHeroTemplate(
        heroId: Int,
        templateMat: Mat,
        searchMat: Mat,
        templateResolution: Int,
        roiOffsetX: Int,
        roiOffsetY: Int,
        threshold: Float
    ): CandidateDetection? {
        if (searchMat.rows() < templateMat.rows() || searchMat.cols() < templateMat.cols()) {
            return null
        }

        val resultCols = searchMat.cols() - templateMat.cols() + 1
        val resultRows = searchMat.rows() - templateMat.rows() + 1
        val resultMat = Mat(resultRows, resultCols, CvType.CV_32FC1)

        try {
            // Imgproc.matchTemplate with TM_CCOEFF_NORMED
            Imgproc.matchTemplate(searchMat, templateMat, resultMat, Imgproc.TM_CCOEFF_NORMED)

            val minMaxResult = Core.minMaxLoc(resultMat)
            val maxConfidence = minMaxResult.maxVal.toFloat()

            if (maxConfidence >= threshold) {
                val bestLoc = minMaxResult.maxLoc
                val heroName = templateManager.getHeroName(heroId)

                return CandidateDetection(
                    heroId = heroId,
                    heroName = heroName,
                    confidence = maxConfidence,
                    x = (bestLoc.x + roiOffsetX).toInt(),
                    y = (bestLoc.y + roiOffsetY).toInt(),
                    width = templateResolution,
                    height = templateResolution
                )
            }
            return null
        } catch (e: Exception) {
            Log.w(TAG, "Template matching error for hero $heroId: ${e.message}")
            return null
        } finally {
            resultMat.release()
        }
    }

    /**
     * Applies Non-Maximum Suppression (NMS) to candidate bounding boxes:
     * - Discards detections that overlap spatially with a higher confidence detection
     * - Discards duplicate hero matches
     */
    private fun applyNonMaximumSuppression(
        candidates: List<CandidateDetection>,
        iouThreshold: Float
    ): List<DetectionResult> {
        if (candidates.isEmpty()) return emptyList()

        val sorted = candidates.sortedByDescending { it.confidence }.toMutableList()
        val kept = mutableListOf<DetectionResult>()
        val keptHeroIds = mutableSetOf<Int>()

        while (sorted.isNotEmpty()) {
            val best = sorted.removeAt(0)

            // If this heroId was already accepted with a higher confidence, skip
            if (keptHeroIds.contains(best.heroId)) {
                continue
            }

            kept.add(
                DetectionResult(
                    heroId = best.heroId,
                    heroName = best.heroName,
                    confidence = best.confidence,
                    location = Point(best.x + best.width / 2, best.y + best.height / 2)
                )
            )
            keptHeroIds.add(best.heroId)

            // Filter out any candidates that overlap significantly with the accepted detection
            val iterator = sorted.iterator()
            while (iterator.hasNext()) {
                val candidate = iterator.next()
                if (candidate.heroId == best.heroId) {
                    iterator.remove()
                    continue
                }

                val iou = calculateIoU(best, candidate)
                if (iou > iouThreshold) {
                    iterator.remove()
                }
            }

            // In Mobile Legends draft, an enemy team has at most 5 heroes
            if (kept.size >= 5) break
        }

        return kept
    }

    /**
     * Calculates Intersection over Union (IoU) of two candidate bounding boxes.
     */
    private fun calculateIoU(a: CandidateDetection, b: CandidateDetection): Float {
        val xA = max(a.x, b.x)
        val yA = max(a.y, b.y)
        val xB = min(a.x + a.width, b.x + b.width)
        val yB = min(a.y + a.height, b.y + b.height)

        val intersectionArea = max(0, xB - xA) * max(0, yB - yA)
        val areaA = a.width * a.height
        val areaB = b.width * b.height
        val unionArea = (areaA + areaB - intersectionArea).toFloat()

        return if (unionArea <= 0f) 0f else intersectionArea / unionArea
    }

    private fun calculateSafeRoi(bitmapWidth: Int, bitmapHeight: Int, roi: Rect?): Rect? {
        if (roi == null) return null
        val left = roi.left.coerceIn(0, bitmapWidth - 1)
        val top = roi.top.coerceIn(0, bitmapHeight - 1)
        val right = roi.right.coerceIn(left + 1, bitmapWidth)
        val bottom = roi.bottom.coerceIn(top + 1, bitmapHeight)
        return Rect(left, top, right, bottom)
    }

    companion object {
        private const val TAG = "HeroDetectionEngine"
        const val DEFAULT_CONFIDENCE_THRESHOLD = 0.75f
    }
}
