package com.parseus.codecinfo.ui

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.parseus.codecinfo.R
import com.parseus.codecinfo.data.drm.DrmSimpleInfo

class DrmPresenter(@DrawableRes private val drawable: Int) : Presenter() {

    class ViewHolder(view: View) : Presenter.ViewHolder(view) {
        val cardView = view as ImageCardView
        var simpleInfo: DrmSimpleInfo? = null
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
        val info = item as DrmSimpleInfo
        (viewHolder as ViewHolder).simpleInfo = info
        viewHolder.cardView.apply {
            titleText = context.getString(R.string.category_drm)
            contentText = info.drmName
            mainImage = AppCompatResources.getDrawable(context, drawable)
        }
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
        val vh = viewHolder as ViewHolder
        vh.simpleInfo = null
        with(vh.cardView) {
            // Clear image to free up memory and prevent flickering on reuse
            mainImage = null
        }
    }

    companion object {
        private const val GRID_ITEM_WIDTH = 300
        private const val GRID_ITEM_HEIGHT = 200
    }

}