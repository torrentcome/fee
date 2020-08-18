package com.torrentcome.fee.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.polidea.rxandroidble2.RxBleClient

const val REQUEST_PERMISSION_BLE_SCAN = 101

fun Activity.requestLocationPermission(bleClient: RxBleClient) {
        ActivityCompat.requestPermissions(
            this,
            /*
             * the below would cause a ArrayIndexOutOfBoundsException on API < 23. Yet it should not be called then as runtime
             * permissions are not needed and RxBleClient.isScanRuntimePermissionGranted() returns `true`
             */
            arrayOf(bleClient.recommendedScanRuntimePermissions[0]),
            REQUEST_PERMISSION_BLE_SCAN
        )
    }

fun isLocationPermissionGranted(requestCode: Int, grantResults: IntArray) =
        requestCode == REQUEST_PERMISSION_BLE_SCAN && grantResults[0] == PackageManager.PERMISSION_GRANTED