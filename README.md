# SoftPermissions [![Android CI](https://github.com/privin-dolleth-reyner/SoftPermissions/actions/workflows/android.yml/badge.svg?branch=main&event=release)](https://github.com/privin-dolleth-reyner/SoftPermissions/actions/workflows/android.yml) ![Release](https://jitpack.io/v/privin-dolleth-reyner/SoftPermissions.svg)



A library to handle android permissions.

## Installation

SoftPermissions is installed by adding the following dependency to your ```build.gradle``` file:

```
dependencies {
	implementation 'com.github.privin-dolleth-reyner:SoftPermissions:1.0.1'
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

## Usage

SoftPermssions can be simply called with required permissions, and it will return callback with PermissionStatus
The following code can be called from activity/fragment

```
SoftPermissions.requiredPermission(Manifest.permission.CAMERA)
            .handle(this) { permissionStatus ->
                Toast.makeText(this, permissionStatus.name, Toast.LENGTH_SHORT).show()
            }
```
To handle multiple permissions, use the below code
```
SoftPermissions.requiredMultiplePermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
            .handle(this) { permissionMap, allPermissionsGranted ->
                if (allPermissionsGranted) {
                    Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Some permissions are denied", Toast.LENGTH_LONG).show()
                }
            }
```

To handle denied permissions chain ```handlePermanentlyDeniedPermission()```
```
SoftPermissions.requiredPermission(Manifest.permission.CAMERA)
	    .handlePermanentlyDeniedPermission(getString(R.string.request_camera_msg))
            .handle(this) { permissionStatus ->
                Toast.makeText(this, permissionStatus.name, Toast.LENGTH_SHORT).show()
            }
```
The above code will show a snackbar to user with message and an action to navigate user to app details settings page. (if permission is denied twice)

You can customize the snackBar's backgroundColor,textColor, actionTextColor. If you want to retain snackBar config throught the app then you can pass a boolean.
```
SoftPermissions.requiredPermission(Manifest.permission.CAMERA)
            .handlePermanentlyDeniedPermission(getString(R.string.request_camera_msg))
            .snackBarConfig(ContextCompat.getColor(this, R.color.black),
                ContextCompat.getColor(this, R.color.white),
                ContextCompat.getColor(this, R.color.purple_200),
            true)
            .handle(this) { permissionStatus ->
                Toast.makeText(this, permissionStatus.name, Toast.LENGTH_SHORT).show()
            }
```

## License

```
   Copyright 2022 Privin Dolleth Reyner

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
