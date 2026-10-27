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

public val Icons.Rounded.AutoAwesome: ImageVector
    get() {
        if (_autoAwesome != null) {
            return _autoAwesome!!
        }
        _autoAwesome = materialIcon(name = "Rounded.AutoAwesome") {
            materialPath {
                moveTo(19.46f, 8.0f)
                lineToRelative(0.79f, -1.75f)
                lineTo(22.0f, 5.46f)
                curveToRelative(0.39f, -0.18f, 0.39f, -0.73f, 0.0f, -0.91f)
                lineToRelative(-1.75f, -0.79f)
                lineTo(19.46f, 2.0f)
                curveToRelative(-0.18f, -0.39f, -0.73f, -0.39f, -0.91f, 0.0f)
                lineToRelative(-0.79f, 1.75f)
                lineTo(16.0f, 4.54f)
                curveToRelative(-0.39f, 0.18f, -0.39f, 0.73f, 0.0f, 0.91f)
                lineToRelative(1.75f, 0.79f)
                lineTo(18.54f, 8.0f)
                curveTo(18.72f, 8.39f, 19.28f, 8.39f, 19.46f, 8.0f)
                close()
                moveTo(11.5f, 9.5f)
                lineTo(9.91f, 6.0f)
                curveTo(9.56f, 5.22f, 8.44f, 5.22f, 8.09f, 6.0f)
                lineTo(6.5f, 9.5f)
                lineTo(3.0f, 11.09f)
                curveToRelative(-0.78f, 0.36f, -0.78f, 1.47f, 0.0f, 1.82f)
                lineToRelative(3.5f, 1.59f)
                lineTo(8.09f, 18.0f)
                curveToRelative(0.36f, 0.78f, 1.47f, 0.78f, 1.82f, 0.0f)
                lineToRelative(1.59f, -3.5f)
                lineToRelative(3.5f, -1.59f)
                curveToRelative(0.78f, -0.36f, 0.78f, -1.47f, 0.0f, -1.82f)
                lineTo(11.5f, 9.5f)
                close()
                moveTo(18.54f, 16.0f)
                lineToRelative(-0.79f, 1.75f)
                lineTo(16.0f, 18.54f)
                curveToRelative(-0.39f, 0.18f, -0.39f, 0.73f, 0.0f, 0.91f)
                lineToRelative(1.75f, 0.79f)
                lineTo(18.54f, 22.0f)
                curveToRelative(0.18f, 0.39f, 0.73f, 0.39f, 0.91f, 0.0f)
                lineToRelative(0.79f, -1.75f)
                lineTo(22.0f, 19.46f)
                curveToRelative(0.39f, -0.18f, 0.39f, -0.73f, 0.0f, -0.91f)
                lineToRelative(-1.75f, -0.79f)
                lineTo(19.46f, 16.0f)
                curveTo(19.28f, 15.61f, 18.72f, 15.61f, 18.54f, 16.0f)
                close()
            }
        }
        return _autoAwesome!!
    }

private var _autoAwesome: ImageVector? = null
