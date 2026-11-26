package com.itn.securebrowser

import androidx.compose.runtime.staticCompositionLocalOf

enum class DesignVariant { FULL, FAITHFUL }

val LocalDesignVariant = staticCompositionLocalOf { DesignVariant.FULL }
