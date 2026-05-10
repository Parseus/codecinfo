package com.parseus.codecinfo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.SwipeDismissFrameLayout
import androidx.wear.widget.WearableLinearLayoutManager
import com.parseus.codecinfo.databinding.WearFragmentChangelogBinding
import com.parseus.codecinfo.ui.adapters.ChangelogItem
import com.parseus.codecinfo.ui.adapters.WearChangelogAdapter
import com.parseus.codecinfo.utils.applyRotaryInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class WearChangelogFragment : Fragment() {

    private var _binding: WearFragmentChangelogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WearFragmentChangelogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeDismissRoot.addCallback(object : SwipeDismissFrameLayout.Callback() {
            override fun onDismissed(layout: SwipeDismissFrameLayout) {
                parentFragmentManager.popBackStack()
            }
        })

        val adapter = WearChangelogAdapter()
        binding.wearableRecyclerView.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(requireContext(), object : WearableLinearLayoutManager.LayoutCallback() {
                override fun onLayoutFinished(child: View, parent: RecyclerView) {
                    // Calculate a "center proximity" value (0.0 to 1.0)
                    val centerOffset = child.height / 2f / parent.height.toFloat()
                    val yRelativeToCenterOffset = child.top / parent.height.toFloat() + centerOffset

                    // This math slightly scales down items as they move away from the center,
                    // but keeps them from overlapping by not over-translating them.
                    val progresToCenter = abs(0.5f - yRelativeToCenterOffset)
                    val scale = 1f - progresToCenter * 0.2f // Scale down to 80% at the edges

                    child.scaleX = scale
                    child.scaleY = scale

                    // Adjust alpha to fade out items at the top and bottom edges
                    child.alpha = 1f - progresToCenter
                }
            })
            this.adapter = adapter
            applyRotaryInput()
            requestFocus()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val entries = withContext(Dispatchers.IO) { parseChangelog() }
            adapter.submitList(entries) {
                if (isAdded) {
                    binding.wearableRecyclerView.requestFocus()
                }
            }
        }
    }

    private fun parseChangelog(): List<ChangelogItem> {
        val context = context ?: return emptyList()
        return try {
            val htmlText = context.assets.open("changelog.html").use { it.bufferedReader().readText() }
            val items = mutableListOf<ChangelogItem>()
            val versionRegex = Regex("<h2>Version (.+?)</h2>", RegexOption.IGNORE_CASE)
            val listRegex = Regex("<ul>(.+?)</ul>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))

            val versions = versionRegex.findAll(htmlText).map { it.groupValues[1] }.toList()
            val lists = listRegex.findAll(htmlText).map { it.groupValues[1] }.toList()

            for (i in versions.indices) {
                items.add(ChangelogItem.Version(versions[i]))
                val changes = lists[i].split(Regex("</?li>", RegexOption.IGNORE_CASE))
                    .map { it.trim().replace(Regex("\\s+"), " ") } // Cleans up HTML indentation
                    .filter { it.isNotEmpty() && !it.startsWith("<ul") }
                    .map { it.replace(Regex("<br/?>", RegexOption.IGNORE_CASE), "\n") }
                changes.forEach { items.add(ChangelogItem.Change(it)) }
            }
            items
        } catch (_: Exception) { emptyList() }
    }

    override fun onResume() {
        super.onResume()
        binding.wearableRecyclerView.requestFocus()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}