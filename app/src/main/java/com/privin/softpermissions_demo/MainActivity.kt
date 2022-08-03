package com.privin.softpermissions_demo

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.privin.softpermissions.SoftPermissions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnCamera = findViewById<Button>(R.id.btnReqCam)
        val btnCameraAndStorage = findViewById<Button>(R.id.btnReqCamAndStorage)
        val permission = SoftPermissions.checkPermission(this, Manifest.permission.CAMERA)
        Toast.makeText(this, "Camera permission: "+ permission.name, Toast.LENGTH_SHORT).show()
        btnCamera.setOnClickListener {
            requestCameraPermission()
        }
        btnCameraAndStorage.setOnClickListener {
            requestCameraAndStoragePermission()
        }
    }

    private fun requestCameraPermission() {
        SoftPermissions.handle(this) { permissionStatus ->
            Toast.makeText(this, permissionStatus.name, Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestCameraAndStoragePermission() {
        SoftPermissions.handle(this) { permissionMap, allPermissionsGranted ->
            if (allPermissionsGranted) {
                Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Some permissions are denied", Toast.LENGTH_LONG).show()
            }
        }
    }
}