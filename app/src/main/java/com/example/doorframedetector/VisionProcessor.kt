package com.example.doorframedetector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import kotlin.math.abs

class VisionProcessor(private val context: Context, private val listener: DetectionListener) {
    
    interface DetectionListener {
        fun onDoorFrameDetected(centerX: Float, centerY: Float, width: Float, height: Float)
        fun onDetectionCleared()
    }
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentDetectionJob: Job? = null
    
    // Initialize ML Kit Object Detector
    private val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .enableClassification()
        .enableMultipleObjects()
        .build()
    
    private val objectDetector = ObjectDetection.getClient(options)
    
    // Door frame characteristics
    private val doorFrameLabels = listOf("door", "portal", "entrance", "frame", "access")
    
    fun processImage(imageProxy: ImageProxy, rotationDegrees: Int) {
        // Cancel previous detection job
        currentDetectionJob?.cancel()
        
        currentDetectionJob = coroutineScope.launch {
            try {
                val mediaImage = imageProxy.image ?: return@launch
                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                
                val objects = objectDetector.process(inputImage).await()
                
                detectDoorFrameFromObjects(objects)
            } catch (e: Exception) {
                // Log error but don't crash
                android.util.Log.e("VisionProcessor", "Detection error: ${e.message}")
            } finally {
                imageProxy.close()
            }
        }
    }
    
    private fun detectDoorFrameFromObjects(objects: List<DetectedObject>) {
        var bestDoorFrame: DetectedObject? = null
        var bestScore = 0f
        
        for (obj in objects) {
            val score = calculateDoorFrameScore(obj)
            if (score > bestScore) {
                bestScore = score
                bestDoorFrame = obj
            }
        }
        
        if (bestScore > 0.5f && bestDoorFrame != null) {
            val bounds = bestDoorFrame.boundingBox
            val centerX = (bounds.left + bounds.right) / 2f
            val centerY = (bounds.top + bounds.bottom) / 2f
            val width = bounds.width().toFloat()
            val height = bounds.height().toFloat()
            
            // Analyze aspect ratio for door frame validation
            val aspectRatio = width / height
            val isDoorFrameLike = aspectRatio in 0.4f..0.6f // Door frames are typically tall rectangles
            
            if (isDoorFrameLike) {
                withContext(Dispatchers.Main) {
                    listener.onDoorFrameDetected(centerX, centerY, width, height)
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                listener.onDetectionCleared()
            }
        }
    }
    
    private fun calculateDoorFrameScore(obj: DetectedObject): Float {
        var score = 0f
        
        // Check classification labels
        for (label in obj.labels) {
            if (doorFrameLabels.any { label.text.contains(it, ignoreCase = true) }) {
                score += label.confidence * 1.5f
            } else {
                score += label.confidence * 0.5f
            }
        }
        
        // Aspect ratio heuristic: door frames are tall rectangles
        val bounds = obj.boundingBox
        val aspectRatio = bounds.width().toFloat() / bounds.height().toFloat()
        
        // Ideal door aspect ratio is around 0.5 (width:height = 1:2)
        val ratioScore = 1f - abs(0.5f - aspectRatio)
        score += ratioScore * 0.3f
        
        // Size heuristic: door frames are typically large relative to frame
        val sizeScore = minOf(1f, bounds.width() * bounds.height() / (1920f * 1080f))
        score += sizeScore * 0.2f
        
        return score
    }
    
    fun stop() {
        currentDetectionJob?.cancel()
        coroutineScope.cancel()
    }
    
    // Alternative: Traditional CV approach for door frame detection
    fun detectDoorFrameWithGeometric(image: Bitmap): DoorFrameRect? {
        // This is a simplified geometric detection - in practice would need edge detection
        
        // For demo: return simulated detection
        return if (Math.random() > 0.7) {
            DoorFrameRect(
                x = (image.width * 0.4).toInt(),
                y = (image.height * 0.2).toInt(),
                width = (image.width * 0.2).toInt(),
                height = (image.height * 0.6).toInt()
            )
        } else {
            null
        }
    }
    
    data class DoorFrameRect(val x: Int, val y: Int, val width: Int, val height: Int)
}