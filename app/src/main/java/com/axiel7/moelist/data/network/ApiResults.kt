package com.axiel7.moelist.data.network

import com.axiel7.moelist.data.model.Response
import kotlinx.serialization.Serializable
import java.io.IOException
import retrofit2.Response as HttpResponse

/*
 * Turning Retrofit responses into what the repositories expect.
 *
 * Ktor ran with `expectSuccess = false`, so an error body like {"error":"invalid_token"} was
 * decoded into the result's own `error` / `message` fields. These helpers keep that contract:
 * - list endpoints ([Response]) never throw for HTTP errors; they return
 *   Response(error = ..., message = ...),
 * - single objects return the body or throw [ApiException] (the repositories already catch it).
 * The message always names the HTTP code when MAL sends none, so ApiErrorMapper can tell a 404
 * from a 5xx and the real reason reaches the UI instead of a blank error.
 */

/** Error body of MAL ({"error","message"}) and Jikan ({"status","type","message","error"}). */
@Serializable
internal data class ApiErrorBody(
    val error: String? = null,
    val message: String? = null,
)

/** An HTTP error (non-2xx) with whatever the server said about it. */
class ApiException(
    val code: Int,
    val error: String?,
    override val message: String,
) : IOException(message)

internal fun HttpResponse<*>.toApiException(): ApiException {
    val raw = runCatching { errorBody()?.string() }.getOrNull()
    val body = raw?.let { runCatching { NetworkJson.decodeFromString<ApiErrorBody>(it) }.getOrNull() }
    val error = body?.error?.takeIf { it.isNotBlank() }
    val detail = body?.message?.takeIf { it.isNotBlank() }
    return ApiException(
        code = code(),
        error = error,
        message = listOfNotNull(error, detail).joinToString(": ")
            .let { if (it.isEmpty()) "HTTP ${code()}" else "$it (HTTP ${code()})" },
    )
}

/** Body of a successful call, or [ApiException]. */
internal fun <T> HttpResponse<T>.bodyOrThrow(): T {
    if (!isSuccessful) throw toApiException()
    return body() ?: throw ApiException(code(), null, "Empty response (HTTP ${code()})")
}

/** Body of a successful list call, or a [Response] carrying the error (never throws for HTTP errors). */
internal fun <T> HttpResponse<Response<T>>.bodyOrError(): Response<T> {
    if (isSuccessful) {
        return body() ?: Response(message = "Empty response (HTTP ${code()})")
    }
    val exception = toApiException()
    return Response(error = exception.error, message = exception.message)
}
