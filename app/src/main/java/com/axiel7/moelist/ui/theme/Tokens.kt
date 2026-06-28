package com.axiel7.moelist.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Spacing — 4dp grid
val Spacing4 = 4.dp
val Spacing8 = 8.dp
val Spacing12 = 12.dp
val Spacing16 = 16.dp
val Spacing20 = 20.dp
val Spacing24 = 24.dp
val Spacing32 = 32.dp
val Spacing48 = 48.dp

// Shape scale — aligned to M3 shape tokens
// None=0, ExtraSmall=4, Small=8, Medium=12, Large=16, ExtraLarge=28, Full=Circle
val ShapeExtraSmall = RoundedCornerShape(4.dp)
val ShapeSmall = RoundedCornerShape(8.dp)
val ShapeMedium = RoundedCornerShape(12.dp)
val ShapeLarge = RoundedCornerShape(16.dp)
val ShapeExtraLarge = RoundedCornerShape(28.dp)
val ShapeFull = CircleShape

// Semantic shape aliases — use these in components instead of raw dp values
val ShapeCard = ShapeLarge          // Root card containers
val ShapePoster = ShapeMedium       // Media poster images
val ShapeChip = ShapeFull           // Badges, chips, pills
val ShapeButton = ShapeFull         // Icon buttons, action buttons
val ShapeProgressBar = ShapeFull    // Progress bar clip
