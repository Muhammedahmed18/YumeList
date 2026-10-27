package com.axiel7.moelist.ui.details.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.ui.composables.InfoTitle
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.details.MediaDetailsUiState
import com.axiel7.moelist.ui.details.composables.MediaInfoView
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.MenuBook
import com.axiel7.moelist.ui.icons.rounded.Add
import com.axiel7.moelist.ui.icons.rounded.Bookmark
import com.axiel7.moelist.ui.icons.rounded.Event
import com.axiel7.moelist.ui.icons.rounded.History
import com.axiel7.moelist.ui.icons.rounded.Info
import com.axiel7.moelist.ui.icons.rounded.LiveTv
import com.axiel7.moelist.ui.icons.rounded.Movie
import com.axiel7.moelist.ui.icons.rounded.RssFeed
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize

/** "More info": studios / source / dates / season / broadcast (anime) or authors / serialization / dates / status / volumes (manga). */
@Composable
fun DetailsInfo(
    uiState: MediaDetailsUiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        InfoTitle(text = stringResource(R.string.more_info))
        Surface(
            shape = RoundedCornerShape(DetailsCardCorner),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DetailsHorizontalPadding)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.isAnime) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MediaInfoView(
                            title = stringResource(R.string.studios),
                            info = uiState.studiosJoined,
                            iconVector = Icons.Rounded.Movie,
                            modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                        )
                        MediaInfoView(
                            title = stringResource(R.string.source),
                            info = (uiState.mediaDetails as? AnimeDetails)?.source?.localized(),
                            iconVector = Icons.Rounded.History,
                            modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MediaInfoView(
                            title = stringResource(R.string.start_date),
                            info = uiState.mediaDetails?.startDate?.parseDateAndLocalize(),
                            iconVector = Icons.Rounded.Add,
                            modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                        )
                        MediaInfoView(
                            title = stringResource(R.string.end_date),
                            info = uiState.mediaDetails?.endDate?.parseDateAndLocalize(),
                            iconVector = Icons.Rounded.Event,
                            modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MediaInfoView(
                            title = stringResource(R.string.season),
                            info = (uiState.mediaDetails as? AnimeDetails)?.startSeason?.seasonYearText(),
                            iconVector = Icons.Rounded.RssFeed,
                            modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                        )
                        MediaInfoView(
                            title = stringResource(R.string.broadcast),
                            info = (uiState.mediaDetails as? AnimeDetails)?.broadcast?.timeText(
                                isAiring = false
                            ),
                            iconVector = Icons.Rounded.LiveTv,
                            modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MediaInfoView(
                            title = stringResource(R.string.authors),
                            info = (uiState.mediaDetails as? MangaDetails)?.authors
                                ?.joinToString { "${it.node.firstName} ${it.node.lastName}" },
                            iconVector = Icons.Rounded.Movie,
                            modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                        )
                        MediaInfoView(
                            title = stringResource(R.string.serialization),
                            info = uiState.serializationJoined,
                            iconVector = Icons.Rounded.Bookmark,
                            modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MediaInfoView(
                            title = stringResource(R.string.start_date),
                            info = uiState.mediaDetails?.startDate?.parseDateAndLocalize(),
                            iconVector = Icons.Rounded.Add,
                            modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                        )
                        MediaInfoView(
                            title = stringResource(R.string.end_date),
                            info = uiState.mediaDetails?.endDate?.parseDateAndLocalize(),
                            iconVector = Icons.Rounded.Event,
                            modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MediaInfoView(
                            title = stringResource(R.string.status),
                            info = uiState.mediaDetails?.status?.localized(),
                            iconVector = Icons.Rounded.Info,
                            modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                        )
                        MediaInfoView(
                            title = stringResource(R.string.volumes),
                            info = (uiState.mediaDetails as? MangaDetails)?.numVolumes.countOrDash(),
                            iconVector = Icons.AutoMirrored.Rounded.MenuBook,
                            modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                }
            }
        }
    }
}
