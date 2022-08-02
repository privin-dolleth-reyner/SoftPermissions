package com.privin.softpermissions

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnCamera = findViewById<Button>(R.id.btnReqCam)
        val btnCameraAndStorage = findViewById<Button>(R.id.btnReqCamAndStorage)
        btnCamera.setOnClickListener {
            requestCameraPermission()
        }
        btnCameraAndStorage.setOnClickListener {
            requestCameraAndStoragePermission()
        }
    }

    private fun requestCameraPermission() {
        SoftPermissions.requiredPermission(Manifest.permission.CAMERA)
            .handlePermanentlyDeniedPermission()
            .handle(this) { permissionStatus ->
                Toast.makeText(this, permissionStatus.name, Toast.LENGTH_SHORT).show()
            }
    }

    private fun requestCameraAndStoragePermission() {
        SoftPermissions.requiredMultiplePermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
            .handlePermanentlyDeniedPermission()
            .handle(this) { permissionMap, allPermissionsGranted ->
                Toast.makeText(this, allPermissionsGranted.toString(), Toast.LENGTH_SHORT).show()
            }
    }
}