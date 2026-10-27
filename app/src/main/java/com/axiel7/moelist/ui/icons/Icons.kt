/*
 * Copyright 2020 The Android Open Source Project
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

package com.axiel7.moelist.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.DefaultFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/*
 * YumeList's own copy of the Material icons it actually uses (Phase 7), replacing the
 * material-icons-extended library (~36 MB of icons, ~11,000 files) with ~110 small files.
 *
 * Same API as androidx.compose.material.icons: call sites still write `Icons.Rounded.Star`,
 * only the imports point here. The icon files in the sub-packages are copied unchanged from
 * material-icons 1.7.8 (Apache 2.0, notices kept).
 *
 * To add an icon: copy its file from the material-icons-extended sources (or write the
 * ImageVector) into the matching folder, and change its package/imports to this one.
 */
object Icons {
    object Filled
    object Outlined
    object Rounded

    object AutoMirrored {
        object Filled
        object Outlined
        object Rounded

        /** Alias for [AutoMirrored.Filled]. */
        val Default = Filled
    }

    /** Alias for [Filled], the baseline icon theme. */
    val Default = Filled
}

inline fun materialIcon(
    name: String,
    autoMirror: Boolean = false,
    block: ImageVector.Builder.() -> ImageVector.Builder
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = MaterialIconDimension.dp,
    defaultHeight = MaterialIconDimension.dp,
    viewportWidth = MaterialIconDimension,
    viewportHeight = MaterialIconDimension,
    autoMirror = autoMirror
).block().build()

inline fun ImageVector.Builder.materialPath(
    fillAlpha: Float = 1f,
    strokeAlpha: Float = 1f,
    pathFillType: PathFillType = DefaultFillType,
    pathBuilder: PathBuilder.() -> Unit
) = path(
    fill = SolidColor(Color.Black),
    fillAlpha = fillAlpha,
    stroke = null,
    strokeAlpha = strokeAlpha,
    strokeLineWidth = 1f,
    strokeLineCap = StrokeCap.Butt,
    strokeLineJoin = StrokeJoin.Bevel,
    strokeLineMiter = 1f,
    pathFillType = pathFillType,
    pathBuilder = pathBuilder
)

// All Material icons are 24dp by 24dp, with a 24 by 24 viewport
@PublishedApi
internal const val MaterialIconDimension = 24f
