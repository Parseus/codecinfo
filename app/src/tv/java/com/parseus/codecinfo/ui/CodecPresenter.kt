package com.parseus.codecinfo.ui

import android.os.Build
import android.view.PointerIcon
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.codecinfo.CodecSimpleInfo
import com.parseus.codecinfo.data.knownproblems.KNOWN_PROBLEMS_DB

data class CodecSearchItem(
    val info: CodecSimpleInfo,
    val highlightedId: CharSequence,
    val highlightedName: CharSequence
)

class CodecPresenter(@DrawableRes private val drawable: Int) : Presenter() {

    class ViewHolder(view: View) : Presenter.ViewHolder(view) {
        val cardView = view as ImageCardView
        var searchItem: CodecSearchItem? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setBackgroundColor(parent.context.getColor(R.color.teal_700))

            cardType = ImageCardView.CARD_TYPE_INFO_UNDER
            infoVisibility = ImageCardView.CARD_REGION_VISIBLE_ALWAYS
            setMainImageDimensions(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT)
            setMainImageScaleType(ImageView.ScaleType.CENTER_INSIDE)

            if (Build.VERSION.SDK_INT >= 24) {
                pointerIcon = PointerIcon.getSystemIcon(context, PointerIcon.TYPE_HAND)
            }
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any?) {
        val context = viewHolder.view.context
        val info = if (item is CodecSearchItem) item.info else item as CodecSimpleInfo
        val searchItem = item as? CodecSearchItem
        (viewHolder as ViewHolder).searchItem = searchItem
        viewHolder.cardView.apply {
            titleText = searchItem?.highlightedId ?: info.codecId
            contentText = searchItem?.highlightedName ?: info.codecName
            contentDescription = "$titleText: $contentText"
            mainImage = AppCompatResources.getDrawable(context, drawable)

            // Reset badge for every bind to handle recycling
            badgeImage = when {
                KNOWN_PROBLEMS_DB.any { it.isAffected(context, info.codecName) } -> {
                    AppCompatResources.getDrawable(context, R.drawable.ic_error)
                }
                info.isHardwareAccelereated -> {
                    AppCompatResources.getDrawable(context, R.drawable.ic_hardware)
                }
                else -> null
            }
        }
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(viewHolder, item, payloads)
        } else {
            val info = if (item is CodecSearchItem) item.info else item as CodecSimpleInfo
            val searchItem = item as? CodecSearchItem
            (viewHolder as ViewHolder).cardView.apply {
                titleText = searchItem?.highlightedId ?: info.codecId
                contentText = searchItem?.highlightedName ?: info.codecName
                contentDescription = "$titleText: $contentText"
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
        val vh = viewHolder as ViewHolder
        vh.searchItem = null
        with(vh.cardView) {
            // Clear images to free up memory and prevent flickering on reuse
            mainImage = null
            badgeImage = null
        }
    }

    companion object {
        private const val GRID_ITEM_WIDTH = 300
        private const val GRID_ITEM_HEIGHT = 200
    }

}