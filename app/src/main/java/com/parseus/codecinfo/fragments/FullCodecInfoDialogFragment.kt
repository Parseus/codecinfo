package com.parseus.codecinfo.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ShareCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.parseus.codecinfo.MainActivity
import com.parseus.codecinfo.R
import com.parseus.codecinfo.adapters.CodecInfoAdapter
import com.parseus.codecinfo.codecinfo.CodecUtils
import kotlinx.android.synthetic.main.full_codec_info_fragment_layout.*

class FullCodecInfoDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.full_codec_info_fragment_layout, container, false)

        val toolbar = ViewCompat.requireViewById<Toolbar>(view, R.id.dialogToolbar)
        toolbar.title = requireContext().getString(R.string.codec_details)
        (requireActivity() as MainActivity).setSupportActionBar(toolbar)
        (requireActivity() as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel)
        }

        setHasOptionsMenu(true)

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val codecId = arguments!!.getString("codecId")
        val codecName = arguments!!.getString("codecName")
        val codecInfoMap = CodecUtils.getDetailedCodecInfo(requireContext(), codecId, codecName)
        val codecAdapter = CodecInfoAdapter(codecInfoMap)

        full_codec_info_name.text = codecName
        full_codec_info_content.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = codecAdapter
            ViewCompat.setNestedScrollingEnabled(this, false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.fragment_bar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId

        if (id == android.R.id.home) {
            dismiss()
        } else if (id == R.id.fragment_menu_item_share) {
            val codecId = arguments!!.getString("codecId")
            val codecName = arguments!!.getString("codecName")
            val header = "${requireContext().getString(R.string.codec_details)}: $codecName\n\n"
            val codecStringBuilder = StringBuilder(header)
            val codecInfoMap = CodecUtils.getDetailedCodecInfo(requireContext(), codecId, codecName)

            for (key in codecInfoMap.keys) {
                codecStringBuilder.append("$key\n${codecInfoMap[key]}\n\n")
            }

            ShareCompat.IntentBuilder.from(activity).setType("text/plain")
                    .setText(codecStringBuilder.toString()).startChooser()
        }

        return true
    }

}