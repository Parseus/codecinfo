package com.parseus.codecinfo.ui.fragments

import android.os.Bundle
import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.os.BundleCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialSharedAxis
import com.kieronquinn.monetcompat.app.MonetFragment
import com.kieronquinn.monetcompat.extensions.views.applyMonetRecursively
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.DetailsProperty
import com.parseus.codecinfo.data.codecinfo.getDetailedCodecInfo
import com.parseus.codecinfo.data.codecinfo.isDetailedCodecInfoCached
import com.parseus.codecinfo.data.drm.DrmVendor
import com.parseus.codecinfo.data.drm.getDetailedDrmInfo
import com.parseus.codecinfo.data.drm.isDetailedDrmInfoCached
import com.parseus.codecinfo.data.knownproblems.KNOWN_PROBLEMS_DB
import com.parseus.codecinfo.data.knownproblems.KnownProblem
import com.parseus.codecinfo.databinding.ItemDetailsFragmentLayoutBinding
import com.parseus.codecinfo.ui.CustomLinearLayoutManager
import com.parseus.codecinfo.ui.MainActivity
import com.parseus.codecinfo.ui.adapters.DetailItem
import com.parseus.codecinfo.ui.adapters.DetailsItemDecoration
import com.parseus.codecinfo.ui.adapters.MobileDetailsAdapter
import com.parseus.codecinfo.ui.expandablelist.ExpandableItemAnimator
import com.parseus.codecinfo.ui.externalLinks.ExternalLinksViewModel
import com.parseus.codecinfo.utils.getPrimaryColor
import com.parseus.codecinfo.utils.getSurfaceColor
import com.parseus.codecinfo.utils.isDynamicThemingEnabled
import com.parseus.codecinfo.utils.isInTwoPaneMode
import com.parseus.codecinfo.utils.isNativeMonetAvailable
import com.parseus.codecinfo.utils.setTextWithOptionalAutosizing
import com.parseus.codecinfo.utils.updateColors
import com.parseus.codecinfo.viewmodels.ItemsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class DetailsFragment : MonetFragment() {

    private var _binding: ItemDetailsFragmentLayoutBinding? = null
    internal val binding get() = _binding!!

    private lateinit var propertyList: List<DetailsProperty>
    private var knownProblems: List<KnownProblem> = emptyList()
    private var isKnownProblemsExpanded = true
    private lateinit var detailsAdapter: MobileDetailsAdapter

    private val externalLinksViewModel: ExternalLinksViewModel by activityViewModels()

    var codecId: String? = null
    var codecName: String? = null

    var drmName: String? = null
    var drmUuid: UUID? = null

    private val viewModel: ItemsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        if (!requireContext().isInTwoPaneMode()) {
            enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
                addListener(object : Transition.TransitionListener {
                    override fun onTransitionStart(transition: Transition) {
                        (activity as? MainActivity)?.updateUIState(forceHideDetails = true)
                    }

                    override fun onTransitionCancel(transition: Transition) {
                        (activity as? MainActivity)?.updateUIState(forceHideDetails = false)
                    }

                    override fun onTransitionEnd(transition: Transition) {
                        (activity as? MainActivity)?.updateUIState(forceHideDetails = false)
                    }

                    override fun onTransitionPause(transition: Transition) {}
                    override fun onTransitionResume(transition: Transition) {}
                })
            }
        }

        _binding = ItemDetailsFragmentLayoutBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        val container = (view?.parent as? ViewGroup)
            ?: activity?.findViewById(R.id.content_fragment)
        container?.let { TransitionManager.endTransitions(it) }
        view?.let {
            (exitTransition as? Transition)?.removeTarget(it)
            (reenterTransition as? Transition)?.removeTarget(it)
        }

        binding.itemDetailsRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isDynamicThemingEnabled(requireContext()) && !isNativeMonetAvailable()) {
            view.applyMonetRecursively()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        view.requestApplyInsets()

        binding.loadingProgress.updateColors(requireContext())

        if (!requireContext().isInTwoPaneMode()) {
            // Apply background color only on mobile to reduce overdraw on bigger devices
            binding.endRoot.setBackgroundColor(getSurfaceColor(requireContext()))
        }

        val bundle = savedInstanceState ?: arguments
        bundle?.let {
            codecId = it.getString("codecId")
            codecName = it.getString("codecName")
            drmName = it.getString("drmName")
            drmUuid = BundleCompat.getSerializable(it, "drmUuid", UUID::class.java)
        }

        binding.itemDetailsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                binding.fullCodecInfoName.isHeaderLifted = recyclerView.computeVerticalScrollOffset() > 0
            }
        })

        detailsAdapter = MobileDetailsAdapter { position ->
            val item = detailsAdapter.currentList[position]
            if (item is DetailItem.Header) {
                isKnownProblemsExpanded = !isKnownProblemsExpanded
                updateFullDetailsList()
            }
        }

        binding.itemDetailsRecyclerView.apply {
            layoutManager = CustomLinearLayoutManager(context)
            adapter = detailsAdapter
            addItemDecoration(DetailsItemDecoration(context))
            itemAnimator = ExpandableItemAnimator()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.refreshDetailsTrigger.collect {
                        loadDetails()
                    }
                }

                loadDetails()
            }
        }
    }

    private suspend fun loadDetails() {
        binding.loadingProgress.isVisible = true

        propertyList = if (codecId != null && codecName != null && isDetailedCodecInfoCached(codecId!!, codecName!!)) {
            getDetailedCodecInfo(requireContext(), codecId!!, codecName!!)
        } else if (drmName != null && drmUuid != null && isDetailedDrmInfoCached(drmUuid!!)) {
            getDetailedDrmInfo(requireContext(), drmUuid!!, DrmVendor.getFromUuid(drmUuid!!))
        } else {
            withContext(Dispatchers.IO) {
                when {
                    codecId != null && codecName != null ->
                        getDetailedCodecInfo(requireContext(), codecId!!, codecName!!)

                    drmName != null && drmUuid != null ->
                        getDetailedDrmInfo(requireContext(), drmUuid!!, DrmVendor.getFromUuid(drmUuid!!))

                    else -> emptyList()
                }
            }
        }

        knownProblems = if (codecName != null && KNOWN_PROBLEMS_DB.isNotEmpty()) {
            KNOWN_PROBLEMS_DB.filter {
                it.isAffected(requireContext(), codecName!!)
            }
        } else {
            emptyList()
        }
        knownProblems.forEach { problem ->
            problem.urls.forEach {
                externalLinksViewModel.prefetchExternalLink.value = it.toUri()
            }
        }

        binding.loadingProgress.isVisible = false
        showFullDetails()
    }

    private fun showFullDetails() {
        binding.fullCodecInfoName.setTextWithOptionalAutosizing(
            text = codecName ?: drmName ?: "",
            minSize = 12, maxSize = 20, step = 1, unit = TypedValue.COMPLEX_UNIT_PX
        )
        binding.fullCodecInfoName.setTextColor(getPrimaryColor(requireContext()))

        if (Build.VERSION.SDK_INT >= 24) {
            binding.fullCodecInfoName.setOnLongClickListener { v ->
                val textToDrag = (v as TextView).text
                val item = ClipData.Item(textToDrag)
                val dragData = ClipData(textToDrag, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
                v.startDragAndDrop(dragData, View.DragShadowBuilder(v), null, View.DRAG_FLAG_GLOBAL)
            }
            binding.fullCodecInfoName.pointerIcon =
                PointerIcon.getSystemIcon(requireContext(), PointerIcon.TYPE_HAND)
        }

        updateFullDetailsList()
    }

    private fun updateFullDetailsList() {
        val fullList = mutableListOf<DetailItem>()
        if (knownProblems.isNotEmpty()) {
            fullList.add(DetailItem.Header(0L, R.string.known_issue_warning, isKnownProblemsExpanded))
            if (isKnownProblemsExpanded) {
                knownProblems.forEach {
                    fullList.add(DetailItem.KnownProblemItem(it))
                }
            }
        }
        propertyList.forEach {
            fullList.add(DetailItem.PropertyItem(it))
        }
        detailsAdapter.submitList(fullList)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("codecId", codecId)
        outState.putString("codecName", codecName)
        outState.putString("drmName", drmName)
        outState.putSerializable("drmUuid", drmUuid)
    }

}
