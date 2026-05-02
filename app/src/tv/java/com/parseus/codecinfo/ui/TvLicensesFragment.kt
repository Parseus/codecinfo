package com.parseus.codecinfo.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.Library
import com.parseus.codecinfo.data.TV_LIBRARIES
import com.parseus.codecinfo.databinding.ItemDetailsAdapterRowBinding
import com.parseus.codecinfo.utils.ToastCompat
import com.parseus.codecinfo.utils.externalAppIntentFlags

class TvLicensesFragment : BaseVerticalGridSupportFragment(), OnItemViewClickedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.about_licenses)

        onItemViewClickedListener = this

        val adapter = ArrayObjectAdapter(LibraryPresenter())
        adapter.addAll(0, TV_LIBRARIES)
        setAdapter(adapter)
    }

    override fun onItemClicked(itemViewHolder: Presenter.ViewHolder?, item: Any?,
                               rowViewHolder: RowPresenter.ViewHolder?, row: Row?) {
        if (item is Library) {
            val intent = Intent(Intent.ACTION_VIEW, item.url.toUri())
            intent.addFlags(externalAppIntentFlags)
            try {
                startActivity(intent)
            } catch (_: Exception) {
                ToastCompat.makeText(requireContext(), R.string.no_apps_for_action, Toast.LENGTH_LONG).show()
            }
        }
    }

    private class LibraryPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            val binding = ItemDetailsAdapterRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding.root)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
            val library = item as Library
            val binding = ItemDetailsAdapterRowBinding.bind(viewHolder.view)
            binding.codecProperty.text = library.name
            binding.codecValue.text = library.url
        }

        override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
    }

}
