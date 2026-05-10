package com.parseus.codecinfo.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.Library
import com.parseus.codecinfo.databinding.WearLicenseAdapterRowBinding
import com.parseus.codecinfo.utils.externalAppIntentFlags

class WearLicensesAdapter : ListAdapter<Library, WearLicensesAdapter.ViewHolder>(LibraryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WearLicenseAdapterRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindLibrary(getItem(position))
    }

    inner class ViewHolder(binding: WearLicenseAdapterRowBinding) : RecyclerView.ViewHolder(binding.root) {

        private val libraryName = binding.libraryName
        private val libraryUrl = binding.libraryUrl

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val library = getItem(position)
                    val intent = Intent(Intent.ACTION_VIEW, library.url.toUri())
                    intent.addFlags(externalAppIntentFlags)
                    try {
                        itemView.context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(itemView.context, R.string.no_apps_for_action, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        fun bindLibrary(library: Library) {
            libraryName.text = library.name
            libraryUrl.text = library.url
        }

    }

    private class LibraryDiffCallback : DiffUtil.ItemCallback<Library>() {
        override fun areItemsTheSame(oldItem: Library, newItem: Library) = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: Library, newItem: Library) = oldItem == newItem
    }

}