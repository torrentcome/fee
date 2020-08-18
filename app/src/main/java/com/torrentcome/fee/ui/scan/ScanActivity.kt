package com.torrentcome.fee.ui.scan

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.polidea.rxandroidble2.scan.ScanResult
import com.torrentcome.fee.R
import com.torrentcome.fee.ui.co.CoActivity
import com.torrentcome.fee.utils.isLocationPermissionGranted
import com.torrentcome.fee.utils.showSnackbarShort
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_scan.*
import kotlinx.android.synthetic.main.include_progress_view.*

@AndroidEntryPoint
class ScanActivity : AppCompatActivity(R.layout.activity_scan) {

    private val viewModel: ScanViewModel by viewModels()

    private val resultsAdapter = ScanAdapter {
        goToCo(it)
        viewModel.stopScan()
    }

    private fun goToCo(it: ScanResult) {
        startActivity(
            CoActivity.launch(
                this,
                it.bleDevice.macAddress,
                it.bleDevice.name.toString()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(scan_results) {
            setHasFixedSize(false)
            adapter = resultsAdapter
        }

        viewModel.scan.observe(this, Observer {
            when (it) {
                is ScanViewModel.Command.Empty -> {
                    progress.visibility = View.VISIBLE
                }
                is ScanViewModel.Command.Success -> {
                    progress.visibility = View.GONE
                    println(it.res.toString())
                    resultsAdapter.addScanResult(it.res)
                    if (it.res.bleDevice.macAddress == "E4:3F:39:A0:71:EB") {
                        goToCo(it.res)
                        viewModel.stopScan()
                    }
                }
                is ScanViewModel.Command.Fail -> {
                    progress.visibility = View.GONE
                    it.message.message?.let { it1 -> showSnackbarShort(it1) }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (!viewModel.isScanRuntimePermissionGranted())
            viewModel.requestPerm(this)
        else {
            viewModel.scan()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (isLocationPermissionGranted(requestCode, grantResults)) {
            viewModel.scan()
        }
    }
}