package com.privin.softpermissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

class SoftPermissions : AppCompatActivity() {

    private lateinit var rootView: ConstraintLayout
    private lateinit var msgContainerView: View

    private var shouldHandlePermanentlyDenied: Boolean = false

    private lateinit var mode: String
    private lateinit var permission: String
    private lateinit var permissions: Array<String>

    private val snackBar: Snackbar by lazy {
        createSnackBar()
    }

    enum class PermissionStatus {
        /** Permission granted */
        GRANTED,

        /** // Permission denied once, show custom msg why this permission is required for user to enable permissions and request again */
        DENIED_ONCE,

        /** Permission denied, will have to manually enable from settings */
        DENIED,

        /** Permission was not requested before */
        NOT_REQUESTED
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            when {
                permissionGranted -> {
                    permissionResult.invoke(PermissionStatus.GRANTED)
                    finish()
                }
                Build.VERSION.SDK_INT >= M && shouldShowRequestPermissionRationale(permission) -> {
                    permissionResult.invoke(PermissionStatus.DENIED_ONCE)
                    finish()
                }
                else -> {
                    if (shouldHandlePermanentlyDenied) {
                        showNoPermissionMsg()
                    }
                    permissionResult.invoke(PermissionStatus.DENIED)
                }
            }

        }

    private val multipleRequestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionMap ->
            var isAnyPermissionPermanentlyDenied = false
            val softPermissionMap = hashMapOf<String, PermissionStatus>()
            for (entry in permissionMap.entries){
                val value = when {
                    entry.value -> {
                        PermissionStatus.GRANTED
                    }
                    Build.VERSION.SDK_INT >= M && shouldShowRequestPermissionRationale(entry.key) -> {
                        PermissionStatus.DENIED_ONCE
                    }
                    else -> {
                        isAnyPermissionPermanentlyDenied = true
                        PermissionStatus.DENIED
                    }
                }
                softPermissionMap[entry.key] = value
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
        logRequest(this, permission)
        requestPermission.launch(permission)
    }

    private fun requestPermissions(permissions: Array<String>) {
        if (permissions.isEmpty()) return
        permissions.forEach { permission ->
            logRequest(this, permission)
        }
        multipleRequestPermissions.launch(permissions)
    }

    private fun logRequest(context: Context, permission: String) {
        val sharedPref = context.getSharedPreferences(SOFT_PERMISSIONS_PREF, Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean(permission, true).apply()
    }

    private fun createSnackBar(): Snackbar {
        val msg = intent?.getStringExtra(ARG_HANDLE_PERMANENTLY_DENIED_MSG) ?: resources.getString(R.string.no_permission_msg)
        val snackBar = Snackbar.make(
            msgContainerView,
            msg,
            Snackbar.LENGTH_INDEFINITE
        )
        intent?.extras?.let { bundle ->
            val backgroundColor = bundle.getInt(ARG_CONFIG_SNACK_BAR_BACKGROUND_COLOR, -1)
            val textColor = bundle.getInt(ARG_CONFIG_SNACK_BAR_TEXT_COLOR, -1)
            val actionTextColor = bundle.getInt(ARG_CONFIG_SNACK_BAR_ACTION_TEXT_COLOR, -1)
            if (backgroundColor != -1){
                snackBar.setBackgroundTint(backgroundColor)
            }
            if (textColor != -1){
                snackBar.setTextColor(textColor)
            }
            if (actionTextColor != -1){
                snackBar.setActionTextColor(actionTextColor)
            }
        }

        snackBar.setAction(resources.getString(R.string.settings)) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
            finish()
        }
        return snackBar
    }

    private fun showNoPermissionMsg() {
        if (snackBar.isShown.not()) {
            snackBar.show()
        }
    }

    companion object {
        private lateinit var permissionResult: (PermissionStatus) -> Unit
        private lateinit var multiplePermissionResult: (HashMap<String, PermissionStatus>, Boolean) -> Unit

        private const val SOFT_PERMISSIONS_PREF = "soft_permissions_pref"

        private const val ARG_MODE = "mode"
        private const val ARG_PERMISSION = "permission"
        private const val ARG_MULTIPLE_PERMISSIONS = "multiple_permissions"
        private const val ARG_HANDLE_PERMANENTLY_DENIED = "handle_permanently_denied"
        private const val ARG_HANDLE_PERMANENTLY_DENIED_MSG = "handle_permanently_denied_msg"
        private const val ARG_CONFIG_SNACK_BAR_TEXT_COLOR = "arg_config_snack_bar_text_color"
        private const val ARG_CONFIG_SNACK_BAR_ACTION_TEXT_COLOR = "arg_config_snack_bar_action_text_color"
        private const val ARG_CONFIG_SNACK_BAR_BACKGROUND_COLOR = "arg_config_snack_bar_background_color"

        private var permissionIntent = Intent()

        private fun isNeverRequested(context: Context, permission: String): Boolean {
            val sharedPref =
                context.getSharedPreferences(SOFT_PERMISSIONS_PREF, Context.MODE_PRIVATE)
            return sharedPref.getBoolean(permission, false).not()
        }

        fun snackBarConfig(@ColorInt backgroundColor: Int? = null, @ColorInt textColor: Int? = null, @ColorInt actionTextColor: Int? = null): Companion {
            backgroundColor?.let {
                permissionIntent.putExtra(ARG_CONFIG_SNACK_BAR_BACKGROUND_COLOR, it)
            }
            textColor?.let {
                permissionIntent.putExtra(ARG_CONFIG_SNACK_BAR_TEXT_COLOR, it)
            }
            actionTextColor?.let {
                permissionIntent.putExtra(ARG_CONFIG_SNACK_BAR_ACTION_TEXT_COLOR, it)
            }
            return this
        }

        fun checkPermission(activity: Activity, permission: String): PermissionStatus {
            val shouldShowRationale = activity.run {
                Build.VERSION.SDK_INT >= M && shouldShowRequestPermissionRationale(permission)
            }

            return getPermissionStatus(activity, permission, shouldShowRationale)
        }

        fun checkPermission(fragment: Fragment, permission: String): PermissionStatus {
            val shouldShowRationale = fragment.run {
                Build.VERSION.SDK_INT >= M && shouldShowRequestPermissionRationale(permission)
            }

            return getPermissionStatus(fragment.requireContext(), permission, shouldShowRationale)
        }

        private fun getPermissionStatus(
            context: Context,
            permission: String,
            shouldShowRationale: Boolean
        ): PermissionStatus {
            return when (ContextCompat.checkSelfPermission(context, permission)) {
                PackageManager.PERMISSION_GRANTED -> PermissionStatus.GRANTED
                else -> {
                    if (shouldShowRationale) PermissionStatus.DENIED_ONCE
                    else if (isNeverRequested(context, permission)) PermissionStatus.NOT_REQUESTED
                    else PermissionStatus.DENIED
                }
            }
        }

        fun requiredPermission(permission: String): Companion {
            permissionIntent.putExtra(ARG_PERMISSION, permission)
            return this
        }

        fun requiredMultiplePermissions(vararg permissions: String): Companion {
            permissionIntent.putExtra(ARG_MULTIPLE_PERMISSIONS, permissions)
            return this
        }

        // Shows snackBar message with action to settings page to enable permissions
        fun handlePermanentlyDeniedPermission(msg: String? = null): Companion {
            permissionIntent.putExtra(ARG_HANDLE_PERMANENTLY_DENIED, true)
            msg?.let {
                permissionIntent.putExtra(ARG_HANDLE_PERMANENTLY_DENIED_MSG, it)
            }
            return this
        }

        // handles single request permission
        fun handle(context: Context, permissionResult: (PermissionStatus) -> Unit) {
            permissionIntent.putExtra(ARG_MODE, "single")
            permissionIntent.setClass(context, SoftPermissions::class.java)
            this.permissionResult = permissionResult
            context.startActivity(permissionIntent)
            permissionIntent = Intent()
        }

        /* handles multiple request permissions
        returns hashmap of permission and their permission Status {PermissionStatus}
        returns true when all the permission in the list were granted.*/
        fun handle(
            context: Context,
            multiplePermissionResult: (HashMap<String, PermissionStatus>, Boolean) -> Unit
        ) {
            permissionIntent.putExtra(ARG_MODE, "multiple")
            permissionIntent.setClass(context, SoftPermissions::class.java)
            this.multiplePermissionResult = multiplePermissionResult
            context.startActivity(permissionIntent)
            permissionIntent = Intent()
        }
    }
}