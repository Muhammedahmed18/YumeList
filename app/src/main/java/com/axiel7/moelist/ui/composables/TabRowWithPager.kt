package com.axiel7.moelist.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabIndicatorScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.TabRowItem
import com.axiel7.moelist.ui.theme.MoeListTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> TabRowWithPager(
    tabs: Array<TabRowItem<T>>,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
    beyondBoundsPageCount: Int = 0,
    isTabScrollable: Boolean = false,
    isPrimaryTab: Boolean = true,
    containerColor: Color = Color.Unspecified,
    pagerState: PagerState? = null,
    showPager: Boolean = true,
    pageContent: @Composable (page: Int, currentPage: Int) -> Unit,
) {
    val defaultPagerState = rememberPagerState(initialPage = initialPage) { tabs.size }
    val state = pagerState ?: defaultPagerState
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(CircleShape)
                .background(
                    if (containerColor != Color.Unspecified) containerColor
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
        ) {
            val tabsLayout = @Composable {
                tabs.forEachIndexed { index, item ->
                    val selected = state.currentPage == index
                    Tab(
                        modifier = Modifier
                            .zIndex(2f)
                            .clip(CircleShape),
                        selected = selected,
                        onClick = { scope.launch { state.animateScrollToPage(index) } },
                        text = if (item.title != null) {
                            {
                                Text(
                                    text = stringResource(item.title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        } else null,
                        icon = if (item.icon != null) {
                            {
                                Icon(
                                    painter = painterResource(item.icon),
                                    contentDescription = item.value.toString()
                                )
                            }
                        } else null,
                        selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val indicator: @Composable TabIndicatorScope.() -> Unit = {
                Box(
                    Modifier
                        .tabIndicatorOffset(state.currentPage)
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }

            if (isTabScrollable) {
                if (isPrimaryTab) {
                    PrimaryScrollableTabRow(
                        selectedTabIndex = state.currentPage,
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = indicator,
                        tabs = tabsLayout
                    )
                } else {
                    SecondaryScrollableTabRow(
                        selectedTabIndex = state.currentPage,
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = indicator,
                        tabs = tabsLayout
                    )
                }
            } else {
                if (isPrimaryTab) {
                    PrimaryTabRow(
                        selectedTabIndex = state.currentPage,
                        containerColor = Color.Transparent,
                        indicator = indicator,
                        divider = {},
                        tabs = tabsLayout
                    )
                } else {
                    SecondaryTabRow(
                        selectedTabIndex = state.currentPage,
                        containerColor = Color.Transparent,
                        indicator = indicator,
                        divider = {},
                        tabs = tabsLayout
                    )
                }
            }
        }

        if (showPager) {
            HorizontalPager(
                state = state,
                modifier = Modifier.weight(1f),
                beyondViewportPageCount = if (beyondBoundsPageCount < 0) 0 else beyondBoundsPageCount,
                key = { tabs[it].value!! }
            ) { page ->
                if (
                    page !in ((state.currentPage - (beyondBoundsPageCount + 1))
                            ..(state.currentPage + (beyondBoundsPageCount + 1)))
                ) {
                    // To make sure only X offscreen pages are being composed
                    return@HorizontalPager
                }
                pageContent(page, state.currentPage)
            }
        }
    }//: Column
}

@Preview(showBackground = true)
@Composable
fun TabRowWithPagerPreview() {
    MoeListTheme {
        Surface {
            val tabs = arrayOf(
                TabRowItem(value = "Watching", title = R.string.watching),
                TabRowItem(value = "Plan to Watch", title = R.string.ptw),
                TabRowItem(value = "Completed", title = R.string.completed)
            )
            TabRowWithPager(
                tabs = tabs,
                modifier = Modifier.fillMaxSize()
            ) { page, _ ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Content for ${tabs[page].value}")
                }
            }
        }
    }
}
