package com.parseus.codecinfo.ui

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.text.method.LinkMovementMethodCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kieronquinn.monetcompat.extensions.applyMonet
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.Library
import com.parseus.codecinfo.data.LicenseType
import com.parseus.codecinfo.databinding.LicenseDialogLayoutBinding
import com.parseus.codecinfo.databinding.LicenseGroupItemBinding
import com.parseus.codecinfo.ui.externalLinks.ExternalLinksViewModel
import com.parseus.codecinfo.utils.isDynamicThemingEnabled
import com.parseus.codecinfo.utils.isNativeMonetAvailable
import com.parseus.codecinfo.utils.updateButtonColors

class LicenseDialogManager(private val activity: FragmentActivity) {

    private val alertDialogBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle(R.string.about_licenses)
        .setPositiveButton(android.R.string.ok, null)
    private var alertDialog: AlertDialog? = null
    private val libraries = mutableListOf<Library>()
    private var noticeTitle = activity.getString(R.string.about_license_notice_title)

    private val adapter = LicenseAdapter()
    private val binding = LicenseDialogLayoutBinding.inflate(LayoutInflater.from(activity))

    private val externalLinksViewModel: ExternalLinksViewModel by activity.viewModels()

    init {
        binding.licenseRecyclerView.adapter = adapter
        alertDialogBuilder.setView(binding.root)
    }

    fun setLibrary(library: Library): LicenseDialogManager {
        libraries.add(library)
        return this
    }

    fun show() {
        val groupedLibraries = libraries.groupBy { it.license }
        adapter.submitList(groupedLibraries.map { (license, libs) ->
            LicenseGroup(license, libs)
        })

        if (alertDialog == null) {
            alertDialog = alertDialogBuilder.create()
        }
        alertDialog!!.run {
            show()
            if (isDynamicThemingEnabled(activity) && !isNativeMonetAvailable()) {
                applyMonet()
            }
            updateButtonColors(alertDialogBuilder.context)
        }
    }

    private data class LicenseGroup(val licenseType: LicenseType, val libraries: List<Library>)

    private inner class LicenseAdapter : ListAdapter<LicenseGroup, LicenseViewHolder>(LicenseDiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicenseViewHolder {
            val binding = LicenseGroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return LicenseViewHolder(binding)
        }

        override fun onBindViewHolder(holder: LicenseViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    private inner class LicenseViewHolder(private val binding: LicenseGroupItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(group: LicenseGroup) {
            binding.licenseNoticeTitle.text = group.libraries.first().getNotice(noticeTitle)
            
            val libraryNamesBuilder = SpannableStringBuilder()
            group.libraries.forEachIndexed { index, library ->
                val nameSpannable = library.getNameSpannable()
                val start = libraryNamesBuilder.length
                libraryNamesBuilder.append(nameSpannable)
                
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        externalLinksViewModel.launchExternalLink.value = library.url.toUri()
                    }
                }
                libraryNamesBuilder.setSpan(
                    clickableSpan,
                    start,
                    libraryNamesBuilder.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                
                if (index < group.libraries.size - 1) {
                    libraryNamesBuilder.append("\n")
                }
            }
            
            binding.libraryNames.apply {
                text = libraryNamesBuilder
                movementMethod = LinkMovementMethodCompat.getInstance()
                setLineSpacing(
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f,
                        resources.displayMetrics), 1f)
            }

            val licenseSpannable = group.licenseType.getSpannable()
            binding.licenseFullText.apply {
                text = licenseSpannable
                movementMethod = LinkMovementMethodCompat.getInstance()
            }
        }
    }

    private class LicenseDiffCallback : DiffUtil.ItemCallback<LicenseGroup>() {
        override fun areItemsTheSame(oldItem: LicenseGroup, newItem: LicenseGroup) = oldItem.licenseType == newItem.licenseType
        override fun areContentsTheSame(oldItem: LicenseGroup, newItem: LicenseGroup) = oldItem == newItem
    }
}
