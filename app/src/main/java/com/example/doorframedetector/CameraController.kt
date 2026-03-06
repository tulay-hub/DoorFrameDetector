package com.example.doorframedetector

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CameraController(private val context: Context) {
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageAnalysis: ImageAnalysis? = null
    
    private val _frameFlow = MutableSharedFlow<Bitmap>(replay = 0)
    val frameFlow: SharedFlow<Bitmap> = _frameFlow.asSharedFlow()
    
    suspend fun initializeCamera(): Boolean {
        return try {
            cameraProvider = getCameraProvider()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun startCamera(analysisExecutor: ExecutorService, frameProcessor: (ImageProxy) -> Unit) {
        val cameraProvider = cameraProvider ?: return
        
        // Unbind existing use cases
        cameraProvider.unbindAll()
        
        // Image Analysis Use Case for real-time processing
        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .setTargetRotation(context.resources.displayMetrics.widthPixels)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(analysisExecutor) { image ->
                    frameProcessor(image)
                }
            }
        
        // Camera Selector - use back camera
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        
        // Bind use cases to camera
        camera = cameraProvider.bindToLifecycle(
            (context as androidx.lifecycle.LifecycleOwner),
            cameraSelector,
            imageAnalysis
        )
    }
    
    fun stopCamera() {
        cameraProvider?.unbindAll()
        camera = null
        imageAnalysis = null
    }
    
    fun getCameraResolution(): Pair<Int, Int> {
        return camera?.cameraInfo?.let { info ->
            val resolution = info.getSensorResolution()
            resolution.width to resolution.height
        } ?: (1920 to 1080) // Default fallback
    }
    
    private suspend fun getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(context).also { future ->
            future.addListener({
                try {
                    continuation.resume(future.get())
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }
    
    fun release() {
        stopCamera()
        cameraExecutor.shutdown()
    }
}