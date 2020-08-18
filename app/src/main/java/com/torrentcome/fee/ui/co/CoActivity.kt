package com.torrentcome.fee.ui.co

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.helpers.ValueInterpreter
import com.torrentcome.fee.R
import com.torrentcome.fee.exception.ObjectNotAllowedException
import com.torrentcome.fee.utils.*
import com.torrentcome.fee.utils.showSnackbarShort
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_co.*
import kotlinx.android.synthetic.main.include_progress_view.*
import java.util.*

private const val MAC_ADDRESS = "mac_address"
private const val NAME: String = "name"

private var UUID_BATTERY = UUID.randomUUID().toString()
private var UUID_REQUEST = UUID.randomUUID().toString()

private var UUID_FRAME_STRIDE_01 = UUID.randomUUID().toString()

private var ENABLE_VERSION = 5
private var ENABLE_CALIBRE = 3
private var ENABLE_STRIDE_EVENT = 15

@AndroidEntryPoint
class CoActivity : AppCompatActivity(R.layout.activity_co) {

    private val viewModel: CoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val macAddress = intent.getStringExtra(MAC_ADDRESS)
        val name = intent.getStringExtra(NAME)
        subtitle.text = name

        val drawPlay = ContextCompat.getDrawable(this, R.drawable.ic_baseline_play_arrow_24)
        val drawStop = ContextCompat.getDrawable(this, R.drawable.ic_baseline_stop_24)

        val bleDevice = viewModel.getBleDevice(macAddress)

        viewModel.lookAt(bleDevice)
        viewModel.state.observe(this, Observer { command ->
            when (command) {
                CoViewModel.Command.State.Empty -> {
                    progress.visible()
                }
                is CoViewModel.Command.State.Fail -> {
                    progress.gone()

                    showSnackbarShort(command.message.message)
                }
                is CoViewModel.Command.State.Success -> {
                    progress.gone()

                    connect_toggle.text = command.res.name
                    when (command.res) {
                        RxBleConnection.RxBleConnectionState.CONNECTED -> {
                            setUpToggleUI(drawStop, Color.GREEN)
                        }
                        RxBleConnection.RxBleConnectionState.CONNECTING -> {
                            setUpToggleUI(drawStop, Color.BLUE)
                        }
                        RxBleConnection.RxBleConnectionState.DISCONNECTED -> {
                            setUpToggleUI(drawPlay, Color.GRAY)
                        }
                        RxBleConnection.RxBleConnectionState.DISCONNECTING -> {
                            setUpToggleUI(drawPlay, Color.RED)
                        }
                    }
                }
                else -> throw ObjectNotAllowedException(command.javaClass)
            }
        })

        viewModel.tryToConnectWith(bleDevice)

        connect_toggle.setOnClickListener {
            viewModel.tryToConnectWith(bleDevice)
        }

        viewModel.connection.observe(this, Observer { command ->
            when (command) {
                is CoViewModel.Command.Connection.Fail -> {
                    showSnackbarShort(command.message.message)
                }
                is CoViewModel.Command.Connection.Success -> {
                    viewModel.discoverService(command.res)
                }
                else -> throw ObjectNotAllowedException(command.javaClass)

            }
        })

        viewModel.discover.observe(this, Observer { command ->
            when (command) {
                is CoViewModel.Command.Discover.Fail -> {
                    showSnackbarShort(command.message.message)
                }
                is CoViewModel.Command.Discover.Success -> {
                    onSuccess(command.res1)
                }
                else -> throw ObjectNotAllowedException(command.javaClass)
            }
        })

        viewModel.service.observe(this, Observer { command ->
            when (command) {
                is CoViewModel.Command.Service.Fail -> {
                    showSnackbarShort(command.message.message)
                }
                is CoViewModel.Command.Service.Success -> {
                }
                else -> throw ObjectNotAllowedException(command.javaClass)
            }
        })

        viewModel.characteristic.observe(this, Observer { command ->
            when (command) {
                is CoViewModel.Command.Characteristic.Success -> {
                    when (command.uuid) {
                        UUID_BATTERY -> {
                            battery.text = "" + ValueInterpreter.getIntValue(
                                command.res,
                                ValueInterpreter.FORMAT_SINT32,
                                0
                            ) / 10
                            voltage.text = format(command, ValueInterpreter.FORMAT_SINT32)
                        }
                        UUID_REQUEST -> {
                            val res = command.res
                            when (valuInt8(res, 0)) {
                                ENABLE_VERSION -> version.text = "" + valuInt8(res, 1)
                                ENABLE_CALIBRE -> {
                                    val intValue0 = valuInt8(res, 0)
                                    val intValue = valuInt8(res, 1)

                                    first_calibre.text = "[id = $intValue0][va = $intValue]"
                                }
                                ENABLE_STRIDE_EVENT -> first_stride.text = format(command)

                                else -> showSnackbarShort("id = " + valuInt8(res, 0))
                            }
                        }
                        UUID_FRAME_STRIDE_01 -> {
                            first_stride.text = format(command)
                        }
                    }
                }
                is CoViewModel.Command.Characteristic.Fail -> {
                    when (command.uuid) {
                        UUID_BATTERY -> battery.text = command.message.toString()
                        UUID_REQUEST -> version.text = command.message.toString()
                    }
                }
                else -> throw ObjectNotAllowedException(command.javaClass)
            }
        })
    }

    private fun format(command: CoViewModel.Command.Characteristic.Success): String {
        return "" + ValueInterpreter.getIntValue(
            command.res,
            ValueInterpreter.FORMAT_SINT32,
            4
        )
    }


    private fun format(command: CoViewModel.Command.Characteristic.Success, valueIn : Int): String {
        return "id = [" + valuInt8(command.res, 0) + "] and value = [" + ValueInterpreter.getIntValue(
            command.res,
            valueIn,
            1
        ) + "]"
    }

    private fun valuInt8(res: ByteArray, index: Int): Int? {
        return ValueInterpreter.getIntValue(
            res,
            ValueInterpreter.FORMAT_UINT8,
            index
        )
    }

    private fun onSuccess(bleConnection: RxBleConnection) {
        viewModel.notificationFromService(bleConnection, UUID_BATTERY)
        viewModel.notificationFromService(bleConnection, UUID_REQUEST)

        viewModel.multiWrite(
            bleConnection,
            UUID_REQUEST,
            ENABLE_VERSION.intToUInt8(),
            ENABLE_CALIBRE.intToUInt8(),
            ENABLE_STRIDE_EVENT.intToUInt8()
        )

        start_calibre.visible()
        start_calibre.setOnClickListener {
            viewModel.writeAndReadOnNotification(
                bleConnection,
                UUID_REQUEST,
                UUID_REQUEST,
                ENABLE_CALIBRE.intToUInt8()
            )
        }
    }

    private fun setUpToggleUI(drawStop: Drawable?, color: Int) {
        connect_toggle.setBackgroundColor(color)
        connect_toggle.setCompoundDrawablesWithIntrinsicBounds(
            drawStop,
            null,
            null,
            null
        )
    }

    companion object {
        fun launch(
            context: Context,
            macAddress: String,
            name: String
        ) = Intent(context, CoActivity::class.java).apply {
            putExtra(MAC_ADDRESS, macAddress)
            putExtra(NAME, name)
        }
    }
}