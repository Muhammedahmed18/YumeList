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

public val Icons.Rounded.PlayCircleOutline: ImageVector
    get() {
        if (_playCircleOutline != null) {
            return _playCircleOutline!!
        }
        _playCircleOutline = materialIcon(name = "Rounded.PlayCircleOutline") {
            materialPath {
                moveTo(10.8f, 15.9f)
                lineToRelative(4.67f, -3.5f)
                curveToRelative(0.27f, -0.2f, 0.27f, -0.6f, 0.0f, -0.8f)
                lineTo(10.8f, 8.1f)
                curveToRelative(-0.33f, -0.25f, -0.8f, -0.01f, -0.8f, 0.4f)
                verticalLineToRelative(7.0f)
                curveToRelative(0.0f, 0.41f, 0.47f, 0.65f, 0.8f, 0.4f)
                close()
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.41f, 0.0f, -8.0f, -3.59f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.59f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.59f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.59f, 8.0f, -8.0f, 8.0f)
                close()
            }
        }
        return _playCircleOutline!!
    }

private var _playCircleOutline: ImageVector? = null
