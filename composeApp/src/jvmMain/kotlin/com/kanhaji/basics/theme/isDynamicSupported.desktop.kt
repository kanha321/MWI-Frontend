package com.kanhaji.basics.theme

// only windows 10 and windows 11 currently support dynamic color themes
actual fun isDynamicColorSupported(): Boolean {
    val osName = System.getProperty("os.name")?.lowercase() ?: return false
    val osVersion = System.getProperty("os.version") ?: return false

    if (!osName.contains("windows")) return false

    return osVersion.startsWith("10.") || osVersion.startsWith("11.")
}