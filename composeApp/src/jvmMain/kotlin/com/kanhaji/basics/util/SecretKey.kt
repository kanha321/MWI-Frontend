package com.kanhaji.basics.util

import javax.crypto.spec.SecretKeySpec

object SecretKey {
    fun get() = SecretKeySpec("keyPlaceholder12".toByteArray(), "AES")
}