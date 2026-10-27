/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.axiel7.moelist.ui.icons.rounded

import androidx.compose.ui.graphics.vector.ImageVector
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.materialIcon
import com.axiel7.moelist.ui.icons.materialPath

public val Icons.Rounded.Autorenew: ImageVector
    get() {
        if (_autorenew != null) {
            return _autorenew!!
        }
        _autorenew = materialIcon(name = "Rounded.Autorenew") {
            materialPath {
                moveTo(12.0f, 6.0f)
                verticalLineToRelative(1.79f)
                curveToRelative(0.0f, 0.45f, 0.54f, 0.67f, 0.85f, 0.35f)
                lineToRelative(2.79f, -2.79f)
                curveToRelative(0.2f, -0.2f, 0.2f, -0.51f, 0.0f, -0.71f)
                lineToRelative(-2.79f, -2.79f)
                curveToRelative(-0.31f, -0.31f, -0.85f, -0.09f, -0.85f, 0.36f)
                lineTo(12.0f, 4.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, 3.58f, -8.0f, 8.0f)
                curveToRelative(0.0f, 1.04f, 0.2f, 2.04f, 0.57f, 2.95f)
                curveToRelative(0.27f, 0.67f, 1.13f, 0.85f, 1.64f, 0.34f)
                curveToRelative(0.27f, -0.27f, 0.38f, -0.68f, 0.23f, -1.04f)
                curveTo(6.15f, 13.56f, 6.0f, 12.79f, 6.0f, 12.0f)
                curveToRelative(0.0f, -3.31f, 2.69f, -6.0f, 6.0f, -6.0f)
                close()
                moveTo(17.79f, 8.71f)
                curveToRelative(-0.27f, 0.27f, -0.38f, 0.69f, -0.23f, 1.04f)
                curveToRelative(0.28f, 0.7f, 0.44f, 1.46f, 0.44f, 2.25f)
                curveToRelative(0.0f, 3.31f, -2.69f, 6.0f, -6.0f, 6.0f)
                verticalLineToRelative(-1.79f)
                curveToRelative(0.0f, -0.45f, -0.54f, -0.67f, -0.85f, -0.35f)
                lineToRelative(-2.79f, 2.79f)
                curveToRelative(-0.2f, 0.2f, -0.2f, 0.51f, 0.0f, 0.71f)
                lineToRelative(2.79f, 2.79f)
                curveToRelative(0.31f, 0.31f, 0.85f, 0.09f, 0.85f, -0.35f)
                lineTo(12.0f, 20.0f)
                curveToRelative(4.42f, 0.0f, 8.0f, -3.58f, 8.0f, -8.0f)
                curveToRelative(0.0f, -1.04f, -0.2f, -2.04f, -0.57f, -2.95f)
                curveToRelative(-0.27f, -0.67f, -1.13f, -0.85f, -1.64f, -0.34f)
                close()
            }
        }
        return _autorenew!!
    }

private var _autorenew: ImageVector? = null
