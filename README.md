# SoftPermissions

A library to handle android permissions targeting android M and above. This library is intented for kotlin users.

##Installation

SoftPermissions is installed by adding the following dependency to your ```build.gradle``` file:

```
dependencies {
	        implementation 'com.github.privin-dolleth-reyner:SoftPermissions:1.0.0'
}
```

And add the following in ```settings.gradle``` :

```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

##Usage

SoftPermssions can be simply called with required permissions, and it will return callback with PermissionStatus

```
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
            .handle(this) { permissionStatus ->
                Toast.makeText(this, permissionStatus.name, Toast.LENGTH_SHORT).show()
            }
    }

    private fun requestCameraAndStoragePermission() {
        SoftPermissions.requiredMultiplePermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
            .handle(this) { permissionMap, allPermissionsGranted ->
                if (allPermissionsGranted) {
                     Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG).show()
                } else {
                     Toast.makeText(this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
                }
            } 
    }
}

```
