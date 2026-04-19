package com.axiel7.moelist.ui.userlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.exampleUserAnimeList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.ui.base.TabRowItem
import com.axiel7.moelist.ui.composables.TabRowWithPager
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.userlist.composables.StandardUserMediaListItem

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun UserMediaListModernPreview() {
    val tabRowItems = arrayOf(
        TabRowItem(value = ListStatus.WATCHING, title = ListStatus.WATCHING.stringRes),
        TabRowItem(value = ListStatus.PLAN_TO_WATCH, title = ListStatus.PLAN_TO_WATCH.stringRes),
        TabRowItem(value = ListStatus.COMPLETED, title = ListStatus.COMPLETED.stringRes)
    )
    
    val pagerState = rememberPagerState { tabRowItems.size }
    var searchQuery by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }

    MoeListTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // The Modern "Pocket" Header
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .zIndex(1f),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                    border = BorderStroke(
                        width = 0.5.dp, 
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column {
                        SearchBar(
                            inputField = {
                                SearchBarDefaults.InputField(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    onSearch = { searchActive = false },
                                    expanded = searchActive,
                                    onExpandedChange = { searchActive = it },
                                    placeholder = { Text(text = stringResource(R.string.search)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Search,
                                            contentDescription = null
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchActive) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "clear",
                                                modifier = Modifier.combinedClickable(
                                                    onClick = {
                                                        if (searchQuery.isNotEmpty()) searchQuery = ""
                                                        else searchActive = false
                                                    }
                                                )
                                            )
                                        }
                                    },
                                )
                            },
                            expanded = searchActive,
                            onExpandedChange = { searchActive = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = SearchBarDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            ),
                            shape = MaterialTheme.shapes.extraLarge
                        ) { }

                        TabRowWithPager(
                            tabs = tabRowItems,
                            modifier = Modifier.fillMaxWidth(),
                            isTabScrollable = true,
                            isPrimaryTab = true,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            pagerState = pagerState,
                            showPager = false,
                            pageContent = { _, _ -> }
                        )
                    }
                }

                // The List Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                ) {
                    items(5) {
                        StandardUserMediaListItem(
                            item = exampleUserAnimeList,
                            listStatus = ListStatus.WATCHING,
                            onClick = {},
                            onLongClick = {},
                            onClickPlus = {}
                        )
                    }
                }
            }
        }
    }
}
