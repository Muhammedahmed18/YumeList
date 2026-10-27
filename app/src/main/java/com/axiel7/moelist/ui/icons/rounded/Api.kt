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

public val Icons.Rounded.Api: ImageVector
    get() {
        if (_api != null) {
            return _api!!
        }
        _api = materialIcon(name = "Rounded.Api") {
            materialPath {
                moveTo(13.0f, 13.0f)
                lineTo(13.0f, 13.0f)
                curveToRelative(-0.56f, 0.56f, -1.45f, 0.56f, -2.0f, 0.01f)
                lineTo(11.0f, 13.0f)
                curveToRelative(-0.55f, -0.55f, -0.55f, -1.44f, 0.0f, -1.99f)
                lineTo(11.0f, 11.0f)
                curveToRelative(0.55f, -0.55f, 1.44f, -0.55f, 1.99f, 0.0f)
                lineTo(13.0f, 11.0f)
                curveTo(13.55f, 11.55f, 13.55f, 12.45f, 13.0f, 13.0f)
                close()
                moveTo(12.0f, 6.0f)
                lineToRelative(2.12f, 2.12f)
                lineToRelative(2.5f, -2.5f)
                lineToRelative(-3.2f, -3.2f)
                curveToRelative(-0.78f, -0.78f, -2.05f, -0.78f, -2.83f, 0.0f)
                lineToRelative(-3.2f, 3.2f)
                lineToRelative(2.5f, 2.5f)
                lineTo(12.0f, 6.0f)
                close()
                moveTo(6.0f, 12.0f)
                lineToRelative(2.12f, -2.12f)
                lineToRelative(-2.5f, -2.5f)
                lineToRelative(-3.2f, 3.2f)
                curveToRelative(-0.78f, 0.78f, -0.78f, 2.05f, 0.0f, 2.83f)
                lineToRelative(3.2f, 3.2f)
                lineToRelative(2.5f, -2.5f)
                lineTo(6.0f, 12.0f)
                close()
                moveTo(18.0f, 12.0f)
                lineToRelative(-2.12f, 2.12f)
                lineToRelative(2.5f, 2.5f)
                lineToRelative(3.2f, -3.2f)
                curveToRelative(0.78f, -0.78f, 0.78f, -2.05f, 0.0f, -2.83f)
                lineToRelative(-3.2f, -3.2f)
                lineToRelative(-2.5f, 2.5f)
                lineTo(18.0f, 12.0f)
                close()
                moveTo(12.0f, 18.0f)
                lineToRelative(-2.12f, -2.12f)
                lineToRelative(-2.5f, 2.5f)
                lineToRelative(3.2f, 3.2f)
                curveToRelative(0.78f, 0.78f, 2.05f, 0.78f, 2.83f, 0.0f)
                lineToRelative(3.2f, -3.2f)
                lineToRelative(-2.5f, -2.5f)
                lineTo(12.0f, 18.0f)
                close()
            }
        }
        return _api!!
    }

private var _api: ImageVector? = null
