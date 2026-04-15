package com.axiel7.moelist.ui.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import coil3.compose.AsyncImage
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.TextIconHorizontal
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.profile.composables.UserStatsView
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize
import com.axiel7.moelist.utils.MAL_PROFILE_URL
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

@Composable
fun ProfileView(
    navActionManager: NavActionManager
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileViewContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileViewContent(
    uiState: ProfileUiState,
    event: ProfileEvent?,
    navActionManager: NavActionManager
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var selectedStatsTab by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    val openMalProfile: () -> Unit = {
        val name = uiState.user?.name
        if (name != null) {
            context.openLink(MAL_PROFILE_URL + name)
        }
    }

    val titleAlpha by animateFloatAsState(
        targetValue = if (scrollState.value > 150) 1f else 0f,
        label = "titleAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.title_profile),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.alpha(titleAlpha)
                    ) 
                },
                navigationIcon = {
                    GlassIconButton(
                        onClick = navActionManager::goBack,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.title_home) // Using home as a fallback for back
                        )
                    }
                },
                actions = {
                    GlassIconButton(onClick = { navActionManager.toSettings() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_settings_24),
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    GlassIconButton(
                        onClick = openMalProfile,
                        enabled = uiState.user?.name != null
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_language_24),
                            contentDescription = stringResource(R.string.view_profile_mal)
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    GlassIconButton(
                        onClick = { event?.logOut() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_power_settings_new_24),
                            contentDescription = stringResource(R.string.logout)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()) // Only use bottom padding to allow bleed at top
        ) {
            val containerWidthDp = remember(windowInfo, density) {
                with(density) { windowInfo.containerSize.width.toDp() }
            }
            val widthClass = when {
                containerWidthDp < 600.dp -> WindowWidthSizeClass.Compact
                containerWidthDp < 840.dp -> WindowWidthSizeClass.Medium
                else -> WindowWidthSizeClass.Expanded
            }
            val horizontalPadding = when (widthClass) {
                WindowWidthSizeClass.Compact -> 16.dp
                WindowWidthSizeClass.Medium -> 24.dp
                else -> 32.dp
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .statusBarsPadding()
                        .padding(horizontal = horizontalPadding)
                        .padding(top = 56.dp), // Adjust for TopAppBar
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileHeader(
                        uiState = uiState,
                        widthClass = widthClass,
                        onAvatarClick = {
                            uiState.profilePictureUrl?.let {
                                navActionManager.toFullPoster(listOf(it))
                            }
                        }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .padding(horizontal = horizontalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    val tabLabels = listOf(
                        stringResource(R.string.anime),
                        stringResource(R.string.manga)
                    )
                    PrimaryTabRow(
                        selectedTabIndex = selectedStatsTab,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium),
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        tabLabels.forEachIndexed { index, label ->
                            Tab(
                                selected = selectedStatsTab == index,
                                onClick = { selectedStatsTab = index },
                                text = { 
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (selectedStatsTab == index) FontWeight.Bold else FontWeight.Normal
                                    ) 
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val mediaType =
                        if (selectedStatsTab == 0) MediaType.ANIME else MediaType.MANGA
                    UserStatsView(
                        uiState = uiState,
                        mediaType = mediaType,
                        showSectionTitle = false,
                        compactStats = widthClass == WindowWidthSizeClass.Compact,
                        onRefreshManga = { event?.refreshMangaStats() }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileHeader(
    uiState: ProfileUiState,
    widthClass: WindowWidthSizeClass,
    onAvatarClick: () -> Unit
) {
    val avatarSize = when (widthClass) {
        WindowWidthSizeClass.Compact -> 110.dp
        WindowWidthSizeClass.Medium -> 120.dp
        else -> 130.dp
    }
    val nameStyle = when (widthClass) {
        WindowWidthSizeClass.Compact -> MaterialTheme.typography.headlineMedium
        else -> MaterialTheme.typography.headlineLarge
    }

    if (widthClass == WindowWidthSizeClass.Compact) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ProfileAvatar(
                url = uiState.profilePictureUrl,
                size = avatarSize,
                onClick = onAvatarClick
            )
            ProfileMeta(
                uiState = uiState,
                nameStyle = nameStyle,
                centered = true
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            ProfileAvatar(
                url = uiState.profilePictureUrl,
                size = avatarSize,
                onClick = onAvatarClick
            )
            ProfileMeta(
                uiState = uiState,
                nameStyle = nameStyle,
                centered = false
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    url: String?,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        tonalElevation = 2.dp,
        shadowElevation = 12.dp,
        modifier = Modifier.size(size)
    ) {
        if (url.isNullOrEmpty()) {
            Image(
                painter = painterResource(R.drawable.ic_round_account_circle_24),
                contentDescription = stringResource(R.string.title_profile),
                modifier = Modifier
                    .fillMaxSize()
                    .defaultPlaceholder(true),
                contentScale = ContentScale.Crop
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = stringResource(R.string.title_profile),
                placeholder = painterResource(R.drawable.ic_round_account_circle_24),
                error = painterResource(R.drawable.ic_round_account_circle_24),
                fallback = painterResource(R.drawable.ic_round_account_circle_24),
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = dropUnlessResumed { onClick() }),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileMeta(
    uiState: ProfileUiState,
    nameStyle: androidx.compose.ui.text.TextStyle,
    centered: Boolean
) {
    Column(
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = uiState.user?.name ?: "Loading...",
            modifier = Modifier
                .defaultPlaceholder(visible = uiState.isLoading),
            color = MaterialTheme.colorScheme.onSurface,
            style = nameStyle,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = if (centered) Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally) else Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            uiState.user?.location?.let { location ->
                if (location.isNotBlank()) {
                    TextIconHorizontal(
                        text = location,
                        icon = R.drawable.ic_round_location_on_24,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            uiState.user?.birthday?.let { birthday ->
                TextIconHorizontal(
                    text = birthday.parseDateAndLocalize().orEmpty(),
                    icon = R.drawable.ic_round_cake_24,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            TextIconHorizontal(
                text = if (uiState.user?.joinedAt != null)
                    uiState.user.joinedAt.parseDateAndLocalize(
                        inputFormat = DateTimeFormatter.ISO_DATE_TIME
                    ).orEmpty()
                else "Loading...",
                icon = R.drawable.ic_round_access_time_24,
                modifier = Modifier
                    .defaultPlaceholder(visible = uiState.isLoading),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview
@Composable
fun ProfilePreview() {
    MoeListTheme {
        Surface {
            ProfileViewContent(
                uiState = ProfileUiState(),
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}
