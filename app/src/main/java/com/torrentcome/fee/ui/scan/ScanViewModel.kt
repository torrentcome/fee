package com.torrentcome.fee.ui.scan

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import com.torrentcome.fee.utils.requestLocationPermission
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class ScanViewModel @ViewModelInject constructor(private val bleClient: RxBleClient) :
    ViewModel() {

    sealed class Command {
        object Empty : Command()
        data class Fail(val message: Throwable) : Command()
        data class Success(val res: ScanResult) : Command()
    }

    // subscription
    private var scanDisposable: Disposable? = null

    // mutable
    private val _scan = MutableLiveData<Command>()

    // visible
    val scan: LiveData<Command> = _scan

    init {
        _scan.postValue(Command.Empty)
    }

    override fun onCleared() {
        super.onCleared()
        scanDisposable?.dispose()
    }

    fun requestPerm(activity: ScanActivity) = activity.requestLocationPermission(bleClient)

    fun isScanRuntimePermissionGranted() = bleClient.isScanRuntimePermissionGranted

    fun scan() {
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

        val scanFilter = ScanFilter.Builder().build()

        return bleClient.scanBleDevices(scanSettings, scanFilter)
            .filter { !it.bleDevice.name.isNullOrEmpty() }
            .observeOn(Schedulers.io())
            .subscribe({
                _scan.postValue(Command.Success(it))
            }, {
                _scan.postValue(Command.Fail(it))
            })
            .let { scanDisposable = it }
    }

    fun stopScan() {
        scanDisposable?.dispose()
    }
}