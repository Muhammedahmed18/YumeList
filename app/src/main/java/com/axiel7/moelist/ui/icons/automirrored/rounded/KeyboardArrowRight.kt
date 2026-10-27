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

package com.axiel7.moelist.ui.icons.automirrored.rounded

import androidx.compose.ui.graphics.vector.ImageVector
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.materialIcon
import com.axiel7.moelist.ui.icons.materialPath

public val Icons.AutoMirrored.Rounded.KeyboardArrowRight: ImageVector
    get() {
        if (_keyboardArrowRight != null) {
            return _keyboardArrowRight!!
        }
        _keyboardArrowRight = materialIcon(name = "AutoMirrored.Rounded.KeyboardArrowRight",
                autoMirror = true) {
            materialPath {
                moveTo(9.29f, 15.88f)
                lineTo(13.17f, 12.0f)
                lineTo(9.29f, 8.12f)
                curveToRelative(-0.39f, -0.39f, -0.39f, -1.02f, 0.0f, -1.41f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
                lineToRelative(4.59f, 4.59f)
                curveToRelative(0.39f, 0.39f, 0.39f, 1.02f, 0.0f, 1.41f)
                lineTo(10.7f, 17.3f)
                curveToRelative(-0.39f, 0.39f, -1.02f, 0.39f, -1.41f, 0.0f)
                curveToRelative(-0.38f, -0.39f, -0.39f, -1.03f, 0.0f, -1.42f)
                close()
            }
        }
        return _keyboardArrowRight!!
    }

private var _keyboardArrowRight: ImageVector? = null
