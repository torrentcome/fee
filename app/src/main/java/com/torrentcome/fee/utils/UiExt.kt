package com.torrentcome.fee.utils

import android.app.Activity
import android.view.View
import com.google.android.material.snackbar.Snackbar

internal fun Activity.showSnackbarShort(text: CharSequence?) {
    text?.let {
        Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show()
    }
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}