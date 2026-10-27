package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.ui.base.TabRowItem
import kotlinx.coroutines.launch

/**
 * Status selector for the user list (Watching, Completed, On hold...), as a scrollable row of
 * M3 Expressive toggle buttons synced with the list's [pagerState].
 *
 * - The selected status is a filled button whose shape morphs from a pill to a rounded square;
 *   pressing any button squishes it slightly (Expressive pressed shape).
 * - Swiping the pager selects the matching status and scrolls it into view.
 * - The haptic tick on page change stays where it was (UserMediaListWithTabsView).
 */
@Composable
fun StatusTabRow(
    tabs: Array<TabRowItem<ListStatus>>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val rowState = rememberLazyListState()

    // Keep the selected status visible (one item of context on its left)
    LaunchedEffect(pagerState.currentPage) {
        rowState.animateScrollToItem((pagerState.currentPage - 1).coerceAtLeast(0))
    }

    LazyRow(
        state = rowState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(
            items = tabs,
            key = { _, tab -> tab.value.name },
        ) { index, tab ->
            val selected = index == pagerState.currentPage
            ToggleButton(
                checked = selected,
                onCheckedChange = {
                    // Tapping the selected status again does nothing
                    if (!selected) {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    }
                },
                icon = {
                    Icon(
                        imageVector = tab.value.icon,
                        contentDescription = null,
                    )
                },
            ) {
                Text(text = tab.value.localized())
            }
        }
    }
}
