package com.parseus.codecinfo.tv

import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.VerticalGridView
import com.parseus.codecinfo.adapters.CodecInfoAdapter
import com.parseus.codecinfo.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.codecinfo.getDetailedCodecInfo

class CodecInfoPresenter(private val codecInfoMap: Map<String, String>) : Presenter() {

    inner class ViewHolder(view: View) : Presenter.ViewHolder(view) {
        val gridView = view as VerticalGridView
    }

    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        val gridView = VerticalGridView(parent.context)
        return ViewHolder(gridView)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
        val infoAdapter = CodecInfoAdapter(codecInfoMap)
        (viewHolder as ViewHolder).gridView.adapter = infoAdapter
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {}

}