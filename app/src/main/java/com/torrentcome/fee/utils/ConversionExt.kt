package com.torrentcome.fee.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

fun String.toUUID(): UUID = UUID.fromString(this)

fun Int.intToUInt8(): ByteArray {
    val bytes = ByteArray(4)
    ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putInt(this and 0xff)
    return bytes.copyOfRange(0, 1)
}
