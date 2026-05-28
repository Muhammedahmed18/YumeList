package com.axiel7.moelist.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object TranslateUtils {

    fun Context.openTranslator(text: String) {
        val uri = Uri.parse("https://translate.google.com/?sl=auto&tl=en&text=${Uri.encode(text)}&op=translate")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
}