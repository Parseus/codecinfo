package com.parseus.codecinfo.data

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import com.parseus.codecinfo.ui.ImprovedBulletSpan

enum class LicenseType(private val licenseText: String) {
    APACHE2(APACHE2_LICENSE),
    MIT(MIT_LICENSE);

    private var cachedSpannable: Spanned? = null

    fun getSpannable(): Spanned {
        return cachedSpannable ?: SpannableStringBuilder(licenseText).apply {
            if (this@LicenseType == APACHE2) {
                val url = "https://www.apache.org/licenses/LICENSE-2.0"
                val start = indexOf(url)
                if (start != -1) {
                    setSpan(URLSpan(url), start, start + url.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }.also { cachedSpannable = it }
    }
}

data class Library(val name: String, val url: String, val license: LicenseType) {
    private var cachedNotice: Spanned? = null
    private var cachedName: Spanned? = null

    fun getNotice(noticeTitle: String): Spanned {
        return cachedNotice ?: SpannableStringBuilder().apply {
            val start = length
            append(noticeTitle)
            setSpan(StyleSpan(Typeface.BOLD), start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }.also { cachedNotice = it }
    }

    fun getNameSpannable(): Spanned {
        return cachedName ?: SpannableStringBuilder(name).apply {
            setSpan(ImprovedBulletSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(UnderlineSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }.also { cachedName = it }
    }
}

private const val APACHE2_LICENSE = """Licensed under the Apache License, Version 2.0 (the "License").

You may not use this file except in compliance with the License. You may obtain a copy of the License at

https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

See the License for the specific language governing permissions and limitations under the License."""

private const val MIT_LICENSE = """Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE."""
