package com.parseus.codecinfo.ui

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

class CodecPresenter(@DrawableRes private val drawable: Int) : Presenter() {

    class ViewHolder(view: View) : Presenter.ViewHolder(view) {
        val cardView = view as ImageCardView
        var simpleInfo: CodecSimpleInfo? = null
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
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any?) {
        val info = item as CodecSimpleInfo
        (viewHolder as ViewHolder).simpleInfo = info
        viewHolder.cardView.apply {
            titleText = info.codecId
            contentText = info.codecName
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

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
        val vh = viewHolder as ViewHolder
        vh.simpleInfo = null
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