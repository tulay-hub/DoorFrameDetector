package com.example.doorframedetector

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageProxy
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), Scene.OnUpdateListener, VisionProcessor.DetectionListener {
    private lateinit var arSceneView: ArSceneView
    private lateinit var groundStatusText: TextView
    private lateinit var doorframeStatusText: TextView
    private lateinit var instructionText: TextView
    
    private var groundDetected = false
    private var doorframeDetected = false
    private var doorframeMarkerNode: Node? = null
    
    private lateinit var cameraController: CameraController
    private lateinit var visionProcessor: VisionProcessor
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize views
        arSceneView = findViewById(R.id.ar_scene_view)
        groundStatusText = findViewById(R.id.ground_status)
        doorframeStatusText = findViewById(R.id.doorframe_status)
        instructionText = findViewById(R.id.instruction_text)
        
        // Initialize components
        cameraController = CameraController(this)
        visionProcessor = VisionProcessor(this, this)
        
        // Check camera permission
        if (!hasCameraPermission()) {
            requestCameraPermission()
        } else {
            setupAR()
        }
    }
    
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupAR()
            } else {
                Toast.makeText(
                    this,
                    "需要摄像头权限才能使用此应用",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }
    
    private fun setupAR() {
        coroutineScope.launch {
            // Configure AR Scene
            arSceneView.scene.addOnUpdateListener(this@MainActivity)
            
            // Initialize camera for real-time processing
            val cameraInitialized = withContext(Dispatchers.IO) {
                cameraController.initializeCamera()
            }
            
            if (cameraInitialized) {
                // Start camera feed for vision processing
                cameraController.startCamera(analysisExecutor) { image ->
                    processCameraFrame(image)
                }
                
                runOnUiThread {
                    instructionText.text = "正在启动摄像头检测..."
                }
            } else {
                runOnUiThread {
                    instructionText.text = "摄像头初始化失败，使用AR检测模式"
                }
            }
        }
    }
    
    private fun processCameraFrame(image: ImageProxy) {
        visionProcessor.processImage(image, image.imageInfo.rotationDegrees)
    }
    
    private fun simulateGroundDetection() {
        runOnUiThread {
            groundDetected = true
            groundStatusText.text = getString(R.string.ground_detected)
            groundStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            
            // After ground detection, start door frame detection simulation
            instructionText.postDelayed({
                simulateDoorFrameDetection()
            }, 2000)
        }
    }
    
    private fun simulateDoorFrameDetection() {
        runOnUiThread {
            doorframeDetected = true
            doorframeStatusText.text = getString(R.string.doorframe_detected)
            doorframeStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
            instructionText.text = "门框已标记！请参考AR可视化"
            
            // Add a simple visual marker for door frame (simulated)
            addDoorFrameMarker()
        }
    }
    
    private fun addDoorFrameMarker() {
        // This is a simplified marker - in a real app, you would detect actual door frame
        // and place a 3D rectangle at its location
        
        val scene = arSceneView.scene
        val frame = arSceneView.arFrame ?: return
        
        // Create a simple cube to represent door frame
        val anchor = frame.camera.pose
        val anchorNode = AnchorNode()
        anchorNode.setParent(scene)
        
        MaterialFactory.makeTransparentWithColor(this, Color(android.graphics.Color.YELLOW))
            .thenAccept { material ->
                val cube = ShapeFactory.makeCube(Vector3(0.2f, 0.8f, 0.01f), Vector3.zero(), material)
                val cubeNode = Node()
                cubeNode.setParent(anchorNode)
                cubeNode.renderable = cube
                cubeNode.localPosition = Vector3(0f, 0.4f, -2f) // Place 2 meters in front
            }
    }
    
    override fun onUpdate(frameTime: FrameTime) {
        // Real-time processing for ground and door frame detection
        val frame = arSceneView.arFrame ?: return
        
        // Check for detected planes (ground)
        val updatedPlanes = frame.getUpdatedTrackables(Plane::class.java)
        for (plane in updatedPlanes) {
            if (plane.trackingState == TrackingState.TRACKING) {
                if (plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING && !groundDetected) {
                    // Ground plane detected
                    runOnUiThread {
                        groundDetected = true
                        groundStatusText.text = getString(R.string.ground_detected)
                        groundStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                    }
                    
                    // Here you could add visual feedback for the ground plane
                }
            }
        }
        
        // Simple door frame detection simulation based on feature points
        // In a real implementation, you would use ML or CV algorithms here
        if (groundDetected && !doorframeDetected) {
            // Simulate door frame detection when enough feature points are found
            val pointCloud = frame.acquirePointCloud()
            if (pointCloud.size > 100) {
                // Enough feature points for detection
                runOnUiThread {
                    doorframeDetected = true
                    doorframeStatusText.text = getString(R.string.doorframe_detected)
                    doorframeStatusText.setTextColor(ContextCompat.getColor(
                        this,
                        android.R.color.holo_orange_dark
                    ))
                    instructionText.text = "正在分析门框结构..."
                }
            }
            pointCloud.release()
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (hasCameraPermission()) {
            arSceneView.resume()
        }
    }
    
    override fun onPause() {
        super.onPause()
        arSceneView.pause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources
        coroutineScope.cancel()
        analysisExecutor.shutdown()
        cameraController.release()
        visionProcessor.stop()
        arSceneView.destroy()
    }
    
    // VisionProcessor.DetectionListener implementation
    override fun onDoorFrameDetected(centerX: Float, centerY: Float, width: Float, height: Float) {
        runOnUiThread {
            if (!doorframeDetected) {
                doorframeDetected = true
                doorframeStatusText.text = "检测到门框 (${width.toInt()}x${height.toInt()})"
                doorframeStatusText.setTextColor(ContextCompat.getColor(
                    this,
                    android.R.color.holo_orange_dark
                ))
                instructionText.text = "正在创建AR标记..."
                
                // Place AR marker at the detected location
                placeDoorFrameARMarker(centerX, centerY)
            }
        }
    }
    
    override fun onDetectionCleared() {
        runOnUiThread {
            if (doorframeDetected) {
                doorframeStatusText.text = "门框检测消失"
                doorframeStatusText.setTextColor(ContextCompat.getColor(
                    this,
                    android.R.color.holo_red_dark
                ))
            }
            doorframeDetected = false
            
            // Remove existing door frame marker
            removeDoorFrameMarker()
        }
    }
    
    private fun placeDoorFrameARMarker(screenX: Float, screenY: Float) {
        // Convert screen coordinates to world position
        val frame = arSceneView.arFrame ?: return
        
        // Create hit test at the detected location
        val hits = frame.hitTest(screenX, screenY)
        for (hit in hits) {
            val trackable = hit.trackable
            if (trackable is Plane && trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING) {
                // Found a ground plane, create anchor
                val anchor = hit.createAnchor()
                val anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arSceneView.scene)
                
                // Create door frame visualization
                addDoorFrameVisualization(anchorNode)
                return
            }
        }
        
        // If no plane found, create at estimated depth
        addDoorFrameMarker()
    }
    
    private fun addDoorFrameVisualization(anchorNode: AnchorNode) {
        // Remove previous marker
        removeDoorFrameMarker()
        
        // Create yellow transparent box for door frame
        MaterialFactory.makeTransparentWithColor(this, Color(android.graphics.Color.YELLOW))
            .thenAccept { material ->
                // Create a tall, thin box to represent door frame
                val doorFrameModel = ShapeFactory.makeCube(
                    Vector3(0.15f, 0.8f, 0.05f),  // width, height, depth
                    Vector3(0f, 0.4f, 0f),  // center at half height
                    material
                )
                
                val doorFrameNode = Node()
                doorFrameNode.setParent(anchorNode)
                doorFrameNode.renderable = doorFrameModel
                doorFrameMarkerNode = doorFrameNode
                
                // Also add a text label
                instructionText.postDelayed({
                    runOnUiThread {
                        instructionText.text = "门框已标记完成！"
                    }
                }, 500)
            }
    }
    
    private fun removeDoorFrameMarker() {
        doorframeMarkerNode?.let { node ->
            node.setParent(null)
            doorframeMarkerNode = null
        }
    }
}