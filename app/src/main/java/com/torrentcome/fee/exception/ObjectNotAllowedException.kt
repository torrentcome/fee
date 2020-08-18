package com.torrentcome.fee.exception

class ObjectNotAllowedException(private val javaClass: Class<Any>?) : Exception() {
    override val message: String?
        get() = javaClass.toString() + "<-> Object not allowed to observe here, please check your viewModel"
}