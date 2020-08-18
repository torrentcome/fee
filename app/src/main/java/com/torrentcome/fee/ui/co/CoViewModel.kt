package com.torrentcome.fee.ui.co

import android.bluetooth.BluetoothGattService
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.polidea.rxandroidble2.*
import com.torrentcome.fee.utils.toUUID
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*


class CoViewModel @ViewModelInject constructor(private val bleClient: RxBleClient) :
    ViewModel() {

    sealed class Command {
        object State {
            object Empty : Command()
            data class Fail(val message: Throwable) : Command()
            data class Success(val res: RxBleConnection.RxBleConnectionState) : Command()
        }

        object Connection {
            data class Fail(val message: Throwable) : Command()
            data class Success(val res: RxBleConnection) : Command()
        }

        object Discover {
            data class Fail(val message: Throwable) : Command()
            data class Success(val res1: RxBleConnection, val res: RxBleDeviceServices) : Command()
        }

        object Service {
            data class Fail(val message: Throwable) : Command()
            data class Success(val res: BluetoothGattService) : Command()
        }

        object Characteristic {
            data class Fail(val message: Throwable, val uuid: String) : Command()
            data class Success(val res: ByteArray, val uuid: String, val type: Int?) : Command()
        }
    }

    // subscription
    private var stateDisposable: Disposable? = null
    private var connectionDisposable = CompositeDisposable()
    private var discoverDisposable = CompositeDisposable()
    private var serviceDisposable = CompositeDisposable()
    private var characteristicsDisposable = CompositeDisposable()

    // mutable
    private val _state = MutableLiveData<Command>()
    private val _connection = MutableLiveData<Command>()
    private val _discover = MutableLiveData<Command>()
    private val _service = MutableLiveData<Command>()
    private val _characteristic = MutableLiveData<Command>()

    // visible
    val state: LiveData<Command> = _state
    val connection: LiveData<Command> = _connection
    val discover: LiveData<Command> = _discover
    val service: LiveData<Command> = _service
    val characteristic: LiveData<Command> = _characteristic

    init {
        _state.postValue(Command.State.Empty)
    }

    fun getBleDevice(macAddress: String?): RxBleDevice? {
        if (macAddress.isNullOrEmpty()) {
            _state.postValue(Command.State.Fail(Exception("mac address is null, connection is not possible")))
        }
        return bleClient.getBleDevice(macAddress!!)
    }

    fun lookAt(device: RxBleDevice?) {
        if (device == null) {
            _state.postValue(Command.State.Fail(Exception("rx ble device is null, connection is not possible")))
        }
        device!!.observeConnectionStateChanges()
            .observeOn(Schedulers.io())
            .subscribe { _state.postValue(Command.State.Success(it)) }
            .let { stateDisposable = it }
    }

    fun tryToConnectWith(device: RxBleDevice?) {
        when (device?.connectionState) {
            RxBleConnection.RxBleConnectionState.CONNECTED -> {
                connectionDisposable.clear()
            }
            RxBleConnection.RxBleConnectionState.CONNECTING -> {
            }
            RxBleConnection.RxBleConnectionState.DISCONNECTING -> {
            }
            null -> {
            }
            RxBleConnection.RxBleConnectionState.DISCONNECTED -> {
                device.establishConnection(false)
                    .observeOn(Schedulers.io())
                    .subscribe({
                        _connection.postValue(Command.Connection.Success(it))
                    }, {
                        _connection.postValue(Command.Connection.Fail(it))
                    })
                    .let { connectionDisposable.add(it) }
            }
        }
    }

    fun discoverService(rxBleConnection: RxBleConnection) {
        rxBleConnection.discoverServices()
            .observeOn(Schedulers.io())
            .subscribe({
                _discover.postValue(Command.Discover.Success(rxBleConnection, it))
            }, {
                _discover.postValue(Command.Discover.Fail(it))
            }
            ).let { discoverDisposable.add(it) }
    }

    fun notificationFromService(
        connectionD: RxBleConnection,
        uuid: String,
        type: Int? = null,
        callback: ((ByteArray, String) -> Unit)? = null
    ) {
        connectionD.setupNotification(UUID.fromString(uuid))
            .doOnNext { notificationO -> println("$notificationO has been setup") }
            .flatMap { it }
            .subscribe({
                _characteristic.postValue(Command.Characteristic.Success(it, uuid, type))
                callback?.invoke(it, uuid)
            }, {
                _characteristic.postValue(Command.Characteristic.Fail(it, uuid))
            })
            .let { characteristicsDisposable.add(it) }
    }

    fun writeAndReadOnNotification(
        rxBleConnection: RxBleConnection,
        readOn: String,
        writeTo: String,
        bytesToWrite: ByteArray
    ) {
        rxBleConnection.writeCharacteristic(writeTo.toUUID(), bytesToWrite)
            .flatMapObservable {
                rxBleConnection.setupNotification(
                    readOn.toUUID(),
                    NotificationSetupMode.COMPAT
                )
            }
            .flatMap { it }
            .observeOn(Schedulers.io())
            .subscribe()
            .let { characteristicsDisposable.add(it) }
    }

    fun multiWrite(
        rxBleConnection: RxBleConnection,
        writeTo: String,
        bytesToWrite: ByteArray,
        bytesToWrite1: ByteArray,
        bytesToWrite2: ByteArray
    ) {
        rxBleConnection.writeCharacteristic(writeTo.toUUID(), bytesToWrite)
            .flatMap { rxBleConnection.writeCharacteristic(writeTo.toUUID(), bytesToWrite1) }
            .flatMap { rxBleConnection.writeCharacteristic(writeTo.toUUID(), bytesToWrite2) }
            .observeOn(Schedulers.io())
            .subscribe(
            ).let { characteristicsDisposable.add(it) }
    }

    override fun onCleared() {
        super.onCleared()
        connectionDisposable.dispose()
        stateDisposable?.dispose()
        serviceDisposable.dispose()
        discoverDisposable.dispose()
        characteristicsDisposable.dispose()
    }


}