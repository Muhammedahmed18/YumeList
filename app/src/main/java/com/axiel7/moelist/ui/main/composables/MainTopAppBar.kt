package com.axiel7.moelist.ui.main.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.Route

@Composable
fun MainTopAppBar(
    isLoggedIn: Boolean,
    profilePicture: String?,
    isVisible: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier,
    showSort: Boolean = false,
    onSortClick: (() -> Unit)? = null,
    topBarOffsetY: Animatable<Float, AnimationVector1D>? = null,
    topBarHeightPx: Float = 0f,
) {
    val collapseFraction = if (topBarHeightPx > 0 && topBarOffsetY != null) {
        (topBarOffsetY.value / -topBarHeightPx).coerceIn(0f, 1f)
    } else {
        0f
    }

    val horizontalPadding by animateDpAsState(
        targetValue = lerp(16.dp, 0.dp, collapseFraction),
        label = "TopBarPadding"
    )
    val bottomPadding by animateDpAsState(
        targetValue = lerp(4.dp, 0.dp, collapseFraction),
        label = "TopBarBottomPadding"
    )
    val cornerRadius by animateDpAsState(
        targetValue = lerp(28.dp, 0.dp, collapseFraction),
        label = "TopBarCorner"
    )
    val elevation by animateDpAsState(
        targetValue = lerp(2.dp, 0.dp, collapseFraction),
        label = "TopBarElevation"
    )

    AnimatedContent(
        targetState = isVisible,
        transitionSpec = {
            slideInVertically(initialOffsetY = { -it }) togetherWith
                    slideOutVertically(targetOffsetY = { -it })
        },
        label = "TopAppBarVisibility"
    ) { visible ->
        if (visible) {
            Card(
                onClick = dropUnlessResumed { navController.navigate(Route.Search()) },
                modifier = modifier
                    .statusBarsPadding()
                    .graphicsLayer {
                        translationY = if (topBarOffsetY != null) {
                             // Limit translationY to avoid sliding out completely if needed, 
                             // but we already use 0.99f in collapsable.
                             // To make it look "pinned", we can just NOT translate it or translate less.
                             // If we want it to morph AND stay at the top, we should use translationY = 0.
                             // But the current logic uses translationY in MainActivity.
                             0f 
                        } else 0f
                    }
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = horizontalPadding)
                    .padding(bottom = bottomPadding),
                shape = RoundedCornerShape(cornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = elevation
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_search_24),
                        contentDescription = "search",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.search),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (showSort) {
                        IconButton(
                            onClick = { onSortClick?.invoke() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_round_sort_24),
                                contentDescription = "sort",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (isLoggedIn) {
                        AsyncImage(
                            model = profilePicture,
                            contentDescription = "profile",
                            placeholder = painterResource(R.drawable.ic_round_account_circle_24),
                            error = painterResource(R.drawable.ic_round_account_circle_24),
                            modifier = Modifier
                                .clip(RoundedCornerShape(100))
                                .size(32.dp)
                                .clickable { navController.navigate(Route.Profile) }
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_account_circle_24),
                            contentDescription = "profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(100))
                                .size(32.dp)
                                .clickable { navController.navigate(Route.Profile) }
                        )
                    }
                }
            }//:Card
        } else {
            Box(modifier = Modifier.fillMaxWidth())
        }
    }
}
