package com.privin.softpermissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar

class SoftPermissions : AppCompatActivity() {

    private lateinit var rootView: ConstraintLayout
    private lateinit var msgContainerView: View

    private var shouldHandlePermanentlyDenied: Boolean = false

    private lateinit var mode: String
    private lateinit var permission: String
    private lateinit var permissions: Array<String>

    enum class PermissionStatus {
        GRANTED,  // Permission granted
        DENIED,   // Permission denied once, show custom msg why this permission is required for user to enable permissions and request again
        PERMANENTLY_DENIED // Permission denied, will have to manually enable from settings
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            when {
                permissionGranted -> {
                    permissionResult.invoke(PermissionStatus.GRANTED)
                    finish()
                }
                shouldShowRequestPermissionRationale(permission) -> {
                    permissionResult.invoke(PermissionStatus.DENIED)
                    finish()
                }
                else -> {
                    if (shouldHandlePermanentlyDenied) {
                        showNoPermissionMsg()
                    }
                    permissionResult.invoke(PermissionStatus.PERMANENTLY_DENIED)
                }
            }

        }

    private val multipleRequestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionMap ->
            var isAnyPermissionPermanentlyDenied = false
            val softPermissionMap = hashMapOf<String, PermissionStatus>()
            permissionMap.entries.forEach {
                val value = when {
                    it.value -> {
                        PermissionStatus.GRANTED
                    }
                    shouldShowRequestPermissionRationale(it.key) -> {
                        PermissionStatus.DENIED
                    }
                    else -> {
                        isAnyPermissionPermanentlyDenied = true
                        PermissionStatus.PERMANENTLY_DENIED
                    }
                }
                softPermissionMap[it.key] = value
            }

            multiplePermissionResult.invoke(
                softPermissionMap,
                isAnyPermissionPermanentlyDenied.not()
            )

            if (shouldHandlePermanentlyDenied && isAnyPermissionPermanentlyDenied) {
                showNoPermissionMsg()
            } else {
                finish()
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soft_permissions)
        rootView = findViewById(R.id.root)
        msgContainerView = findViewById(R.id.msg_container)
        init()
        initListeners()
        makeRequest()
    }

    private fun init() {
        mode = intent?.getStringExtra(ARG_MODE) ?: "single"
        permission = intent?.getStringExtra(ARG_PERMISSION) ?: ""
        permissions = intent?.getStringArrayExtra(ARG_MULTIPLE_PERMISSIONS) ?: arrayOf("")
        shouldHandlePermanentlyDenied =
            intent?.getBooleanExtra(ARG_HANDLE_PERMANENTLY_DENIED, false) ?: false
    }

    private fun makeRequest() {
        if (mode == "single") {
            requestPermission(permission)
        } else {
            requestPermissions(permissions)
        }
    }

    private fun initListeners() {
        rootView.setOnClickListener {
            finish()
        }
    }

    private fun requestPermission(permission: String) {
        if (permission.isEmpty()) return
        requestPermission.launch(permission)
    }

    private fun requestPermissions(permissions: Array<String>) {
        if (permissions.isEmpty()) return
        multipleRequestPermissions.launch(permissions)
    }

    private fun showNoPermissionMsg() {
        val snackBar = Snackbar.make(
            msgContainerView,
            resources.getString(R.string.no_permission_msg),
            Snackbar.LENGTH_INDEFINITE
        )
        snackBar.setAction(resources.getString(R.string.settings)) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
            finish()
        }
        snackBar.show()
    }

    companion object {
        private lateinit var permissionResult: (PermissionStatus) -> Unit
        private lateinit var multiplePermissionResult: (HashMap<String, PermissionStatus>, Boolean) -> Unit

        private const val ARG_MODE = "mode"
        private const val ARG_PERMISSION = "permission"
        private const val ARG_MULTIPLE_PERMISSIONS = "multiple_permissions"
        private const val ARG_HANDLE_PERMANENTLY_DENIED = "handle_permanently_denied"

        private val permissionIntent = Intent()

        fun requiredPermission(permission: String): Companion {
            permissionIntent.putExtra(ARG_PERMISSION, permission)
            return this
        }

        fun requiredMultiplePermissions(vararg permissions: String): Companion {
            permissionIntent.putExtra(ARG_MULTIPLE_PERMISSIONS, permissions)
            return this
        }

        // Shows snackBar message with action to settings page to enable permissions
        fun handlePermanentlyDeniedPermission(): Companion {
            permissionIntent.putExtra(ARG_HANDLE_PERMANENTLY_DENIED, true)
            return this
        }

        //handles single request permission
        fun handle(context: Context, permissionResult: (PermissionStatus) -> Unit) {
            permissionIntent.putExtra(ARG_MODE, "single")
            permissionIntent.setClass(context, SoftPermissions::class.java)
            this.permissionResult = permissionResult
            context.startActivity(permissionIntent)
        }

        //handles multiple request permissions
        // returns hashmap of permission and their permission Status {PermissionStatus}
        // returns true when all the permission in the list were granted.
        fun handle(
            context: Context,
            multiplePermissionResult: (HashMap<String, PermissionStatus>, Boolean) -> Unit
        ) {
            permissionIntent.putExtra(ARG_MODE, "multiple")
            permissionIntent.setClass(context, SoftPermissions::class.java)
            this.multiplePermissionResult = multiplePermissionResult
            context.startActivity(permissionIntent)
        }
    }
}