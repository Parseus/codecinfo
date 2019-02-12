package com.parseus.codecinfo.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ShareCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.parseus.codecinfo.MainActivity
import com.parseus.codecinfo.R
import com.parseus.codecinfo.SettingsActivity
import com.parseus.codecinfo.adapters.CodecInfoAdapter
import com.parseus.codecinfo.codecinfo.getDetailedCodecInfo
import kotlinx.android.synthetic.main.codec_details_fragment_layout.*

class CodecDetailsDialogFragment : DialogFragment() {

    private var dismissDialog = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.codec_details_fragment_layout, container, false)

        val toolbar: Toolbar

        try {
            toolbar = ViewCompat.requireViewById(view, R.id.dialogToolbar)
        } catch (e: Exception) {
            dismissDialog = true
            return null
        }

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

    override fun onResume() {
        super.onResume()

        if (dismissDialog) {
            requireActivity().supportFragmentManager.popBackStack()
            dismiss()
            return
        }
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
        val codecInfoMap = getDetailedCodecInfo(requireContext(), codecId!!, codecName!!)
        val codecAdapter = CodecInfoAdapter(codecInfoMap)

        (full_codec_info_name as TextView).text = codecName
        full_codec_info_content.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = codecAdapter
            ViewCompat.setNestedScrollingEnabled(this, false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.fragment_bar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @SuppressLint("InflateParams")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            android.R.id.home -> dismiss()
            R.id.fragment_menu_item_share -> {
                val codecId = arguments!!.getString("codecId")
                val codecName = arguments!!.getString("codecName")
                val header = "${requireContext().getString(R.string.codec_details)}: $codecName\n\n"
                val codecStringBuilder = StringBuilder(header)
                val codecInfoMap = getDetailedCodecInfo(requireContext(), codecId!!, codecName!!)

                for (key in codecInfoMap.keys) {
                    codecStringBuilder.append("$key\n${codecInfoMap[key]}\n\n")
                }

                ShareCompat.IntentBuilder.from(activity).setType("text/plain")
                        .setText(codecStringBuilder.toString()).startChooser()
            }
            R.id.menu_item_settings -> startActivity(Intent(requireActivity(), SettingsActivity::class.java))
        }

        return true
    }

}