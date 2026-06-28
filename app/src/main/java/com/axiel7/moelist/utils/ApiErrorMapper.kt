package com.axiel7.moelist.utils

import com.axiel7.moelist.App
import com.axiel7.moelist.R

/**
 * Centralized mapping of raw API/exception error strings into localized,
 * user-friendly messages.
 *
 * Repositories and view models should never push raw codes like
 * `"invalid_token"` or low-level exception messages like
 * `"Unable to resolve host \"api.myanimelist.net\""` straight to the UI.
 * Funnel everything through [mapApiError] (or [Throwable.toUiMessage]).
 */
object ApiErrorMapper {

    /** Error codes used internally to signal special UI flows. */
    const val CODE_SESSION_EXPIRED = "invalid_token"

    /**
     * @param raw The raw error code (e.g. `result.error`) or message
     *            (e.g. `result.message`) coming from the API or an exception.
     *            Passing both is handled by [mapApiError] with two params.
     */
    fun mapApiError(raw: String?): String {
        val context = App.instance.applicationContext
        if (raw.isNullOrBlank()) {
            return context.getString(R.string.something_went_wrong)
        }
        return when {
            raw.equals(CODE_SESSION_EXPIRED, ignoreCase = true) ||
                raw.contains("invalid_token", ignoreCase = true) ||
                raw.equals("invalid_grant", ignoreCase = true) ->
                context.getString(R.string.session_expired)

            raw.contains("Unable to resolve host", ignoreCase = true) ||
                raw.contains("UnknownHost", ignoreCase = true) ||
                raw.contains("Failed to connect", ignoreCase = true) ||
                raw.contains("timeout", ignoreCase = true) ||
                raw.contains("Network is unreachable", ignoreCase = true) ||
                raw.contains("Software caused connection abort", ignoreCase = true) ||
                raw.contains("No address associated", ignoreCase = true) ||
                raw.contains("Unable to reach", ignoreCase = true) ||
                raw.contains("Connection refused", ignoreCase = true) ->
                context.getString(R.string.network_error)

            raw.contains("404") || raw.contains("Not Found", ignoreCase = true) ->
                context.getString(R.string.not_found)

            raw.contains("500") ||
                raw.contains("502") ||
                raw.contains("503") ||
                raw.contains("504") ||
                raw.contains("Server error", ignoreCase = true) ->
                context.getString(R.string.error_server)

            else -> context.getString(R.string.something_went_wrong)
        }
    }

    /**
     * Convenience for callers that have both an error code and a message
     * (e.g. `Response.error` and `Response.message`). The code is preferred
     * because it's stable; falls back to the message otherwise.
     */
    fun mapApiError(error: String?, message: String?): String {
        return mapApiError(error ?: message)
    }

    /** Returns `true` if the raw input represents an auth failure. */
    fun isSessionExpired(raw: String?): Boolean {
        if (raw.isNullOrBlank()) return false
        return raw.equals(CODE_SESSION_EXPIRED, ignoreCase = true) ||
            raw.contains("invalid_token", ignoreCase = true) ||
            raw.equals("invalid_grant", ignoreCase = true)
    }
}

/** Convenience extension for exception → user-friendly message. */
fun Throwable.toUiMessage(): String = ApiErrorMapper.mapApiError(this.message)
