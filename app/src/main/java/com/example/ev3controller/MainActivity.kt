package com.example.ev3controller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ev3controller.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    
    // Request codes
    private companion object {
        const val TAG = "EV3Controller"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Check permissions
        if (allPermissionsGranted()) {
            startCamera()
            setupBluetooth()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Setup control listeners
        setupControls()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraView.surfaceProvider)
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            // Request Bluetooth to be enabled
            // In production, you would handle this with a proper dialog
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var ev3Comm: EV3Comm
    private lateinit var gamepadHandler: GamepadHandler
    private val EV3_IP = "10.0.1.1" // Default EV3 IP
    private val EV3_PORT = 1234 // Default EV3 port

    private fun setupControls() {
        // Initialize EV3 communication
        ev3Comm = EV3Comm(EV3_IP, EV3_PORT)
        gamepadHandler = GamepadHandler(ev3Comm)

        // Connect to EV3
        Thread {
            val connected = ev3Comm.connect()
            runOnUiThread {
                binding.txtConnectionStatus.text = if (connected) "EV3 Connected" else "EV3 Connection Failed"
            }
        }.start()

        // Setup button listeners
        binding.btnForward.setOnClickListener {
            ev3Comm.moveForward(50)
        }
        
        binding.btnBackward.setOnClickListener {
            ev3Comm.moveBackward(50)
        }
        
        binding.btnLeft.setOnClickListener {
            ev3Comm.turnLeft(45)
        }
        
        binding.btnRight.setOnClickListener {
            ev3Comm.turnRight(45)
        }
        
        binding.btnActionA.setOnClickListener {
            // Custom action A
        }
        
        binding.btnActionB.setOnClickListener {
            // Custom action B
        }
        
        binding.btnActionC.setOnClickListener {
            // Custom action C
        }
        
        binding.btnActionD.setOnClickListener {
            // Custom action D
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return gamepadHandler.handleKeyEvent(event) || super.dispatchKeyEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        return gamepadHandler.handleMotionEvent(event) || super.dispatchGenericMotionEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
                setupBluetooth()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}