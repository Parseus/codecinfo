package com.parseus.codecinfo.ui

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.PrecomputedTextCompat
import androidx.lifecycle.lifecycleScope
import androidx.leanback.widget.*
import com.parseus.codecinfo.R
import com.parseus.codecinfo.databinding.ItemDetailsAdapterRowBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source

class TvChangelogFragment : BaseVerticalGridSupportFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.about_changelog)

        val adapter = ArrayObjectAdapter(ChangelogPresenter())
        setAdapter(adapter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewLifecycleOwner.lifecycleScope.launch {
            loadChangelog()
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private suspend fun loadChangelog() {
        val entries = withContext(Dispatchers.IO) {
            val htmlText = requireContext().assets.open("changelog.html").source().buffer().use { it.readUtf8() }
            val entries = mutableListOf<ChangelogEntry>()
            val versionRegex = Regex("<h2>Version (.+?)</h2>")
            val listRegex = Regex("<ul>(.+?)</ul>", RegexOption.DOT_MATCHES_ALL)

            val versionMatches = versionRegex.findAll(htmlText).toList()
            val listMatches = listRegex.findAll(htmlText).toList()

            for (i in versionMatches.indices) {
                val version = versionMatches[i].groupValues[1]
                val listHtml = listMatches[i].groupValues[1]
                val changes = listHtml.split("<li>", "</li>")
                    .filter { it.trim().isNotEmpty() && !it.contains("<ul>") && !it.contains("</ul>") }
                    .map { it.trim().replace("<br>", "\n").replace("<br/>", "\n") }
                entries.add(ChangelogEntry(version, changes))
            }
            entries
        }
        (adapter as ArrayObjectAdapter).addAll(0, entries)
    }

    data class ChangelogEntry(val version: String, val changes: List<String>)

    private inner class ChangelogPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            val binding = ItemDetailsAdapterRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding.root)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
            val entry = item as ChangelogEntry
            val binding = ItemDetailsAdapterRowBinding.bind(viewHolder.view)
            binding.codecProperty.text = getString(R.string.app_version, entry.version)

            lifecycleScope.launch {
                val precomputedText = withContext(Dispatchers.Default) {
                    val spannableBuilder = SpannableStringBuilder()
                    entry.changes.forEachIndexed { index, change ->
                        val start = spannableBuilder.length
                        spannableBuilder.append(change)
                        val end = spannableBuilder.length
                        spannableBuilder.setSpan(ImprovedBulletSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        if (index < entry.changes.size - 1) {
                            spannableBuilder.append("\n")
                        }
                    }
                    PrecomputedTextCompat.create(spannableBuilder, binding.codecValue.textMetricsParamsCompat)
                }
                binding.codecValue.setPrecomputedText(precomputedText)
            }
        }

        override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
    }

}
