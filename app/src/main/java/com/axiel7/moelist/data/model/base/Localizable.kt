package com.axiel7.moelist.data.model.base

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

interface Localizable {
    @get:StringRes
    val labelRes: Int
        get() = 0

    @Composable
    fun localized(): String = if (labelRes != 0) stringResource(labelRes) else ""
}
