package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.BaseResponse
import com.axiel7.moelist.utils.NumExtensions.isGreaterThanZero
import org.apache.commons.text.StringEscapeUtils

abstract class BaseMyListStatus : BaseResponse {
    abstract val status: ListStatus
    abstract val score: Int
    abstract val updatedAt: String?
    abstract val startDate: String?
    abstract val finishDate: String?
    abstract val progress: Int?
    abstract val repeatCount: Int?
    abstract val repeatValue: Int?
    abstract val isRepeating: Boolean
    abstract val priority: Int
    abstract val tags: List<String>?
    abstract val comments: String?

    override val error: String? = null
    override val message: String? = null

    fun hasRepeated() = isRepeating || repeatCount.isGreaterThanZero()

    fun hasNotes() = !comments.isNullOrBlank() || !tags.isNullOrEmpty()

    // MAL API returns special characters escaped
    fun notesEscaped() = StringEscapeUtils.unescapeHtml4(comments)
}

@Composable
fun Int.scoreText() = when (this) {
    0 -> ""
    1 -> stringResource(R.string.score_apalling)
    2 -> stringResource(R.string.score_horrible)
    3 -> stringResource(R.string.score_very_bad)
    4 -> stringResource(R.string.score_bad)
    5 -> stringResource(R.string.score_average)
    6 -> stringResource(R.string.score_fine)
    7 -> stringResource(R.string.score_good)
    8 -> stringResource(R.string.score_very_good)
    9 -> stringResource(R.string.score_great)
    10 -> stringResource(R.string.score_masterpiece)
    else -> ""
}
